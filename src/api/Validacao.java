package api;

/** Validacoes de entrada reutilizadas pelos controllers. Lancam ApiException (400). */
public final class Validacao {
	private Validacao() {
	}

	public static String textoObrigatorio(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw ApiException.invalido("CAMPO_OBRIGATORIO", "O campo '" + campo + "' é obrigatório.");
		}
		return valor.trim();
	}

	public static String cpf(String valor) {
		String digitos = textoObrigatorio(valor, "cpf").replaceAll("\\D", "");
		if (!digitos.matches("\\d{11}")) {
			throw ApiException.invalido("CPF_INVALIDO", "O CPF deve ter 11 dígitos.");
		}
		return digitos;
	}

	public static String email(String valor) {
		String email = textoObrigatorio(valor, "email");
		if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw ApiException.invalido("EMAIL_INVALIDO", "E-mail em formato inválido.");
		}
		return email;
	}

	public static String telefone(String valor) {
		String digitos = textoObrigatorio(valor, "telefone").replaceAll("\\D", "");
		if (!digitos.matches("\\d{10,11}")) {
			throw ApiException.invalido("TELEFONE_INVALIDO", "O telefone deve ter 10 ou 11 dígitos (com DDD).");
		}
		return digitos;
	}

	public static int inteiro(String valor, String campo) {
		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException e) {
			throw ApiException.invalido("PARAMETRO_INVALIDO", "O parâmetro '" + campo + "' deve ser um número inteiro.");
		}
	}
}
