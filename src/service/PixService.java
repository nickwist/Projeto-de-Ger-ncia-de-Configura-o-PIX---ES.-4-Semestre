package service;

import java.util.UUID;
import model.ContaBancaria;

public class PixService {
    
    public String gerarChaveAleatoria() {
        return UUID.randomUUID().toString();
    }

    public boolean validarChave(String chave, String tipo) {
        if (chave == null || tipo == null) return false;

        switch (tipo.toUpperCase()) {
            case "CPF":
                return chave.matches("\\d{11}");
            case "CNPJ":
                return chave.matches("\\d{14}");
            case "EMAIL":
                return chave.contains("@") && chave.contains(".");
            case "TELEFONE":
                return chave.matches("\\d{10,11}");
            default:
                return false;
        }
    }

    public boolean transferir(ContaBancaria contaOrigem, ContaBancaria contaDestino, double valor) {
        if (contaOrigem == null || contaDestino == null || valor <= 0) {
            return false;
        }

        if (contaOrigem.sacar(valor)) {
            contaDestino.depositar(valor);
            return true;
        }

        return false;
    }

    public boolean receber(ContaBancaria contaDestino, ContaBancaria contaOrigem, double valor) {
        if (contaOrigem == null || contaDestino == null || valor <= 0) {
            return false;
        }

        if (contaOrigem.sacar(valor)) {
            contaDestino.depositar(valor);
            return true;
        }

        return false;
    }

}
