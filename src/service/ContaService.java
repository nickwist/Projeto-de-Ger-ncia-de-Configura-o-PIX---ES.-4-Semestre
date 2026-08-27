package service;

import model.ContaBancaria;
import model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContaService {
    private List<ContaBancaria> contas = new ArrayList<>();

    public ContaBancaria criarConta(String numeroConta, Usuario dono) {
        ContaBancaria novaConta = new ContaBancaria(numeroConta, dono);
        contas.add(novaConta);
        return novaConta;
    }

    public Optional<ContaBancaria> buscarPorNumero(String numero) {
        return contas.stream()
                      .filter(c -> c.getNumeroConta().equals(numero))
                      .findFirst();
    }

    public boolean atualizarNumeroConta(String numeroAntigo, String novoNumero) {
        Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroAntigo);
        if (contaOpt.isPresent()) {
            contaOpt.get().setNumeroConta(novoNumero);
            return true;
        }
        return false;
    }

    public boolean removerConta(String numero) {
        return contas.removeIf(c -> c.getNumeroConta().equals(numero));
    }

    public List<ContaBancaria> listarContas() {
        return contas;
    }
}
