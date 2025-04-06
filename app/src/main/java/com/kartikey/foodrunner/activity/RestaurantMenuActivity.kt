package com.kartikey.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.RestaurantMenuAdapter
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.RestaurantMenu
import com.kartikey.foodrunner.utils.ConnectionManager

class RestaurantMenuActivity : AppCompatActivity() {

    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: RestaurantMenuAdapter
    lateinit var restaurantId: String
    lateinit var restaurantName: String
    lateinit var proceedToCartLayout: RelativeLayout
    lateinit var btnProceedToCart: Button
    lateinit var restaurantMenuProgressDialog: RelativeLayout

    var restaurantMenuList = arrayListOf<RestaurantMenu>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        proceedToCartLayout = findViewById(R.id.rlProceedToCart)
        btnProceedToCart = findViewById(R.id.btnProceedToCart)
        restaurantMenuProgressDialog =
            findViewById(R.id.restaurantMenuProgressDialog)

        toolbar = findViewById(R.id.toolBar)

        restaurantId = intent.getStringExtra("restaurantId").toString()
        restaurantName = intent.getStringExtra("restaurantName").toString()

        setToolBar()
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewRestaurantMenu)
        
        fetchData()
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(this)) {
            restaurantMenuProgressDialog.visibility = View.VISIBLE
            
            FirebaseHelper.getRestaurantMenu(restaurantId) { menuItems ->
                if (menuItems.isNotEmpty()) {
                    restaurantMenuList.clear()
                    restaurantMenuList.addAll(menuItems)
                    
                    menuAdapter = RestaurantMenuAdapter(
                        this,
                        restaurantId,
                        restaurantName,
                        proceedToCartLayout,
                        btnProceedToCart,
                        restaurantMenuList
                    )
                    
                    recyclerView.adapter = menuAdapter
                    recyclerView.layoutManager = layoutManager
                } else {
                    Toast.makeText(
                        this,
                        "No menu items available!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                restaurantMenuProgressDialog.visibility = View.GONE
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
        supportActionBar?.title = restaurantName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_back_arrow)
    }

    override fun onBackPressed() {
        if (menuAdapter.getSelectedItemCount() > 0) {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay")
            { _, _ ->
                super.onBackPressed()
            }
            alterDialog.setNegativeButton("No")
            { _, _ ->
                //do nothing
            }
            alterDialog.show()
        } else {
            super.onBackPressed()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                if (menuAdapter.getSelectedItemCount() > 0) {
                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will remove everything from cart")
                    alterDialog.setPositiveButton("Okay")
                    { _, _ ->
                        super.onBackPressed()
                    }
                    alterDialog.setNegativeButton("No")
                    { _, _ ->
                        //do nothing
                    }
                    alterDialog.show()
                } else {
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}