package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import java.util.Map;
import java.util.UUID;
import service.ContaService;
import service.UsuarioService;

/** Ponto de entrada da API REST do PIX (Javalin). */
public class ApiServer {

	public static void main(String[] args) {
		int porta = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));

		UsuarioService usuarios = new UsuarioService();
		ContaService contas = new ContaService();

		Javalin app = criar(usuarios, contas);
		app.start(porta);
	}

	static Javalin criar(UsuarioService usuarios, ContaService contas) {
		Javalin app = Javalin.create(config -> {
			config.showJavalinBanner = false;
		});

		configurarObservabilidade(app);
		configurarErros(app);

		app.get("/saude", ctx -> ctx.json(Map.of("status", "ok")));

		new UsuarioController(usuarios, contas).registrar(app);
		new ContaController(usuarios, contas).registrar(app);

		return app;
	}

	// correlation id + log estruturado de toda requisicao
	private static void configurarObservabilidade(Javalin app) {
		app.before(ctx -> {
			String cid = ctx.header("X-Correlation-Id");
			if (cid == null || cid.isBlank()) {
				cid = UUID.randomUUID().toString();
			}
			ctx.attribute("cid", cid);
			ctx.attribute("inicio", System.nanoTime());
			ctx.header("X-Correlation-Id", cid);
		});

		app.after(ctx -> {
			Long inicio = ctx.attribute("inicio");
			long ms = inicio == null ? 0 : (System.nanoTime() - inicio) / 1_000_000;
			// loga o padrao da rota (/chaves/{valor}), nunca o valor real: pode ser CPF/e-mail
			String rota;
			try {
				rota = ctx.endpointHandlerPath();
				if (rota.startsWith("No handler")) {
					rota = "(sem-rota)";
				}
			} catch (Exception e) {
				rota = "(sem-rota)";
			}
			Logs.requisicao(Logs.cid(ctx), ctx.method().name(), rota, ctx.status().getCode(), ms);
		});
	}

	// toda falha sai no mesmo formato JSON
	private static void configurarErros(Javalin app) {
		app.exception(ApiException.class, (e, ctx) -> erro(ctx, e.getStatus(), e.getCodigo(), e.getMessage()));

		app.exception(HttpResponseException.class, (e, ctx) -> {
			int status = e.getStatus();
			String codigo = status == 404 ? "NAO_ENCONTRADO"
			                : status == 405 ? "METODO_NAO_PERMITIDO"
			                : "REQUISICAO_INVALIDA";
			erro(ctx, status, codigo, e.getMessage());
		});

		app.exception(Exception.class, (e, ctx) -> {
			Logs.erro(Logs.cid(ctx), "erro_interno", e);
			erro(ctx, 500, "ERRO_INTERNO", "Erro interno do servidor.");
		});
	}

	static void erro(Context ctx, int status, String codigo, String mensagem) {
		ctx.status(status);
		ctx.json(Map.of(
		             "status", status,
		             "erro", codigo,
		             "mensagem", mensagem,
		             "correlationId", Logs.cid(ctx)));
	}
}
