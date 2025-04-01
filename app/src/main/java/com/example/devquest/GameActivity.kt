package com.example.devquest

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val user = intent.getParcelableExtra<User>("USER")

        val commands = findViewById<LinearLayout >(R.id.Commands)
        val variables = findViewById<LinearLayout >(R.id.Variables)
        val script = findViewById<LinearLayout>(R.id.Script)

        enableDragAndDrop(script)

        createCommands(commands)


        var pociones: HashMap<String, Int> = HashMap<String, Int>()

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
            textSize = 18f
            setPadding(32, 16, 32, 16)
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
            setPadding(16, 16, 16, 16) // Agregar un poco de espaciado al contenedor
        }
        // Cargar la fuente medieval
        val typeface = ResourcesCompat.getFont(this, R.font.medieval)

        // Crear el primer TextView con el contenido del texto arrastrado
        val firstTextView = TextView(this).apply {
            text = draggedText
            textSize = 18f // Puedes ajustar el tamaño del texto
            setPadding(32, 16, 32, 16)
            setTextColor(resources.getColor(android.R.color.black, theme))
            gravity = android.view.Gravity.LEFT

            // Aplicar la fuente medieval al TextView
            typeface?.let {
                setTypeface(it)
            }
        }

        // Crear el segundo LinearLayout dentro del nuevo LinearLayout (puedes agregar más elementos aquí si lo necesitas)
        val innerLinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Puedes cambiar la orientación aquí si prefieres apilar horizontalmente
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0x2200FF00)
            setPadding(70, 100, 16, 100)// Color de fondo para ver el área ocupada por el inner layout
        }

        // Crear el segundo TextView (esto puede ser otro tipo de información o elemento)
        val secondTextView = TextView(this).apply {
            text = "End" + draggedText // Aquí puedes agregar un texto diferente si lo prefieres
            textSize = 16f
            setPadding(32, 16, 32, 16)
            setTextColor(resources.getColor(android.R.color.black, theme))
            gravity = android.view.Gravity.LEFT

            // Aplicar la fuente medieval al TextView
            typeface?.let {
                setTypeface(it)
            }
        }

        // Agregar los elementos al LinearLayout principal
        newLayout.addView(firstTextView)
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

}