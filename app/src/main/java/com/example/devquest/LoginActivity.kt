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
import com.example.devquest.API.LoginRequest
import com.example.devquest.API.RegisterRequest
import com.example.devquest.API.RetrofitClient
import com.example.devquest.ui.theme.Level
import com.example.devquest.ui.theme.User
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)


        btnLogin.setOnClickListener {


            val emailEditText = findViewById<EditText>(R.id.etEmail)
            val passwordEditText = findViewById<EditText>(R.id.etPassword)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            //API COMPROBAR EMAIL
            /*if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Email invalido", Toast.LENGTH_SHORT).show()
                emailEditText.text.clear()
                passwordEditText.text.clear()
                return@setOnClickListener
            }*/

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {

                        //API LOGIN
                        //val LoginRequest = LoginRequest(email, password)
                        //Log.d("API TESTS", "Testeando API con: $LoginRequest")

                        // Cambia el apiService.register para que devuelva Response<ResponseBody>
                        //val response = RetrofitClient.apiService.login(LoginRequest)

                        /*if (response.isSuccessful) {
                            val bodyString = response.body()?.string()

                            // Intentamos parsear la respuesta como User
                            val gson = Gson()
                            var user = try {
                                gson.fromJson(bodyString, User::class.java)
                            } catch (e: Exception) {
                                null
                            }*/

                            val user = createUser()

                            if (user != null && user.username.isNotEmpty()) {
                                Toast.makeText(this@LoginActivity, "Bienvenido, ${user.username}!", Toast.LENGTH_SHORT).show()
                                Log.d("API TESTS", user.toString())
                                sendToMenu(user)
                                finish()
                            } else {
                                // Si no es User, mostramos el mensaje de error (asumimos que bodyString es un string de error)
                                Toast.makeText(this@LoginActivity, "ERROR EN EL LOGIN", Toast.LENGTH_SHORT).show()
                                //Log.d("API TESTS", "Mensaje de error: $bodyString")
                            }
                        /*} else {
                            Toast.makeText(this@LoginActivity, "Error HTTP: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.d("API TESTS", "Error HTTP: ${response.code()}")
                        }*/
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.d("API TESTS", "Error de conexion con la API: ${e.message}")
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
