package model;

public class ContaBancaria {
    private String numeroConta;
    private double saldo;
    private Usuario dono;
    private String chavePix;

    public ContaBancaria(String numeroConta, Usuario dono) {
        this.numeroConta = numeroConta;
        this.dono = dono;
        this.saldo = 0.0;
    }

    public void depositar(double valor) {
        if (valor > 0) {
            this.saldo += valor;
        }
    }

    public boolean sacar(double valor) {
        if (valor > 0 && this.saldo >= valor) {
            this.saldo -= valor;
            return true;
        }
        return false;
    }

    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public double getSaldo() { return saldo; }
    public Usuario getDono() { return dono; }
    public void setDono(Usuario dono) { this.dono = dono; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    @Override
    public String toString() {
        return "Conta{" + "Número='" + numeroConta + '\'' + ", Saldo=R$" + saldo + ", Dono=" + dono.getNome() + '}';
    }
}