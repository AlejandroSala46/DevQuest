package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.devquest.API.LoginRequest
import com.example.devquest.API.RetrofitClient
import com.example.devquest.ui.theme.User
import kotlinx.coroutines.launch

class RegisterActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<Button>(R.id.btnLogin)


        btnRegister.setOnClickListener {
            val usuario = findViewById<EditText>(R.id.etUsuario).text.toString()
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            Log.d("API TESTS", "Testeando API")

            if (usuario.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                Toast.makeText(this, "Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    try {
                        val registerRequest = LoginRequest(usuario, password)
                        Log.d("API TESTS", "Testeando API con: $registerRequest")
                        val user = RetrofitClient.apiServiceLogin.login(registerRequest)

                        if (user != null) {
                            Toast.makeText(this@RegisterActivity, "Bienvenido, ${user.name}!", Toast.LENGTH_SHORT).show()
                            Log.d("API TESTS", user.toString())
                            sendToMenu(user)
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Error a la hora de crear el usuario", Toast.LENGTH_SHORT).show()
                            Log.d("API TESTS", "Error a la hora de crear el usuario")
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
}