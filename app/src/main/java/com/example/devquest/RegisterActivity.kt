package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.devquest.API.RegisterRequest
import com.example.devquest.API.RetrofitClient
import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import com.google.gson.Gson
import kotlinx.coroutines.launch

class RegisterActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<Button>(R.id.btnLogin)


        btnRegister.setOnClickListener {
            val usuarioEditText = findViewById<EditText>(R.id.etUsuario)
            val emailEditText = findViewById<EditText>(R.id.etEmail)
            val passwordEditText = findViewById<EditText>(R.id.etPassword)
            val password2EditText = findViewById<EditText>(R.id.etConfirmPassword)

            val usuario = usuarioEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val password2 = password2EditText.text.toString()
            Log.d("API TESTS", "Testeando API")

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Email invalido", Toast.LENGTH_SHORT).show()
                emailEditText.text.clear()
                passwordEditText.text.clear()
                password2EditText.text.clear()
                return@setOnClickListener
            }
            else if (password != password2 || password.length < 6){
                Toast.makeText(this, "Contraseñas no validas", Toast.LENGTH_SHORT).show()
                passwordEditText.text.clear()
                password2EditText.text.clear()
                return@setOnClickListener
            }

            if (usuario.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {


                lifecycleScope.launch {
                    try {
                        val registerRequest = RegisterRequest(email, password, usuario)
                        Log.d("API TESTS", "Testeando API con: $registerRequest")

                        // Cambia el apiService.register para que devuelva Response<ResponseBody>
                        val response = RetrofitClient.apiService.register(registerRequest)

                        if (response.isSuccessful) {
                            val bodyString = response.body()?.string()

                            // Intentamos parsear la respuesta como User
                            val gson = Gson()
                            val user = try {
                                gson.fromJson(bodyString, User::class.java)
                            } catch (e: Exception) {
                                null
                            }

                            if (user != null && user.username.isNotEmpty()) {
                                Toast.makeText(this@RegisterActivity, "Bienvenido, ${user.username}!", Toast.LENGTH_SHORT).show()
                                Log.d("API TESTS", user.toString())
                                val user = createUser()
                                sendToMenu(user)
                                finish()
                            } else {
                                // Si no es User, mostramos el mensaje de error (asumimos que bodyString es un string de error)
                                Toast.makeText(this@RegisterActivity, bodyString ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                                Log.d("API TESTS", "Mensaje de error: $bodyString")
                            }
                        } else {
                            Toast.makeText(this@RegisterActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.d("API TESTS", "Error HTTP: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("API TESTS", "Error de conexion con la API: ${e.message}")
                    }
                }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener{

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
            username = "JohnDoe",
            email = "johndoe@example.com",
            role = "user",
            levels_completed = listOf(
                Level(
                    id = 1,
                    name = "Nivel 1",
                    description = "Combina veneno y salud",
                    maxPuntacion = 4,
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
                    maxPuntacion = 4,
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
                    maxPuntacion = 14,
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
                    maxPuntacion = 15,
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
                    maxPuntacion = 20,
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
                    maxPuntacion = 25,
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