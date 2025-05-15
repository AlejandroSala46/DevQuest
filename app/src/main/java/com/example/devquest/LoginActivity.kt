package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.devquest.API.LoginRequest
import com.example.devquest.API.RetrofitClient
import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)


        btnLogin.setOnClickListener {
            val usuario = findViewById<EditText>(R.id.etUsuario).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (usuario.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val loginRequest = LoginRequest(usuario, password)

                        val user = createUser()

                        //val user = RetrofitClient.apiServiceLogin.login(loginRequest)

                        if (user != null) {
                            Toast.makeText(this@LoginActivity, "Bienvenido, ${user.name}!", Toast.LENGTH_SHORT).show()
                            sendToMenu(user)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()

                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener{

            val  intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

            val user = intent.getParcelableExtra<User>("USER")

            if (user != null) {
                sendToMenu(user)
            }
        }
    }

     private fun sendToMenu(user: User){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER", user)
        startActivity(intent)
    }
    private fun createUser(): User{
        val user = User(
            auth = "skdjbf",
            name = "JohnDoe",
            email = "johndoe@example.com",
            //password = "hashed_password",
            LevelsComplete = listOf(
                Level(
                    id = 1,
                    name = "Nivel 1",
                    description = "Combina veneno y salud",
                    listPotions = hashMapOf(
                        "VENENO" to 1,
                        "SALUD" to 1
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE2"),
                    isCompleted = true
                ),
                Level(
                    id = 2,
                    name = "Nivel 2",
                    description = "Combina veneno y vigor",
                    listPotions = hashMapOf(
                        "VENENO" to 1,
                        "VIGOR" to 1
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE4"),
                    isCompleted = false
                ),
                Level(
                    id = 3,
                    name = "Nivel 3",
                    description = "Tres pociones simples",
                    listPotions = hashMapOf(
                        "VENENO" to 2,
                        "SALUD" to 2,
                        "VIGOR" to 1
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE2", "ESTANTE4"),
                    isCompleted = false
                ),
                Level(
                    id = 4,
                    name = "Nivel 4",
                    description = "Introduce velocidad en la mezcla",
                    listPotions = hashMapOf(
                        "VENENO" to 2,
                        "VELOCIDAD" to 2,
                        "SALUD" to 2
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE2", "ESTANTE3"),
                    isCompleted = false
                ),
                Level(
                    id = 5,
                    name = "Nivel 5",
                    description = "Cuatro tipos de pociones",
                    listPotions = hashMapOf(
                        "VENENO" to 10,
                        "SALUD" to 3,
                        "VELOCIDAD" to 2,
                        "VIGOR" to 2
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE2", "ESTANTE3", "ESTANTE4"),
                    isCompleted = false
                ),
                Level(
                    id = 6,
                    name = "Nivel 6",
                    description = "Más cantidad y lógica compleja",
                    listPotions = hashMapOf(
                        "VENENO" to 4,
                        "SALUD" to 6,
                        "VELOCIDAD" to 4,
                        "VIGOR" to 4
                    ),
                    listCommands = listOf("IF", "ESTANTE1", "ESTANTE2", "ESTANTE3", "ESTANTE4"),
                    isCompleted = false
                )
            )
        )



        return user;
    }
}
