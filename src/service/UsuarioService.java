package service;

import model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private List<Usuario> usuarios = new ArrayList<>();
    private int proximoId = 1;

    public Usuario cadastrar(String nome, String cpf, String email, String telefone) {
        Usuario novoUsuario = new Usuario(proximoId++, nome, cpf, email, telefone, "123456");
        usuarios.add(novoUsuario);
        return novoUsuario;
    }

    // novo metodo com senha
    public Usuario cadastrar(String nome, String cpf, String email, String telefone, String senha) {
        Usuario novoUsuario = new Usuario(proximoId++, nome, cpf, email, telefone, senha);
        usuarios.add(novoUsuario);
        return novoUsuario;
    }

    public Optional<Usuario> buscarPorId(int id) {
        return usuarios.stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    public Optional<Usuario> buscarPorCpfOuEmail(String login) {
        return usuarios.stream()
                .filter(u -> u.getCpf().equals(login) || u.getEmail().equalsIgnoreCase(login))
                .findFirst();
    }

    public boolean atualizar(int id, String nome, String email, String telefone) {
        Optional<Usuario> usuarioOpt = buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            u.setNome(nome);
            u.setEmail(email);
            u.setTelefone(telefone);
            return true;
        }
        return false;
    }

    public boolean remover(int id) {
        return usuarios.removeIf(u -> u.getId() == id);
    }

    public List<Usuario> listarTodos() {
        return usuarios;
    }
}
