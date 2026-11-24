package service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import model.Localizacao;

public class LocalizacaoService {

    private Map<Integer, Localizacao> localizacoes;
    private TransacaoService transacaoService;

    public LocalizacaoService(TransacaoService transacaoService) {
        this.localizacoes = new HashMap<>();
        this.transacaoService = transacaoService;
    }

    // Registra a localização de uma transação PIX.
    public void registrarLocalizacao(int transacaoId, double latitude, double longitude) {

        if (transacaoId <= 0) {
            throw new IllegalArgumentException("ID da transação inválido.");
        }

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude inválida.");
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude inválida.");
        }

        // Verifica se a transação existe.
        boolean transacaoExiste = transacaoService.getHistoricoTransacoes()
                .stream()
                .anyMatch(t -> t.getId() == transacaoId);

        if (!transacaoExiste) {
            throw new NoSuchElementException("Transação não encontrada.");
        }

        // Impede o cadastro duplicado.
        if (localizacoes.containsKey(transacaoId)) {
            throw new IllegalStateException(
                    "Esta transação já possui uma localização registrada."
            );
        }

        Localizacao localizacao = new Localizacao(
                transacaoId,
                latitude,
                longitude
        );

        localizacoes.put(transacaoId, localizacao);
    }

    // Consulta a localização pelo ID da transação.
    public Localizacao consultarLocalizacao(int transacaoId) {

        if (transacaoId <= 0) {
            throw new IllegalArgumentException("ID da transação inválido.");
        }

        Localizacao localizacao = localizacoes.get(transacaoId);

        if (localizacao == null) {
            throw new NoSuchElementException(
                    "Localização não encontrada para esta transação."
            );
        }

        return localizacao;
    }
}