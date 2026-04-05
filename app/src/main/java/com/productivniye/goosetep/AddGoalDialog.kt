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
    private val subtasks = mutableListOf<Subtask>()
    private var nextSubtaskId = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnGoalAddedListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_add_goal, null)

        val editGoalTitle = view.findViewById<EditText>(R.id.editGoalTitle)
        val subtasksContainer = view.findViewById<LinearLayout>(R.id.subtasksContainer)
        val buttonAddSubtask = view.findViewById<Button>(R.id.buttonAddSubtask)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)

        // Добавление подзадачи
        buttonAddSubtask.setOnClickListener {
            addSubtask(subtasksContainer)
        }

        // Добавляем первую подзадачу для примера
        addSubtask(subtasksContainer)

        // Сохранение
        buttonSave.setOnClickListener {
            val title = editGoalTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название цели!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = Goal(
                id = System.currentTimeMillis().toInt(),
                title = title,
                subtasks = subtasks.toMutableList()
            )

            listener?.onGoalAdded(goal)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun addSubtask(container: LinearLayout) {
        val subtaskView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_subtask_in_dialog, container, false)

        val editTitle = subtaskView.findViewById<EditText>(R.id.editSubtaskTitle)
        val buttonRemove = subtaskView.findViewById<ImageView>(R.id.buttonRemoveSubtask)

        val subtaskId = nextSubtaskId++
        val currentSubtask = Subtask(subtaskId, "")
        subtasks.add(currentSubtask)

        editTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                currentSubtask.title = editTitle.text.toString()
            }
        }

        buttonRemove.setOnClickListener {
            container.removeView(subtaskView)
            subtasks.remove(currentSubtask)
        }

        container.addView(subtaskView)
    }
}