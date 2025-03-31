package com.example.devquest

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        val commands = findViewById<RelativeLayout>(R.id.Commands)
        val variables = findViewById<View>(R.id.Variables)
        val script = findViewById<RelativeLayout>(R.id.Script)

        createCommands(commands)
        enableDragAndDrop(script)

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

    private fun createCommands(commands: RelativeLayout) {
        val textView = TextView(this).apply {
            text = "IF"
            textSize = 18f
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
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

    private fun enableDragAndDrop(script: RelativeLayout) {
        script.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as? TextView
                    draggedView?.let {
                        val newParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            leftMargin = event.x.toInt()
                            topMargin = event.y.toInt()
                        }
                        script.addView(it, newParams)
                    }
                    true
                }

                else -> true
            }
        }
    }

}