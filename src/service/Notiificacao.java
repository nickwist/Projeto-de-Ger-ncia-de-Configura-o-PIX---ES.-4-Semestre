package service;
import model.Transacao;
import model.Usuario;

public class Notiificacao {
    public void notificarUsuario(Transacao transacao, Usuario usuario){
        if (transacao==null || usuario == null){
            throw new IllegalArgumentException("Não há transacao e usuario de destino, por favor, inserir.");
        }
        //mensagem informando o nome do usuario que esta fazendo a transacao,
        //usuario de destino, valor, status e o id da transacao
        String msg = String.format("NOTIFICACAO PIX: Olá, %s! A transação no valor de %.2f foi regustrada com o status %s e ID %d.",
            usuario.getNome(), transacao.getValor(), transacao.getStatus(), transacao.getId()
        );

        System.out.println(msg);
    }
}
