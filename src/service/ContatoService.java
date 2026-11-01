package service;

import model.Contato;
import model.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContatoService {

    private Map<Integer, List<Contato>> contatosPorUsuario = new HashMap<>();
    
    public boolean adicionarContato(Usuario dono, Usuario usuarioContato) {

        if (dono == null || usuarioContato == null) {
            return false; // Não é possível adicionar um contato nulo
        }

        if (dono.getId() == usuarioContato.getId()) {
            return false; // Não é possível adicionar a si mesmo como contato
        }

        List<Contato> contatos = contatosPorUsuario.computeIfAbsent(
            dono.getId(), id -> new ArrayList<>()
        );
        
        for (Contato contato : contatos) {
            if (contato.getUsuario().getId() == usuarioContato.getId()) {
                return false; // Contato já existe
            }
        }

        contatos.add(new Contato(usuarioContato));
        return true; // Contato adicionado com sucesso
    }

    public List<Contato> listarContatos(Usuario dono) {

        if (dono == null) {
            return new ArrayList<>(); // Retorna lista vazia se o dono for nulo
        }

        return contatosPorUsuario.getOrDefault(
            dono.getId(), new ArrayList<>()
        );

    }

    public boolean removerContato(Usuario dono, int idContato) {

        if (dono == null) {
            return false; // Não é possível remover contato de um dono nulo
        }

        List<Contato> contatos = contatosPorUsuario.get(dono.getId());
        
        if (contatos == null) {
            return false; // Nenhum contato encontrado para o dono
        }

        return contatos.removeIf(
            contato -> contato.getUsuario().getId() == idContato
        );
    }
}
