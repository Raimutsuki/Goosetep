package com.productivniye.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим элементы по их id
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        val resultText = findViewById<TextView>(R.id.resultText)

        // Обработка нажатия кнопки
        buttonSubmit.setOnClickListener {
            val name = editTextName.text.toString().trim()
            if (name.isNotEmpty()) {
                val message = "Привет, $name! Рад тебя видеть!"
                resultText.text = message
                editTextName.text.clear()
            } else {
                Toast.makeText(this, "Пожалуйста, введите имя!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}