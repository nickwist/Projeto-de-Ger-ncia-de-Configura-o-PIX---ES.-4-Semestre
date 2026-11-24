package service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import model.ContaBancaria;
import model.StatusTransacao;
import model.Transacao;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransacaoServiceTest {

	private TransacaoService service;
	private ContaBancaria origem;
	private ContaBancaria destino;

	@BeforeEach
	void setUp() {
		service = new TransacaoService();
		origem = new ContaBancaria("001-1", new Usuario(1, "Nick", "11111111111", "nick@email.com", "85911111111"));
		destino = new ContaBancaria("002-2", new Usuario(2, "Duda", "22222222222", "duda@email.com", "85922222222"));
		origem.depositar(3000);
	}

	@Test
	void transferenciaMoveSaldoERegistraNoHistorico() {
		Transacao t = service.execuTransacao(origem, destino, 100);
		assertEquals(StatusTransacao.CONCLUIDA, t.getStatus());
		assertEquals(2900.0, origem.getSaldo(), 0.001);
		assertEquals(100.0, destino.getSaldo(), 0.001);
		assertEquals(1, service.getHistoricoTransacoes().size());
	}

	@Test
	void idsDasTransacoesSaoSequenciais() {
		Transacao a = service.execuTransacao(origem, destino, 10);
		Transacao b = service.execuTransacao(origem, destino, 10);
		assertEquals(1, a.getId());
		assertEquals(2, b.getId());
	}

	@Test
	void valorZeroOuNegativoEhRejeitado() {
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(origem, destino, 0));
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(origem, destino, -10));
	}

	@Test
	void limiteDe2000PorPixEhRespeitado() {
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(origem, destino, 2000.01));
		Transacao t = service.execuTransacao(origem, destino, 2000);
		assertEquals(StatusTransacao.CONCLUIDA, t.getStatus());
	}

	@Test
	void saldoInsuficienteEhRejeitadoESemEfeito() {
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(destino, origem, 10));
		assertEquals(3000.0, origem.getSaldo(), 0.001);
		assertTrue(service.getHistoricoTransacoes().isEmpty());
	}

	@Test
	void contaNaoPodeTransferirParaSiMesma() {
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(origem, origem, 10));
	}

	@Test
	void contasNulasSaoRejeitadas() {
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(null, destino, 10));
		assertThrows(IllegalArgumentException.class, () -> service.execuTransacao(origem, null, 10));
	}

	@Test
	void contaBloqueadaNaoMovimenta() {
		destino.bloquearConta();
		assertThrows(IllegalStateException.class, () -> service.execuTransacao(origem, destino, 10));
		assertEquals(3000.0, origem.getSaldo(), 0.001);
	}

	@Test
	void consultarSaldoRetornaOSaldoDaConta() {
		assertEquals(3000.0, service.consultarSaldo(origem), 0.001);
		assertThrows(IllegalArgumentException.class, () -> service.consultarSaldo(null));
	}

	@Test
	void estornoDevolveOValorEMarcaComoEstornada() {
		Transacao t = service.execuTransacao(origem, destino, 100);
		assertTrue(service.estornarTransacao(t.getId()));
		assertEquals(StatusTransacao.ESTORNADA, t.getStatus());
		assertEquals(3000.0, origem.getSaldo(), 0.001);
		assertEquals(0.0, destino.getSaldo(), 0.001);
	}

	@Test
	void naoEstornaDuasVezesNemTransacaoInexistente() {
		Transacao t = service.execuTransacao(origem, destino, 100);
		assertTrue(service.estornarTransacao(t.getId()));
		assertFalse(service.estornarTransacao(t.getId()));
		assertFalse(service.estornarTransacao(99));
	}

	@Test
	void naoEstornaSeODestinoJaGastouOValor() {
		Transacao t = service.execuTransacao(origem, destino, 100);
		destino.sacar(100);
		assertFalse(service.estornarTransacao(t.getId()));
		assertEquals(StatusTransacao.CONCLUIDA, t.getStatus());
	}

	@Test
	void transacaoConcluidaNaoPodeSerCancelada() {
		Transacao t = service.execuTransacao(origem, destino, 100);
		assertFalse(service.cancelarTransacao(t.getId()));
	}

	@Test
	void filtrarPorDataSoTraTransacoesDaquelaData() {
		service.execuTransacao(origem, destino, 10);
		assertEquals(1, service.filtrarPorData(LocalDate.now()).size());
		assertEquals(0, service.filtrarPorData(LocalDate.now().minusDays(1)).size());
		assertThrows(IllegalArgumentException.class, () -> service.filtrarPorData(null));
	}

	@Test
	void relatorioPorPeriodoValidaAsDatas() {
		service.execuTransacao(origem, destino, 10);
		LocalDate hoje = LocalDate.now();
		assertEquals(1, service.gerarRelatorioPorPeriodo(hoje, hoje).size());
		assertThrows(IllegalArgumentException.class, () -> service.gerarRelatorioPorPeriodo(hoje.plusDays(1), hoje));
		assertThrows(IllegalArgumentException.class, () -> service.gerarRelatorioPorPeriodo(null, hoje));
	}

	@Test
	void relatorioPorUsuarioIncluiQuemEnviouEQuemRecebeu() {
		service.execuTransacao(origem, destino, 10);
		assertEquals(1, service.gerarRelatorioPorUsuario(1).size());
		assertEquals(1, service.gerarRelatorioPorUsuario(2).size());
		assertEquals(0, service.gerarRelatorioPorUsuario(99).size());
	}
}