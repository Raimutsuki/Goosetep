package com.productivniye.goosetep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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
        val goalView = LinearLayout(this)
        goalView.orientation = LinearLayout.VERTICAL
        goalView.setPadding(0, 16, 0, 16)

        val titleView = TextView(this)
        titleView.text = goal.title
        titleView.textSize = 20f
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        goalView.addView(titleView)

        val tasksContainer = LinearLayout(this)
        tasksContainer.orientation = LinearLayout.VERTICAL
        tasksContainer.setPadding(32, 8, 0, 0)

        for (task in goal.tasks) {
            addTaskDisplay(tasksContainer, task)
        }

        goalView.addView(tasksContainer)
        goalsContainer.addView(goalView)

        Toast.makeText(this, "Цель добавлена: ${goal.title}", Toast.LENGTH_SHORT).show()
    }

    private fun addTaskDisplay(container: LinearLayout, task: Task) {
        val taskView = LinearLayout(this)
        taskView.orientation = LinearLayout.VERTICAL
        taskView.setPadding(0, 8, 0, 8)

        val taskTitle = TextView(this)
        taskTitle.text = task.title
        taskTitle.textSize = 16f
        taskTitle.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        taskTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        taskView.addView(taskTitle)

        val subtasksContainer = LinearLayout(this)
        subtasksContainer.orientation = LinearLayout.VERTICAL
        subtasksContainer.setPadding(24, 4, 0, 0)

        for (subtask in task.subtasks) {
            addSubtaskDisplay(subtasksContainer, subtask)
        }

        taskView.addView(subtasksContainer)
        container.addView(taskView)
    }

    private fun addSubtaskDisplay(container: LinearLayout, subtask: Subtask) {
        val inflater = LayoutInflater.from(this)
        val subtaskView = inflater.inflate(R.layout.item_subtask_display, container, false)

        val checkbox = subtaskView.findViewById<CheckBox>(R.id.subtaskCheckbox)
        val title = subtaskView.findViewById<TextView>(R.id.subtaskTitle)

        checkbox.isChecked = subtask.isCompleted
        title.text = subtask.title

        updateSubtaskStyle(title, subtask.isCompleted)

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            subtask.isCompleted = isChecked
            updateSubtaskStyle(title, isChecked)
        }

        container.addView(subtaskView)
    }

    private fun updateSubtaskStyle(title: TextView, isCompleted: Boolean) {
        if (isCompleted) {
            title.paintFlags = title.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            title.alpha = 0.6f
        } else {
            title.paintFlags = title.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            title.alpha = 1.0f
        }
    }
}