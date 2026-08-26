package service;

import model.ContaBancaria;
import model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContaService {
    // Lista que simula o banco de dados de contas
    private List<ContaBancaria> contas = new ArrayList<>();

    // Funcionalidade: Criar Conta Bancária
    // Note que ele recebe o objeto Usuario, garantindo que a conta tenha um dono
    public ContaBancaria criarConta(String numeroConta, Usuario dono) {
        ContaBancaria novaConta = new ContaBancaria(numeroConta, dono);
        contas.add(novaConta);
        return novaConta;
    }

    // Método auxiliar: Buscar conta por número (Essencial para o PIX)
    public Optional<ContaBancaria> buscarPorNumero(String numero) {
        return contas.stream()
                      .filter(c -> c.getNumeroConta().equals(numero))
                      .findFirst();
    }

    // Funcionalidade: Atualizar Dados da Conta
    public boolean atualizarNumeroConta(String numeroAntigo, String novoNumero) {
        Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroAntigo);
        if (contaOpt.isPresent()) {
            contaOpt.get().setNumeroConta(novoNumero);
            return true;
        }
        return false;
    }

    // Funcionalidade: Remover Conta Bancária
    public boolean removerConta(String numero) {
        return contas.removeIf(c -> c.getNumeroConta().equals(numero));
    }

    // Método auxiliar: Listar todas as contas
    public List<ContaBancaria> listarContas() {
        return contas;
    }
}