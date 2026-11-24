package model;

public class Localizacao {

    private int transacaoId;
    private double latitude;
    private double longitude;

    public Localizacao(int transacaoId, double latitude, double longitude) {
        this.transacaoId = transacaoId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getTransacaoId() {
        return transacaoId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}