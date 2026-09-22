package model;

import java.util.ArrayList;
import java.util.List;

public class ContaBancaria {
    private String numeroConta;
    private double saldo;
    private Usuario dono;
    private final  List<String> chavesPix = new ArrayList<>(); // o final no nome significa que essa variável nunca vai apontar pra outra lista depois de criada, mas que o conteúdo de dentro pode mudar com add

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
    public double setSaldo(double saldo) {return this.saldo = saldo; }
    public Usuario getDono() { return dono; }
    public void setDono(Usuario dono) { this.dono = dono; }
    public List<String> getChavesPix() { return chavesPix; }
    public void adicionarChavePix(String chavePix) { chavesPix.add(chavePix); }

    @Override
    public String toString() {
        return "Conta{" + "Número='" + numeroConta + '\'' + ", Saldo=R$" + saldo + ", Dono=" + dono.getNome() + '}';
    }
}