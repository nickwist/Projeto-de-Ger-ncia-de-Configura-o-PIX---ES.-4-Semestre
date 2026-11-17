package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import model.ContaBancaria;
import model.StatusTransacao;
import model.Transacao;
import service.ContaService;
import service.TransacaoService;

/**
 * Saldo, transferencia PIX, historico, extrato e estorno.
 * As regras do PIX sao conferidas aqui (para devolver um codigo de erro especifico)
 * e o TransacaoService executa a movimentacao.
 */
public class TransacaoController {

	// mesmo valor do TransacaoService (la a constante e privada)
	private static final double LIMITE_PIX = 2000.00;

	public record TransferenciaRequest(String contaOrigem, String chaveDestino, Double valor) {
	}

	public record TransacaoResposta(int id, String contaOrigem, String contaDestino, double valor,
	                                String status, String dataHora) {
		static TransacaoResposta de(Transacao t) {
			return new TransacaoResposta(
			           t.getId(),
			           t.getContaOrigem().getNumeroConta(),
			           t.getContaDestino().getNumeroConta(),
			           t.getValor(),
			           t.getStatus().name(),
			           t.getDataHora().toString());
		}
	}

	public record TransferenciaResposta(TransacaoResposta transacao, double saldoOrigem) {
	}

	private final ContaService contas;
	private final TransacaoService transacoes;

	public TransacaoController(ContaService contas, TransacaoService transacoes) {
		this.contas = contas;
		this.transacoes = transacoes;
	}

	public void registrar(Javalin app) {
		app.get("/contas/{numero}/saldo", this::saldo);
		app.get("/contas/{numero}/extrato", this::extrato);

		app.post("/pix/transferencias", this::transferir);

		app.get("/transacoes", this::listar);
		app.get("/transacoes/{id}", this::buscar);
		app.post("/transacoes/{id}/estorno", this::estornar);
	}

	private void saldo(Context ctx) {
		ContaBancaria conta = contaDoPath(ctx);
		ctx.json(Map.of(
		             "numeroConta", conta.getNumeroConta(),
		             "saldo", transacoes.consultarSaldo(conta),
		             "bloqueada", conta.isBloqueada()));
	}

	private void extrato(Context ctx) {
		ContaBancaria conta = contaDoPath(ctx);
		List<TransacaoResposta> lista = transacoes.getHistoricoTransacoes().stream()
		                                .filter(t -> t.getContaOrigem() == conta || t.getContaDestino() == conta)
		                                .map(TransacaoResposta::de)
		                                .toList();
		ctx.json(lista);
	}

	private void transferir(Context ctx) {
		TransferenciaRequest req = ctx.bodyAsClass(TransferenciaRequest.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}

		String numeroOrigem = Validacao.textoObrigatorio(req.contaOrigem(), "contaOrigem");
		String chave = Validacao.chaveParaBusca(Validacao.textoObrigatorio(req.chaveDestino(), "chaveDestino"));
		double valor = Validacao.valorPositivo(req.valor(), "valor");
		if (new BigDecimal(String.valueOf(valor)).stripTrailingZeros().scale() > 2) {
			throw ApiException.invalido("VALOR_INVALIDO", "O valor deve ter no máximo 2 casas decimais.");
		}

		ContaBancaria origem = contas.buscarPorNumero(numeroOrigem)
		                       .orElseThrow(() -> ApiException.regra("CONTA_ORIGEM_NAO_ENCONTRADA",
		                                    "Conta de origem " + numeroOrigem + " não encontrada."));
		ContaBancaria destino = contas.buscarPorChavePix(chave)
		                        .orElseThrow(() -> ApiException.regra("CHAVE_DESTINO_NAO_ENCONTRADA",
		                                     "Chave PIX de destino não encontrada."));

		// regras do PIX
		if (!contas.chavePixEstaAtiva(chave)) {
			throw ApiException.regra("CHAVE_DESTINO_SUSPENSA", "A chave PIX de destino está suspensa.");
		}
		if (origem == destino) {
			throw ApiException.regra("TRANSFERENCIA_MESMA_CONTA", "A conta de origem não pode ser a mesma do destino.");
		}
		if (origem.isBloqueada()) {
			throw ApiException.regra("CONTA_ORIGEM_BLOQUEADA", "A conta de origem está bloqueada.");
		}
		if (destino.isBloqueada()) {
			throw ApiException.regra("CONTA_DESTINO_BLOQUEADA", "A conta de destino está bloqueada.");
		}
		if (valor > LIMITE_PIX) {
			throw ApiException.regra("LIMITE_EXCEDIDO", "O limite por PIX é de R$ 2.000,00.");
		}
		if (origem.getSaldo() < valor) {
			throw ApiException.regra("SALDO_INSUFICIENTE", "Saldo insuficiente para esta transferência.");
		}

		Transacao t;
		try {
			t = transacoes.execuTransacao(origem, destino, valor);
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw ApiException.regra("REGRA_PIX", e.getMessage());
		}

		Logs.evento(Logs.cid(ctx), "transferencia_realizada", Map.of(
		                "transacaoId", t.getId(),
		                "contaOrigem", origem.getNumeroConta(),
		                "contaDestino", destino.getNumeroConta(),
		                "valor", valor));

		ctx.status(201);
		ctx.header("Location", "/transacoes/" + t.getId());
		ctx.json(new TransferenciaResposta(TransacaoResposta.de(t), origem.getSaldo()));
	}

