package api;

/**
 * Erro de negocio/validacao da API. Vira resposta JSON padronizada
 * (status HTTP + codigo curto + mensagem) no handler global do ApiServer.
 */
public class ApiException extends RuntimeException {
	private final int status;
	private final String codigo;

	public ApiException(int status, String codigo, String mensagem) {
		super(mensagem);
		this.status = status;
		this.codigo = codigo;
	}

	public int getStatus() {
		return status;
	}

	public String getCodigo() {
		return codigo;
	}

	// 400: dado ausente ou em formato invalido
	public static ApiException invalido(String codigo, String mensagem) {
		return new ApiException(400, codigo, mensagem);
	}

	// 404: recurso nao existe
	public static ApiException naoEncontrado(String codigo, String mensagem) {
		return new ApiException(404, codigo, mensagem);
	}

	// 409: conflito com o estado atual (duplicado, em uso)
	public static ApiException conflito(String codigo, String mensagem) {
		return new ApiException(409, codigo, mensagem);
	}

	// 422: dado valido, mas viola uma regra do PIX
	public static ApiException regra(String codigo, String mensagem) {
		return new ApiException(422, codigo, mensagem);
	}
}
