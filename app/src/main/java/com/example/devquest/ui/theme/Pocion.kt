package com.example.devquest.ui.theme

data class Pocion(var tipo: TipoPocion) {
    val type : String = tipo.name
    val nombre: String = tipo.getNombre()
    val estante: Int = tipo.getEstante()




    override fun toString(): String {
        return "Pocion{" +
                "nombre='" + nombre + '\'' +
                ", estante=" + estante +
                '}'
    }
}