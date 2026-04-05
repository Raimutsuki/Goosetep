package com.productivniye.goosetep

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val plusBut = findViewById<ImageView>(R.id.plusBut)

        plusBut.setOnClickListener {
            Toast.makeText(this, "Плюсик нажат!", Toast.LENGTH_SHORT).show()
        }
    }
}