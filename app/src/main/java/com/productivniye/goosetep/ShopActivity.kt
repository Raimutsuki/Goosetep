package com.productivniye.goosetep   // ← ИСПРАВИЛ ЗДЕСЬ

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View

// Модель
data class ShopItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val price: Int,
    var isOwned: Boolean = false
)

class ShopActivity : AppCompatActivity() {

    private var coins = 100
    private val myItems = mutableListOf<ShopItem>()
    private val shopItems = mutableListOf<ShopItem>()

    private lateinit var shopAdapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        findViewById<TextView>(R.id.tv_coins).text = coins.toString()


        // === ТОВАРЫ ===
        shopItems.add(ShopItem(1, "Гусь", "🪿", 0, true))
        shopItems.add(ShopItem(2, "Фиолетовый монстр", "🐸", 50))
        shopItems.add(ShopItem(4, "Серый кот", "🐱", 30))
        shopItems.add(ShopItem(6, "Синяя птица", "🐦", 25))
        shopItems.add(ShopItem(7, "Лиса", "🦊", 45))
        shopItems.add(ShopItem(8, "Пингвин", "🐧", 35))
        shopItems.add(ShopItem(10, "Мышь", "🐭", 15))
        shopItems.add(ShopItem(12, "Зелёный слайм", "🟢", 60))
        shopItems.add(ShopItem(14, "Какашка", "💩", 5))
        shopItems.add(ShopItem(15, "Пиксельный динозавр", "🦖", 70))

        myItems.addAll(shopItems.filter { it.isOwned })
        shopItems.removeAll { it.isOwned }

        shopAdapter = ShopAdapter(shopItems) { item ->
            if (coins >= item.price) {
                showBuyDialog(item)
            } else {
                Toast.makeText(this, "Не хватает коинов!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<RecyclerView>(R.id.rv_shop_items).adapter = shopAdapter

        // Кнопка "Назад"
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        updateMyItemsText()
    }

    private fun showBuyDialog(item: ShopItem) {
        AlertDialog.Builder(this)
            .setTitle("Купить предмет?")
            .setMessage("Купить «${item.name}» ${item.emoji} за ${item.price} 💰?")
            .setPositiveButton("Купить") { _, _ ->
                coins -= item.price
                item.isOwned = true
                myItems.add(item)
                shopItems.remove(item)

                shopAdapter.notifyDataSetChanged()
                findViewById<TextView>(R.id.tv_coins).text = coins.toString()

                Toast.makeText(this, "✅ Куплено!", Toast.LENGTH_SHORT).show()
                updateMyItemsText()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateMyItemsText() {
        val text = myItems.joinToString("\n") { "${it.emoji} ${it.name}" }
        findViewById<TextView>(R.id.tv_my_items).text =
            if (text.isEmpty()) "Пока ничего нет" else text
    }
}

// ===================== АДАПТЕР =====================
class ShopAdapter(
    private val items: MutableList<ShopItem>,
    private val onClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tv_emoji)
        val name: TextView = view.findViewById(R.id.tv_name)
        val price: TextView = view.findViewById(R.id.tv_price)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.emoji.text = item.emoji
        holder.name.text = item.name
        holder.price.text = "${item.price} 💰"

        holder.itemView.setOnClickListener { onClick(item) }
    }
}