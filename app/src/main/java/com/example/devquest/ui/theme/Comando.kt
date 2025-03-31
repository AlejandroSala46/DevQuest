package com.example.devquest.ui.theme

data class Comando(var tipoComandos: TipoComandos){

    val id : Int = tipoComandos.id
    val nombreComando: String = tipoComandos.nombreComando
    var pocion: Pocion? = null
        set(pocion){
            field = pocion;
        }



    override fun toString(): String {
        return "Comandos{" +
                "id='" + id + '\'' +
                ", name='" + nombreComando + '\'' +
                ", variable='" + pocion + '\'' +
                '}'
    }
}
