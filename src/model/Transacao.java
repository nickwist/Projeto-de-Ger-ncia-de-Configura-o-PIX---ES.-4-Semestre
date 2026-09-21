package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transacao {
    public enum Status {
        PENDENTE, CONCLUIDA, CANCELADA
    }

    private String id;
    private ContaBancaria contaOrigem;
    private ContaBancaria contaDestino;
    private double valor;
    private LocalDateTime dataHora;
    private Status status;
    private String descricao;

    public Transacao(ContaBancaria contaOrigem, ContaBancaria contaDestino, double valor, String descricao) {
        this.id = UUID.randomUUID().toString();
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
        this.status = Status.PENDENTE;
        this.descricao = descricao;
    }

    // Getters
    public String getId() {
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
        return dataHora;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescricao() {
        return descricao;
    }

    // Setters
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // Métodos de negócio
    public void concluir() {
        this.status = Status.CONCLUIDA;
    }

    public void cancelar() {
        this.status = Status.CANCELADA;
    }

    public boolean isPendente() {
        return this.status == Status.PENDENTE;
    }

    public boolean isConcluida() {
        return this.status == Status.CONCLUIDA;
    }

    public boolean isCancelada() {
        return this.status == Status.CANCELADA;
    }

    @Override
    public String toString() {
        return "Transacao{" +
                "id='" + id + '\'' +
                ", contaOrigem=" + (contaOrigem != null ? contaOrigem.getNumeroConta() : "null") +
                ", contaDestino=" + (contaDestino != null ? contaDestino.getNumeroConta() : "null") +
                ", valor=R$" + String.format("%.2f", valor) +
                ", dataHora=" + dataHora +
                ", status=" + status +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}