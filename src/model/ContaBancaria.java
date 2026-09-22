package model;

import java.util.ArrayList;
import java.util.List;

public class ContaBancaria {
    private String numeroConta;
    private double saldo;
    private Usuario dono;
    private final  List<String> chavesPix = new ArrayList<>(); // o final no nome significa que essa variável nunca vai apontar pra outra lista depois de criada, mas que o conteúdo de dentro pode mudar com add
    //as contas costumam vir acessíveis
    private boolean bloqueada;

    public ContaBancaria(String numeroConta, Usuario dono) {
        this.numeroConta = numeroConta;
        this.dono = dono;
        this.saldo = 0.0;
        this.bloqueada = false;
    }

    //caso seja necessário bloquear a conta
    public void bloquearConta(){
        this.bloqueada = true;
    }
    //e desbloquear
    public void desbloquearConta(){
        this.bloqueada = false;
    }
    //consultar o status da conta, se tá bloqu ou não.
    public boolean isBloqueada(){
        return bloqueada;
    }
    //plus: garantir que não há como fazer transações envolvendo a conta bloqueada
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

    //aqui entramos no quesito de segurança, adicionando métodos de controle de acesse à conta
    //e evitando fraudes

}