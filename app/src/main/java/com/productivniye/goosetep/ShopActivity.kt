package com.productivniye.goosetep

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class ShopItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val price: Int,
    var isOwned: Boolean = false,
    var isSelected: Boolean = false
)

class ShopActivity : AppCompatActivity() {

    private var coins = 100
    private val myItems = mutableListOf<ShopItem>()
    private val shopItems = mutableListOf<ShopItem>()

    private lateinit var myAdapter: MyItemsAdapter
    private lateinit var shopAdapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        findViewById<TextView>(R.id.tv_coins).text = coins.toString()

        // === ТОВАРЫ ===
        shopItems.add(ShopItem(1, "Гусь", "🪿", 0, true, true)) // выбран по умолчанию
        shopItems.add(ShopItem(2, "Жабка", "🐸", 50))
        shopItems.add(ShopItem(4, "Кот", "🐱", 30))
        shopItems.add(ShopItem(6, "Птичка", "🐦", 25))
        shopItems.add(ShopItem(8, "Пингвин", "🐧", 35))
        shopItems.add(ShopItem(10, "Мышь", "🐭", 15))
        shopItems.add(ShopItem(14, "Какашка", "💩", 5))
        shopItems.add(ShopItem(15, "Динозавр", "🦖", 70))

        myItems.addAll(shopItems.filter { it.isOwned })
        shopItems.removeAll { it.isOwned }

        // Адаптеры
        myAdapter = MyItemsAdapter(myItems) { selected ->
            // Снимаем выбор со всех предметов
            myItems.forEach { it.isSelected = false }
            selected.isSelected = true
            myAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Выбран: ${selected.name}", Toast.LENGTH_SHORT).show()
        }

        shopAdapter = ShopAdapter(shopItems) { item ->
            if (coins >= item.price) {
                showBuyDialog(item)
            } else {
                Toast.makeText(this, "Не хватает коинов!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<RecyclerView>(R.id.rv_my_items).apply {
            layoutManager = GridLayoutManager(this@ShopActivity, 2)
            adapter = myAdapter
        }

        findViewById<RecyclerView>(R.id.rv_shop_items).adapter = shopAdapter

        // Кнопка назад
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun showBuyDialog(item: ShopItem) {
        AlertDialog.Builder(this)
            .setTitle("Купить предмет?")
            .setMessage("Купить «${item.name}» ${item.emoji} за ${item.price} 💰?")
            .setPositiveButton("Купить") { _, _ ->
                coins -= item.price
                item.isOwned = true

                // Снимаем выбор с предыдущих и выбираем новый
                myItems.forEach { it.isSelected = false }
                item.isSelected = true

                myItems.add(item)
                shopItems.remove(item)

                myAdapter.notifyDataSetChanged()
                shopAdapter.notifyDataSetChanged()

                findViewById<TextView>(R.id.tv_coins).text = coins.toString()
                Toast.makeText(this, "✅ Куплено и выбрано!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

// ===================== АДАПТЕР "МОИ ПРЕДМЕТЫ" =====================
class MyItemsAdapter(
    private val items: MutableList<ShopItem>,
    private val onSelect: (ShopItem) -> Unit
) : RecyclerView.Adapter<MyItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tv_emoji)
        val name: TextView = view.findViewById(R.id.tv_name)
        val status: TextView = view.findViewById(R.id.tv_price)
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
        holder.status.text = if (item.isSelected) "✓ Выбран" else ""

        // Цвет плитки
        holder.itemView.setBackgroundColor(
            if (item.isSelected) 0xFF6200EE.toInt() else 0xFF424242.toInt()
        )

        // ←←← ЭТО БЫЛО ПРОПУЩЕНО
        holder.itemView.setOnClickListener {
            onSelect(item)
        }
    }
}

// ===================== АДАПТЕР МАГАЗИНА =====================
class ShopAdapter(
    private val items: MutableList<ShopItem>,
    private val onClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        holder.itemView.setBackgroundColor(0xFF424242.toInt()) // тёмный цвет
        holder.itemView.setOnClickListener { onClick(item) }
    }
}