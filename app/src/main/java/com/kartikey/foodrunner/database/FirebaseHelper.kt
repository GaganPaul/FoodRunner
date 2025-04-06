package com.kartikey.foodrunner.database

import android.content.Context
import android.util.Log
import com.kartikey.foodrunner.model.CartItems
import com.kartikey.foodrunner.model.OrderHistoryRestaurant
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.model.RestaurantMenu
import com.kartikey.foodrunner.utils.LocalAssetManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object FirebaseHelper {

    private const val TAG = "FirebaseHelper"
    
    // Collection references - made public for access from adapters
    const val COLLECTION_RESTAURANTS = "restaurants"
    const val COLLECTION_FAVORITES = "favorites"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CART = "cart"
    const val COLLECTION_MENU = "menu"
    
    // Local storage for favorites, cart, and orders
    private val favoriteRestaurants = HashMap<String, HashMap<String, Restaurant>>()
    private val cartItems = HashMap<String, HashMap<String, CartItems>>()
    private val orderHistory = HashMap<String, ArrayList<OrderHistoryRestaurant>>()

    // Save restaurant to favorites collection
    fun saveRestaurantToFavorites(userId: String, restaurant: Restaurant, callback: (Boolean) -> Unit) {
        if (!favoriteRestaurants.containsKey(userId)) {
            favoriteRestaurants[userId] = HashMap()
        }
        favoriteRestaurants[userId]?.put(restaurant.restaurantId, restaurant)
        callback(true)
    }

    // Check if restaurant is in favorites
    fun isRestaurantInFavorites(userId: String, restaurantId: String, callback: (Boolean) -> Unit) {
        val isFavorite = favoriteRestaurants[userId]?.containsKey(restaurantId) ?: false
        callback(isFavorite)
    }

    // Remove restaurant from favorites
    fun removeRestaurantFromFavorites(userId: String, restaurantId: String, callback: (Boolean) -> Unit) {
        favoriteRestaurants[userId]?.remove(restaurantId)
        callback(true)
    }

    // Get all favorite restaurants
    fun getAllFavoriteRestaurants(userId: String, callback: (ArrayList<Restaurant>) -> Unit) {
        val restaurantList = ArrayList<Restaurant>()
        favoriteRestaurants[userId]?.values?.let { restaurantList.addAll(it) }
        callback(restaurantList)
    }

    // Get all restaurants
    fun getAllRestaurants(context: Context, callback: (ArrayList<Restaurant>) -> Unit) {
        // Use local data
        val restaurants = LocalAssetManager.loadRestaurantsFromAssets(context)
        callback(restaurants)
    }

    // Get restaurant menu
    fun getRestaurantMenu(context: Context, restaurantId: String, callback: (ArrayList<RestaurantMenu>) -> Unit) {
        // Use local data
        val menuItems = LocalAssetManager.loadRestaurantMenu(context, restaurantId)
        callback(menuItems)
    }

    // Add items to cart
    fun addItemToCart(userId: String, cartItem: CartItems, callback: (Boolean) -> Unit) {
        if (!cartItems.containsKey(userId)) {
            cartItems[userId] = HashMap()
        }
        cartItems[userId]?.put(cartItem.itemId, cartItem)
        callback(true)
    }

    // Get cart items
    fun getCartItems(userId: String, restaurantId: String, callback: (ArrayList<CartItems>) -> Unit) {
        val items = ArrayList<CartItems>()
        cartItems[userId]?.values?.forEach { item ->
            if (item.restaurantId == restaurantId) {
                items.add(item)
            }
        }
        callback(items)
    }

    // Clear cart
    fun clearCart(userId: String, callback: (Boolean) -> Unit) {
        cartItems[userId]?.clear()
        callback(true)
    }

    // Place order
    fun placeOrder(userId: String, restaurantId: String, restaurantName: String, totalCost: String, cartItems: ArrayList<CartItems>, callback: (Boolean) -> Unit) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val orderId = UUID.randomUUID().toString()
        
        val order = OrderHistoryRestaurant(
            orderId,
            restaurantName,
            totalCost,
            currentDate
        )
        
        if (!orderHistory.containsKey(userId)) {
            orderHistory[userId] = ArrayList()
        }
        
        orderHistory[userId]?.add(order)
        clearCart(userId) { success ->
            callback(success)
        }
    }

    // Get order history
    fun getOrderHistory(userId: String, callback: (ArrayList<OrderHistoryRestaurant>) -> Unit) {
        val orders = orderHistory[userId] ?: ArrayList()
        callback(orders)
    }
}