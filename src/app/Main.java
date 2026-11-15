package app; //precisamos de um pacote para agrupar as classes

import model.ChavePix;
import model.ContaBancaria;
import model.Contato;
import model.Usuario;
import model.Transacao;
import service.ContaService;
import service.UsuarioService;
import service.TransacaoService;
import service.Notificacao;
import service.ContatoService;

import java.time.LocalDate;
import java.util.List;

public class Main { //Só pra testar
	public static void main(String[] args) {
		UsuarioService usuarioService = new UsuarioService();
		ContaService contaService = new ContaService();
		TransacaoService transacaoService = new TransacaoService();
		Notificacao notificacao = new Notificacao();
		ContatoService contatoService = new ContatoService();

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

		// Desbloqueia a conta da Maria antes de testar o limite
		contaMaria.desbloquearConta();

		System.out.println("Conta da Maria desbloqueada? " + !contaMaria.isBloqueada());

		// Adiciona saldo para isolar o teste do limite
		contaNick.depositar(3000.0);

		System.out.println("Saldo do Nick para o teste: " + contaNick.getSaldo());

		System.out.println("\n=== TESTE DE LIMITE PIX ===");

		try {
			transacaoService.execuTransacao(contaNick, contaMaria, 2500.0);

			System.out.println("ERRO: a transferência acima do limite foi permitida!");

		} catch (IllegalArgumentException e) {
			System.out.println("Transferência rejeitada como esperado: " + e.getMessage());
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

		// ===== TESTES DE CONTATOS =====
		System.out.println("\n===== TESTE DE CONTATOS =====");

		// Nick adiciona Maria
		boolean contatoAdicionado = contatoService.adicionarContato(nick, maria);
		System.out.println("Maria adicionada aos contatos de Nick? " + contatoAdicionado);

		// Tentar adicionar Maria novamente
		boolean contatoDuplicado = contatoService.adicionarContato(nick, maria);
		System.out.println("Tentar adicionar Maria novamente: " + contatoDuplicado);

		// Tentar adicionar o próprio Nick
		boolean contatoProprio = contatoService.adicionarContato(nick, nick);
		System.out.println("Tentar adicionar a si mesmo: " + contatoProprio);

		// Testar listagem de contatos
		System.out.println("\n===== LISTAGEM DE CONTATOS =====");

		List<Contato> contatosNick = contatoService.listarContatos(nick);

		for (Contato contato : contatosNick) {
			System.out.println(contato);
		}

		// Testar remoção de contato
		System.out.println("\n===== REMOÇÃO DE CONTATOS =====");

		boolean contatoRemovido = contatoService.removerContato(nick, maria.getId());

		System.out.println("Maria removida dos contatos de Nick? " + contatoRemovido);

		// Listar novamente para verificar se foi removida
		System.out.println("Contatos de Nick após a remoção:");

		for (Contato contato : contatoService.listarContatos(nick)) {
			System.out.println(contato);
		}

		// TESTE DE CHAVES PIX

		System.out.println("\n=== TESTE DE CHAVES PIX ===");

		// Verificar se a chave do Nick foi associada
		System.out.println("Chaves cadastradas do Nick:");

		for (ChavePix chave : contaNick.getChavesPix()) {
			System.out.println("Valor: " + chave.getValor());
			System.out.println("Tipo: " + chave.getTipo());
			System.out.println("Suspensa: " + chave.isSuspensa());
		}

		// Verificar se a conta pode ser encontrada pela chave
		boolean encontrou = contaService
		                    .buscarPorChavePix("nick@email.com")
		                    .isPresent();

		System.out.println("Conta encontrada pela chave? " + encontrou);

		// TESTES DA BRANCH/FEAT DE DESBLOQUEIO DE CHAVE PIX SUSPENSA E ALTERAÇÃO DA CHAVE
		// TESTE DE SUSPENSÃO E DESBLOQUEIO DE CHAVE PIX
		System.out.println("\n=== TESTE DE SUSPENSÃO E DESBLOQUEIO ===");

		String chaveTeste = "nick@email.com";

		// 1. Suspender a chave
		boolean suspendeu = contaService.suspenderChavePix(chaveTeste);
		System.out.println("Chave suspensa? " + suspendeu);

		// 2. Consultar o estado da chave
		for (ChavePix chave : contaNick.getChavesPix()) {
			if (chave.getValor().equals(chaveTeste)) {
				System.out.println("Estado após suspensão: " + chave.isSuspensa());
			}
		}

		// 3. Desbloquear a chave
		boolean desbloqueou = contaService.desbloquearChavePix(chaveTeste);
		System.out.println("Chave desbloqueada? " + desbloqueou);

		// 4. Consultar o estado novamente
		for (ChavePix chave : contaNick.getChavesPix()) {
			if (chave.getValor().equals(chaveTeste)) {
				System.out.println("Estado após desbloqueio: " + chave.isSuspensa());
			}
		}

		// TESTE: BLOQUEIO DE OPERACOES COM CHAVE PIX SUSPENSA
		System.out.println("\n=== TESTE DE OPERACOES COM CHAVE SUSPENSA ===");

		// 1. Suspender novamente a chave do Nick
		boolean suspendeuNovamente = contaService.suspenderChavePix("nick@email.com");
		System.out.println("Nick teve a chave suspensa? " + suspendeuNovamente);

		// 2. Tentar receber PIX usando a chave suspensa
		boolean recebeuComChaveSuspensa =
		    contaService.receberViaPix("nick@email.com", 100.0);

		System.out.println("Recebeu com chave suspensa? " + recebeuComChaveSuspensa);

		// 3. Tentar transferir usando a chave suspensa do Nick
		boolean transferiuComChaveSuspensa =
		    contaService.transferirViaPix(
		        "nick@email.com",
		        "maria@email.com",
		        50.0
		    );

		System.out.println("Transferiu com chave suspensa? " + transferiuComChaveSuspensa);

		// 4. Desbloquear a chave do Nick para não deixá-la suspensa no final do teste
		boolean desbloqueouNovamente = contaService.desbloquearChavePix("nick@email.com");
		System.out.println("Nick teve a chave desbloqueada? " + desbloqueouNovamente);

		// TESTE DE ALTERACAO DE CHAVE PIX
		System.out.println("\n=== TESTE DE ALTERACAO DE CHAVE PIX ===");

		// 1. Alterar a chave do Nick
		boolean alterou = contaService.alterarChavePix(
		                      "nick@email.com",
		                      "nick.novo@email.com"
		                  );

		System.out.println("Chave do Nick alterada? " + alterou);

		// 2. Verificar se a chave antiga ainda existe
		boolean chaveAntigaExiste = contaService
		                            .buscarPorChavePix("nick@email.com")
		                            .isPresent();

		System.out.println("Chave antiga ainda existe? " + chaveAntigaExiste);

		// 3. Verificar se a nova chave está associada a uma conta
		boolean chaveNovaExiste = contaService
		                          .buscarPorChavePix("nick.novo@email.com")
		                          .isPresent();

		System.out.println("Nova chave existe? " + chaveNovaExiste);

		// 4. Tentar usar uma chave que já pertence à Maria
		boolean alterouParaChaveDuplicada = contaService.alterarChavePix(
		                                        "nick.novo@email.com",
		                                        "maria@email.com"
		                                    );

		System.out.println("Alterou para chave duplicada? " + alterouParaChaveDuplicada);

		// 5. Tentar alterar para um e-mail inválido
		boolean alterouParaChaveInvalida = contaService.alterarChavePix(
		                                       "nick.novo@email.com",
		                                       "email-invalido"
		                                   );

		System.out.println("Alterou para chave invalida? " + alterouParaChaveInvalida);

        // Teste de estorno de transação PIX
        System.out.println("\n=== TESTE DE ESTORNO PIX ===");

        // PREPARAÇÃO DO TESTE: adiciona saldo fictício para permitir o estorno.
        // Em produção, o saldo deve ser alterado por uma operação financeira válida.
        contaMaria.setSaldo(contaMaria.getSaldo() + 100.0);

        double saldoOrigemAntes = contaNick.getSaldo();
        double saldoDestinoAntes = contaMaria.getSaldo();

        boolean estornoRealizado = transacaoService.estornarTransacao(t1.getId());

		if (estornoRealizado) {
    		notificacao.notificarEstorno(t1);
		}

        System.out.println("Estorno realizado: " + estornoRealizado);
        System.out.println("Saldo de Nick antes: R$ " + saldoOrigemAntes);
        System.out.println("Saldo de Nick depois: R$ " + contaNick.getSaldo());
        System.out.println("Saldo de Maria antes: R$ " + saldoDestinoAntes);
        System.out.println("Saldo de Maria depois: R$ " + contaMaria.getSaldo());
        System.out.println("Status da transação: " + t1.getStatus());

        // Testa se é possível estornar a mesma transação novamente
        boolean segundoEstorno = transacaoService.estornarTransacao(t1.getId());

        System.out.println("Segundo estorno realizado: " + segundoEstorno);

		// ===== TESTE DO HISTÓRICO DE NOTIFICAÇÕES =====
		System.out.println("\n===== HISTÓRICO DE NICK =====");
		for (String mensagem : notificacao.listarNotificacoes(nick)) {
			System.out.println(mensagem);
		}

		System.out.println("\n===== HISTÓRICO DE MARIA =====");
		for (String mensagem : notificacao.listarNotificacoes(maria)) {
			System.out.println(mensagem);
		}
	}

	//PRA TESTAR TESTAR TESTAR TESTARO CÓDIGO, COPIAR ISSO AQUI DEBAIXO
	// /javac -d out src/model/*.java src/service/*.java src/app/*.java
	// java -cp out app.Main

}