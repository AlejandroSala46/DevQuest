package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devquest.ui.theme.User

class RegisterActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)


        btnRegister.setOnClickListener {
            val usuario = findViewById<EditText>(R.id.etUsuario).text.toString()
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (usuario.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                Toast.makeText(this, "Bienvenido, $usuario!", Toast.LENGTH_SHORT).show()

                val user = User(usuario, email, password, emptyList());

                val intent = Intent(this, LoginActivity::class.java)

                intent.putExtra("USER", user)

                startActivity(intent)

                finish()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener{

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}