	// filtros opcionais: ?usuarioId=  |  ?data=AAAA-MM-DD  |  ?inicio=AAAA-MM-DD&fim=AAAA-MM-DD
	private void listar(Context ctx) {
		String usuarioId = ctx.queryParam("usuarioId");
		String data = ctx.queryParam("data");
		String inicio = ctx.queryParam("inicio");
		String fim = ctx.queryParam("fim");

		List<Transacao> lista;
		if (usuarioId != null) {
			lista = transacoes.gerarRelatorioPorUsuario(Validacao.inteiro(usuarioId, "usuarioId"));
		} else if (data != null) {
			lista = transacoes.filtrarPorData(data(data, "data"));
		} else if (inicio != null || fim != null) {
			if (inicio == null || fim == null) {
				throw ApiException.invalido("PERIODO_INCOMPLETO", "Informe 'inicio' e 'fim' juntos.");
			}
			try {
				lista = transacoes.gerarRelatorioPorPeriodo(data(inicio, "inicio"), data(fim, "fim"));
			} catch (IllegalArgumentException e) {
				throw ApiException.invalido("PERIODO_INVALIDO", e.getMessage());
			}
		} else {
			lista = transacoes.ListarTodas();
		}
		ctx.json(lista.stream().map(TransacaoResposta::de).toList());
	}

	private void buscar(Context ctx) {
		ctx.json(TransacaoResposta.de(obter(ctx)));
	}

	private void estornar(Context ctx) {
		Transacao t = obter(ctx);

		if (t.getStatus() != StatusTransacao.CONCLUIDA) {
			throw ApiException.regra("TRANSACAO_NAO_ESTORNAVEL", "Só é possível estornar transações concluídas.");
		}
		if (t.getContaDestino().getSaldo() < t.getValor()) {
			throw ApiException.regra("SALDO_INSUFICIENTE_ESTORNO", "A conta de destino não tem saldo para o estorno.");
		}

		if (!transacoes.estornarTransacao(t.getId())) {
			throw ApiException.regra("FALHA_ESTORNO", "Não foi possível estornar a transação.");
		}

		Logs.evento(Logs.cid(ctx), "transacao_estornada", Map.of("transacaoId", t.getId(), "valor", t.getValor()));
		ctx.json(TransacaoResposta.de(t));
	}

	// ---------- auxiliares ----------

	private LocalDate data(String valor, String campo) {
		try {
			return LocalDate.parse(valor);
		} catch (DateTimeParseException e) {
			throw ApiException.invalido("DATA_INVALIDA", "O campo '" + campo + "' deve estar no formato AAAA-MM-DD.");
		}
	}

	private ContaBancaria contaDoPath(Context ctx) {
		String numero = ctx.pathParam("numero");
		return contas.buscarPorNumero(numero)
		       .orElseThrow(() -> ApiException.naoEncontrado("CONTA_NAO_ENCONTRADA", "Conta " + numero + " não encontrada."));
	}

	private Transacao obter(Context ctx) {
		int id = Validacao.inteiro(ctx.pathParam("id"), "id");
		return transacoes.getHistoricoTransacoes().stream()
		       .filter(t -> t.getId() == id)
		       .findFirst()
		       .orElseThrow(() -> ApiException.naoEncontrado("TRANSACAO_NAO_ENCONTRADA", "Transação " + id + " não encontrada."));
	}
}
