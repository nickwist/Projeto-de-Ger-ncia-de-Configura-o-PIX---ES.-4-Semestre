package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;
import model.ChavePix;
import model.ContaBancaria;
import model.Usuario;
import service.ContaService;
import service.UsuarioService;

/** CRUD de contas bancarias, bloqueio e deposito. Reaproveita o ContaService existente. */
public class ContaController {

	public record ContaRequest(String numeroConta, Integer usuarioId) {
	}

	public record ContaAtualizacao(String numeroConta, Integer usuarioId) {
	}

	public record DepositoRequest(Double valor) {
	}

	public record ChaveResposta(String valor, String tipo, boolean suspensa) {
		static ChaveResposta de(ChavePix c) {
			return new ChaveResposta(c.getValor(), c.getTipo(), c.isSuspensa());
		}
	}

	public record ContaResposta(String numeroConta, int usuarioId, String usuarioNome, double saldo,
	                            boolean bloqueada, List<ChaveResposta> chavesPix) {
		static ContaResposta de(ContaBancaria c) {
			Usuario d = c.getDono();
			return new ContaResposta(
			           c.getNumeroConta(),
			           d == null ? 0 : d.getId(),
			           d == null ? null : d.getNome(),
			           c.getSaldo(),
			           c.isBloqueada(),
			           c.getChavesPix().stream().map(ChaveResposta::de).toList());
		}
	}

	private final UsuarioService usuarios;
	private final ContaService contas;

	public ContaController(UsuarioService usuarios, ContaService contas) {
		this.usuarios = usuarios;
		this.contas = contas;
	}

	public void registrar(Javalin app) {
		app.post("/contas", this::criar);
		app.get("/contas", this::listar);
		app.get("/contas/{numero}", this::buscar);
		app.put("/contas/{numero}", this::atualizar);
		app.delete("/contas/{numero}", this::remover);

		app.post("/contas/{numero}/depositos", this::depositar);
		app.post("/contas/{numero}/bloqueio", this::bloquear);
		app.delete("/contas/{numero}/bloqueio", this::desbloquear);
	}

	private void criar(Context ctx) {
		ContaRequest req = ctx.bodyAsClass(ContaRequest.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}
		String numero = Validacao.numeroConta(req.numeroConta());
		Usuario dono = buscarDono(req.usuarioId());

		if (contas.buscarPorNumero(numero).isPresent()) {
			throw ApiException.conflito("CONTA_JA_EXISTE", "Já existe uma conta com este número.");
		}

		ContaBancaria nova = contas.criarConta(numero, dono);

		Logs.evento(Logs.cid(ctx), "conta_criada", Map.of("conta", numero, "usuarioId", dono.getId()));

		ctx.status(201);
		ctx.header("Location", "/contas/" + numero);
		ctx.json(ContaResposta.de(nova));
	}

	private void listar(Context ctx) {
		String filtro = ctx.queryParam("usuarioId");
		List<ContaBancaria> lista = contas.listarContas();
		if (filtro != null) {
			int id = Validacao.inteiro(filtro, "usuarioId");
			lista = lista.stream().filter(c -> c.getDono() != null && c.getDono().getId() == id).toList();
		}
		ctx.json(lista.stream().map(ContaResposta::de).toList());
	}

	private void buscar(Context ctx) {
		ctx.json(ContaResposta.de(obter(ctx)));
	}

	private void atualizar(Context ctx) {
		ContaBancaria atual = obter(ctx);

		ContaAtualizacao req = ctx.bodyAsClass(ContaAtualizacao.class);
		if (req == null || (req.numeroConta() == null && req.usuarioId() == null)) {
			throw ApiException.invalido("NADA_PARA_ATUALIZAR", "Informe numeroConta e/ou usuarioId.");
		}

		String novoNumero = null;
		if (req.numeroConta() != null) {
			novoNumero = Validacao.numeroConta(req.numeroConta());
			if (!novoNumero.equals(atual.getNumeroConta()) && contas.buscarPorNumero(novoNumero).isPresent()) {
				throw ApiException.conflito("CONTA_JA_EXISTE", "Já existe uma conta com este número.");
			}
		}

		Usuario novoDono = req.usuarioId() == null ? null : buscarDono(req.usuarioId());

		contas.atualizarConta(atual.getNumeroConta(), novoNumero, novoDono);

		Logs.evento(Logs.cid(ctx), "conta_atualizada", Map.of("conta", atual.getNumeroConta()));
		ctx.json(ContaResposta.de(atual));
	}

	private void remover(Context ctx) {
		ContaBancaria atual = obter(ctx);
		if (atual.getSaldo() > 0) {
			throw ApiException.conflito("CONTA_COM_SALDO", "Não é possível excluir uma conta com saldo.");
		}
		contas.removerConta(atual.getNumeroConta());
		Logs.evento(Logs.cid(ctx), "conta_removida", Map.of("conta", atual.getNumeroConta()));
		ctx.status(204);
	}

	// deposito simples, para colocar saldo na conta (necessario para testar transferencias)
	private void depositar(Context ctx) {
		ContaBancaria conta = obter(ctx);
		DepositoRequest req = ctx.bodyAsClass(DepositoRequest.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}
		double valor = Validacao.valorPositivo(req.valor(), "valor");

		if (conta.isBloqueada()) {
			throw ApiException.regra("CONTA_BLOQUEADA", "Não é possível depositar em uma conta bloqueada.");
		}

		conta.depositar(valor);
		Logs.evento(Logs.cid(ctx), "deposito_realizado", Map.of("conta", conta.getNumeroConta(), "valor", valor));
		ctx.json(ContaResposta.de(conta));
	}

	private void bloquear(Context ctx) {
		ContaBancaria conta = obter(ctx);
		conta.bloquearConta();
		Logs.evento(Logs.cid(ctx), "conta_bloqueada", Map.of("conta", conta.getNumeroConta()));
		ctx.json(ContaResposta.de(conta));
	}

	private void desbloquear(Context ctx) {
		ContaBancaria conta = obter(ctx);
		conta.desbloquearConta();
		Logs.evento(Logs.cid(ctx), "conta_desbloqueada", Map.of("conta", conta.getNumeroConta()));
		ctx.json(ContaResposta.de(conta));
	}

	// dono informado no corpo: se nao existir, o dado e valido mas viola uma regra (422)
	private Usuario buscarDono(Integer usuarioId) {
		if (usuarioId == null) {
			throw ApiException.invalido("CAMPO_OBRIGATORIO", "O campo 'usuarioId' é obrigatório.");
		}
		return usuarios.buscarPorId(usuarioId)
		       .orElseThrow(() -> ApiException.regra("USUARIO_NAO_ENCONTRADO", "Usuário " + usuarioId + " não encontrado."));
	}

	// conta do path ou 404
	private ContaBancaria obter(Context ctx) {
		String numero = ctx.pathParam("numero");
		return contas.buscarPorNumero(numero)
		       .orElseThrow(() -> ApiException.naoEncontrado("CONTA_NAO_ENCONTRADA", "Conta " + numero + " não encontrada."));
	}
}
