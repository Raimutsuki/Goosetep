package com.productivniye.goosetep

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.animation.ObjectAnimator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity(), OnGoalAddedListener {

    // Views
    private lateinit var goalsContainer: LinearLayout
    private lateinit var overallProgressBar: ProgressBar
    private lateinit var overallProgressText: TextView
    private lateinit var selectedEmojiView: TextView
    private lateinit var levelProgressBar: ProgressBar
    private lateinit var tvLevel: TextView
    private lateinit var tvXp: TextView

    private val goals = mutableListOf<Goal>()
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private var playerProgress = PlayerProgress()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Инициализация View
        goalsContainer = findViewById(R.id.goalsContainer)
        overallProgressBar = findViewById(R.id.overallProgressBar)
        overallProgressText = findViewById(R.id.overallProgressText)
        selectedEmojiView = findViewById(R.id.tv_selected_emoji)
        levelProgressBar = findViewById(R.id.levelProgressBar)
        tvLevel = findViewById(R.id.tv_level)
        tvXp = findViewById(R.id.tv_xp)

        val plusBut = findViewById<ImageView>(R.id.plusBut)

        plusBut.setOnClickListener {
            val dialog = AddGoalDialog()
            dialog.show(supportFragmentManager, "AddGoalDialog")
        }

        val btnShop = findViewById<Button>(R.id.btnShop)
        btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<ImageView>(R.id.btn_settings)
        settingsButton.setOnClickListener {
            openSettings()
        }

        loadProgress()
        loadGoals()
        loadSelectedEmoji()
        updateLevelUI()
    }

    // ===================== ПРОГРЕСС ИГРОКА =====================
    private fun loadProgress() {
        val json = prefs.getString("player_progress", null)
        if (json != null) {
            try {
                playerProgress = gson.fromJson(json, PlayerProgress::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveProgress() {
        val json = gson.toJson(playerProgress)
        prefs.edit().putString("player_progress", json).apply()
    }

    private fun addCoins(amount: Int) {
        playerProgress.coins += amount
        saveProgress()
    }

    private fun updateLevelUI() {
        tvLevel.text = "Уровень ${playerProgress.level}"
        val current = playerProgress.totalXP
        val needed = playerProgress.getXPForNextLevel()
        val progress = if (needed > 0) (current * 100 / needed) else 0

        levelProgressBar.progress = progress
        tvXp.text = "$current / $needed XP"
    }

    // ===================== ЦЕЛИ =====================
    private fun loadGoals() {
        val json = prefs.getString("goals", null)
        if (json != null) {
            try {
                val type = object : TypeToken<MutableList<Goal>>() {}.type
                val savedGoals: MutableList<Goal> = gson.fromJson(json, type)
                goals.clear()
                goals.addAll(savedGoals)

                goalsContainer.removeAllViews()
                for (goal in goals) {
                    addGoalToUI(goal)
                }
                updateOverallProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveGoals() {
        val json = gson.toJson(goals)
        prefs.edit().putString("goals", json).apply()
    }

    private fun addGoalToUI(goal: Goal) {
        val goalView = LayoutInflater.from(this)
            .inflate(R.layout.item_goal, goalsContainer, false)

        val goalTitle = goalView.findViewById<TextView>(R.id.goalTitle)
        val expandIcon = goalView.findViewById<ImageView>(R.id.expandIcon)
        val tasksContainer = goalView.findViewById<LinearLayout>(R.id.subtasksContainer)
        val goalHeader = goalView.findViewById<LinearLayout>(R.id.goalHeader)
        val progressBar = goalView.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = goalView.findViewById<TextView>(R.id.progressText)
        val btnDelete = goalView.findViewById<ImageView>(R.id.btnDeleteGoal)

        goalTitle.text = goal.title

        @SuppressLint("SetTextI18n")
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

            btnDelete.visibility = if (completed == total && total > 0) View.VISIBLE else View.GONE

            updateOverallProgress()
            saveGoals()
        }

        tasksContainer.removeAllViews()

        for (task in goal.tasks) {
            val taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task_display, tasksContainer, false)

            val taskTitle = taskView.findViewById<TextView>(R.id.taskTitle)
            val subtasksContainer = taskView.findViewById<LinearLayout>(R.id.subtasksContainer)

            taskTitle.text = task.title

            for (subtask in task.subtasks) {
                addSubtaskDisplay(subtasksContainer, subtask) { _ ->
                    updateGoalProgress()
                }
            }

            tasksContainer.addView(taskView)
        }

        updateGoalProgress()

        btnDelete.setOnClickListener {
            if (isGoalFullyCompleted(goal)) {
                showDeleteConfirmation(goal, goalView)
            }
        }

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
    }

    private fun isGoalFullyCompleted(goal: Goal): Boolean {
        for (task in goal.tasks) {
            for (subtask in task.subtasks) {
                if (!subtask.isCompleted) return false
            }
        }
        return goal.tasks.isNotEmpty()
    }

    private fun showDeleteConfirmation(goal: Goal, goalView: View) {
        AlertDialog.Builder(this)
            .setTitle("Удалить цель?")
            .setMessage("Удалить «${goal.title}»?")
            .setPositiveButton("Удалить") { _, _ ->
                goals.remove(goal)
                goalsContainer.removeView(goalView)
                saveGoals()
                updateOverallProgress()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onGoalAdded(goal: Goal) {
        goals.add(goal)
        addGoalToUI(goal)
        saveGoals()
        updateOverallProgress()
    }

    @SuppressLint("SetTextI18n")
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
            val wasCompleted = subtask.isCompleted
            subtask.isCompleted = isChecked
            updateSubtaskStyle(title, isChecked)

            if (isChecked && !wasCompleted) {
                val levelUp = playerProgress.addXP(1)
                addCoins(1)

                if (levelUp) {
                    addCoins(50)
                    Toast.makeText(this, "🎉 Новый уровень ${playerProgress.level}! +50 монет", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "+1 XP +1 💰", Toast.LENGTH_SHORT).show()
                }
                updateLevelUI()
                saveProgress()
            }

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

    private fun loadSelectedEmoji() {
        val json = prefs.getString("shop_data", null)
        val selectedId = if (json != null) {
            try {
                val type = object : TypeToken<ShopData>() {}.type
                val data: ShopData = gson.fromJson(json, type)
                data.selectedId
            } catch (e: Exception) {
                1
            }
        } else 1

        val emoji = when (selectedId) {
            1 -> "🪿"
            2 -> "🐸"
            4 -> "🐱"
            6 -> "🐦"
            8 -> "🐧"
            10 -> "🐭"
            14 -> "💩"
            15 -> "🦖"
            else -> "🪿"
        }
        selectedEmojiView.text = emoji
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadSelectedEmoji()
    }

    override fun onPause() {
        super.onPause()
        saveGoals()
        saveProgress()
    }
}