package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;
import model.Usuario;
import service.ContaService;
import service.UsuarioService;

/** CRUD de usuarios. Reaproveita o UsuarioService existente. */
public class UsuarioController {

	public record UsuarioRequest(String nome, String cpf, String email, String telefone) {
	}

	public record UsuarioAtualizacao(String nome, String email, String telefone) {
	}

	public record UsuarioResposta(int id, String nome, String cpf, String email, String telefone) {
		static UsuarioResposta de(Usuario u) {
			return new UsuarioResposta(u.getId(), u.getNome(), u.getCpf(), u.getEmail(), u.getTelefone());
		}
	}

	private final UsuarioService usuarios;
	private final ContaService contas;

	public UsuarioController(UsuarioService usuarios, ContaService contas) {
		this.usuarios = usuarios;
		this.contas = contas;
	}

	public void registrar(Javalin app) {
		app.post("/usuarios", this::criar);
		app.get("/usuarios", this::listar);
		app.get("/usuarios/{id}", this::buscar);
		app.put("/usuarios/{id}", this::atualizar);
		app.delete("/usuarios/{id}", this::remover);
	}

	private void criar(Context ctx) {
		UsuarioRequest req = ctx.bodyAsClass(UsuarioRequest.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}

		String nome = Validacao.textoObrigatorio(req.nome(), "nome");
		String cpf = Validacao.cpf(req.cpf());
		String email = Validacao.email(req.email());
		String telefone = Validacao.telefone(req.telefone());

		boolean cpfJaExiste = usuarios.listarTodos().stream().anyMatch(u -> cpf.equals(u.getCpf()));
		if (cpfJaExiste) {
			throw ApiException.conflito("CPF_JA_CADASTRADO", "Já existe um usuário com este CPF.");
		}

		Usuario novo = usuarios.cadastrar(nome, cpf, email, telefone);

		Logs.evento(Logs.cid(ctx), "usuario_criado", Map.of(
		                "usuarioId", novo.getId(),
		                "cpf", Logs.mascararCpf(cpf),
		                "email", Logs.mascararEmail(email)));

		ctx.status(201);
		ctx.header("Location", "/usuarios/" + novo.getId());
		ctx.json(UsuarioResposta.de(novo));
	}

	private void listar(Context ctx) {
		List<UsuarioResposta> lista = usuarios.listarTodos().stream().map(UsuarioResposta::de).toList();
		ctx.json(lista);
	}

	private void buscar(Context ctx) {
		ctx.json(UsuarioResposta.de(obter(ctx)));
	}

	private void atualizar(Context ctx) {
		Usuario atual = obter(ctx);

		UsuarioAtualizacao req = ctx.bodyAsClass(UsuarioAtualizacao.class);
		if (req == null) {
			throw ApiException.invalido("CORPO_AUSENTE", "O corpo da requisição é obrigatório.");
		}
		String nome = Validacao.textoObrigatorio(req.nome(), "nome");
		String email = Validacao.email(req.email());
		String telefone = Validacao.telefone(req.telefone());

		usuarios.atualizar(atual.getId(), nome, email, telefone);

		Logs.evento(Logs.cid(ctx), "usuario_atualizado", Map.of("usuarioId", atual.getId()));
		ctx.json(UsuarioResposta.de(obter(ctx)));
	}

	private void remover(Context ctx) {
		Usuario atual = obter(ctx);

		boolean temConta = contas.listarContas().stream()
		                   .anyMatch(c -> c.getDono() != null && c.getDono().getId() == atual.getId());
		if (temConta) {
			throw ApiException.conflito("USUARIO_POSSUI_CONTAS", "Remova as contas do usuário antes de excluí-lo.");
		}

		usuarios.remover(atual.getId());
		Logs.evento(Logs.cid(ctx), "usuario_removido", Map.of("usuarioId", atual.getId()));
		ctx.status(204);
	}

	// busca o usuario do path ou lanca 404
	private Usuario obter(Context ctx) {
		int id = Validacao.inteiro(ctx.pathParam("id"), "id");
		return usuarios.buscarPorId(id)
		       .orElseThrow(() -> ApiException.naoEncontrado("USUARIO_NAO_ENCONTRADO", "Usuário " + id + " não encontrado."));
	}
}
