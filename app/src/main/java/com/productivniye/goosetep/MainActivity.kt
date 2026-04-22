package com.productivniye.goosetep

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.ProgressBar
import android.animation.ObjectAnimator

class MainActivity : AppCompatActivity(), OnGoalAddedListener {

    private lateinit var goalsContainer: LinearLayout
    private lateinit var overallProgressBar: ProgressBar
    private lateinit var overallProgressText: TextView
    private val goals = mutableListOf<Goal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goalsContainer = findViewById(R.id.goalsContainer)
        overallProgressBar = findViewById(R.id.overallProgressBar)
        overallProgressText = findViewById(R.id.overallProgressText)

        val plusBut = findViewById<ImageView>(R.id.plusBut)

        plusBut.setOnClickListener {
            val dialog = AddGoalDialog()
            dialog.show(supportFragmentManager, "AddGoalDialog")
        }
    }

    override fun onGoalAdded(goal: Goal) {
        goals.add(goal)

        val goalView = LayoutInflater.from(this)
            .inflate(R.layout.item_goal, goalsContainer, false)

        val goalTitle = goalView.findViewById<TextView>(R.id.goalTitle)
        val expandIcon = goalView.findViewById<ImageView>(R.id.expandIcon)
        val tasksContainer = goalView.findViewById<LinearLayout>(R.id.subtasksContainer)
        val goalHeader = goalView.findViewById<LinearLayout>(R.id.goalHeader)
        val progressBar = goalView.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = goalView.findViewById<TextView>(R.id.progressText)

        goalTitle.text = goal.title

        // Функция обновления прогресса цели
        fun updateGoalProgress() {
            var total = 0
            var completed = 0
            for (task in goal.tasks) {
                for (subtask in task.subtasks) {
                    total++
                    if (subtask.isCompleted) completed++
                }
            }
            val progress = if (total > 0) (completed * 100 / total) else 0
            animateProgress(progressBar, progress)
            progressText.text = "$completed/$total"

            // Обновляем общий прогресс
            updateOverallProgress()
        }

        // Очищаем контейнер
        tasksContainer.removeAllViews()

        // Проходим по всем задачам
        for (task in goal.tasks) {
            val taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task_display, tasksContainer, false)

            val taskTitle = taskView.findViewById<TextView>(R.id.taskTitle)
            val subtasksContainer = taskView.findViewById<LinearLayout>(R.id.subtasksContainer)

            taskTitle.text = task.title

            // Добавляем подзадачи
            for (subtask in task.subtasks) {
                addSubtaskDisplay(subtasksContainer, subtask) { _ ->
                    updateGoalProgress()
                }
            }

            tasksContainer.addView(taskView)
        }

        // Обновляем прогресс после добавления всех подзадач
        updateGoalProgress()

        // Функция сворачивания/разворачивания
        fun updateUI() {
            if (goal.isExpanded) {
                tasksContainer.visibility = View.VISIBLE
                expandIcon.animate().rotation(0f).setDuration(200).start()
            } else {
                tasksContainer.visibility = View.GONE
                expandIcon.animate().rotation(-90f).setDuration(200).start()
            }
        }

        goalHeader.setOnClickListener {
            goal.isExpanded = !goal.isExpanded
            updateUI()
        }

        updateUI()
        goalsContainer.addView(goalView)

        // Обновляем общий прогресс при добавлении новой цели
        updateOverallProgress()
    }

    private fun updateOverallProgress() {
        var totalSubtasks = 0
        var completedSubtasks = 0

        for (goal in goals) {
            for (task in goal.tasks) {
                for (subtask in task.subtasks) {
                    totalSubtasks++
                    if (subtask.isCompleted) completedSubtasks++
                }
            }
        }

        val progress = if (totalSubtasks > 0) (completedSubtasks * 100 / totalSubtasks) else 0
        animateProgress(overallProgressBar, progress)
        overallProgressText.text = "$completedSubtasks/$totalSubtasks"
    }

    private fun addSubtaskDisplay(
        container: LinearLayout,
        subtask: Subtask,
        onCheckedChangeListener: ((Boolean) -> Unit)? = null
    ) {
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
            onCheckedChangeListener?.invoke(isChecked)
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

    private fun animateProgress(progressBar: ProgressBar, targetProgress: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", targetProgress).apply {
            duration = 300
            start()
        }
    }
}