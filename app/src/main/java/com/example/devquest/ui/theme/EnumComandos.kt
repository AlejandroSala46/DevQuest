package com.example.devquest.ui.theme

enum class TipoComandos(
    val tipo: Tipo,
    val nombreComando: String = "",
) {
    IF(Tipo.IF, "IF"),
    ESTANTE1(Tipo.ESTANTE,"ESTANTE 1"),
    ESTANTE2(Tipo.ESTANTE, "ESTANTE 2"),
    ESTANTE3(Tipo.ESTANTE, "ESTANTE 3"),
    ESTANTE4(Tipo.ESTANTE, "ESTANTE 4");

    enum class Tipo {
        IF,
        ESTANTE
    }
}