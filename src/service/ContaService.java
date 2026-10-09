package service; //Aqui mantemos tudo relacionado a conta (criar, buscar, atualizar, receber, transferir)

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.ContaBancaria;
import model.Usuario;
import model.ChavePix;

public class ContaService {
	private List<ContaBancaria> contas = new ArrayList<>();
	private PixService pixService = new PixService();
	//private List<String> chavesPix = new ArrayList<>(); //aqui só guardamos textos soltos aqui

	public ContaBancaria criarConta(String numeroConta, Usuario dono) {
		ContaBancaria novaConta = new ContaBancaria(numeroConta, dono);
		contas.add(novaConta);
		return novaConta;
	}

	public Optional<ContaBancaria> buscarPorNumero(String numero) {
		return contas.stream()
		       .filter(c -> c.getNumeroConta().equals(numero))
		       .findFirst();
	}

	public Optional<ContaBancaria> buscarPorChavePix(String valorChave) {
		for (ContaBancaria conta : contas) {
			for (ChavePix chave : conta.getChavesPix()) {
				if (chave.getValor().equals(valorChave)) {
					return Optional.of(conta);
				}
			}
		}
		return Optional.empty();
	}
	public boolean atualizarConta(String numeroAntigo, String novoNumero, Usuario novoDono) {
		Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroAntigo);
		if (contaOpt.isPresent()) {
			ContaBancaria conta = contaOpt.get();

			if (novoNumero != null && !novoNumero.isEmpty()) {
				conta.setNumeroConta(novoNumero);
			}

			if (novoDono != null) {
				conta.setDono(novoDono);
			}

			return true;

		}
		return false;
	}

	public boolean removerConta(String numero) {
		return contas.removeIf(c -> c.getNumeroConta().equals(numero));
	}

	public List<ContaBancaria> listarContas() {
		return contas;
	}

	public boolean associarChavePix(String numeroConta, String chave, String tipo) { //é pra associar uma conta específica, com validação
		//Verificando se a chave não tá associada já
		if(!pixService.validarChave(chave, tipo)) {
			return false; //estamos perguntando se a chave é valida
		}
		if(buscarPorChavePix(chave).isPresent()) {
			return false;
		}
		//Procurando a conta que vai receber a chave
		Optional<ContaBancaria> contaOpt = buscarPorNumero(numeroConta);

		//se a conta existe, associamos a chave
		if(contaOpt.isPresent()) {
			ChavePix novaChave = new ChavePix(chave, tipo.toUpperCase());
			contaOpt.get().adicionarChavePix(novaChave);
			return true;
		}

		//se a conta não existe
		return false;
	}

	public boolean alterarChavePix(String chaveAntiga, String novaChave) {

		//procura a conta associada à chave antiga
		Optional<ContaBancaria> contaOpt = buscarPorChavePix(chaveAntiga);

		if (!contaOpt.isPresent()) {
			return false;
		}

		//procura o objeto da chave antiga
		ContaBancaria conta = contaOpt.get();
		ChavePix chaveEncontrada = null;

		for (ChavePix chave : conta.getChavesPix()) {
			if (chave.getValor().equals(chaveAntiga)) {
				chaveEncontrada = chave;
				break;
			}
		}

		if (chaveEncontrada == null) {
			return false;
		}

		//verifica se a nova chave é válida
		if (!pixService.validarChave(novaChave, chaveEncontrada.getTipo())) {
			return false;
		}

		//verifica se a nova chave já está cadastrada
		if (buscarPorChavePix(novaChave).isPresent()) {
			return false;
		}

		//altera o valor da chave
		chaveEncontrada.alterarValor(novaChave);

		return true;
	}

	public boolean chavePixEstaAtiva(String valorChave) {
		for (ContaBancaria conta : contas) {
			for (ChavePix chave : conta.getChavesPix()) {
				if (chave.getValor().equals(valorChave)) {
					return !chave.isSuspensa();
				}
			}
		}

		return false;
	}

	public boolean receberViaPix(String chave, double valor) {
		Optional<ContaBancaria> contaOpt = buscarPorChavePix(chave);

		//verifica se a conta existe e se a chave está ativa
		if (!contaOpt.isPresent() || !chavePixEstaAtiva(chave)) {
			return false;
		}

		//se a chave estiver ativa, deposita o valor
		if (valor > 0) {
			contaOpt.get().depositar(valor);
			return true;
		}
		return false;
	}

	public boolean transferirViaPix(String chaveOrigem, String chaveDestino, double valor) {
		Optional<ContaBancaria> contaOrigemOpt = buscarPorChavePix(chaveOrigem);
		Optional<ContaBancaria> contaDestinoOpt = buscarPorChavePix(chaveDestino);

		// Verifica se as contas existem e se as chaves estão ativas
		if (!contaOrigemOpt.isPresent()
		        || !contaDestinoOpt.isPresent()
		        || !chavePixEstaAtiva(chaveOrigem)
		        || !chavePixEstaAtiva(chaveDestino)) {
			return false;
		}

		if (valor <= 0) {
			return false;
		}

		ContaBancaria contaOrigem = contaOrigemOpt.get();
		ContaBancaria contaDestino = contaDestinoOpt.get();

		// Retira o valor da conta de origem
		if (contaOrigem.sacar(valor)) {
			// Deposita o valor na conta de destino
			contaDestino.depositar(valor);
			return true;
		}

		return false;
	}

	public boolean suspenderChavePix(String valorChave) {
		for (ContaBancaria conta : contas) {
			for (ChavePix chave : conta.getChavesPix()) {
				if (chave.getValor().equals(valorChave)) {
					if (chave.isSuspensa()) {
						return false;
					}

					chave.suspender();
					return true;
				}
			}
		}

		return false;
	}

	public boolean desbloquearChavePix(String valorChave) {
		for (ContaBancaria conta : contas) {
			for (ChavePix chave : conta.getChavesPix()) {
				if (chave.getValor().equals(valorChave)) {
					if (!chave.isSuspensa()) {
						return false;
					}

					chave.desbloquear();
					return true;
				}
			}
		}

		return false;
	}
}