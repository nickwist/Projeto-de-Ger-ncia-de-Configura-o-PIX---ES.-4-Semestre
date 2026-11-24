package service;

import model.RegistroLogin;
import model.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutenticacaoService {
    private UsuarioService usuarioService;
    private Usuario usuarioLogado;
    private List<RegistroLogin> historicoLogins = new ArrayList<>();

    public AutenticacaoService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Optional<Usuario> login(String cpfOuEmail, String senha) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCpfOuEmail(cpfOuEmail);

        if (usuarioOpt.isPresent() && usuarioOpt.get().verificarSenha(senha)) {
            this.usuarioLogado = usuarioOpt.get();
            historicoLogins.add(new RegistroLogin(cpfOuEmail, true));
            return usuarioOpt;
        }

        historicoLogins.add(new RegistroLogin(cpfOuEmail, false));
        return Optional.empty();
    }

    public void logout() {
        this.usuarioLogado = null;
    }

    public boolean estaAutenticado() {
        return this.usuarioLogado != null;
    }

    public Optional<Usuario> getUsuarioLogado() {
        return Optional.ofNullable(this.usuarioLogado);
    }

    public List<RegistroLogin> getHistoricoLogins() {
        return historicoLogins;
    }
}