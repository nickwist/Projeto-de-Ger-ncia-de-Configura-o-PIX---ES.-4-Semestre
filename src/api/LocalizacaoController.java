package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import model.Localizacao;
import service.LocalizacaoService;

public class LocalizacaoController {

    // Dados recebidos ao registrar uma localização.
    public record LocalizacaoRequest(Double latitude, Double longitude) {
    }

    // Dados enviados na resposta da API.
    public record LocalizacaoResposta(int transacaoId, double latitude, double longitude) {
        static LocalizacaoResposta de(Localizacao localizacao) {
            return new LocalizacaoResposta(
                    localizacao.getTransacaoId(),
                    localizacao.getLatitude(),
                    localizacao.getLongitude()
            );
        }
    }

    private final LocalizacaoService localizacoes;

    public LocalizacaoController(LocalizacaoService localizacoes) {
        this.localizacoes = localizacoes;
    }

    // Registra as rotas da localização.
    public void registrar(Javalin app) {
        app.post("/transacoes/{id}/localizacao", this::registrarLocalizacao);
        app.get("/transacoes/{id}/localizacao", this::consultarLocalizacao);
    }

    // Recebe e registra uma localização.
    private void registrarLocalizacao(Context ctx) {

        int transacaoId = Validacao.inteiro(ctx.pathParam("id"), "id");

        LocalizacaoRequest req = ctx.bodyAsClass(LocalizacaoRequest.class);

        if (req == null || req.latitude() == null || req.longitude() == null) {
            throw ApiException.invalido(
                    "LOCALIZACAO_INVALIDA",
                    "Latitude e longitude são obrigatórias."
            );
        }

        try {
            localizacoes.registrarLocalizacao(
                    transacaoId,
                    req.latitude(),
                    req.longitude()
            );
        } catch (IllegalArgumentException e) {
            throw ApiException.invalido("LOCALIZACAO_INVALIDA", e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            throw ApiException.naoEncontrado("TRANSACAO_NAO_ENCONTRADA", e.getMessage());
        } catch (IllegalStateException e) {
            throw ApiException.conflito("LOCALIZACAO_JA_EXISTE", e.getMessage());
        }

        ctx.status(201);
        ctx.json(new LocalizacaoResposta(
                transacaoId,
                req.latitude(),
                req.longitude()
        ));
    }

    // Consulta uma localização já registrada.
    private void consultarLocalizacao(Context ctx) {

        int transacaoId = Validacao.inteiro(ctx.pathParam("id"), "id");

        try {
            Localizacao localizacao = localizacoes.consultarLocalizacao(transacaoId);
            ctx.json(LocalizacaoResposta.de(localizacao));
        } catch (IllegalArgumentException e) {
            throw ApiException.invalido("ID_INVALIDO", e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            throw ApiException.naoEncontrado("LOCALIZACAO_NAO_ENCONTRADA", e.getMessage());
        }
    }
}