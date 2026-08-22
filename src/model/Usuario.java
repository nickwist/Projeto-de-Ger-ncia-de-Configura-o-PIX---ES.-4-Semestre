package model;

public class Usuario {
    private int id;
    private String nome;
    private int cpf;
    private String email;
    private int telefone;

    public Usuario(int id, String nome, int cpf,
    String email, int telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf
        this.email = email;
        this.telefone = telefone;
    }

    public String getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(int cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(int telefone) { this.telefone = telefone; }

    @Override
    public String toString() {
        return "Usuario{" + "id='" + id +
        "', nome ='" + nome + ", cpf ='" + cpf + "'}";
    }
}