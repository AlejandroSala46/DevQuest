package com.example.devquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.devquest.ui.theme.User


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val user = intent.getParcelableExtra<User>("USER")

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnConfig = findViewById<Button>(R.id.btnConfig)
        val btnExit = findViewById<Button>(R.id.btnExit)

        btnPlay.setOnClickListener { v: View? ->
            val intent = Intent(this, ListLevelsActivity::class.java)
            intent.putExtra("USER", user)
            startActivity(intent)
        }

        btnConfig.setOnClickListener { v: View? -> }

        btnExit.setOnClickListener { v: View? ->
            // Acciones para "Salir"
            finish() // Cierra la aplicación
        }
    }


}
