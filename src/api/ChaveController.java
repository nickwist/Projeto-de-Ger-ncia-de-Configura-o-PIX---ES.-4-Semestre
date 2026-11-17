package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.ChavePix;
import model.ContaBancaria;
import service.ContaService;
import service.PixService;

/**
 *Chaves PIX: (gerar, validar, associar a uma conta, consultar, alterar)
 * suspender/desbloquear e remover. Reaproveita ContaService e PixService.
 */
public class ChaveController {

	private static final Set<String> TIPOS = Set.of("CPF", "CNPJ", "EMAIL", "TELEFONE", "ALEATORIA");
	private static final int LIMITE_CHAVES_POR_CONTA = 5;
	private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

	public record ChaveRequest(String tipo, String valor) {
	}

	public record ChaveAlteracao(String novoValor) {
	}

	public record ChaveDetalhe(String valor, String tipo, boolean suspensa, String numeroConta, String titular) {
		static ChaveDetalhe de(ChavePix c, ContaBancaria conta) {
			return new ChaveDetalhe(
			           c.getValor(), c.getTipo(), c.isSuspensa(), conta.getNumeroConta(),
			           conta.getDono() == null ? null : conta.getDono().getNome());
		}
	}

	private final ContaService contas;
	private final PixService pix = new PixService();

	public ChaveController(ContaService contas) {
		this.contas = contas;
	}

	public void registrar(Javalin app) {
		app.post("/chaves/gerar", this::gerar);
		app.post("/chaves/validar", this::validar);

		app.post("/contas/{numero}/chaves", this::associar);
		app.get("/contas/{numero}/chaves", this::listarDaConta);

		app.get("/chaves/{valor}", this::consultar);
		app.put("/chaves/{valor}", this::alterar);
		app.delete("/chaves/{valor}", this::remover);
		app.post("/chaves/{valor}/suspensao", this::suspender);
		app.delete("/chaves/{valor}/suspensao", this::desbloquear);
	}

	// gera uma chave aleatoria SEM associar a nenhuma conta, ênfase nisso
	private void gerar(Context ctx) {
		ctx.json(Map.of("chave", pix.gerarChaveAleatoria(), "tipo", "ALEATORIA", "associada", false));
	}

	// diz se a chave tem formato valido para o tipo e se esta disponivel ne
	private void validar(Context ctx) {
		ChaveRequest req = corpo(ctx);
		String tipo = tipo(req.tipo());
		try {
			String valor = normalizar(tipo, req.valor());
			boolean disponivel = contas.buscarPorChavePix(valor).isEmpty();
			ctx.json(Map.of("valida", true, "tipo", tipo, "valor", valor, "disponivel", disponivel));
		} catch (ApiException e) {
			ctx.json(Map.of("valida", false, "tipo", tipo, "motivo", e.getMessage()));
		}
	}

	private void associar(Context ctx) {
		ContaBancaria conta = contaDoPath(ctx);
		ChaveRequest req = corpo(ctx);
		String tipo = tipo(req.tipo());

		String valor;
		if (tipo.equals("ALEATORIA") && (req.valor() == null || req.valor().isBlank())) {
			valor = pix.gerarChaveAleatoria();
		} else {
			valor = normalizar(tipo, req.valor());
		}

		//regras do PIX
		if (conta.isBloqueada()) {
			throw ApiException.regra("CONTA_BLOQUEADA", "Não é possível cadastrar chave em uma conta bloqueada.");
		}
		if (conta.getChavesPix().size() >= LIMITE_CHAVES_POR_CONTA) {
			throw ApiException.regra("LIMITE_CHAVES_ATINGIDO",
			                         "Cada conta pode ter no máximo " + LIMITE_CHAVES_POR_CONTA + " chaves PIX.");
		}
		if (tipo.equals("CPF") && conta.getDono() != null && !valor.equals(conta.getDono().getCpf())) {
			throw ApiException.regra("CHAVE_CPF_DIFERENTE_DO_TITULAR", "A chave CPF deve ser o CPF do titular da conta.");
		}
		if (contas.buscarPorChavePix(valor).isPresent()) {
			throw ApiException.conflito("CHAVE_JA_CADASTRADA", "Esta chave PIX já está cadastrada.");
		}

		ChavePix criada;
		if (tipo.equals("ALEATORIA")) {
			// o PixService.validarChave ainda nao conhece o tipo ALEATORIA, então botei pra adicionar direto
			criada = new ChavePix(valor, tipo);
			conta.adicionarChavePix(criada);
		} else {
			if (!contas.associarChavePix(conta.getNumeroConta(), valor, tipo)) {
				throw ApiException.regra("FALHA_ASSOCIACAO", "Não foi possível associar a chave à conta.");
			}
			criada = achar(conta, valor);
		}

		Logs.evento(Logs.cid(ctx), "chave_associada", Map.of(
		                "conta", conta.getNumeroConta(), "tipo", tipo, "chave", Logs.mascararChave(tipo, valor)));

		ctx.status(201);
		ctx.header("Location", "/chaves/" + valor);
		ctx.json(ChaveDetalhe.de(criada, conta));
	}

	private void listarDaConta(Context ctx) {
		ContaBancaria conta = contaDoPath(ctx);
		List<ChaveDetalhe> lista = conta.getChavesPix().stream().map(c -> ChaveDetalhe.de(c, conta)).toList();
		ctx.json(lista);
	}

	private void consultar(Context ctx) {
		String valor = valorDoPath(ctx);
		ContaBancaria conta = contaDaChave(valor);
		ctx.json(ChaveDetalhe.de(achar(conta, valor), conta));
	}

