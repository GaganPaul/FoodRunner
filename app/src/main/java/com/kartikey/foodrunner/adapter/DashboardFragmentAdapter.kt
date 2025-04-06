package com.kartikey.foodrunner.adapter


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.activity.RestaurantMenuActivity
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.utils.LocalAssetManager
import com.squareup.picasso.Picasso


class DashboardFragmentAdapter(val context: Context, var itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<DashboardFragmentAdapter.ViewHolderDashboard>() {

    private lateinit var sharedPreferences: SharedPreferences

    class ViewHolderDashboard(view: View) : RecyclerView.ViewHolder(view) {
        val imgRestaurant: ImageView = view.findViewById(R.id.imgRestaurant)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtPricePerPerson: TextView = view.findViewById(R.id.txtPricePerPerson)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val txtFavourite: TextView = view.findViewById(R.id.txtFavourite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDashboard {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_recycler_view_single_row, parent, false)
        
        sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        
        return ViewHolderDashboard(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolderDashboard, position: Int) {
        val restaurant = itemList[position]
        val userId = sharedPreferences.getString("user_id", "0")!!
        
        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtPricePerPerson.text = restaurant.costForOne + "/Person"
        holder.txtRating.text = restaurant.restaurantRating
        
        // Use Picasso to load images from local assets
        val imageUrl = LocalAssetManager.getImagePath(restaurant.restaurantImage)
        Picasso.get().load(imageUrl).error(R.drawable.ic_default_restaurant_image).into(holder.imgRestaurant)

        // Check if restaurant is favorite
        FirebaseHelper.isRestaurantInFavorites(userId, restaurant.restaurantId) { isFavorite ->
            if (isFavorite) {
                holder.txtFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_favourite_filled, 0, 0, 0
                )
            } else {
                holder.txtFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_favourite_border, 0, 0, 0
                )
            }
        }

        holder.txtFavourite.setOnClickListener {
            FirebaseHelper.isRestaurantInFavorites(userId, restaurant.restaurantId) { isFavorite ->
                if (isFavorite) {
                    FirebaseHelper.removeRestaurantFromFavorites(userId, restaurant.restaurantId) { success ->
                        if (success) {
                            holder.txtFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_favourite_border, 0, 0, 0
                            )
                            Toast.makeText(
                                context,
                                "Removed from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    FirebaseHelper.saveRestaurantToFavorites(userId, restaurant) { success ->
                        if (success) {
                            holder.txtFavourite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_favourite_filled, 0, 0, 0
                            )
                            Toast.makeText(
                                context,
                                "Added to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        holder.llContent.setOnClickListener {
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantId", restaurant.restaurantId)
            intent.putExtra("restaurantName", restaurant.restaurantName)
            context.startActivity(intent)
        }
    }

    fun filterList(filteredList: ArrayList<Restaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }
}