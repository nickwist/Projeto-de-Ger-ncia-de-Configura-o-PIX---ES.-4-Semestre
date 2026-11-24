package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PixServiceTest {

	private PixService pix;

	@BeforeEach
	void setUp() {
		pix = new PixService();
	}

	@Test
	void cpfPrecisaTer11Digitos() {
		assertTrue(pix.validarChave("12345678900", "CPF"));
		assertFalse(pix.validarChave("123", "CPF"));
		assertFalse(pix.validarChave("123.456.789-00", "CPF"));
	}

	@Test
	void cnpjPrecisaTer14Digitos() {
		assertTrue(pix.validarChave("12345678000199", "CNPJ"));
		assertFalse(pix.validarChave("12345678900", "CNPJ"));
	}

	@Test
	void emailPrecisaTerArrobaEPonto() {
		assertTrue(pix.validarChave("nick@email.com", "EMAIL"));
		assertFalse(pix.validarChave("nick", "EMAIL"));
		assertFalse(pix.validarChave("nick@email", "EMAIL"));
	}

	@Test
	void telefonePrecisaTer10Ou11Digitos() {
		assertTrue(pix.validarChave("85999999999", "TELEFONE"));
		assertTrue(pix.validarChave("8533334444", "TELEFONE"));
		assertFalse(pix.validarChave("999", "TELEFONE"));
	}

	@Test
	void tipoNaoDependeDeMaiusculasOuMinusculas() {
		assertTrue(pix.validarChave("12345678900", "cpf"));
	}

	@Test
	void entradaNulaOuTipoDesconhecidoEhInvalida() {
		assertFalse(pix.validarChave(null, "CPF"));
		assertFalse(pix.validarChave("12345678900", null));
		assertFalse(pix.validarChave("12345678900", "RG"));
	}

	@Test
	void chaveAleatoriaEhUuidEDiferenteACadaChamada() {
		String a = pix.gerarChaveAleatoria();
		String b = pix.gerarChaveAleatoria();
		assertTrue(a.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
		assertNotEquals(a, b);
	}
}