	private void alterar(Context ctx) {
		String atual = valorDoPath(ctx);
		ContaBancaria conta = contaDaChave(atual);
		ChavePix chave = achar(conta, atual);

		ChaveAlteracao req = ctx.bodyAsClass(ChaveAlteracao.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}

		if (chave.getTipo().equals("ALEATORIA")) {
			throw ApiException.regra("CHAVE_ALEATORIA_NAO_EDITAVEL",
			                         "Chave aleatória não pode ser alterada. Remova e gere outra.");
		}
		if (conta.isBloqueada()) {
			throw ApiException.regra("CONTA_BLOQUEADA", "Não é possível alterar chave de uma conta bloqueada.");
		}

		String novo = normalizar(chave.getTipo(), req.novoValor());
		if (chave.getTipo().equals("CPF") && conta.getDono() != null && !novo.equals(conta.getDono().getCpf())) {
			throw ApiException.regra("CHAVE_CPF_DIFERENTE_DO_TITULAR", "A chave CPF deve ser o CPF do titular da conta.");
		}
		if (contas.buscarPorChavePix(novo).isPresent()) {
			throw ApiException.conflito("CHAVE_JA_CADASTRADA", "Esta chave PIX já está cadastrada.");
		}

		if (!contas.alterarChavePix(atual, novo)) {
			throw ApiException.regra("FALHA_ALTERACAO", "Não foi possível alterar a chave.");
		}

		Logs.evento(Logs.cid(ctx), "chave_alterada", Map.of(
		                "conta", conta.getNumeroConta(), "tipo", chave.getTipo(),
		                "chave", Logs.mascararChave(chave.getTipo(), novo)));
		ctx.json(ChaveDetalhe.de(chave, conta));
	}

	private void remover(Context ctx) {
		String valor = valorDoPath(ctx);
		ContaBancaria conta = contaDaChave(valor);
		ChavePix chave = achar(conta, valor);
		conta.getChavesPix().removeIf(c -> c.getValor().equals(valor));
		Logs.evento(Logs.cid(ctx), "chave_removida", Map.of(
		                "conta", conta.getNumeroConta(), "tipo", chave.getTipo(),
		                "chave", Logs.mascararChave(chave.getTipo(), valor)));
		ctx.status(204);
	}

	private void suspender(Context ctx) {
		String valor = valorDoPath(ctx);
		ContaBancaria conta = contaDaChave(valor);
		if (!contas.suspenderChavePix(valor)) {
			throw ApiException.conflito("CHAVE_JA_SUSPENSA", "A chave já está suspensa.");
		}
		Logs.evento(Logs.cid(ctx), "chave_suspensa", Map.of("conta", conta.getNumeroConta()));
		ctx.json(ChaveDetalhe.de(achar(conta, valor), conta));
	}

	private void desbloquear(Context ctx) {
		String valor = valorDoPath(ctx);
		ContaBancaria conta = contaDaChave(valor);
		if (!contas.desbloquearChavePix(valor)) {
			throw ApiException.conflito("CHAVE_NAO_SUSPENSA", "A chave não está suspensa.");
		}
		Logs.evento(Logs.cid(ctx), "chave_desbloqueada", Map.of("conta", conta.getNumeroConta()));
		ctx.json(ChaveDetalhe.de(achar(conta, valor), conta));
	}

	// ---------- auxiliares ----------

	private ChaveRequest corpo(Context ctx) {
		ChaveRequest req = ctx.bodyAsClass(ChaveRequest.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}
		return req;
	}

	private String tipo(String valor) {
		String tipo = Validacao.textoObrigatorio(valor, "tipo").toUpperCase();
		if (!TIPOS.contains(tipo)) {
			throw ApiException.invalido("TIPO_CHAVE_INVALIDO", "Tipo deve ser um de: CPF, CNPJ, EMAIL, TELEFONE, ALEATORIA.");
		}
		return tipo;
	}

	// limpa e valida o valor conforme o tipo; devolve a forma padronizada
	private String normalizar(String tipo, String valorBruto) {
		String bruto = Validacao.textoObrigatorio(valorBruto, "valor");
		String valor;
		switch (tipo) {
			case "CPF":
			case "CNPJ":
			case "TELEFONE":
				valor = bruto.replaceAll("\\D", "");
				break;
			case "EMAIL":
				valor = Validacao.email(bruto).toLowerCase();
				break;
			default:
				valor = bruto.toLowerCase();
				if (!valor.matches(UUID_REGEX)) {
					throw ApiException.invalido("CHAVE_INVALIDA", "Chave aleatória deve ser um UUID.");
				}
				return valor;
		}
		if (!pix.validarChave(valor, tipo)) {
			throw ApiException.invalido("CHAVE_INVALIDA", "Valor inválido para o tipo " + tipo + ".");
		}
		return valor;
	}

	private String valorDoPath(Context ctx) {
		String v = ctx.pathParam("valor").trim();
		return v.contains("@") ? v.toLowerCase() : v;
	}

	private ContaBancaria contaDoPath(Context ctx) {
		String numero = ctx.pathParam("numero");
		return contas.buscarPorNumero(numero)
		       .orElseThrow(() -> ApiException.naoEncontrado("CONTA_NAO_ENCONTRADA", "Conta " + numero + " não encontrada."));
	}

	private ContaBancaria contaDaChave(String valor) {
		return contas.buscarPorChavePix(valor)
		       .orElseThrow(() -> ApiException.naoEncontrado("CHAVE_NAO_ENCONTRADA", "Chave PIX não encontrada."));
	}

	private ChavePix achar(ContaBancaria conta, String valor) {
		return conta.getChavesPix().stream().filter(c -> c.getValor().equals(valor)).findFirst().orElseThrow();
	}
}
