package com.example.devquest.ui.theme

import com.example.devquest.R

enum class TipoPocion( private var type : String, private var nombre: String?, private var estante: Int, private var imagen: Int) {
    VENENO("Pocion","Pocion de Veneno",1, R.drawable.potion_veneno),
    SALUD("Pocion","Pocion de Salud", 2, R.drawable.potion_salud),
    VELOCIDAD("Elixir","Elixir de Velocidad",3, R.drawable.potion_velocidad),
    VIGOR("Elixir","Elixir de Fuerza", 4, R.drawable.potion_fuerza);

    fun getType(): String {
        return type!!
    }

    fun getNombre(): String {
        return nombre!!
    }

    fun getEstante(): Int {
        return estante
    }

    fun getImagen(): Int {
        return imagen
    }
}