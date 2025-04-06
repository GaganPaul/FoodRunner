package com.kartikey.foodrunner.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.CartAdapter
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.CartItems
import com.kartikey.foodrunner.utils.ConnectionManager

class CartActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: CartAdapter
    lateinit var restaurantId: String
    lateinit var restaurantName: String
    lateinit var txtRestaurantName: TextView
    lateinit var btnPlaceOrder: Button
    lateinit var selectedItemsId: ArrayList<String>
    lateinit var progressLayout: RelativeLayout

    var cartListItems = arrayListOf<CartItems>()
    var totalCost = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        restaurantId = intent.getStringExtra("restaurantId").toString()
        restaurantName = intent.getStringExtra("restaurantName").toString()
        selectedItemsId = intent.getStringArrayListExtra("selectedItemsId") as ArrayList<String>

        toolbar = findViewById(R.id.toolBar)
        txtRestaurantName = findViewById(R.id.txtRestaurantName)
        txtRestaurantName.text = restaurantName
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        progressLayout = findViewById(R.id.progressLayout)

        btnPlaceOrder.setOnClickListener {
            val sharedPreferences = getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences.getString("user_id", "0")!!
            
            progressLayout.visibility = View.VISIBLE
            
            if (ConnectionManager().checkConnectivity(this)) {
                FirebaseHelper.placeOrder(
                    userId, 
                    restaurantId, 
                    restaurantName, 
                    totalCost.toString(), 
                    cartListItems
                ) { success ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "Order Placed",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        createNotification()
                        
                        val intent = Intent(this, OrderPlacedActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    progressLayout.visibility = View.GONE
                }
            } else {
                val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                alterDialog.setTitle("No Internet")
                alterDialog.setMessage("Internet Connection can't be established!")
                alterDialog.setPositiveButton("Open Settings")
                { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(settingsIntent)
                }
                alterDialog.setNegativeButton("Exit")
                { _, _ ->
                    finishAffinity()
                }
                alterDialog.setCancelable(false)
                alterDialog.create()
                alterDialog.show()
            }
        }
        
        setToolBar()
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewCart)
        
        fetchData()
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(this)) {
            progressLayout.visibility = View.VISIBLE
            
            val sharedPreferences = getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences.getString("user_id", "0")!!
            
            FirebaseHelper.getRestaurantMenu(restaurantId) { menuItems ->
                if (menuItems.isNotEmpty()) {
                    cartListItems.clear()
                    totalCost = 0
                    
                    for (item in menuItems) {
                        if (selectedItemsId.contains(item.id)) {
                            val cartItem = CartItems(
                                item.id,
                                item.name,
                                item.cost_for_one,
                                restaurantId
                            )
                            
                            this.totalCost += item.cost_for_one.toInt()
                            cartListItems.add(cartItem)
                            
                            // Add item to Firebase cart
                            FirebaseHelper.addItemToCart(userId, cartItem) { _ -> }
                        }
                    }
                    
                    menuAdapter = CartAdapter(this, cartListItems)
                    recyclerView.adapter = menuAdapter
                    recyclerView.layoutManager = layoutManager
                    
                    btnPlaceOrder.text = "Place Order (Total Cost: Rs. $totalCost)"
                }
                
                progressLayout.visibility = View.GONE
            }
        } else {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_back_arrow)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun createNotification() {
        val notificationId = 1
        val channelId = "personal_notification"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Order Confirmation"
            val description = "Order placed successfully!"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_correct)
            .setContentTitle("Order Placed")
            .setContentText("Your order has been successfully placed!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(notificationId, builder)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}