package model;

public class Contato {
    private Usuario usuario;

    public Contato(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário do contatonão pode ser nulo.");
        }

        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public String toString() {
        return "Contato: " + usuario.getNome()
        + " | CPF: " + usuario.getCpf()
        + " | E-mail: " + usuario.getEmail();
    }
}