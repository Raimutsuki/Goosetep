package com.productivniye.goosetep

// Подзадача
data class Subtask(
    val id: Int,
    var title: String,
    var isCompleted: Boolean = false
)

// Цель (содержит список подзадач)
data class Goal(
    val id: Int,
    var title: String,
    var subtasks: MutableList<Subtask> = mutableListOf()
)