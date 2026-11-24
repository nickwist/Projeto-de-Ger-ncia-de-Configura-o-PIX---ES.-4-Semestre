package model;

public class Usuario {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String senhaHash;

    public Usuario(int id, String nome, String cpf, String email, String telefone) {
        this(id, nome, cpf, email, telefone, "123456"); // senha padrão caso não informada
    }

    public Usuario(int id, String nome, String cpf,
    String email, String telefone, String senha) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        setSenha(senha);
    }

    public void setSenha(String senha) {
        this.senhaHash = gerarHash(senha);
    }

    public boolean verificarSenha(String senhaDigitada) {
        if (senhaDigitada == null || this.senhaHash == null) return false;
        return this.senhaHash.equals(gerarHash(senhaDigitada));
    }

    private String gerarHash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha", e);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public String toString() {
        return "Usuario{" + "id='" + id +
        "', nome ='" + nome + ", cpf ='" + cpf + "'}";
    }
}
