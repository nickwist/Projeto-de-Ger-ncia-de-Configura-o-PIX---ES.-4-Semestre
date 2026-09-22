package app; //precisamos de um pacote para agrupar as classes

import model.ContaBancaria;
import model.Usuario;
import model.Transacao;
import service.ContaService;
import service.UsuarioService;
import service.TransacaoService;
import service.Notiificacao;

import java.time.LocalDate;
import java.util.List;

public class Main { //Só pra testar
    public static void main(String[] args) {
        UsuarioService usuarioService = new UsuarioService();
        ContaService contaService = new ContaService();
        TransacaoService transacaoService = new TransacaoService();
        Notiificacao notificacao = new Notiificacao();

        //FLUXO ANTIGO (PRIMEIRA ENTREGA PIX)

        //Cadastrando um usuario
        Usuario nick = usuarioService.cadastrar("Nick :D", "12345678900", "nick@email.com", "85999999999");
        System.out.println("Usuário criado:" + nick);

        //criando uma conta pro mlk
        ContaBancaria contaNick = contaService.criarConta("006-7", nick);
        System.out.println("Conta criada:" + contaNick);

        //associando uma chave pix
        boolean associou = contaService.associarChavePix("006-7", "nick@email.com", "EMAIL");
        System.out.println("Chave Associada?" + associou);

        //Recebe um valor via Pix usando chave
        boolean recebeu = contaService.receberViaPix("nick@email.com", 500.0);
        System.out.println("Recebeu R$500 via PIX?" + recebeu);
        System.out.println("Saldo atual: " + contaNick.getSaldo());

        //Criamos um segundo usuário para testar transferência
        Usuario maria = usuarioService.cadastrar("Maria :0", "23456789000", "maria@email.com", "85988888888");
        ContaBancaria contaMaria = contaService.criarConta("006-9", maria);
        boolean associouMaria = contaService.associarChavePix("006-9", "maria@email.com", "EMAIL");
        System.out.println("Chave de Maria foi Associada? " + associouMaria);

        //NOVO NOVO NOVO AQUI FML: EXECUÇÃO DE TRANSAÇÃO VIA TransacaoService

        System.out.println("\nNOVO NOVO NOVO Testando TransacaoService");

        Transacao t1 = transacaoService.execuTransacao(contaNick, contaMaria, 100.0);
        System.out.println("Transação executada: ID " + t1.getId() + ", status " + t1.getStatus());
        System.out.println("Saldo Nick: " + contaNick.getSaldo());
        System.out.println("Saldo Maria: " + contaMaria.getSaldo());

        //notificando os dois sobre a transação
        notificacao.notificarUsuario(t1, nick);
        notificacao.notificarUsuario(t1, maria);

        //NOVO NOVO NOVO:BLOQUEIO DE CONTA

        System.out.println("\nNOVO NOVO NOVO Testando bloqueio de conta");
        contaMaria.bloquearConta();
        System.out.println("Conta da Maria bloqueada? " + contaMaria.isBloqueada());

        //tentamo fazer uma transação envolvendo a conta bloqueada, deve falhar
        try {
            transacaoService.execuTransacao(contaNick, contaMaria, 50.0);
        } catch (IllegalStateException e) {
            System.out.println("Falhou como esperado: " + e.getMessage());
        }

        contaMaria.desbloquearConta();
        System.out.println("Conta da Maria desbloqueada? " + !contaMaria.isBloqueada());

        //NOVO NOVO NOVO: CONSULTAR SALDO
        System.out.println("\n NOVO NOVO NOVO Testando consulta de saldo");
        double saldoNick = transacaoService.consultarSaldo(contaNick);
        System.out.println("Saldo consultado da conta do Nick: " + saldoNick);

        //NOVO NOVO NOVO: MAIS TRANSAÇÕES PARA TESTAR RELATÓRIOS
        Transacao t2 = transacaoService.execuTransacao(contaMaria, contaNick, 20.0);
        System.out.println("\nSegunda transação executada: ID " + t2.getId());

        //NOVO NOVO NOVO: CANCELAR TRANSAÇÃO

        System.out.println("\nNOVO NOVO NOVO Testando cancelamento de transação");
        boolean cancelou = transacaoService.cancelarTransacao(t1.getId());
        System.out.println("Cancelou a transação " + t1.getId() + "? " + cancelou);
        //Nota: como execuTransacao já marca a transação como CONCLUIDA na hora, nunca existe uma transação PENDENTE pra cancelar de verdade. É esperado que dê false e o teste deu false mesmo.

        //NOVO NOVO NOVO: LISTAR TODAS AS TRANSAÇÕES
        System.out.println("\nNOVO NOVO NOVO Listando todas as transações");
        List<Transacao> todas = transacaoService.ListarTodas();
        for (Transacao t : todas) {
            System.out.println("ID " + t.getId() + " | R$" + t.getValor() + " | " + t.getStatus());
        }

        //NOVO NOVO: FILTRAR POR DATA

        System.out.println("\nNOVO NOVO NOVO Filtrando transações de hoje");
        List<Transacao> hoje = transacaoService.filtrarPorData(LocalDate.now());
        System.out.println("Transações encontradas hoje: " + hoje.size());

        //NOVO NOVO: RELATÓRIO POR USUÁRIO

        System.out.println("\nNOVO NOVO NOVO Relatório por usuário (Nick)");
        List<Transacao> relatorioNick = transacaoService.gerarRelatorioPorUsuario(nick.getId());
        System.out.println("Transações envolvendo o Nick: " + relatorioNick.size());

        //NOVO NOVO FINALMENTE: RELATÓRIO POR PERÍODO

        System.out.println("\nNOVO NOVO NOVO Relatório por período (últimos 7 dias)");
        List<Transacao> relatorioPeriodo = transacaoService.gerarRelatorioPorPeriodo(
            LocalDate.now().minusDays(7), LocalDate.now()
        );
        System.out.println("Transações no período: " + relatorioPeriodo.size());
    }

    //PRA TESTAR TESTAR TESTAR TESTARO CÓDIGO, COPIAR ISSO AQUI DEBAIXO
    // /javac -d out src/model/*.java src/service/*.java src/app/*.java
    // java -cp out app.Main
}