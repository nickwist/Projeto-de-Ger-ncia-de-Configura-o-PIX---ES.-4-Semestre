package service;

import model.StatusTransacao;
import model.Transacao;
import model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notificacao {

    private Map<Integer, List<String>> notificacoesPorUsuario = new HashMap<>();

    public void notificarUsuario(Transacao transacao, Usuario usuario) {
        if (transacao==null || usuario == null){
            throw new IllegalArgumentException(
                "Transação e usuário não podem ser nulos.");
        }

        String mensagem;

        // Verifica se o usuário é o remetente ou destinatário da transação
        if (transacao.getContaOrigem().getDono().getId() == usuario.getId()) {
            mensagem = String.format(
                "PIX ENVIADO: Olá %s! Você enviou R$ %.2f. Status: %s. ID: %d.",
                usuario.getNome(),
                transacao.getValor(),
                transacao.getStatus(),
                transacao.getId()
            );
        } else if (transacao.getContaDestino().getDono().getId() == usuario.getId()) {
            mensagem = String.format(
                "PIX RECEBIDO: Olá %s! Você recebeu R$ %.2f. Status: %s. ID: %d.",
                usuario.getNome(),
                transacao.getValor(),
                transacao.getStatus(),
                transacao.getId()
            );
        } else {
            return; // O usuário não está envolvido na transação
        }

        List<String> notificacoes = notificacoesPorUsuario.computeIfAbsent(
            usuario.getId(), id -> new ArrayList<>()
        );
        notificacoes.add(mensagem);

        System.out.println(mensagem); // Para fins de depuração, imprime a notificação no console
    }

    public void notificarEstorno(Transacao transacao) {
        if (transacao==null){
            throw new IllegalArgumentException(
                "A transação não pode ser nula."
            );
        }

        if (transacao.getStatus() != StatusTransacao.ESTORNADA) {
            throw new IllegalArgumentException(
                "Não é possível notificar um estorno não realizado."
            );
        }

        Usuario origem = transacao.getContaOrigem().getDono();
        Usuario destino = transacao.getContaDestino().getDono();

        String mensagemOrigem = String.format(
            "PIX ESTORNADO: Olá %s! O PIX enviado de R$ %.2f foi estornado. ID: %d.",
            origem.getNome(),
            transacao.getValor(),
            transacao.getId()
        );

        String mensagemDestino = String.format(
            "PIX ESTORNADO: Olá %s! O PIX recebido de R$ %.2f foi estornado. ID: %d.",
            destino.getNome(),
            transacao.getValor(),
            transacao.getId()
        );

        notificacoesPorUsuario.computeIfAbsent(
            origem.getId(), id -> new ArrayList<>()
        ).add(mensagemOrigem);

        notificacoesPorUsuario.computeIfAbsent(
            destino.getId(), id -> new ArrayList<>()
        ).add(mensagemDestino);

        System.out.println(mensagemOrigem); // Para fins de depuração, imprime a notificação no console
        System.out.println(mensagemDestino); // Para fins de depuração, imprime a notificação no console

    }

    public List<String> listarNotificacoes(Usuario usuario) {
        if (usuario == null) {
            return new ArrayList<>(); // Retorna lista vazia se o usuário for nulo
        }

        return notificacoesPorUsuario.getOrDefault(
            usuario.getId(), new ArrayList<>()
        );
        
    /*Recebe o usuário cujo histórico queremos consultar.
    Verifica se o usuário é nulo.
    Procura as notificações pelo ID do usuário.
    Se não existir histórico, retorna uma lista vazia.*/

    }
}
