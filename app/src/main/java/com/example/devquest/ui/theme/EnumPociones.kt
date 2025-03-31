package com.example.devquest.ui.theme

enum class TipoPocion( private var type : String, private var nombre: String?, private var estante: Int) {
    VENENO("Pocion","Pocion de Veneno",1),
    SALUD("Pocion","Pocion de Salud", 2),
    VELOCIDAD("Elixir","Elixir de Velocidad",3),
    VIGOR("Elixir","Elixir de Vigor", 4);


    fun getNombre(): String {
        return nombre!!
    }

    fun getEstante(): Int {
        return estante
    }
}