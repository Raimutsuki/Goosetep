package com.productivniye.goosetep

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE) // теперь один prefs

        loadShopState()

        findViewById<TextView>(R.id.tv_coins).text = coins.toString()

        initAllItems()

        myAdapter = MyItemsAdapter(myItems) { selected ->
            selectItem(selected)
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

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initAllItems() {
        val allItems = listOf(
            ShopItem(1, "Гусь", "🪿", 0, isOwned = true),
            ShopItem(2, "Жабка", "🐸", 50),
            ShopItem(4, "Кот", "🐱", 30),
            ShopItem(6, "Птичка", "🐦", 25),
            ShopItem(8, "Пингвин", "🐧", 35),
            ShopItem(10, "Мышь", "🐭", 15),
            ShopItem(14, "Какашка", "💩", 5),
            ShopItem(15, "Динозавр", "🦖", 70)
        )

        myItems.clear()
        shopItems.clear()

        val savedData = loadSavedShopData()

        for (item in allItems) {
            val isOwned = item.id == 1 || savedData.ownedIds.contains(item.id)
            if (isOwned) {
                item.isOwned = true
                item.isSelected = item.id == savedData.selectedId
                myItems.add(item)
            } else {
                shopItems.add(item)
            }
        }

        if (myItems.none { it.isSelected }) {
            myItems.find { it.id == 1 }?.isSelected = true
        }
    }

    private fun selectItem(selected: ShopItem) {
        myItems.forEach { it.isSelected = false }
        selected.isSelected = true
        myAdapter.notifyDataSetChanged()

        saveShopState()
        Toast.makeText(this, "Выбран: ${selected.name}", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showBuyDialog(item: ShopItem) {
        AlertDialog.Builder(this)
            .setTitle("Купить предмет?")
            .setMessage("Купить «${item.name}» ${item.emoji} за ${item.price} 💰?")
            .setPositiveButton("Купить") { _, _ ->
                coins -= item.price
                item.isOwned = true

                myItems.forEach { it.isSelected = false }
                item.isSelected = true

                myItems.add(item)
                shopItems.remove(item)

                myAdapter.notifyDataSetChanged()
                shopAdapter.notifyDataSetChanged()

                findViewById<TextView>(R.id.tv_coins).text = coins.toString()

                saveShopState()
                Toast.makeText(this, "✅ Куплено и выбрано!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ===================== JSON СОХРАНЕНИЕ =====================
    private fun loadSavedShopData(): ShopData {
        val json = prefs.getString("shop_data", null)
        return if (json != null) {
            val type = object : TypeToken<ShopData>() {}.type
            gson.fromJson(json, type)
        } else {
            ShopData(coins = 100, selectedId = 1)
        }
    }

    private fun loadShopState() {
        val data = loadSavedShopData()
        coins = data.coins
    }

    private fun saveShopState() {
        val ownedIds = myItems.map { it.id }.toMutableSet()
        val selectedId = myItems.find { it.isSelected }?.id ?: 1

        val shopData = ShopData(
            coins = coins,
            ownedIds = ownedIds,
            selectedId = selectedId
        )

        val json = gson.toJson(shopData)
        prefs.edit().putString("shop_data", json).apply()
    }

    override fun onPause() {
        super.onPause()
        saveShopState()
    }
}

// Адаптеры остаются без изменений
class MyItemsAdapter(
    private val items: MutableList<ShopItem>,
    private val onSelect: (ShopItem) -> Unit
) : RecyclerView.Adapter<MyItemsAdapter.ViewHolder>() {
    // ... (тот же код что был раньше)
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

        holder.itemView.setBackgroundColor(
            if (item.isSelected) 0xFF6200EE.toInt() else 0xFF424242.toInt()
        )

        holder.itemView.setOnClickListener { onSelect(item) }
    }
}

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.emoji.text = item.emoji
        holder.name.text = item.name
        holder.price.text = "${item.price} 💰"

        holder.itemView.setBackgroundColor(0xFF424242.toInt())
        holder.itemView.setOnClickListener { onClick(item) }
    }
}