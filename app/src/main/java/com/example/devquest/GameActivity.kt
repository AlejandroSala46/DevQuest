package com.example.devquest

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.devquest.ui.theme.Comando
import com.example.devquest.ui.theme.Pocion
import com.example.devquest.ui.theme.TipoComandos
import com.example.devquest.ui.theme.TipoPocion
import com.example.devquest.ui.theme.User
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    var listaPociones: ArrayList<Pocion> = ArrayList<Pocion>()
    var pociones: HashMap<String, Int> = HashMap<String, Int>()
    var scriptTrancript : Array<Array<Array<Array<String>>>>  = Array(4){
        Array(4) {
            Array(4) {
                Array(4) {
                    ""
                }
            } // Inicializamos con valores ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val user = intent.getParcelableExtra<User>("USER")

        val commands = findViewById<LinearLayout >(R.id.Commands)
        val variables = findViewById<LinearLayout >(R.id.Variables)
        val script = findViewById<LinearLayout>(R.id.Script)



        enableDragAndDrop(script)

        createCommands(commands)




        pociones.put("SALUD", 5);
        pociones.put("VENENO", 2);
        pociones.put("VIGOR", 1);
        pociones.put("VELOCIDAD", 4);

        createPotions(pociones)


    }


    private fun createPotions(pociones: HashMap<String, Int>) {

        for (pocion in pociones) {
            for (i in 1..pocion.value) {
                val pociontoCreate = Pocion(TipoPocion.valueOf(pocion.key))
                listaPociones.add(pociontoCreate)
            }
        }

        listaPociones.shuffle(Random(System.currentTimeMillis()))

    }

    private fun createCommands(commands: LinearLayout) {
        val textView = TextView(this).apply {
            text = "IF"
            textSize = 15f
            setPadding(32, 5, 32, 5)
            setTextColor(resources.getColor(android.R.color.white, theme)) // Texto blanco
            background = resources.getDrawable(R.drawable.rounded_button, theme)// Agregar espacio dentro del botón
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            setDrag(this)
        }
        commands.addView(textView)
    }

    private fun setDrag(view: TextView) {
        view.setOnLongClickListener {
            val clipData = ClipData.newPlainText("COMMAND", view.text)
            val shadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(clipData, shadowBuilder, view, 0)
            true
        }
    }

    private fun enableDragAndDrop(script: LinearLayout) {
        Log.d("DragEvent", "Drag enable on script")
        val originalColor = script.drawingCacheBackgroundColor

        script.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Comprobamos si el tipo MIME del clip es adecuado
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        return@setOnDragListener true
                    }
                    return@setOnDragListener false
                }

                // Cuando un elemento entra en la zona de drop
                DragEvent.ACTION_DRAG_ENTERED -> {
                    script.setBackgroundColor(23) // Cambiar color de fondo como señal visual
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    script.setBackgroundColor(0xFFffce9f.toInt()) // Cambiar color de fondo como señal visual
                    true
                }


                DragEvent.ACTION_DROP -> {
                    Log.d("DragEvent", "Se esta dropeando")

                    script.setBackgroundColor(0xFFffce9f.toInt())
                    val draggedView = event.localState as? TextView
                    draggedView?.let {
                        // Crear un nuevo TextView para evitar mover el original
                        val newLayout = createDropLayout(it.text.toString())

                        script.addView(newLayout) // Agregar nuevo bloque al script

                        //TODO
                        newLayout.S
                        // Hacer que el nuevo TextView también sea arrastrable si es necesario
                        setDragLinear(newLayout)
                        //setOnDragListenerForDelete(script, newLayout)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Si el layout está fuera de la zona, eliminarlo
                    if (!isInsideRelativeLayout(event, script)) {
                        script.removeView(event.localState as? LinearLayout) // Eliminar el layout
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun createDropLayout(draggedText: String): LinearLayout {
        // Crear un nuevo LinearLayout principal para contener
        val newLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Apilar verticalmente
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 5, 16, 5) // Agregar un poco de espaciado al contenedor
        }
        // Cargar la fuente medieval
        val typeface = ResourcesCompat.getFont(this, R.font.medieval)

        val conditionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL // Alineación horizontal
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 5, 0, 16)
        }
        // Crear el primer TextView con el contenido del texto arrastrado
        val firstTextView = TextView(this).apply {
            text = draggedText
            textSize = 15f // Puedes ajustar el tamaño del texto
            setPadding(32, 16, 0, 16)
            setTextColor(resources.getColor(android.R.color.black, theme))
            gravity = android.view.Gravity.LEFT

            // Aplicar la fuente medieval al TextView
            typeface?.let {
                setTypeface(it)
            }
        }

        // Crear el Spinner con opciones
        val spinner = Spinner(this).apply {
            val opciones = setListPotions()
            adapter = ArrayAdapter(this@GameActivity, android.R.layout.simple_spinner_dropdown_item, opciones)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            id = View.generateViewId()
        }

        conditionLayout.addView(firstTextView)
        conditionLayout.addView(spinner)


        // Crear el segundo LinearLayout dentro del nuevo LinearLayout (puedes agregar más elementos aquí si lo necesitas)
        val innerLinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Puedes cambiar la orientación aquí si prefieres apilar horizontalmente
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0x2200FF00)
            setPadding(25, 100, 0, 100)// Color de fondo para ver el área ocupada por el inner layout
        }

        // Crear el segundo TextView (esto puede ser otro tipo de información o elemento)
        val secondTextView = TextView(this).apply {
            text = "End" + draggedText // Aquí puedes agregar un texto diferente si lo prefieres
            textSize = 15f
            setPadding(32, 5, 0, 5)
            setTextColor(resources.getColor(android.R.color.black, theme))
            gravity = android.view.Gravity.LEFT

            // Aplicar la fuente medieval al TextView
            typeface?.let {
                setTypeface(it)
            }
        }

        // Agregar los elementos al LinearLayout principal
        newLayout.addView(conditionLayout)
        newLayout.addView(innerLinearLayout)
        newLayout.addView(secondTextView)

        enableDragAndDrop(innerLinearLayout)

        return newLayout;
    }


    // Función para verificar si el layout está dentro del RelativeLayout
    private fun isInsideRelativeLayout(event: DragEvent, script: LinearLayout): Boolean {
        val layoutRect = Rect()
        script.getGlobalVisibleRect(layoutRect)

        val eventX = event.x.toInt()
        val eventY = event.y.toInt()

        return layoutRect.contains(eventX, eventY)
    }

    // Función para hacer arrastrable el LinearLayout
    private fun setDragLinear(view: LinearLayout) {
        view.setOnLongClickListener { v ->
            // Crear ClipData con la información del LinearLayout
            val clipData = ClipData.newPlainText("LINEAR_LAYOUT", "Arrastrando LinearLayout")

            // Crear un DragShadowBuilder para proporcionar una sombra visual
            val shadowBuilder = View.DragShadowBuilder(v)

            // Iniciar el drag
            v.startDragAndDrop(clipData, shadowBuilder, v, 0)


        }
    }

    private fun setListPotions(): MutableList<String> {
        val listaPociones = mutableListOf<String>()
        for (pocion in pociones) {
            listaPociones.add(pocion.key)
        }
        return listaPociones

    }

    fun getIndex(script : ViewGroup, targetView: View, column : Int): Pair<Int, Int> {
        // Iterar sobre los hijos directos del ViewGroup

        val targetIndex = script.indexOfChild(targetView)
        if (script.getChildAt(targetIndex).id == targetView.id) {
            return Pair(column, targetIndex)
        }

        val viewGroup = script.getChildAt(targetIndex) as ViewGroup

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            // Si el hijo es el targetView, devolver el índice
            if (child == targetView) {
                return Pair(column, i)
            }

            // Si el hijo es un ViewGroup (puede tener más hijos dentro), llamar recursivamente
            if (child is ViewGroup) {
                val newColumn = column + 1
                val childIndex = getIndex(child, targetView, column)
                if (childIndex.second != -1) {
                    // Si lo encontramos dentro del ViewGroup, devolvemos el índice
                    return Pair(column, i)
                }
            }
        }

        // Si no encontramos el targetView, devolver -1
        return Pair(column, -1)
    }

}