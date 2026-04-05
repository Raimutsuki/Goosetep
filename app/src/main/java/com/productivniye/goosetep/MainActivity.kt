package com.productivniye.goosetep

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), OnGoalAddedListener {

    private lateinit var goalsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goalsContainer = findViewById(R.id.goalsContainer)

        val plusBut = findViewById<ImageView>(R.id.plusBut)

        plusBut.setOnClickListener {
            val dialog = AddGoalDialog()
            dialog.show(supportFragmentManager, "AddGoalDialog")
        }
    }

    override fun onGoalAdded(goal: Goal) {
        // Создаём контейнер для цели
        val goalView = LinearLayout(this)
        goalView.orientation = LinearLayout.VERTICAL
        goalView.setPadding(0, 16, 0, 16)

        // Название цели (без точки)
        val titleView = TextView(this)
        titleView.text = goal.title
        titleView.textSize = 20f
        titleView.setTextColor(resources.getColor(android.R.color.white, theme))
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        titleView.setPadding(0, 0, 0, 8)

        goalView.addView(titleView)

        // Контейнер для подзадач (с отступом слева)
        val subtasksContainer = LinearLayout(this)
        subtasksContainer.orientation = LinearLayout.VERTICAL
        subtasksContainer.setPadding(32, 0, 0, 0)

        // Добавляем подзадачи
        for (subtask in goal.subtasks) {
            val subtaskView = LayoutInflater.from(this)
                .inflate(R.layout.item_subtask_display, subtasksContainer, false)

            val title = subtaskView.findViewById<TextView>(R.id.subtaskTitle)
            title.text = subtask.title

            subtasksContainer.addView(subtaskView)
        }

        goalView.addView(subtasksContainer)
        goalsContainer.addView(goalView)

        Toast.makeText(this, "Цель добавлена: ${goal.title}", Toast.LENGTH_SHORT).show()
    }
}