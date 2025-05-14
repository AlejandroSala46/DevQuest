package com.example.devquest.ui.theme

import android.widget.LinearLayout
import android.widget.Spinner

data class Comando(var tipoComandos: TipoComandos){

    val nombreComando: String = tipoComandos.nombreComando
    var spinnerCategoria: Spinner? = null
        set(spinnerCategoria){
            field = spinnerCategoria;
        }
    var spinnerOpcion: Spinner? = null
        set(spinnerOpcion){
            field = spinnerOpcion;
        }
    var parentLayout: LinearLayout? = null
        set(parentLayout){
            field = parentLayout;
        }
    var layoutDrop : LinearLayout? = null
        set(layoutDrop){
            field = layoutDrop;
        }
    var layout: LinearLayout? = null
        set(layout){
            field = layout;
        }
    var column : Int? = 0
        set(column){
            field = column;
        }
    var row : Int? = 0
        set(row){
            field = row;
        }

    override fun toString(): String {
        return "Comando(tipoComandos=$tipoComandos, nombreComando='$nombreComando', parentLayout=${parentLayout!!.id},layoutDrop=${layoutDrop!!.id}, ,layout=${layout!!.id}, column=$column, row=$row)"
    }


}
