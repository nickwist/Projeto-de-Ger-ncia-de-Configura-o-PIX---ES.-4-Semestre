package model;
public enum StatusTransacao{
    PENDENTE, // A transação ainda está pendente
    CONCLUIDA, // A transferência foi concluída
    CANCELADA, // A transação pendente foi cancelada
    ESTORNADA // A transação concluída teve seu valor devolvido
}