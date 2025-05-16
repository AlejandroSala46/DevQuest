package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.util.Log
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.example.devquest.API.RetrofitClient
import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import kotlinx.coroutines.launch

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

    @Suppress("MissingSuperCall")
    override fun onBackPressed() {
        val user = intent.getParcelableExtra<User>("USER")
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER", user)
        startActivity(intent)
        finish()
    }

    // Verifica si el nivel es el último no completado
    private fun isLastIncompleteLevel(levels: List<Level>): Int {
        return levels.indexOfFirst { !it.isCompleted }.takeIf { it != -1 }?.let { levels[it].id } ?: -1
    }


    private fun createLevelsButtons(user: User, linearLayout: LinearLayout){

        try {

            // Agregar los niveles de manera dinámica
            user.levels_completed.forEach { level ->
                val button = Button(this)
                button.text = level.name
                button.background = getDrawable(R.drawable.rounded_button)
                if (level.isCompleted || (level.id == isLastIncompleteLevel(user.levels_completed))) {
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
                        lifecycleScope.launch {
                            try {
                                val token = user.auth // Asegúrate que user.auth tenga el token correcto
                                val level_id = level.id

                                //API LEVEL
                                //Log.d("API TESTS", "Testeando API: Direccion: auth/levels/$level_id con token: $token")
                                //val levelDetail = RetrofitClient.apiService.getLevel(token, level_id)

                                //if (levelDetail != null) {
                                if (level_id != null) {
                                    val intent = Intent(this@ListLevelsActivity, GameActivity::class.java)
                                    intent.putExtra("USER", user)                 // Enviamos el objeto User
                                    intent.putExtra("LEVEL_ID", level.id)         // Enviamos el ID del nivel seleccionado
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this@ListLevelsActivity, "Error al cargar el nivel", Toast.LENGTH_SHORT).show()
                                    Log.d("API TESTS", "Error al cargar el nivel con ID: ${level.id}")
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@ListLevelsActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.d("API TESTS", "Error de conexión: ${e.message}")
                            }
                        }
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
