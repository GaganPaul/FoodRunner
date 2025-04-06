package com.kartikey.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.CartItems
import com.kartikey.foodrunner.model.OrderHistoryRestaurant
import com.kartikey.foodrunner.utils.ConnectionManager
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    val context: Context,
    val orderedRestaurantList: ArrayList<OrderHistoryRestaurant>
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolderOrderHistoryRestaurant>() {

    class ViewHolderOrderHistoryRestaurant(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val recyclerViewItemsOrdered: RecyclerView =
            view.findViewById(R.id.recyclerViewItemsOrdered)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistoryRestaurant {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_history_recycler_single_row, parent, false)

        return ViewHolderOrderHistoryRestaurant(view)
    }

    override fun getItemCount(): Int {
        return orderedRestaurantList.size
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHistoryRestaurant, position: Int) {
        val restaurantObject = orderedRestaurantList[position]
        holder.txtRestaurantName.text = restaurantObject.restaurantName
        
        // Format date for display
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(restaurantObject.orderPlacedAt)
            holder.txtDate.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            // If date parsing fails, display as is but limit to date part
            val dateStr = restaurantObject.orderPlacedAt
            if (dateStr.length > 10) {
                holder.txtDate.text = dateStr.substring(0, 10)
            } else {
                holder.txtDate.text = dateStr
            }
        }

        val layoutManager = LinearLayoutManager(context)
        
        if (ConnectionManager().checkConnectivity(context)) {
            // Get order items from Firestore
            val orderId = restaurantObject.orderId
            
            FirebaseHelper.db.collection(FirebaseHelper.COLLECTION_ORDERS)
                .document(orderId)
                .collection("items")
                .get()
                .addOnSuccessListener { documents ->
                    val orderItemsPerRestaurant = ArrayList<CartItems>()
                    
                    for (document in documents) {
                        val itemObject = CartItems(
                            document.getString("itemId") ?: "",
                            document.getString("itemName") ?: "",
                            document.getString("itemPrice") ?: "",
                            "000" // Restaurant ID not needed for display
                        )
                        orderItemsPerRestaurant.add(itemObject)
                    }
                    
                    val orderedItemAdapter = CartAdapter(
                        context,
                        orderItemsPerRestaurant
                    )
                    
                    holder.recyclerViewItemsOrdered.adapter = orderedItemAdapter
                    holder.recyclerViewItemsOrdered.layoutManager = layoutManager
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to load order items!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}