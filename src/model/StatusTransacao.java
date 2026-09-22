package model;
public enum StatusTransacao{
    PENDENTE, // Transação registrada, mas n foi concluida
    CONCLUIDA, // Valor transferido 
    CANCELADA // Transação cancelada antes da conclusao
}