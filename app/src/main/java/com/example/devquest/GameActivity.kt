package com.example.devquest

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Switch
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
    var scriptTranscript: ArrayList<Comando> = ArrayList<Comando>()
    var listComands: ArrayList<Comando> = ArrayList<Comando>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val user = intent.getParcelableExtra<User>("USER")

        val commands = findViewById<LinearLayout>(R.id.Commands)
        val variables = findViewById<LinearLayout>(R.id.Variables)
        val script = findViewById<LinearLayout>(R.id.Script)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnExit = findViewById<Button>(R.id.btnExit)

        script.id = View.generateViewId()

        listComands.add(Comando(TipoComandos.IF))
        listComands.add(Comando(TipoComandos.ESTANTE))

        enableDragAndDrop(script)

        createCommands(commands, variables)

        btnPlay.setOnClickListener { v: View? ->
            startGame()
        }


        //pociones.put("SALUD", 5)
        pociones.put("VENENO", 2)
        /*pociones.put("VIGOR", 1)
        pociones.put("VELOCIDAD", 4)*/

        createPotions(pociones)


    }

    private fun startGame() {

        scriptTranscript.sortWith(compareBy({ it.column }, { it.row }))

        for (comando in scriptTranscript) {
            Log.d("GameActivityCommand", comando.toString())
        }

        for (pocion in listaPociones) {
            Log.d("Pocion", pocion.toString())

            var pocionSituada : Boolean = false


            val numEstante : Int? = playCommands(0, 0, 1, pocion)

            if (numEstante != null) {
                pocionSituada = true
            } else {
                Log.d("GameActivity", "No se pudo ubicar la pocion, num estante es null")
                break
            }

            if (pocionSituada){

                if(numEstante == pocion.estante){
                    Log.d("GameActivity", "Se pudo ubicar la pocion")
                }
                else{
                    Log.d("GameActivity", "No se pudo ubicar la pocion, $numEstante es distinto a ${pocion.estante}")
                    break
                }

            }

        }
    }

    private fun playCommands(column: Int, row: Int, parent: Int, pocion: Pocion): Int? {

        val command: Comando?

        try {
            // Buscar el comando que coincide con las coordenadas
            command = scriptTranscript.find { it.column == column && it.row == row && parent == it.parentLayout!!.id }
            command!!.layout!!.setBackgroundColor(0xFFFF9999.toInt())

        } catch (e: Exception) {
            // Si no se encuentra el comando, devolver null
            return null
        }

        Log.d("GameActivity", "Comando: $command")

        // Procesar según el tipo de comando
        return when (command.tipoComandos) {

            // Si es un comando IF
            TipoComandos.IF -> {
                val varControl = command.spinner!!.selectedItem.toString()

                Log.d("GameActivity", "IF($varControl = ${pocion.type})")

                // Realizamos la comparación y luego retornamos el valor adecuado
                if (ifCommand(pocion, varControl)) {
                    // Llamada recursiva si el comando IF es verdadero
                    val nextCommand = playCommands(column.plus(1), 0, command.layout!!.id.toInt(), pocion)

                    Handler(Looper.getMainLooper()).postDelayed({
                        command!!.layout!!.setBackgroundColor(0xFFFFCE9F.toInt())
                    }, 1000)

                    if (nextCommand == null) {
                        // Si no encontramos un comando después de la ejecución de IF, retrocedemos una columna y avanzamos una fila
                        return playCommands(column, row + 1, parent, pocion)

                    } else {
                        return nextCommand
                    }

                } else {
                    // Llamada recursiva si el comando IF es falso
                    val nextCommand = playCommands(column, row.plus(1), parent, pocion)
                    Log.d("GameActivity", "FALSE")
                    return nextCommand
                }
            }

            // Si es un comando ESTANTE
            TipoComandos.ESTANTE -> {
                val estante = command.nombreComando.toString()
                val numEstante = extractNumFromEstante(estante)

                Handler(Looper.getMainLooper()).postDelayed({
                    command!!.layout!!.setBackgroundColor(0xFFFFCE9F.toInt())
                }, 1000)

                Log.d("GameActivity", "ESTANTE($estante) con numero $numEstante")
                return numEstante
            }

            // Si no es ninguno de los anteriores
            else -> null
        }
    }




    private fun ifCommand(pocion: Pocion, varControl: String): Boolean {
        return varControl == pocion.type
    }

    private fun varEstante(estante: String): Int? {
        if (estante.contains("ESTANTE")) {

            val numeroEstante = extractNumFromEstante(estante)

            if (numeroEstante != null) {
                return numeroEstante
            } else {
                return null
            }

        }
        return null
    }

    private fun extractNumFromEstante(estante: String): Int? {
        val regex = "\\d+".toRegex()  // Busca uno o más dígitos
        val match = regex.find(estante)?.value

        if (match != null && match.toIntOrNull() != null) {
            val numeroEstante = match.toInt()
            return numeroEstante

        } else {
            return null
        }

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

    private fun createCommands(commands: LinearLayout, variables: LinearLayout) {
        for (command in listComands) {

            val textView = textCommands(command.nombreComando)

            if (command.tipoComandos == TipoComandos.IF) {
                commands.addView(textView)
            } else if (command.tipoComandos == TipoComandos.ESTANTE) {
                variables.addView(textView)
            }

        }

    }

    private fun textCommands(textCommand: String): TextView {
        val textView = TextView(this).apply {
            text = textCommand
            textSize = 15f
            setPadding(32, 5, 32, 5)
            setTextColor(resources.getColor(android.R.color.white, theme)) // Texto blanco
            background = resources.getDrawable(
                R.drawable.rounded_button,
                theme
            )// Agregar espacio dentro del botón
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            setDrag(this)
        }

        return textView
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
                    return@setOnDragListener event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
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
                        // Crear un nuevo TextView para evitar mover el origina

                        var newLayoutCreation: Pair<LinearLayout, Comando>? = null


                        if (it.text.toString() == "IF") {
                            newLayoutCreation = createDropLayoutCommand(it.text.toString())
                        } else if (it.text.toString().contains("ESTANTE")) {
                            newLayoutCreation = createDropLayoutVariable(it.text.toString())
                        }

                        val comando = newLayoutCreation!!.second
                        val newLayout = newLayoutCreation.first

                        script.addView(newLayout)

                        val coords = getIndex(script, newLayout)


                        comando.parentLayout = script
                        comando.column = coords.first
                        comando.row = coords.second

                        scriptTranscript.add(comando)



                        for (comando in scriptTranscript) {
                            Log.d("Comando", comando.toString())
                        }

                        // Hacer que el nuevo TextView también sea arrastrable si es necesario
                        setDragLinear(newLayout)
                        //setOnDragListenerForDelete(script, newLayout)
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {

                    val layoutToRemove = event.localState as? LinearLayout
                    // Si el layout está fuera de la zona, eliminarlo
                    if (!isInsideRelativeLayout(event, script) && layoutToRemove != null) {

                        script.removeView(layoutToRemove) // Eliminar el layout
                        updateRowNumAfterDelete(layoutToRemove)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun updateRowNumAfterDelete(layautToRemove: LinearLayout){

        val comandToRemove : Comando?

        try{
            comandToRemove = scriptTranscript.find { it.layout == layautToRemove }!!
        }catch (e: Exception){
            return
        }
        Log.d("Comando", comandToRemove.toString())

        for (comando in scriptTranscript) {

            if (comando.parentLayout == comandToRemove.parentLayout && comando.row!! > comandToRemove.row!!){
                comando.row = comando.row!!.minus(1)
            }

        }

        scriptTranscript.removeIf{it.layout == comandToRemove.layout}

        for (comando in scriptTranscript) {
            Log.d("Comando", "Lista comandos despues de borrar: $comando")
        }


    }


    private fun createDropLayoutCommand(draggedText: String): Pair<LinearLayout, Comando> {
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
        val firstTextView = createTextView(draggedText)

        // Crear el Spinner con opciones
        val spinner = Spinner(this).apply {
            val opciones = setListPotions()
            adapter = ArrayAdapter(
                this@GameActivity,
                android.R.layout.simple_spinner_dropdown_item,
                opciones
            )
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
            orientation =
                LinearLayout.VERTICAL // Puedes cambiar la orientación aquí si prefieres apilar horizontalmente
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0x2200FF00)
            setPadding(
                25,
                100,
                0,
                100
            )// Color de fondo para ver el área ocupada por el inner layout
        }

        // Crear el segundo TextView (esto puede ser otro tipo de información o elemento)

        val endDraggedText = "END" + draggedText

        val secondTextView = createTextView(endDraggedText)

        // Agregar los elementos al LinearLayout principal
        newLayout.addView(conditionLayout)
        newLayout.addView(innerLinearLayout)
        newLayout.addView(secondTextView)

        innerLinearLayout.id = View.generateViewId()

        val comando = Comando(TipoComandos.IF)
        comando.spinner = spinner
        comando.layout = innerLinearLayout


        enableDragAndDrop(innerLinearLayout)


        return Pair(newLayout, comando)
    }

    private fun createDropLayoutVariable(draggedText: String): Pair<LinearLayout, Comando> {

        val newLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Apilar verticalmente
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 5, 16, 5) // Agregar un poco de espaciado al contenedor
        }

        val newVariable = createTextView(draggedText)

        newLayout.addView(newVariable)

        newLayout.id = View.generateViewId()

        val comando = Comando(TipoComandos.ESTANTE)
        comando.layout = newLayout

        return Pair(newLayout, comando)

    }

    private fun createTextView(draggedText: String): TextView {
        val textView = TextView(this).apply {
            text = draggedText // Aquí puedes agregar un texto diferente si lo prefieres
            textSize = 15f
            setPadding(32, 5, 0, 5)
            setTextColor(resources.getColor(android.R.color.black, theme))
            gravity = android.view.Gravity.LEFT

            // Aplicar la fuente medieval al TextView
            typeface?.let {
                setTypeface(it)
            }
        }
        return textView
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

    fun getIndex(parentLayout: LinearLayout, child: View): Pair<Int, Int> {
        val parentGroup = parentLayout as ViewGroup
        Log.d("ComandoId", parentLayout.id.toString())
        for (command in scriptTranscript) {  // Asumo que es scriptTranscript, no "scriptTrancript"
            if (command.layout == parentLayout) {
                val index = parentLayout.indexOfChild(child)
                val column: Int = command.column!!

                return Pair(column.plus(1), index)
            }
        }

        val index = parentGroup.indexOfChild(child)
        // Si no encuentra el layout en el array, devolvemos un valor por defecto
        return Pair(0, index)
    }

}