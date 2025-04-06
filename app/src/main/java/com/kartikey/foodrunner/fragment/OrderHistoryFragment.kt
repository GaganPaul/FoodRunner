package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.OrderHistoryAdapter
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.OrderHistoryRestaurant
import com.kartikey.foodrunner.utils.ConnectionManager


class OrderHistoryFragment : Fragment() {

    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: OrderHistoryAdapter
    lateinit var recyclerViewAllOrders: RecyclerView
    lateinit var orderHistoryProgressDialog: RelativeLayout
    lateinit var orderHistoryNoOrders: RelativeLayout
    lateinit var orderHistoryDefaultOrders: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)

        recyclerViewAllOrders = view.findViewById(R.id.recyclerViewAllOrders)
        orderHistoryProgressDialog = view.findViewById(R.id.orderHistoryProgressLayout)
        orderHistoryNoOrders = view.findViewById(R.id.orderHistoryNoOrders)
        orderHistoryDefaultOrders = view.findViewById(R.id.linearLayoutOrder)

        return view
    }

    override fun onResume() {
        super.onResume()
        setItemsForEachRestaurant()
    }

    fun setItemsForEachRestaurant() {
        layoutManager = LinearLayoutManager(activity)

        val orderedRestaurantList = ArrayList<OrderHistoryRestaurant>()
        val sharedPreferences = activity?.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        val userId = sharedPreferences?.getString("user_id", "000")!!

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            orderHistoryProgressDialog.visibility = View.VISIBLE

            FirebaseHelper.getOrderHistory(userId) { orders ->
                if (orders.isNotEmpty()) {
                    orderHistoryNoOrders.visibility = View.GONE
                    
                    orderedRestaurantList.clear()
                    orderedRestaurantList.addAll(orders)
                    
                    menuAdapter = OrderHistoryAdapter(
                        activity as Context,
                        orderedRestaurantList
                    )
                    recyclerViewAllOrders.adapter = menuAdapter
                    recyclerViewAllOrders.layoutManager = layoutManager
                } else {
                    orderHistoryNoOrders.visibility = View.VISIBLE
                    orderHistoryDefaultOrders.visibility = View.INVISIBLE
                }
                
                orderHistoryProgressDialog.visibility = View.GONE
            }
        } else {
            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be established!")
            alterDialog.setPositiveButton("Open Settings")
            { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit")
            { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }
}