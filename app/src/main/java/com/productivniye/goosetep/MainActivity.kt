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

        goalsContainer = findViewById(R.id.goalsContainer)
        overallProgressBar = findViewById(R.id.overallProgressBar)
        overallProgressText = findViewById(R.id.overallProgressText)
        selectedEmojiView = findViewById(R.id.tv_selected_emoji)
        levelProgressBar = findViewById(R.id.levelProgressBar)
        tvLevel = findViewById(R.id.tv_level)
        tvXp = findViewById(R.id.tv_xp)

        findViewById<ImageView>(R.id.plusBut).setOnClickListener {
            AddGoalDialog().show(supportFragmentManager, "AddGoalDialog")
        }

        findViewById<Button>(R.id.btnShop).setOnClickListener {
            startActivity(Intent(this, ShopActivity::class.java))
        }

        findViewById<ImageView>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadProgress()
        loadGoals()
        loadSelectedEmoji()
        updateLevelUI()
    }

    // ===================== ПРОГРЕСС =====================
    private fun loadProgress() {
        prefs.getString("player_progress", null)?.let {
            try { playerProgress = gson.fromJson(it, PlayerProgress::class.java) } catch (_: Exception) {}
        }
    }

    private fun saveProgress() {
        prefs.edit().putString("player_progress", gson.toJson(playerProgress)).apply()
    }

    private fun addCoins(amount: Int) {
        playerProgress.coins += amount
        saveProgress()
    }

    private fun updateLevelUI() {
        tvLevel.text = "Уровень ${playerProgress.level}"
        val needed = playerProgress.getXPForNextLevel()
        val progress = if (needed > 0) (playerProgress.totalXP * 100 / needed) else 0
        levelProgressBar.progress = progress
        tvXp.text = "${playerProgress.totalXP} / $needed XP"
    }

    // ===================== НАГРАДЫ =====================
    private fun giveSubtaskReward(subtask: Subtask) {
        if (subtask.completionRewardGiven) return
        subtask.completionRewardGiven = true

        val levelUp = playerProgress.addXP(1)
        Toast.makeText(this, "+1 XP", Toast.LENGTH_SHORT).show()

        if (levelUp) {
            addCoins(50)
            Toast.makeText(this, "🎉 Новый уровень ${playerProgress.level}! 50 💰", Toast.LENGTH_LONG).show()
        }
        updateLevelUI()
        saveProgress()
    }

    private fun giveTaskReward(task: Task) {
        if (task.completionRewardGiven) return
        task.completionRewardGiven = true

        val levelUp = playerProgress.addXP(10)
        Toast.makeText(this, "+10 XP", Toast.LENGTH_SHORT).show()

        if (levelUp) {
            addCoins(50)
            Toast.makeText(this, "🎉 Новый уровень ${playerProgress.level}! 50 💰", Toast.LENGTH_LONG).show()
        }
        updateLevelUI()
        saveProgress()
    }

    private fun giveGoalReward(goal: Goal) {
        if (goal.completionRewardGiven) return
        goal.completionRewardGiven = true

        addCoins(5)
        val levelUp = playerProgress.addXP(25)

        Toast.makeText(this, "🏆 Цель выполнена! +25 XP", Toast.LENGTH_LONG).show()
        Toast.makeText(this, "5 💰", Toast.LENGTH_SHORT).show()

        if (levelUp) {
            addCoins(50)
            Toast.makeText(this, "🎉 Новый уровень ${playerProgress.level}! 50 💰", Toast.LENGTH_LONG).show()
        }
        updateLevelUI()
        saveProgress()
    }

    private fun loadGoals() {
        prefs.getString("goals", null)?.let {
            try {
                val type = object : TypeToken<MutableList<Goal>>() {}.type
                val saved: MutableList<Goal> = gson.fromJson(it, type)
                goals.clear()
                goals.addAll(saved)
            } catch (_: Exception) {}
        }

        goalsContainer.removeAllViews()
        goals.forEach { addGoalToUI(it) }
        updateOverallProgress()
    }

    private fun saveGoals() {
        prefs.edit().putString("goals", gson.toJson(goals)).apply()
    }

    private fun addGoalToUI(goal: Goal) {
        val goalView = LayoutInflater.from(this).inflate(R.layout.item_goal, goalsContainer, false)

        val goalTitle = goalView.findViewById<TextView>(R.id.goalTitle)
        val expandIcon = goalView.findViewById<ImageView>(R.id.expandIcon)
        val tasksContainer = goalView.findViewById<LinearLayout>(R.id.subtasksContainer)
        val goalHeader = goalView.findViewById<LinearLayout>(R.id.goalHeader)
        val progressBar = goalView.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = goalView.findViewById<TextView>(R.id.progressText)
        val btnDelete = goalView.findViewById<ImageView>(R.id.btnDeleteGoal)

        goalTitle.text = goal.title

        fun updateGoalProgress() {
            var total = 0
            var completed = 0

            for (task in goal.tasks) {
                var taskTotal = 0
                var taskCompleted = 0

                for (subtask in task.subtasks) {
                    taskTotal++
                    total++
                    if (subtask.isCompleted) {
                        taskCompleted++
                        completed++
                    }
                }

                if (taskTotal > 0 && taskCompleted == taskTotal) {
                    giveTaskReward(task)
                }
            }

            val progress = if (total > 0) (completed * 100 / total) else 0
            animateProgress(progressBar, progress)
            progressText.text = "$completed/$total"

            btnDelete.visibility = if (completed == total && total > 0) View.VISIBLE else View.GONE

            if (completed == total && total > 0) {
                giveGoalReward(goal)
            }

            updateOverallProgress()
            saveGoals()
        }

        tasksContainer.removeAllViews()

        for (task in goal.tasks) {
            val taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task_display, tasksContainer, false)

            taskView.findViewById<TextView>(R.id.taskTitle).text = task.title

            val subtasksContainer = taskView.findViewById<LinearLayout>(R.id.subtasksContainer)
            for (subtask in task.subtasks) {
                addSubtaskDisplay(subtasksContainer, subtask) {
                    updateGoalProgress()
                }
            }
            tasksContainer.addView(taskView)
        }

        updateGoalProgress()

        btnDelete.setOnClickListener {
            if (isGoalFullyCompleted(goal)) showDeleteConfirmation(goal, goalView)
        }

        goalHeader.setOnClickListener {
            goal.isExpanded = !goal.isExpanded
            tasksContainer.visibility = if (goal.isExpanded) View.VISIBLE else View.GONE
            expandIcon.animate().rotation(if (goal.isExpanded) 0f else -90f).setDuration(200).start()
        }

        tasksContainer.visibility = if (goal.isExpanded) View.VISIBLE else View.GONE
        goalsContainer.addView(goalView)
    }

    private fun isGoalFullyCompleted(goal: Goal): Boolean {
        return goal.tasks.all { task -> task.subtasks.all { it.isCompleted } } && goal.tasks.isNotEmpty()
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

    private fun updateOverallProgress() {
        var total = 0
        var completed = 0
        for (goal in goals) {
            for (task in goal.tasks) {
                for (sub in task.subtasks) {
                    total++
                    if (sub.isCompleted) completed++
                }
            }
        }
        val progress = if (total > 0) (completed * 100 / total) else 0
        animateProgress(overallProgressBar, progress)
        overallProgressText.text = "$completed/$total"
    }

    private fun addSubtaskDisplay(
        container: LinearLayout,
        subtask: Subtask,
        onCheckedChangeListener: ((Boolean) -> Unit)? = null
    ) {
        val subtaskView = LayoutInflater.from(this)
            .inflate(R.layout.item_subtask_display, container, false)

        val checkbox = subtaskView.findViewById<CheckBox>(R.id.subtaskCheckbox)
        val title = subtaskView.findViewById<TextView>(R.id.subtaskTitle)

        checkbox.isChecked = subtask.isCompleted
        title.text = subtask.title
        updateSubtaskStyle(title, subtask.isCompleted)

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            val wasCompleted = subtask.isCompleted
            subtask.isCompleted = isChecked
            updateSubtaskStyle(title, isChecked)

            if (isChecked && !wasCompleted && !subtask.completionRewardGiven) {
                giveSubtaskReward(subtask)
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
                gson.fromJson<ShopData>(json, type).selectedId
            } catch (_: Exception) { 1 }
        } else 1

        selectedEmojiView.text = when (selectedId) {
            1 -> "🪿"; 2 -> "🐸"; 4 -> "🐱"; 6 -> "🐦"
            8 -> "🐧"; 10 -> "🐭"; 14 -> "💩"; 15 -> "🦖"
            else -> "🪿"
        }
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