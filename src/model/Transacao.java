package model;
//movimentaçao financeira entre duas contas
//informar quando foi realizada a transação (vai ser usado mais tarde em relatório e seguranção também)
import java.time.LocalDateTime;
public class Transacao {
    //todos os atributos necessários para a execução de uma transação:
    //id, conta de origem e de destino, valor, data e status
    private int id;
    private ContaBancaria contaOrigem;
    private ContaBancaria contaDestino;
    private double valor;
    private StatusTransacao status;
    private LocalDateTime data_Hora;

    //construtor
    public Transacao(int id, ContaBancaria contaOrigem, ContaBancaria contaDestino, double valor, StatusTransacao status, LocalDateTime data_Hora){
        this.id = id;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        //status começa pendente pq ainda está "em análise"
        this.status = StatusTransacao.PENDENTE;
        this.data_Hora = data_Hora;
    }
    
//getters e setters    
    public int getId() {
        return id;
    }

    public ContaBancaria getContaOrigem() {
        return contaOrigem;
    }

    public ContaBancaria getContaDestino() {
        return contaDestino;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return data_Hora;
    }

    public StatusTransacao getStatus() {
        return status;
    }

    public void setStatus(StatusTransacao status) {
        this.status = status;
    }
}

