package com.kartikey.foodrunner.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.model.RestaurantMenu
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object LocalAssetManager {
    private const val TAG = "LocalAssetManager"
    private const val RESTAURANTS_FILE = "restaurants.json"
    private const val MENUS_DIRECTORY = "menus"
    
    // Load restaurants from local assets
    fun loadRestaurantsFromAssets(context: Context): ArrayList<Restaurant> {
        val restaurantList = ArrayList<Restaurant>()
        try {
            val jsonString = loadJSONFromAsset(context, RESTAURANTS_FILE)
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val restaurantObject = jsonArray.getJSONObject(i)
                    val restaurant = Restaurant(
                        restaurantObject.getString("id"),
                        restaurantObject.getString("name"),
                        restaurantObject.getString("rating"),
                        restaurantObject.getString("cost_for_one"),
                        restaurantObject.getString("image_name")
                    )
                    restaurantList.add(restaurant)
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing restaurants JSON", e)
        }
        return restaurantList
    }
    
    // Load restaurant menu from local assets
    fun loadRestaurantMenu(context: Context, restaurantId: String): ArrayList<RestaurantMenu> {
        val menuList = ArrayList<RestaurantMenu>()
        try {
            val menuFileName = "$MENUS_DIRECTORY/$restaurantId.json"
            val jsonString = loadJSONFromAsset(context, menuFileName)
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val menuObject = jsonArray.getJSONObject(i)
                    val menuItem = RestaurantMenu(
                        menuObject.getString("id"),
                        menuObject.getString("name"),
                        menuObject.getString("cost_for_one")
                    )
                    menuList.add(menuItem)
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing menu JSON", e)
        } catch (e: IOException) {
            Log.e(TAG, "Menu file not found for restaurant $restaurantId", e)
        }
        return menuList
    }
    
    // Helper method to load JSON from assets
    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        var json: String? = null
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e(TAG, "Error loading JSON from assets: $fileName", e)
            return null
        }
        return json
    }
    
    // Check if a file exists in assets
    fun assetExists(context: Context, fileName: String): Boolean {
        val assetManager: AssetManager = context.assets
        try {
            val inputStream = assetManager.open(fileName)
            inputStream.close()
            return true
        } catch (e: IOException) {
            return false
        }
    }
    
    // Get image path for Picasso loading
    fun getImagePath(imageName: String): String {
        return "file:///android_asset/images/$imageName"
    }
}
