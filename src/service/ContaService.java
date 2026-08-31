package service; //Aqui mantemos tudo relacionado a conta (criar, buscar, atualizar, receber, transferir)

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.ContaBancaria;
import model.Usuario;

public class ContaService {
    private List<ContaBancaria> contas = new ArrayList<>();
    private PixService pixService = new PixService();
    //private List<String> chavesPix = new ArrayList<>(); //aqui só guardamos textos soltos aqui

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

    public Optional<ContaBancaria> buscarPorChavePix(String chavePix){
        //vamo buscar uma conta pela chave pix que está guardada pelo número dela
        for(ContaBancaria conta : contas){
            if(conta.getChavesPix().contains(chavePix)){
                return Optional.of(conta);
            }
        }
        return Optional.empty();
    }
    public boolean atualizarConta(String numeroAntigo, String novoNumero, Usuario novoDono) {
        Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroAntigo);
        if (contaOpt.isPresent()) {
            ContaBancaria conta = contaOpt.get();

            if (novoNumero != null && !novoNumero.isEmpty()) {
                conta.setNumeroConta(novoNumero);
            }

            if (novoDono != null) {
                conta.setDono(novoDono);
            }

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

    public boolean associarChavePix(String numeroConta, String chave, String tipo) { //é pra associar uma conta específica, com validação
        //Verificando se a chave não tá associada já
        if(!pixService.validarChave(chave, tipo)){
            return false; //estamos perguntando se a chave é valida
        }
        if(buscarPorChavePix(chave).isPresent()){
            return false;
        }
        //Procurando a conta que vai receber a chave
        Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroConta);

        //se a conta existe, associamos a chave
        if(contaOpt.isPresent()){
            contaOpt.get().adicionarChavePix(chave);
            return true;
        }

        //se a conta não existe
        return false;
    }
    public boolean receberViaPix(String chave, double valor){
        Optional<ContaBancaria> contaOpt = buscarPorChavePix(chave);
        //se encontrar a conta, deposita nela o valor
        if (contaOpt.isPresent()){
            contaOpt.get().depositar(valor);
            return true;
        }
        return false;
    }

    public boolean transferirViaPix(String chaveOrigem, String chaveDestino, double valor){
        Optional<ContaBancaria> contaOrigemOpt  = buscarPorChavePix(chaveOrigem);

        Optional<ContaBancaria> contaDestinoOpt = buscarPorChavePix(chaveDestino);

        if(!contaOrigemOpt.isPresent() || !contaDestinoOpt.isPresent()){
            return false;
        }

        ContaBancaria contaOrigem = contaOrigemOpt.get();
        ContaBancaria contaDestino = contaDestinoOpt.get();

        //tenta sacar o valor da conta de origem pra ver se tem saldo
        if(contaOrigem.sacar(valor)){
            contaDestino.depositar(valor);
            return true;
        }

        return false;
    }
}
