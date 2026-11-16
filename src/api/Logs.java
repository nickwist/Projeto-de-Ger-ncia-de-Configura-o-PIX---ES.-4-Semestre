package api;

import io.javalin.http.Context;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Observabilidade: logs estruturados em JSON (uma linha por evento),
 * correlation id e mascaramento de dados pessoais (LGPD).
 */
public final class Logs {
	private Logs() {
	}

	public static String cid(Context ctx) {
		String c = ctx.attribute("cid");
		return c == null ? "-" : c;
	}

	public static void requisicao(String cid, String metodo, String rota, int status, long duracaoMs) {
		Map<String, Object> m = base("INFO", "requisicao", cid);
		m.put("metodo", metodo);
		m.put("rota", rota);
		m.put("status", status);
		m.put("duracaoMs", duracaoMs);
		System.out.println(json(m));
	}

	public static void evento(String cid, String evento, Map<String, Object> campos) {
		Map<String, Object> m = base("INFO", evento, cid);
		m.putAll(campos);
		System.out.println(json(m));
	}

	public static void erro(String cid, String evento, Throwable t) {
		Map<String, Object> m = base("ERROR", evento, cid);
		m.put("excecao", t.getClass().getSimpleName());
		m.put("detalhe", String.valueOf(t.getMessage()));
		System.out.println(json(m));
	}

	private static Map<String, Object> base(String nivel, String evento, String cid) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("ts", Instant.now().toString());
		m.put("nivel", nivel);
		m.put("evento", evento);
		m.put("correlationId", cid);
		return m;
	}

	// ---- mascaramento (LGPD): nunca logar CPF, e-mail ou telefone completos ----

	public static String mascararCpf(String cpf) {
		if (cpf == null || cpf.length() != 11) {
			return "***";
		}
		return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
	}

	public static String mascararEmail(String email) {
		if (email == null || !email.contains("@")) {
			return "***";
		}
		int arroba = email.indexOf('@');
		return email.charAt(0) + "***" + email.substring(arroba);
	}

	public static String mascararTelefone(String tel) {
		if (tel == null || tel.length() < 4) {
			return "***";
		}
		return "(**) *****-" + tel.substring(tel.length() - 4);
	}

	// ---- JSON minimo, sem depender de biblioteca ----

	private static String json(Map<String, Object> m) {
		StringBuilder sb = new StringBuilder("{");
		boolean primeiro = true;
		for (Map.Entry<String, Object> e : m.entrySet()) {
			if (!primeiro) {
				sb.append(',');
			}
			primeiro = false;
			sb.append('"').append(escapar(e.getKey())).append("\":");
			Object v = e.getValue();
			if (v instanceof Number || v instanceof Boolean) {
				sb.append(v);
			} else {
				sb.append('"').append(escapar(String.valueOf(v))).append('"');
			}
		}
		return sb.append('}').toString();
	}

	private static String escapar(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"': sb.append("\\\""); break;
				case '\\': sb.append("\\\\"); break;
				case '\n': sb.append("\\n"); break;
				case '\r': sb.append("\\r"); break;
				case '\t': sb.append("\\t"); break;
				default:
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}
}
