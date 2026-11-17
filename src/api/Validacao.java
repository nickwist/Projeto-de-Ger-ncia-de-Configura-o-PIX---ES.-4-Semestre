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

	public static String numeroConta(String valor) {
		String numero = textoObrigatorio(valor, "numeroConta");
		if (!numero.matches("^[0-9A-Za-z-]{3,20}$")) {
			throw ApiException.invalido("NUMERO_CONTA_INVALIDO",
			                            "O número da conta deve ter de 3 a 20 caracteres (letras, números e hífen).");
		}
		return numero;
	}

	public static double valorPositivo(Double valor, String campo) {
		if (valor == null) {
			throw ApiException.invalido("CAMPO_OBRIGATORIO", "O campo '" + campo + "' é obrigatório.");
		}
		if (valor.isNaN() || valor.isInfinite() || valor <= 0) {
			throw ApiException.invalido("VALOR_INVALIDO", "O campo '" + campo + "' deve ser maior que zero.");
		}
		return valor;
	}

	// padroniza uma chave PIX digitada/recebida na URL ou no corpo para busca
	public static String chaveParaBusca(String valor) {
		String t = valor.trim();
		if (t.contains("@")) {
			return t.toLowerCase();
		}
		if (t.matches("[\\d.\\-()/\\s]+")) {
			return t.replaceAll("\\D", "");
		}
		return t.toLowerCase();
	}

	public static int inteiro(String valor, String campo) {
		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException e) {
			throw ApiException.invalido("PARAMETRO_INVALIDO", "O parâmetro '" + campo + "' deve ser um número inteiro.");
		}
	}
}
