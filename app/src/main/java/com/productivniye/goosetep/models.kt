package com.productivniye.goosetep

// Подзадача (самый нижний уровень)
data class Subtask(
    val id: Int,
    var title: String,
    var isCompleted: Boolean = false
)

// Задача (содержит список подзадач)
data class Task(
    val id: Int,
    var title: String,
    var subtasks: MutableList<Subtask> = mutableListOf(),
    var isExpanded: Boolean = true  // для сворачивания/разворачивания
)

// Цель (содержит список задач)
data class Goal(
    val id: Int,
    var title: String,
    var tasks: MutableList<Task> = mutableListOf(),
    var subtasks: MutableList<Subtask> = mutableListOf(),
    var isExpanded: Boolean = true
)