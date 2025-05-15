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
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import com.example.devquest.ui.theme.Level
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class GameActivity : AppCompatActivity() {
    //Variables globales
    private var listaPociones: ArrayList<Pocion> = ArrayList()
    private var pocionesToSort: HashMap<String, Int> = HashMap()
    private var scriptTranscript: ArrayList<Comando> = ArrayList()
    private var listComands: ArrayList<Comando> = ArrayList()
    private var gameJob: Job? = null
    private var gameSpeed: Long = 1000;
    private var puntuacion: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        Log.d("GameActivity", "onCreate")

        var user = intent.getParcelableExtra<User>("USER")
        val levelId = intent.getIntExtra("LEVEL_ID", -1)


        //Variables de la vista
        val commands = findViewById<LinearLayout>(R.id.Commands)
        val variables = findViewById<LinearLayout>(R.id.Variables)
        val script = findViewById<LinearLayout>(R.id.Script)
        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnExit = findViewById<Button>(R.id.btnExit)
        val potionView: FrameLayout? = findViewById<FrameLayout>(R.id.Potion)
        val textPotions: TextView = findViewById(R.id.TextPotions)

        //Inicializamos el nivel
        inicializateLevel(levelId ,user!!, textPotions)

        //Obligamos a generar un ID para el script
        script.id = View.generateViewId()

        //Y habilitamos que se puedan arrastrar los comandos
        enableDragAndDrop(script)

        //Creamos tanto los arrastrables de las variables como de los comandos
        createCommands(commands, variables)

        var isGameRunning = false
        var isFastMode = false

        //Boton de play
        btnPlay.setOnClickListener { v: View? ->
            if (isGameRunning) {
                // Si el juego está en marcha, lo detenemos
                stopGame()
                btnPlay.text = "Jugar"
                btnExit.setOnClickListener { v: View? ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("USER", user)
                    startActivity(intent)
                    finish()  // Cierra la actividad actual
                }
            } else {
                // Si el juego no está en marcha, lo iniciamos
                startGame(potionView, script, user, levelId)
                btnPlay.text = "Parar"
                // Convertir botón en "cámara rápida"
                btnExit.text = "Velocidad x2"
                btnExit.setOnClickListener {
                    isFastMode = !isFastMode
                    toggleSpeedMode(isFastMode)
                    btnExit.text = if (isFastMode) "Normal" else "Velocidad x2"
                }
            }
            isGameRunning = !isGameRunning // Cambiar el estado del juego
        }

        //Boton de exit default
        btnExit.setOnClickListener { v: View? ->
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("USER", user)
            startActivity(intent)
            finish()  // Cierra la actividad actual
        }

    }

    //Funcion para cambiar la velicidad del juego
    private fun toggleSpeedMode(isFastMode: Boolean) {
        gameSpeed = if (isFastMode) 500 else 1000
    }

    //Funcion para parar el juego
    private fun stopGame(): Boolean {
        // Cancelar el juego en ejecución
        gameJob?.cancel()
        return false
    }

    //Funcion de jugar, inicia la compilacion del codigo
    private fun startGame(potionView: FrameLayout?, script: LinearLayout, user: User?, levelId: Int) {

        //Ordenamos el script transcript por columna y fila
        scriptTranscript.sortWith(compareBy({ it.column }, { it.row }))

        //Variable de victoria
        var win = true

        //Iniciamos un lifescope de la compilacion del codigo
        gameJob = lifecycleScope.launch {

            //Recorremos la lista de pociones a ordenar
            for (pocion in listaPociones) {

                //Mostramos su imagen
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


                potionView!!.setBackgroundResource(0)
                delay(gameSpeed)
            }
            if (win) { //Si ganamos nos muestra el pop up de haber ganado
                showLevelCompletedPopup(levelId, user)
                user!!.LevelsComplete[levelId].isCompleted = true
            }else if (!win){ //Si no ganamos nos muestra el pop up de haber perdido
                showLevelFailedPopup(levelId, user)
            }
        }


    }

    //Funcion para compilar los comandos
    private suspend fun playCommands(column: Int, row: Int, parent: Int, pocion: Pocion): Int? {

        val command: Comando?
        val colorRed = Color.parseColor("#FFFF9999")
        val colorBckgrnd = Color.parseColor("#FFFFCE9F")

        puntuacion++; //Subimos la puntuacion cada vez que se entra en un bloque de codigo

        try {
            // Buscar el comando que coincide con las coordenadas
            command = scriptTranscript.find { it.column == column && it.row == row && parent == it.parentLayout!!.id }
            changeColorOfLayout(command!!.layout!!, colorRed)
            Log.d("GameActivity", "Se ha cambiado el color del layout : ${command.layoutDrop}")

        } catch (e: Exception) {
            // Si no se encuentra el comando, devolver null
            return null
        }


        Log.d("GameActivity", "Comando: $command")

        //Hacemos una funcion recursiva para ejecutar los comandos
        return when (command.tipoComandos.tipo) {

            TipoComandos.Tipo.IF -> {

                delay(gameSpeed)
                command.layout!!.setBackgroundColor(colorBckgrnd)

                if (ifCommand(pocion, command)) { //Si el comando IF es true entramos en el bloque

                    //Ejecutamos el comando que haya dentro del bloque
                    val nextCommand = playCommands(column + 1, 0, command.layoutDrop!!.id, pocion)

                    //Si la funcion nos devuelve null ejecutamos el siguiente comando
                    if (nextCommand == null) {
                        return playCommands(column, row + 1, parent, pocion)
                    } else { //Si no, devolvemos el comando
                        return nextCommand
                    }
                } else { //Si no, simplemente pasamos al siguiente comando
                    Log.d("GameActivity", "FALSE")
                    return playCommands(column, row + 1, parent, pocion)
                }
            }

            TipoComandos.Tipo.ESTANTE -> {
                val estante = command.nombreComando
                val numEstante = extractNumFromEstante(estante) //Extraemos el numero del estante del texto

                delay(gameSpeed)
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

    private fun ifCommand(pocion: Pocion, command: Comando): Boolean {
        //Si comparamos los tipos de la pocion con el comando, devolvemos true si coinciden
        if (command.spinnerCategoria!!.selectedItem.toString() == "Tipo") {
            return pocion.type == command.spinnerOpcion!!.selectedItem.toString()
        }
        //Si comparamos los efectos de la pocion con el comando, devolvemos true si coinciden
        else if (command.spinnerCategoria!!.selectedItem.toString() == "Efecto") {
            return pocion.effect == command.spinnerOpcion!!.selectedItem.toString()

        }
        else (return false)

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

        //Creamos los comandos a partir de la lista de comandos
        for (command in listComands) {

            val textView = textCommands(command.nombreComando)

            if (command.tipoComandos.tipo == TipoComandos.Tipo.IF) {
                commands.addView(textView)
            } else if (command.tipoComandos.tipo == TipoComandos.Tipo.ESTANTE) {
                variables.addView(textView)
            }

        }

    }

    //Generamos los text commands
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

    //Habilitamos el drag de los comandos
    private fun setDrag(view: TextView) {
        view.setOnLongClickListener {
            val clipData = ClipData.newPlainText("COMMAND", view.text)
            val shadowBuilder = View.DragShadowBuilder(view)
            view.startDragAndDrop(clipData, shadowBuilder, view, 0)
            true
        }
    }


    //Funcion para habilitar el drag and drop en un LinearLayout
    private fun enableDragAndDrop(script: LinearLayout) {
        Log.d("DragEvent", "Drag enable on script")
        val originalColor = script.drawingCacheBackgroundColor

        script.setOnDragListener { _, event -> //Listener de drag de los objetos
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

                        //Creamos una comando o una variable en funcion de que arrastramos
                        if (it.text.toString() == "IF") {
                            newLayoutCreation = createDropLayoutCommand(it.text.toString())
                        } else if (it.text.toString().contains("ESTANTE")) {
                            newLayoutCreation = createDropLayoutVariable(it.text.toString())
                        }

                        val comando = newLayoutCreation!!.second
                        val newLayout = newLayoutCreation.first

                        script.addView(newLayout)

                        val coords = getIndex(script, newLayout)//Conseguimos las coordenadas del nuevo layout

                        //Añadimos el padre y las coordenadas al comando
                        comando.parentLayout = script
                        comando.column = coords.first
                        comando.row = coords.second

                        scriptTranscript.add(comando) //Añadimos el comando al scriptTranscript

                        // Hacer que el nuevo TextView también sea arrastrable
                        setDragLinear(newLayout)

                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> { //Funcion que comprueba si hay que eliminar algo

                    val layoutToRemove = event.localState as? LinearLayout
                    // Si el layout está fuera de la zona, eliminarlo
                    if (!isInsideLinearLayout(event, script) && layoutToRemove != null) {

                        script.removeView(layoutToRemove) // Eliminar el layout
                        updateRowNumAfterDelete(layoutToRemove) //Actualizamos el scriptTranscript

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

        //Recuperamos el comando que queremos eliminar del scriptTranscript
        try{
            comandToRemove = scriptTranscript.find { it.layout == layautToRemove }!!
        }catch (e: Exception){
            Log.d("ComandoRemove", "No se encontro el layout a borrar")
            return
        }
        Log.d("ComandoRemove", comandToRemove.toString())

        //Actualizamos el scriptTranscript para no tener en cuenta ese comando
        for (comando in scriptTranscript.toList()) {

            if (comando.parentLayout == comandToRemove.parentLayout && comando.row!! > comandToRemove.row!!){
                comando.row = comando.row!!.minus(1)
            }
            if (comando.parentLayout == comandToRemove.layoutDrop){
                updateRowNumAfterDelete(comando.layout!!)
            }

        }

        //Eliminamos el comando
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


        }


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

        // Spinner de categoría (Tipo o Efecto)
        val spinnerCategoria = Spinner(this).apply {
            val categorias = listOf("Tipo", "Efecto")
            adapter = ArrayAdapter(
                this@GameActivity,
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            id = View.generateViewId()
        }


        // Spinner de opciones dependientes
        val spinnerOpciones = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            id = View.generateViewId()
        }

        // Función para actualizar opciones según la categoría
        fun actualizarOpciones(categoria: String) {
            val opciones = when (categoria) {
                "Tipo" -> listaPociones.map { it.type }.distinct()
                "Efecto" -> setListPotions()
                else -> emptyList()
            }
            val adapter = ArrayAdapter(
                this@GameActivity,
                android.R.layout.simple_spinner_dropdown_item,
                opciones
            )
            spinnerOpciones.adapter = adapter
        }

        // Inicializar opciones con la categoría por defecto
        actualizarOpciones("Tipo")

        // Cambiar lista del segundo spinner cuando cambia la categoría
        spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val categoriaSeleccionada = parent.getItemAtPosition(position) as String
                actualizarOpciones(categoriaSeleccionada)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        conditionLayout.addView(firstTextView)
        conditionLayout.addView(spinnerCategoria)
        conditionLayout.addView(spinnerOpciones)


        // Crear el segundo LinearLayout dentro del nuevo LinearLayout
        val innerLinearLayout = LinearLayout(this).apply {
            orientation =
                LinearLayout.VERTICAL
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

        // Crear el segundo TextView
        val endDraggedText = "END" + draggedText
        val secondTextView = createTextView(endDraggedText)


        // Agregar los elementos al LinearLayout principal
        newLayout.addView(conditionLayout)
        newLayout.addView(innerLinearLayout)
        newLayout.addView(secondTextView)

        innerLinearLayout.id = View.generateViewId()
        newLayout.id = View.generateViewId()

        val comando = Comando(TipoComandos.IF)
        comando.spinnerCategoria = spinnerCategoria
        comando.spinnerOpcion = spinnerOpciones
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
        // Cargar la fuente medieval
        val typeface = ResourcesCompat.getFont(this, R.font.medieval)

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

    // Función para verificar si el layout está dentro del LinearLayout
    private fun isInsideLinearLayout(event: DragEvent, script: LinearLayout): Boolean {
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

    //Funcion para generar la lista de pociones que se ira mostrando en pantalla
    private fun setListPotions(): MutableList<String> {
        val listaPociones = mutableListOf<String>()
        for (pocion in pocionesToSort) {
            listaPociones.add(pocion.key)
        }
        return listaPociones

    }

    //Funcion para conseguir el indice de un elemento en el scriptTranscript
    private fun getIndex(parentLayout: LinearLayout, child: View): Pair<Int, Int> {

        val parentGroup = parentLayout as ViewGroup
        Log.d("ComandoIdParent", parentLayout.id.toString())
        for (comando in scriptTranscript) {
            Log.d("Comandos:", comando.toString())
        }
        //Recorremos el scriptTranscript
        for (command in scriptTranscript) {
            //Si encontramos el padre del layout en el scriptTranscript
            if (command.layoutDrop == parentLayout) {
                val index = parentLayout.indexOfChild(child) //Obtenemos el indice del layout
                val column: Int = command.column!!

                return Pair(column.plus(1), index)
            }
        }

        val index = parentGroup.indexOfChild(child)
        // Si no encuentra el layout en el array, devolvemos un valor por defecto
        return Pair(0, index)
    }

    //Pop up de victoria
    private fun showLevelCompletedPopup(level: Int, user: User?) {

        //Recuperamos los elementos de la vista de victoria
        val levelCompletedLayout = findViewById<FrameLayout>(R.id.levelCompletedLayout)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val restartButton = findViewById<Button>(R.id.restartButton)
        val levelMessage = findViewById<TextView>(R.id.levelMessage)
        val score = findViewById<TextView>(R.id.scoreTextView)

        // Mostrar el popup con el mensaje de nivel alcanzado
        levelCompletedLayout.visibility = View.VISIBLE
        levelMessage.text = "¡Has completado el nivel $level!"
        score.text = "Puntuación: $puntuacion"

        // Animacion
        levelCompletedLayout.alpha = 0f
        levelCompletedLayout.animate().alpha(1f).setDuration(500).start()

        // Configurar el botón de continuar
        continueButton.setOnClickListener {
            // Redirigir al menú o siguiente acción
            val intent = Intent(this, ListLevelsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("USER", user)
            startActivity(intent)
            finish()
        }
        //Reiniciamos la pantalla
        restartButton.setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent)
        }


    }

    //Pop up de derrota
    private fun showLevelFailedPopup(level: Int, user: User?) {

        val levelFailedLayout = findViewById<FrameLayout>(R.id.levelFailedLayout)
        val restartButton = findViewById<Button>(R.id.retryButton)
        val exitButton = findViewById<Button>(R.id.exitButton)

        // Mostrar el popup con el mensaje de nivel alcanzado
        levelFailedLayout.visibility = View.VISIBLE

        //Animacion
        levelFailedLayout.alpha = 0f
        levelFailedLayout.animate().alpha(1f).setDuration(500).start()

        // Configurar el botón de continuar
        exitButton.setOnClickListener {
            // Redirigir al menú o siguiente acción
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("USER", user)
            startActivity(intent)
            finish()
        }

        restartButton.setOnClickListener {
            val intent = intent
            finish()
            startActivity(intent)
        }


    }

    //Inicializamos las variables a null
    private fun inicializateNulls() {
        Log.d("GameActivity", "null inicializate")
        listaPociones.clear()
        pocionesToSort.clear()
        scriptTranscript.clear()
        listComands.clear()
        gameJob?.cancel()
        gameSpeed = 1000;
        puntuacion = 0;
    }

    //Inicializamos el nivel
    private fun inicializateLevel(levelId: Int, user: User, TextPotions: TextView) {
        Log.d("GameActivity", "null inicializate")
        inicializateNulls()

        //Buscamos el nivel que queremos cargar
        val levelToLoad: Level = user.LevelsComplete.find { it.id == levelId }!!

        //Cargamos los datos del nivel
        pocionesToSort = levelToLoad.listPotions
        loadPotions(pocionesToSort)

        loadCommands(levelToLoad.listCommands)

        TextPotions.text = pocionesToSort.entries.joinToString(", ") { "${it.key}: ${it.value}" }


    }

    //Cargamos las pociones en la lista de pociones
    private fun loadPotions(pociones: HashMap<String, Int>) {

        for (pocion in pociones) {
            for (i in 1..pocion.value) {
                val pociontoCreate = Pocion(TipoPocion.valueOf(pocion.key))
                listaPociones.add(pociontoCreate)
            }
        }

        listaPociones.shuffle(Random(System.currentTimeMillis()))

    }

    //Cargamos los comandos en la lista de comandos
    private fun loadCommands (commands: List<String>){
        for (command in commands){
            val comando = Comando(TipoComandos.valueOf(command))
            listComands.add(comando)
        }

    }
}