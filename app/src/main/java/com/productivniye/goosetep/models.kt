package com.productivniye.goosetep

// Подзадача (самый нижний уровень)
data class Subtask(
    val id: Int,
    var title: String,
    var isCompleted: Boolean = false,
    var completionRewardGiven: Boolean = false
)

// Задача (содержит список подзадач)
data class Task(
    val id: Int,
    var title: String,
    var subtasks: MutableList<Subtask> = mutableListOf(),
    var isExpanded: Boolean = true,  // для сворачивания/разворачивания
    var completionRewardGiven: Boolean = false
)

// Цель (содержит список задач)
data class Goal(
    val id: Int,
    var title: String,
    var tasks: MutableList<Task> = mutableListOf(),
    var subtasks: MutableList<Subtask> = mutableListOf(),
    var isExpanded: Boolean = true,
    var completionRewardGiven: Boolean = false

)

// ===================== ДАННЫЕ МАГАЗИНА =====================
data class ShopData(
    val coins: Int = 100,
    val ownedIds: MutableSet<Int> = mutableSetOf(),
    val selectedId: Int = 1
)

// ===================== ПРОГРЕСС ИГРОКА =====================
data class PlayerProgress(
    var totalXP: Int = 0,
    var level: Int = 1,
    var coins: Int = 100
) {
    fun getXPForNextLevel(): Int = 50 * level + 50

    fun addXP(amount: Int): Boolean {
        totalXP += amount
        val needed = getXPForNextLevel()

        return if (totalXP >= needed) {
            totalXP -= needed
            level++
            true
        } else false
    }
}