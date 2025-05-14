package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.widget.ImageButton
import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User

class ListLevelsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lisoflevels)

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutLevels)
        val user = intent.getParcelableExtra<User>("USER")
        val btnExit = findViewById<ImageButton>(R.id.btnBack)


        btnExit.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER", user)
            startActivity(intent)
        }

        createLevelsButtons(user!!, linearLayout)


    }

    // Verifica si el nivel es el último no completado
    private fun isLastIncompleteLevel(levels: List<Level>): Int {
        return levels.indexOfFirst { !it.isCompleted }.takeIf { it != -1 }?.let { levels[it].id } ?: -1
    }


    private fun createLevelsButtons(user: User, linearLayout: LinearLayout){

        try {

            // Agregar los niveles de manera dinámica
            user.LevelsComplete.forEach { level ->
                val button = Button(this)
                button.text = level.name
                button.background = getDrawable(R.drawable.rounded_button)
                if (level.isCompleted || (level.id == isLastIncompleteLevel(user.LevelsComplete))) {
                    button.isEnabled = true

                    button.setBackgroundColor(Color.parseColor("#dfb58b"))


                }
                else{
                    button.isEnabled = false
                    button.setBackgroundColor(Color.parseColor("#c4c9bd"))
                }

                // Configurar el click listener
                button.setOnClickListener {
                    if (button.isEnabled) {
                        val intent = Intent(this, GameActivity::class.java)
                        intent.putExtra("USER", user)                 // Enviamos el objeto User
                        intent.putExtra("LEVEL_ID", level.id)         // Enviamos el ID del nivel seleccionado
                        startActivity(intent)
                    }
                }




                // Agregar el botón al layout
                linearLayout.addView(button)
            }

        }catch (Exception: Exception){
            Toast.makeText(this, "Error a la hora de cargar los niveles del usuario", Toast.LENGTH_SHORT).show()
        }

    }


}
