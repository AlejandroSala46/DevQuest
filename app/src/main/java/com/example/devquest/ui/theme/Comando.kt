package com.example.devquest.ui.theme

import android.widget.LinearLayout
import android.widget.Spinner

data class Comando(var tipoComandos: TipoComandos){

    val nombreComando: String = tipoComandos.nombreComando
    var spinner: Spinner? = null
        set(spinner){
            field = spinner;
        }
    var parentLayout: LinearLayout? = null
        set(parentLayout){
            field = parentLayout;
        }
    var layout : LinearLayout? = null
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
        return "Comando(tipoComandos=$tipoComandos, nombreComando='$nombreComando', parentLayout=${parentLayout!!.id}, layout=${layout!!.id}, column=$column, row=$row)"
    }


}
