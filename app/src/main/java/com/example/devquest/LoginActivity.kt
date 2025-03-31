package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devquest.ui.theme.User

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
                Toast.makeText(this, "Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()

                val user = User(usuario, "", password);

                sendToMenu(user)

                finish()

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
}
