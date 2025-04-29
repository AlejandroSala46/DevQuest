package com.example.devquest

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.devquest.ui.theme.Comando
import com.example.devquest.ui.theme.Pocion
import com.example.devquest.ui.theme.TipoComandos
import com.example.devquest.ui.theme.TipoPocion
import com.example.devquest.ui.theme.User
import kotlin.random.Random
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.devquest.ui.theme.Level
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class GameActivity : AppCompatActivity() {

    private var listaPociones: ArrayList<Pocion> = ArrayList()
    private var pocionesToSort: HashMap<String, Int> = HashMap()
    private var scriptTranscript: ArrayList<Comando> = ArrayList()
    private var listComands: ArrayList<Comando> = ArrayList()
    private var gameJob: Job? = null
    private lateinit var levelCompletedLayout: FrameLayout
    private lateinit var continueButton: Button
    private lateinit var restartButton: Button
    private lateinit var levelMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        Log.d("GameActivity", "onCreate")

        val user = intent.getParcelableExtra<User>("USER")
        val levelId = intent.getIntExtra("LEVEL_ID", -1)



        val commands = findViewById<LinearLayout>(R.id.Commands)
        val variables = findViewById<LinearLayout>(R.id.Variables)
        val script = findViewById<LinearLayout>(R.id.Script)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnExit = findViewById<Button>(R.id.btnExit)
        val potionView: FrameLayout? = findViewById<FrameLayout>(R.id.Potion)
        val textPotions: TextView = findViewById(R.id.TextPotions)

        inicializateLevel(levelId ,user!!, textPotions)

        script.id = View.generateViewId()

        enableDragAndDrop(script)

        createCommands(commands, variables)

        var isGameRunning = false

        btnPlay.setOnClickListener { v: View? ->
            if (isGameRunning) {
                // Si el juego está en marcha, lo detenemos
                stopGame()
                btnPlay.text = "Jugar"
            } else {
                // Si el juego no está en marcha, lo iniciamos
                startGame(potionView, script)
                btnPlay.text = "Parar"
            }
            isGameRunning = !isGameRunning // Cambiar el estado del juego
        }

        btnExit.setOnClickListener { v: View? ->
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()  // Cierra la actividad actual
        }

    }

    private fun stopGame(): Boolean {
        // Cancelar el juego en ejecución
        gameJob?.cancel()
        return false
    }

    private fun startGame(potionView: FrameLayout?, script: LinearLayout) {
        scriptTranscript.sortWith(compareBy({ it.column }, { it.row }))

        for (comando in scriptTranscript) {
            Log.d("ComandoGame", comando.toString())
        }

        var win = true
        gameJob = lifecycleScope.launch {
            for (pocion in listaPociones) {
                Log.d("GamePocion", pocion.toString())

                potionView!!.setBackgroundResource(pocion.imagen)

                val numEstante: Int? = playCommands(0, 0, script.id, pocion)

                if (numEstante != null) {
                    if (numEstante == pocion.estante) {
                        Log.d("GameActivity", "Se pudo ubicar la pocion")
                        potionView.setBackgroundResource(0)
                    } else {
                        Log.d("GameActivity", "No se pudo ubicar la pocion, $numEstante es distinto a ${pocion.estante}")
                        win = false
                        break
                    }
                } else {
                    Log.d("GameActivity", "No se pudo ubicar la pocion, num estante es null")
                    win = false
                    break
                }

                // Esperar 5 segundos sin bloquear la UI
                potionView!!.setBackgroundResource(0)
                delay(1000)
            }
            if (win) {
                showLevelCompletedPopup(1)
            }
        }


    }

    private suspend fun playCommands(column: Int, row: Int, parent: Int, pocion: Pocion): Int? {

        val command: Comando?
        val colorRed = Color.parseColor("#FFFF9999")
        val colorBckgrnd = Color.parseColor("#FFFFCE9F")

        try {
            // Buscar el comando que coincide con las coordenadas
            command = scriptTranscript.find { it.column == column && it.row == row && parent == it.parentLayout!!.id }
            changeColorOfLayout(command!!.layout!!, colorRed)
            Log.d("GameActivity", "Se ha cambiado el color del layout : ${command.layoutDrop}")

        } catch (e: Exception) {
            // Si no se encuentra el comando, devolver null
            return null
        }

        // Espera 4 segundos sin bloquear la UI
        //delay(4000)

        Log.d("GameActivity", "Comando: $command")

        return when (command.tipoComandos.tipo) {

            TipoComandos.Tipo.IF -> {
                val varControl = command.spinner!!.selectedItem.toString()
                Log.d("GameActivity", "IF($varControl = ${pocion.type})")

                delay(1000)
                command.layout!!.setBackgroundColor(colorBckgrnd)

                if (ifCommand(pocion, varControl)) {

                    val nextCommand = playCommands(column + 1, 0, command.layoutDrop!!.id, pocion)
                    // Después de 1 segundo, restaurar el color
                    if (nextCommand == null) {
                        return playCommands(column, row + 1, parent, pocion)
                    } else {
                        return nextCommand
                    }
                } else {
                    Log.d("GameActivity", "FALSE")
                    return playCommands(column, row + 1, parent, pocion)
                }
            }

            TipoComandos.Tipo.ESTANTE -> {
                val estante = command.nombreComando
                val numEstante = extractNumFromEstante(estante)

                delay(1000)
                command.layout!!.setBackgroundColor(colorBckgrnd)

                Log.d("GameActivity", "ESTANTE($estante) con numero $numEstante")
                return numEstante
            }

            else -> null
        }
    }


    private fun changeColorOfLayout(Layout: LinearLayout, color: Int) {
        val newDrawable = GradientDrawable()
        newDrawable.setColor(color)
        Layout.background = newDrawable
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




    private fun createCommands(commands: LinearLayout, variables: LinearLayout) {
        for (command in listComands) {

            val textView = textCommands(command.nombreComando)

            if (command.tipoComandos.tipo == TipoComandos.Tipo.IF) {
                commands.addView(textView)
            } else if (command.tipoComandos.tipo == TipoComandos.Tipo.ESTANTE) {
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

                        for (comando in scriptTranscript) {
                            Log.d("Comando", "Lista comandos despues de borrar: $comando")
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun updateRowNumAfterDelete(layautToRemove: LinearLayout){

        val comandToRemove : Comando?
        Log.d("ComandoRemoveId", layautToRemove.id.toString())
        try{
            comandToRemove = scriptTranscript.find { it.layout == layautToRemove }!!
        }catch (e: Exception){
            Log.d("ComandoRemove", "No se encontro el layout a borrar")
            return
        }
        Log.d("ComandoRemove", comandToRemove.toString())

        for (comando in scriptTranscript.toList()) {

            if (comando.parentLayout == comandToRemove.parentLayout && comando.row!! > comandToRemove.row!!){
                comando.row = comando.row!!.minus(1)
            }
            if (comando.parentLayout == comandToRemove.layoutDrop){
                updateRowNumAfterDelete(comando.layout!!)
            }

        }

        scriptTranscript.removeIf{it.layout == comandToRemove.layout}




    }


    private fun createDropLayoutCommand(draggedText: String): Pair<LinearLayout, Comando> {

        val backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#FFFFCE9F")) // Fondo con color
            cornerRadius = 24f
            setStroke(4, Color.BLACK) // Grosor del borde (4px) y color negro
        }
        // Crear un nuevo LinearLayout principal para contener
        val newLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL // Apilar verticalmente
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            //setPadding(16, 5, 16, 5) // Agregar un poco de espaciado al contenedor

        }
        // Cargar la fuente medieval
        val typeface = ResourcesCompat.getFont(this, R.font.medieval)

        val conditionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL // Alineación horizontal
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            //setPadding(16, 5, 0, 16)
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
            background = backgroundDrawable
            setPadding(
                25,
                0,
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
        newLayout.id = View.generateViewId()

        val comando = Comando(TipoComandos.IF)
        comando.spinner = spinner
        comando.layoutDrop = innerLinearLayout
        comando.layout = newLayout


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

        val nombreComando = draggedText.replace(" ", "")

        val tipoComando = TipoComandos.valueOf(nombreComando)

        val comando = Comando(tipoComando)
        comando.layout = newLayout
        comando.layoutDrop = newLayout

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
        for (pocion in pocionesToSort) {
            listaPociones.add(pocion.key)
        }
        return listaPociones

    }

    private fun getIndex(parentLayout: LinearLayout, child: View): Pair<Int, Int> {
        val parentGroup = parentLayout as ViewGroup
        Log.d("ComandoIdParent", parentLayout.id.toString())
        for (command in scriptTranscript) {  // Asumo que es scriptTranscript, no "scriptTrancript"
            if (command.layoutDrop == parentLayout) {
                val index = parentLayout.indexOfChild(child)
                val column: Int = command.column!!

                return Pair(column.plus(1), index)
            }
        }

        val index = parentGroup.indexOfChild(child)
        // Si no encuentra el layout en el array, devolvemos un valor por defecto
        return Pair(0, index)
    }

    private fun showLevelCompletedPopup(level: Int) {

        levelCompletedLayout = findViewById(R.id.levelCompletedLayout)
        continueButton = findViewById(R.id.continueButton)
        restartButton = findViewById(R.id.restartButton)
        levelMessage = findViewById(R.id.levelMessage)

        // Mostrar el popup con el mensaje de nivel alcanzado
        levelCompletedLayout.visibility = View.VISIBLE
        levelMessage.text = "¡Has completado el nivel $level!"

        // Puedes agregar animaciones para hacerlo más atractivo, por ejemplo:
        levelCompletedLayout.alpha = 0f
        levelCompletedLayout.animate().alpha(1f).setDuration(500).start()

        // Configurar el botón de continuar
        continueButton.setOnClickListener {
            // Redirigir al menú o siguiente acción
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        restartButton.setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent)
        }


    }
    private fun inicializateNulls() {
        Log.d("GameActivity", "null inicializate")
        listaPociones.clear()
        pocionesToSort.clear()
        scriptTranscript.clear()
        listComands.clear()
        gameJob?.cancel()
    }
    private fun inicializateLevel(levelId: Int, user: User, TextPotions: TextView) {
        Log.d("GameActivity", "null inicializate")
        inicializateNulls()

        val levelToLoad: Level = user.LevelsComplete.find { it.id == levelId }!!

        pocionesToSort = levelToLoad.listPotions
        loadPotions(pocionesToSort)

        loadCommands(levelToLoad.listCommands)

        TextPotions.text = pocionesToSort.entries.joinToString(", ") { "${it.key}: ${it.value}" }


    }

    private fun loadPotions(pociones: HashMap<String, Int>) {

        for (pocion in pociones) {
            for (i in 1..pocion.value) {
                val pociontoCreate = Pocion(TipoPocion.valueOf(pocion.key))
                listaPociones.add(pociontoCreate)
            }
        }

        listaPociones.shuffle(Random(System.currentTimeMillis()))

    }

    private fun loadCommands (commands: List<String>){
        for (command in commands){
            val comando = Comando(TipoComandos.valueOf(command))
            listComands.add(comando)
        }

    }
}