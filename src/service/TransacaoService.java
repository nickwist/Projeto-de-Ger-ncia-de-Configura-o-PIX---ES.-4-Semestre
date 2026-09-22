package service;
//regras de ngc, 
//validacao de saldo;
//transferencia
//registro de historico
//pendencias
//importar as informaçoes das contas e da transação
import model.ContaBancaria; 
import model.StatusTransacao; 
import model.Transacao; 
//listas
import java.util.ArrayList; 
import java.util.List;
//informar a hora
import java.time.LocalDate;

import model.Transacao;
import model.Usuario;

public class TransacaoService {
    //gerencia: transaçoes e consultas
    private List<Transacao> histTransacoes = new ArrayList<>();
    private int proximoId = 1;

    //consultar o saldo da conta, retornando quanto tem ainda
    public double consultarSaldo(ContaBancaria conta){
        if(conta == null){
            throw new IllegalArgumentException("Fail: Conta Inválisa");
        }
        return conta.getSaldo();
    }

    //executar transaçao Pix
    //adicionar transacao ao histórico(lista)
    //validar o saldo e a transferencia entre conta origem e de destino, atualizando os valores
    
    public Transacao execuTransacao(ContaBancaria origem, ContaBancaria destino, double valor){
        if(valor<=0){
            throw new IllegalArgumentException("Valor inválido. Deve ser maior que 0.");
        }

        //caso n tenha saldo na conta
        if (origem.getSaldo() < valor){
            throw new IllegalArgumentException("Você não tem saldo suficiente.");
        }

        //n tem como fazer transaçao envolvendo a conta bloqueada
        if(origem.isBloqueada() || destino.isBloqueada()){
            throw new IllegalStateException("Não foi possível realizar a transação. Uma das contas (ou ambas) estão bloqueadas.");
        }
        
        //se tem saldo e o valor é válido e a conta n estã bloqueada, conseguimos fazer o Pixie

        //criar uma nova transaçao

        Transacao transacao = new Transacao(proximoId++, origem, destino, valor, null, null);

        //retira o valor da conta de origem e o coloca na conta do destino

        origem.setSaldo(origem.getSaldo() - valor);
        destino.setSaldo(destino.getSaldo() + valor);

        //atualizando status
        transacao.setStatus(StatusTransacao.CONCLUIDA);

        //adicionar na lista de historico
        histTransacoes.add(transacao);

        return transacao;

    }

    // CANCELAR apenas UMA TRANSAÇAO PENDENTE
    public boolean cancelarTransacao(int id) {
        
        // Percorrendo a lista de transações uma a uma
        for (Transacao transacao : histTransacoes) {
            //encontrar a transação com o ID procurado
            if (transacao.getId() == id) {
                // fazendo o filtro das transações PENDENTES , que podem ser canceladas
                //mudando o status
                if (transacao.getStatus() == StatusTransacao.PENDENTE) {
                    transacao.setStatus(StatusTransacao.CANCELADA);
                    return true;
                }
                
                // Se encontrou o ID, mas não está PENDENTE, já podemos parar a busca
                break; 
            }
        }
        
        // Retorna false se não encontrou o ID ou se a transação não estava PENDENTE
        return false;
    }
    //retornando o histórico completo de transações
    public List<Transacao> getHistoricoTransacoes(){
        return histTransacoes;
    }

    ///////CONSULTAS E NOTIFICACOES, BRANCH feature/consultas-e-notificacoes
    
    //listando todas as transações
    public List<Transacao> ListarTodas(){
        return new ArrayList<>(histTransacoes);
    }

    //filtrando por data(certificando que ela existe)
    public List<Transacao> filtrarPorData(LocalDate dataDesejada){
        if(dataDesejada == null){
            throw new IllegalArgumentException("Insira uma data válida.");
        }

        //criar a lista vazia que vai guardar os resultados
        List<Transacao> transacoesFiltradas = new ArrayList<>();

        //percorrer a lista de transações com o for tradicional
        for (Transacao transacao : histTransacoes) {
            // comparar apenas o ano, mês e dia da data/hora da transação
            if (transacao.getDataHora().toLocalDate().equals(dataDesejada)) {
                transacoesFiltradas.add(transacao);
            }
        }
        return transacoesFiltradas;
    }

    //envio de notificacoes
    //importar transacao e usuario

    //enviar tipo um popup só pra dar o feedback da transacao
    //caso a transacao n foi bem sucedida(ou seja, transacao nula, e o usuario de destino tbm n recebeu nada)
    //melhor criar uma classe pra isso, pra gerência de feedback?


    ////GERANDO RELATORIOS
    //reunir todas as transaçoes que envolvem o usuario
    public List<Transacao> gerarRelatorioPorUsuario(int idUsuario) {
        
        //criar a lista vazia para armazenar as transacoes pro relatorio
        List<Transacao> relatorio = new ArrayList<>();

        //percorr todas as transações do historico
        for (Transacao transacao : histTransacoes) {
            //verificar se o usuário procurado é a origem da transação (pagador)
            boolean comoContaOrigem = transacao.getContaOrigem().getDono() != null 
                && transacao.getContaOrigem().getDono().getId() == idUsuario;
            
            //verifica se o usuário procurado é o destino da transação (recebedor)
            boolean contaComoDestino = transacao.getContaDestino().getDono() != null 
                && transacao.getContaDestino().getDono().getId() == idUsuario;
            
            //se o usuário participou em qualquer uma das formas, add na lista
            if (comoContaOrigem || contaComoDestino) {
                relatorio.add(transacao);
            }
        }

        return relatorio;
    }
}

