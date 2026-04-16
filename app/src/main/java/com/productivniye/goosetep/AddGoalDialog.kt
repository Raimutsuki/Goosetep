package com.productivniye.goosetep

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment

interface OnGoalAddedListener {
    fun onGoalAdded(goal: Goal)
}

class AddGoalDialog : DialogFragment() {

    private var listener: OnGoalAddedListener? = null

    private val tasks = mutableListOf<Task>()
    private var nextTaskId = 0
    private var nextSubtaskId = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnGoalAddedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_goal, null)

        val editGoalTitle = view.findViewById<EditText>(R.id.editGoalTitle)
        val tasksContainer = view.findViewById<LinearLayout>(R.id.tasksContainer)
        val buttonAddTask = view.findViewById<Button>(R.id.buttonAddTask)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)

        buttonAddTask.setOnClickListener {
            addTask(tasksContainer)
        }

        // Добавляем первую задачу для примера
        addTask(tasksContainer)

        buttonSave.setOnClickListener {
            val title = editGoalTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название цели!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Обновляем названия задач из полей ввода
            for (i in 0 until tasksContainer.childCount) {
                val taskView = tasksContainer.getChildAt(i)
                val editTitle = taskView.findViewById<EditText>(R.id.editTaskTitle)
                tasks[i].title = editTitle.text.toString()

                val subtasksContainer = taskView.findViewById<LinearLayout>(R.id.subtasksContainer)
                for (j in 0 until subtasksContainer.childCount) {
                    val subtaskView = subtasksContainer.getChildAt(j)
                    val editSubtaskTitle = subtaskView.findViewById<EditText>(R.id.editSubtaskTitle)
                    if (j < tasks[i].subtasks.size) {
                        tasks[i].subtasks[j].title = editSubtaskTitle.text.toString()
                    }
                }
            }

            val goal = Goal(
                id = System.currentTimeMillis().toInt(),
                title = title,
                tasks = tasks.toMutableList()
            )

            listener?.onGoalAdded(goal)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun addTask(container: LinearLayout) {
        val inflater = LayoutInflater.from(requireContext())
        val taskView = inflater.inflate(R.layout.item_task_in_dialog, container, false)

        val editTitle = taskView.findViewById<EditText>(R.id.editTaskTitle)
        val subtasksContainer = taskView.findViewById<LinearLayout>(R.id.subtasksContainer)
        val buttonAddSubtask = taskView.findViewById<Button>(R.id.buttonAddSubtask)  // ← Button, не ImageView
        val buttonRemoveTask = taskView.findViewById<ImageView>(R.id.buttonRemoveTask)

        val taskId = nextTaskId++
        val currentTask = Task(taskId, "")
        tasks.add(currentTask)

        buttonAddSubtask.setOnClickListener {
            addSubtask(subtasksContainer, currentTask)
        }

        buttonRemoveTask.setOnClickListener {
            container.removeView(taskView)
            tasks.remove(currentTask)
        }

        addSubtask(subtasksContainer, currentTask)
        container.addView(taskView)
    }

    private fun addSubtask(container: LinearLayout, task: Task) {
        val inflater = LayoutInflater.from(requireContext())
        val subtaskView = inflater.inflate(R.layout.item_subtask_in_dialog, container, false)

        val editTitle = subtaskView.findViewById<EditText>(R.id.editSubtaskTitle)
        val buttonRemove = subtaskView.findViewById<ImageView>(R.id.buttonRemoveSubtask)

        val subtaskId = nextSubtaskId++
        val currentSubtask = Subtask(subtaskId, "")
        task.subtasks.add(currentSubtask)

        editTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                currentSubtask.title = editTitle.text.toString()
            }
        }

        buttonRemove.setOnClickListener {
            container.removeView(subtaskView)
            task.subtasks.remove(currentSubtask)
        }

        container.addView(subtaskView)
    }
}