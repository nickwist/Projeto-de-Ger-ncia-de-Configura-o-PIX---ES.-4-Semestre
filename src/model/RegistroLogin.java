package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroLogin {
    private String identificadorTentado;
    private boolean sucesso;
    private LocalDateTime dataHora;

    public RegistroLogin(String identificadorTentado, boolean sucesso) {
        this.identificadorTentado = identificadorTentado;
        this.sucesso = sucesso;
        this.dataHora = LocalDateTime.now();
    }

    @Override
    public String toString() {
        String dataFormatada = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String status = sucesso ? "SUCESSO" : "FALHA";
        return "[" + dataFormatada + "] Login (" + identificadorTentado + ") -> " + status;
    }
}