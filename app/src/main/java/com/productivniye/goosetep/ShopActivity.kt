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

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private var coins = 100
    private val myItems = mutableListOf<ShopItem>()
    private val shopItems = mutableListOf<ShopItem>()

    private lateinit var myAdapter: MyItemsAdapter
    private lateinit var shopAdapter: ShopAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        loadCoins()
        loadShopData()

        findViewById<TextView>(R.id.tv_coins).text = coins.toString()

        // === ТОВАРЫ ===
        shopItems.add(ShopItem(1, "Гусь", "🪿", 0, isOwned = true, isSelected = true))
        shopItems.add(ShopItem(2, "Жабка", "🐸", 50))
        shopItems.add(ShopItem(4, "Кот", "🐱", 30))
        shopItems.add(ShopItem(6, "Птичка", "🐦", 25))
        shopItems.add(ShopItem(8, "Пингвин", "🐧", 35))
        shopItems.add(ShopItem(10, "Мышь", "🐭", 15))
        shopItems.add(ShopItem(14, "Какашка", "💩", 5))
        shopItems.add(ShopItem(15, "Динозавр", "🦖", 70))

        // Применяем сохранённые owned и selected
        val savedOwnedIds = getOwnedIds()
        val savedSelectedId = getSelectedId()

        shopItems.forEach { item ->
            if (savedOwnedIds.contains(item.id)) {
                item.isOwned = true
                item.isSelected = (item.id == savedSelectedId)
            }
        }

        // Распределяем по спискам
        myItems.clear()
        myItems.addAll(shopItems.filter { it.isOwned })

        shopItems.removeAll { it.isOwned }

        // Если ничего не выбрано — выбираем первого из моих
        if (myItems.none { it.isSelected }) {
            myItems.firstOrNull()?.isSelected = true
        }

        // Адаптеры
        myAdapter = MyItemsAdapter(myItems) { selected ->
            myItems.forEach { it.isSelected = false }
            selected.isSelected = true
            myAdapter.notifyDataSetChanged()
            saveShopData()
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

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadCoins() {
        val json = prefs.getString("player_progress", null)
        if (json != null) {
            try {
                val progress = gson.fromJson(json, PlayerProgress::class.java)
                coins = progress.coins
            } catch (_: Exception) {}
        }
    }

    private fun saveCoins() {
        val json = prefs.getString("player_progress", null)
        val progress = if (json != null) {
            try {
                gson.fromJson(json, PlayerProgress::class.java) ?: PlayerProgress()
            } catch (_: Exception) {
                PlayerProgress()
            }
        } else PlayerProgress()

        progress.coins = coins
        prefs.edit().putString("player_progress", gson.toJson(progress)).apply()
    }

    private fun getOwnedIds(): Set<Int> {
        val json = prefs.getString("shop_data", null) ?: return setOf(1)
        return try {
            val type = object : TypeToken<ShopData>() {}.type
            val data: ShopData = gson.fromJson(json, type)
            data.ownedIds
        } catch (_: Exception) { setOf(1) }
    }

    private fun getSelectedId(): Int {
        val json = prefs.getString("shop_data", null) ?: return 1
        return try {
            val type = object : TypeToken<ShopData>() {}.type
            val data: ShopData = gson.fromJson(json, type)
            data.selectedId
        } catch (_: Exception) { 1 }
    }

    private fun loadShopData() {
        // Загрузка происходит через getOwnedIds() и getSelectedId()
    }

    private fun saveShopData() {
        val ownedIds = myItems.map { it.id }.toMutableSet()
        val selectedId = myItems.find { it.isSelected }?.id ?: 1

        val shopData = ShopData(coins = coins, ownedIds = ownedIds, selectedId = selectedId)
        prefs.edit().putString("shop_data", gson.toJson(shopData)).apply()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showBuyDialog(item: ShopItem) {
        AlertDialog.Builder(this)
            .setTitle("Купить предмет?")
            .setMessage("Купить «${item.name}» ${item.emoji} за ${item.price} 💰?")
            .setPositiveButton("Купить") { _, _ ->
                coins -= item.price
                item.isOwned = true

                // Сбрасываем выбор у всех
                myItems.forEach { it.isSelected = false }
                item.isSelected = true

                myItems.add(item)
                shopItems.remove(item)

                myAdapter.notifyDataSetChanged()
                shopAdapter.notifyDataSetChanged()

                findViewById<TextView>(R.id.tv_coins).text = coins.toString()

                saveCoins()
                saveShopData()

                Toast.makeText(this, "✅ Куплено и выбрано!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        saveCoins()
        saveShopData()
    }

    // ===================== АДАПТЕРЫ =====================
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
}