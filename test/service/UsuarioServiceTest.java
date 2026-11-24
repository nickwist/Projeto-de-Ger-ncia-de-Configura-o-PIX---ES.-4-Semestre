package service;

import static org.junit.jupiter.api.Assertions.*;

import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsuarioServiceTest {

	private UsuarioService service;

	@BeforeEach
	void setUp() {
		service = new UsuarioService();
	}

	@Test
	void cadastrarGeraIdsSequenciais() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111");
		Usuario b = service.cadastrar("Duda", "22222222222", "duda@email.com", "85922222222");
		assertEquals(1, a.getId());
		assertEquals(2, b.getId());
		assertEquals(2, service.listarTodos().size());
	}

	@Test
	void buscarPorIdEncontraUsuarioExistente() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111");
		assertTrue(service.buscarPorId(a.getId()).isPresent());
		assertEquals("Nick", service.buscarPorId(a.getId()).get().getNome());
	}

	@Test
	void buscarPorIdInexistenteRetornaVazio() {
		assertTrue(service.buscarPorId(99).isEmpty());
	}

	@Test
	void atualizarAlteraDadosEMantemCpf() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111");
		assertTrue(service.atualizar(a.getId(), "Nick Lima", "novo@email.com", "85900000000"));
		Usuario atualizado = service.buscarPorId(a.getId()).get();
		assertEquals("Nick Lima", atualizado.getNome());
		assertEquals("novo@email.com", atualizado.getEmail());
		assertEquals("85900000000", atualizado.getTelefone());
		assertEquals("11111111111", atualizado.getCpf());
	}

	@Test
	void atualizarUsuarioInexistenteRetornaFalse() {
		assertFalse(service.atualizar(99, "X", "x@email.com", "85900000000"));
	}

	@Test
	void removerTiraUsuarioDaLista() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111");
		assertTrue(service.remover(a.getId()));
		assertTrue(service.listarTodos().isEmpty());
		assertFalse(service.remover(a.getId()));
	}

	@Test
	void cadastrarSemSenhaUsaSenhaPadrao() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111");
		assertTrue(a.verificarSenha("123456"));
	}

	@Test
	void verificarSenhaAceitaCorretaERejeitaErrada() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111", "segredo");
		assertTrue(a.verificarSenha("segredo"));
		assertFalse(a.verificarSenha("outra"));
		assertFalse(a.verificarSenha(null));
	}

	@Test
	void alterarSenhaInvalidaASenhaAntiga() {
		Usuario a = service.cadastrar("Nick", "11111111111", "nick@email.com", "85911111111", "antiga");
		a.setSenha("nova");
		assertFalse(a.verificarSenha("antiga"));
		assertTrue(a.verificarSenha("nova"));
	}

	@Test
	void buscarPorCpfOuEmailAceitaCpfOuEmailIgnorandoMaiusculas() {
		service.cadastrar("Nick", "11111111111", "Nick@Email.com", "85911111111");
		assertTrue(service.buscarPorCpfOuEmail("11111111111").isPresent());
		assertTrue(service.buscarPorCpfOuEmail("nick@email.com").isPresent());
		assertTrue(service.buscarPorCpfOuEmail("naoexiste@email.com").isEmpty());
	}
}