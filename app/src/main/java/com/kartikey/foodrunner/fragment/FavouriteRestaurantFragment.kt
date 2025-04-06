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
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.DashboardFragmentAdapter
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.utils.ConnectionManager


class FavouriteRestaurantFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var favouriteAdapter: DashboardFragmentAdapter
    lateinit var progressDialog: RelativeLayout
    lateinit var noFavouritesLayout: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourite_restaurant, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewFavouriteRestaurant)
        progressDialog = view.findViewById(R.id.favouriteRestaurantProgressDialog)
        noFavouritesLayout = view.findViewById(R.id.noFavouriteRestaurantsLayout)

        return view
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            progressDialog.visibility = View.VISIBLE
            noFavouritesLayout.visibility = View.INVISIBLE
            
            val sharedPreferences = activity?.getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )
            val userId = sharedPreferences?.getString("user_id", "0")!!
            
            FirebaseHelper.getAllFavoriteRestaurants(userId) { restaurants ->
                if (restaurants.isNotEmpty()) {
                    restaurantInfoList.clear()
                    restaurantInfoList.addAll(restaurants)
                    
                    favouriteAdapter = DashboardFragmentAdapter(
                        activity as Context,
                        restaurantInfoList
                    )
                    recyclerView.adapter = favouriteAdapter
                    recyclerView.layoutManager = layoutManager
                } else {
                    noFavouritesLayout.visibility = View.VISIBLE
                }
                
                progressDialog.visibility = View.GONE
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

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            fetchData()
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
        super.onResume()
    }
}