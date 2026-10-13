package model;

public class ChavePix {

    private String valor;
    private String tipo;
    private boolean suspensa;

    public ChavePix(String valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
        this.suspensa = false;
    }

    public String getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isSuspensa() {
        return suspensa;
    }

    public void suspender() {
        this.suspensa = true;
    }

    public void desbloquear() {
        this.suspensa = false;
    }

    public void alterarValor(String novoValor) {
        this.valor = novoValor;
    }
}