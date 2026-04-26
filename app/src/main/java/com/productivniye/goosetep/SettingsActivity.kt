package com.productivniye.goosetep

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Настраиваем все секции
        setupExpandableSection(
            findViewById(R.id.headerAccount),
            findViewById(R.id.expandableAccount),
            findViewById(R.id.arrowAccount)
        )

        setupExpandableSection(
            findViewById(R.id.headerTheme),
            findViewById(R.id.expandableTheme),
            findViewById(R.id.arrowTheme)
        )

        setupExpandableSection(
            findViewById(R.id.headerNotifications),
            findViewById(R.id.expandableNotifications),
            findViewById(R.id.arrowNotifications)
        )

        setupExpandableSection(
            findViewById(R.id.headerLanguage),
            findViewById(R.id.expandableLanguage),
            findViewById(R.id.arrowLanguage)
        )

        // Переключатель светлой темы
        val switchLightTheme = findViewById<Switch>(R.id.switchLightTheme)
        switchLightTheme.isChecked = prefs.getBoolean("light_theme", false)
        switchLightTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("light_theme", isChecked).apply()

            // Применяем тему
            val mode = if (isChecked)
                AppCompatDelegate.MODE_NIGHT_NO
            else
                AppCompatDelegate.MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Переключатель звука
        val switchSound = findViewById<Switch>(R.id.switchSound)
        switchSound.isChecked = prefs.getBoolean("sound", true)
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound", isChecked).apply()
        }

        // Переключатель вибросигнала
        val switchVibration = findViewById<Switch>(R.id.switchVibration)
        switchVibration.isChecked = prefs.getBoolean("vibration", false)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration", isChecked).apply()
        }

        // Кнопка шестерёнки (закрыть настройки)
        findViewById<ImageView>(R.id.ivSettingsIcon).setOnClickListener {
            finish()
        }

        // Внутри onCreate(), после настройки секций:

// 🔹 Загружаем сохранённый email
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val savedEmail = prefs.getString("account_email", "user@example.com")
        findViewById<TextView>(R.id.tvAccountEmail).text = savedEmail

// 🔹 Обработчик кнопки редактирования
        findViewById<ImageView>(R.id.btnEditAccount).setOnClickListener {
            showEditAccountDialog()
        }

        // 🔹 Функция показа диалога

    }

    private fun showEditAccountDialog() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currentEmail = prefs.getString("account_email", "user@example.com")

        // Поле ввода
        val input = EditText(this).apply {
            hint = "email@example.com"
            setText(currentEmail)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48, 16, 48, 16)
        }

        // Диалог
        AlertDialog.Builder(this)
            .setTitle("Редактировать аккаунт")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newEmail = input.text.toString().trim()
                if (newEmail.isNotEmpty()) {
                    // Сохраняем и обновляем UI
                    prefs.edit().putString("account_email", newEmail).apply()
                    findViewById<TextView>(R.id.tvAccountEmail).text = newEmail
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    // Универсальная функция для раскрываемых секций
    private fun setupExpandableSection(
        header: LinearLayout,
        expandable: LinearLayout,
        arrow: ImageView
    ) {
        header.setOnClickListener {
            if (expandable.visibility == View.VISIBLE) {
                expandable.visibility = View.GONE
                arrow.rotation = 0f
            } else {
                expandable.visibility = View.VISIBLE
                arrow.rotation = 180f
            }
        }
    }
}