package service;

import static org.junit.jupiter.api.Assertions.*;

import model.ContaBancaria;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContaServiceTest {

	private ContaService service;
	private Usuario nick;
	private Usuario duda;

	@BeforeEach
	void setUp() {
		service = new ContaService();
		nick = new Usuario(1, "Nick", "11111111111", "nick@email.com", "85911111111");
		duda = new Usuario(2, "Duda", "22222222222", "duda@email.com", "85922222222");
	}

	@Test
	void criarContaFicaDisponivelParaBusca() {
		ContaBancaria c = service.criarConta("001-1", nick);
		assertEquals(0.0, c.getSaldo(), 0.001);
		assertTrue(service.buscarPorNumero("001-1").isPresent());
		assertTrue(service.buscarPorNumero("999-9").isEmpty());
	}

	@Test
	void atualizarContaTrocaONumero() {
		service.criarConta("001-1", nick);
		assertTrue(service.atualizarConta("001-1", "001-2", null));
		assertTrue(service.buscarPorNumero("001-2").isPresent());
		assertTrue(service.buscarPorNumero("001-1").isEmpty());
	}

	@Test
	void removerContaTiraDaLista() {
		service.criarConta("001-1", nick);
		assertTrue(service.removerConta("001-1"));
		assertFalse(service.removerConta("001-1"));
	}

	@Test
	void associarChaveValidaEncontraContaPelaChave() {
		service.criarConta("001-1", nick);
		assertTrue(service.associarChavePix("001-1", "nick@email.com", "EMAIL"));
		assertTrue(service.buscarPorChavePix("nick@email.com").isPresent());
		assertEquals("001-1", service.buscarPorChavePix("nick@email.com").get().getNumeroConta());
	}

	@Test
	void associarChaveInvalidaOuEmContaInexistenteFalha() {
		service.criarConta("001-1", nick);
		assertFalse(service.associarChavePix("001-1", "nao-eh-email", "EMAIL"));
		assertFalse(service.associarChavePix("999-9", "nick@email.com", "EMAIL"));
	}

	@Test
	void mesmaChaveNaoPodeSerCadastradaDuasVezes() {
		service.criarConta("001-1", nick);
		service.criarConta("002-2", duda);
		assertTrue(service.associarChavePix("001-1", "nick@email.com", "EMAIL"));
		assertFalse(service.associarChavePix("002-2", "nick@email.com", "EMAIL"));
	}

	@Test
	void alterarChaveTrocaOValor() {
		service.criarConta("001-1", nick);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		assertTrue(service.alterarChavePix("nick@email.com", "nick2@email.com"));
		assertTrue(service.buscarPorChavePix("nick2@email.com").isPresent());
		assertTrue(service.buscarPorChavePix("nick@email.com").isEmpty());
	}

	@Test
	void suspenderEDesbloquearChaveMudaOStatus() {
		service.criarConta("001-1", nick);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		assertTrue(service.chavePixEstaAtiva("nick@email.com"));
		assertTrue(service.suspenderChavePix("nick@email.com"));
		assertFalse(service.chavePixEstaAtiva("nick@email.com"));
		assertFalse(service.suspenderChavePix("nick@email.com"));
		assertTrue(service.desbloquearChavePix("nick@email.com"));
		assertTrue(service.chavePixEstaAtiva("nick@email.com"));
	}

	@Test
	void transferirViaPixMoveOSaldoEntreContas() {
		ContaBancaria a = service.criarConta("001-1", nick);
		ContaBancaria b = service.criarConta("002-2", duda);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		service.associarChavePix("002-2", "duda@email.com", "EMAIL");
		a.depositar(500);

		assertTrue(service.transferirViaPix("nick@email.com", "duda@email.com", 200));
		assertEquals(300.0, a.getSaldo(), 0.001);
		assertEquals(200.0, b.getSaldo(), 0.001);
	}

	@Test
	void transferirSemSaldoNaoAlteraNada() {
		ContaBancaria a = service.criarConta("001-1", nick);
		ContaBancaria b = service.criarConta("002-2", duda);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		service.associarChavePix("002-2", "duda@email.com", "EMAIL");
		a.depositar(50);

		assertFalse(service.transferirViaPix("nick@email.com", "duda@email.com", 200));
		assertEquals(50.0, a.getSaldo(), 0.001);
		assertEquals(0.0, b.getSaldo(), 0.001);
	}

	@Test
	void transferirParaChaveSuspensaFalha() {
		ContaBancaria a = service.criarConta("001-1", nick);
		service.criarConta("002-2", duda);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		service.associarChavePix("002-2", "duda@email.com", "EMAIL");
		service.suspenderChavePix("duda@email.com");
		a.depositar(500);

		assertFalse(service.transferirViaPix("nick@email.com", "duda@email.com", 100));
		assertEquals(500.0, a.getSaldo(), 0.001);
	}

	@Test
	void receberViaPixDepositaSoComChaveAtivaEValorPositivo() {
		ContaBancaria a = service.criarConta("001-1", nick);
		service.associarChavePix("001-1", "nick@email.com", "EMAIL");
		assertTrue(service.receberViaPix("nick@email.com", 100));
		assertFalse(service.receberViaPix("nick@email.com", -5));
		assertFalse(service.receberViaPix("x@email.com", 100));
		assertEquals(100.0, a.getSaldo(), 0.001);
	}
}