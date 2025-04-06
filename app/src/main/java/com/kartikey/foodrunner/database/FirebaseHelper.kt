package com.kartikey.foodrunner.database

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kartikey.foodrunner.model.CartItems
import com.kartikey.foodrunner.model.OrderHistoryRestaurant
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.model.RestaurantMenu
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object FirebaseHelper {

    private const val TAG = "FirebaseHelper"
    val db = FirebaseFirestore.getInstance()
    
    // Collection references - made public for access from adapters
    const val COLLECTION_RESTAURANTS = "restaurants"
    const val COLLECTION_FAVORITES = "favorites"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CART = "cart"
    const val COLLECTION_MENU = "menu"

    // Save restaurant to favorites collection
    fun saveRestaurantToFavorites(userId: String, restaurant: Restaurant, callback: (Boolean) -> Unit) {
        val favoriteRef = db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(restaurant.restaurantId)

        val restaurantMap = hashMapOf(
            "restaurantId" to restaurant.restaurantId,
            "restaurantName" to restaurant.restaurantName,
            "restaurantRating" to restaurant.restaurantRating,
            "costForOne" to restaurant.costForOne,
            "restaurantImage" to restaurant.restaurantImage
        )

        favoriteRef.set(restaurantMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving restaurant to favorites", e)
                callback(false)
            }
    }

    // Check if restaurant is in favorites
    fun isRestaurantInFavorites(userId: String, restaurantId: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                callback(document != null && document.exists())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking favorite status", e)
                callback(false)
            }
    }

    // Remove restaurant from favorites
    fun removeRestaurantFromFavorites(userId: String, restaurantId: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .document(restaurantId)
            .delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing restaurant from favorites", e)
                callback(false)
            }
    }

    // Get all favorite restaurants
    fun getAllFavoriteRestaurants(userId: String, callback: (ArrayList<Restaurant>) -> Unit) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_FAVORITES)
            .get()
            .addOnSuccessListener { documents ->
                val restaurantList = ArrayList<Restaurant>()
                for (document in documents) {
                    val restaurant = Restaurant(
                        document.getString("restaurantId") ?: "",
                        document.getString("restaurantName") ?: "",
                        document.getString("restaurantRating") ?: "",
                        document.getString("costForOne") ?: "",
                        document.getString("restaurantImage") ?: ""
                    )
                    restaurantList.add(restaurant)
                }
                callback(restaurantList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting favorite restaurants", e)
                callback(ArrayList())
            }
    }

    // Get all restaurants
    fun getAllRestaurants(callback: (ArrayList<Restaurant>) -> Unit) {
        db.collection(COLLECTION_RESTAURANTS)
            .get()
            .addOnSuccessListener { documents ->
                val restaurantList = ArrayList<Restaurant>()
                for (document in documents) {
                    val restaurant = Restaurant(
                        document.id,
                        document.getString("name") ?: "",
                        document.getString("rating") ?: "",
                        document.getString("cost_for_one") ?: "",
                        document.getString("image_url") ?: ""
                    )
                    restaurantList.add(restaurant)
                }
                callback(restaurantList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting restaurants", e)
                callback(ArrayList())
            }
    }

    // Get restaurant menu
    fun getRestaurantMenu(restaurantId: String, callback: (ArrayList<RestaurantMenu>) -> Unit) {
        db.collection(COLLECTION_RESTAURANTS)
            .document(restaurantId)
            .collection(COLLECTION_MENU)
            .get()
            .addOnSuccessListener { documents ->
                val menuList = ArrayList<RestaurantMenu>()
                for (document in documents) {
                    val menuItem = RestaurantMenu(
                        document.id,
                        document.getString("name") ?: "",
                        document.getString("cost_for_one") ?: ""
                    )
                    menuList.add(menuItem)
                }
                callback(menuList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting restaurant menu", e)
                callback(ArrayList())
            }
    }

    // Add items to cart
    fun addItemToCart(userId: String, cartItem: CartItems, callback: (Boolean) -> Unit) {
        val cartRef = db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CART)
            .document(cartItem.itemId)

        val cartItemMap = hashMapOf(
            "itemId" to cartItem.itemId,
            "itemName" to cartItem.itemName,
            "itemPrice" to cartItem.itemPrice,
            "restaurantId" to cartItem.restaurantId
        )

        cartRef.set(cartItemMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding item to cart", e)
                callback(false)
            }
    }

    // Get cart items
    fun getCartItems(userId: String, restaurantId: String, callback: (ArrayList<CartItems>) -> Unit) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CART)
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .addOnSuccessListener { documents ->
                val cartItems = ArrayList<CartItems>()
                for (document in documents) {
                    val cartItem = CartItems(
                        document.getString("itemId") ?: "",
                        document.getString("itemName") ?: "",
                        document.getString("itemPrice") ?: "",
                        document.getString("restaurantId") ?: ""
                    )
                    cartItems.add(cartItem)
                }
                callback(cartItems)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting cart items", e)
                callback(ArrayList())
            }
    }

    // Clear cart
    fun clearCart(userId: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_CART)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error clearing cart", e)
                        callback(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting cart items to clear", e)
                callback(false)
            }
    }

    // Place order
    fun placeOrder(userId: String, restaurantId: String, restaurantName: String, totalCost: String, cartItems: ArrayList<CartItems>, callback: (Boolean) -> Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val orderId = UUID.randomUUID().toString()

        val orderMap = hashMapOf(
            "orderId" to orderId,
            "restaurantId" to restaurantId,
            "restaurantName" to restaurantName,
            "totalCost" to totalCost,
            "orderPlacedAt" to currentDate,
            "userId" to userId
        )

        // Create order document
        db.collection(COLLECTION_ORDERS)
            .document(orderId)
            .set(orderMap)
            .addOnSuccessListener {
                // Add order items as subcollection
                val batch = db.batch()
                cartItems.forEach { item ->
                    val itemRef = db.collection(COLLECTION_ORDERS)
                        .document(orderId)
                        .collection("items")
                        .document(item.itemId)
                    
                    val itemMap = hashMapOf(
                        "itemId" to item.itemId,
                        "itemName" to item.itemName,
                        "itemPrice" to item.itemPrice
                    )
                    batch.set(itemRef, itemMap)
                }
                
                batch.commit()
                    .addOnSuccessListener {
                        // Order placed successfully, now clear the cart
                        clearCart(userId) { success ->
                            callback(success)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding order items", e)
                        callback(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error placing order", e)
                callback(false)
            }
    }

    // Get order history
    fun getOrderHistory(userId: String, callback: (ArrayList<OrderHistoryRestaurant>) -> Unit) {
        db.collection(COLLECTION_ORDERS)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val orderList = ArrayList<OrderHistoryRestaurant>()
                for (document in documents) {
                    val order = OrderHistoryRestaurant(
                        document.getString("orderId") ?: "",
                        document.getString("restaurantName") ?: "",
                        document.getString("totalCost") ?: "",
                        document.getString("orderPlacedAt") ?: ""
                    )
                    orderList.add(order)
                }
                callback(orderList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting order history", e)
                callback(ArrayList())
            }
    }
} 