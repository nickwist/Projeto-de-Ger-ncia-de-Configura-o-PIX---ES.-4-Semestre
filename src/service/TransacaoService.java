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
        
        //se tem saldo e o valor é válido, conseguimos fazer o Pixie

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

}

