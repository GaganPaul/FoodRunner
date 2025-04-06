package com.kartikey.foodrunner.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.adapter.DashboardFragmentAdapter
import com.kartikey.foodrunner.database.FirebaseHelper
import com.kartikey.foodrunner.model.Restaurant
import com.kartikey.foodrunner.utils.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


class DashboardFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var dashboardAdapter: DashboardFragmentAdapter
    lateinit var etSearch: EditText
    lateinit var radioButtonView: View
    lateinit var progressDialog: RelativeLayout
    lateinit var rlNoRestaurantFound: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()
    var ratingComparator = Comparator<Restaurant>
    { rest1, rest2 ->

        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }

    }

    var costComparator = Comparator<Restaurant>
    { rest1, rest2 ->

        if (rest1.costForOne.compareTo(rest2.costForOne, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.costForOne.compareTo(rest2.costForOne, true)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)
        etSearch = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.recyclerViewDashboard)

        progressDialog = view.findViewById(R.id.dashboardProgressDialog)
        rlNoRestaurantFound = view.findViewById(R.id.noRestaurantFound)
        layoutManager = LinearLayoutManager(activity)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                filterFunction(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        return view
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            progressDialog.visibility = View.VISIBLE
            
            FirebaseHelper.getAllRestaurants(activity as Context) { restaurants ->
                if (restaurants.isNotEmpty()) {
                    restaurantInfoList.clear()
                    restaurantInfoList.addAll(restaurants)
                    
                    dashboardAdapter = DashboardFragmentAdapter(activity as Context, restaurantInfoList)
                    recyclerView.adapter = dashboardAdapter
                    recyclerView.layoutManager = layoutManager
                    
                    rlNoRestaurantFound.visibility = View.GONE
                } else {
                    rlNoRestaurantFound.visibility = View.VISIBLE
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

    fun filterFunction(str: String) {
        val filteredList = arrayListOf<Restaurant>()

        for (item in restaurantInfoList) {
            if (item.restaurantName.toLowerCase(Locale.ROOT)
                    .contains(str.toLowerCase(Locale.ROOT))
            ) {
                filteredList.add(item)
            }
        }

        if (filteredList.size == 0) {
            rlNoRestaurantFound.visibility = View.VISIBLE
        } else {
            rlNoRestaurantFound.visibility = View.GONE
        }

        dashboardAdapter.filterList(filteredList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.sort) {
            radioButtonView = View.inflate(contextParam, R.layout.sort_radio_button, null)
            androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                .setTitle("Sort By?")
                .setView(radioButtonView)
                .setPositiveButton("OK")
                { _, _ ->
                    if (radioButtonView.radioHighToLow.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        restaurantInfoList.reverse()
                        dashboardAdapter.notifyDataSetChanged()
                    }
                    if (radioButtonView.radioLowToHigh.isChecked) {
                        Collections.sort(restaurantInfoList, costComparator)
                        dashboardAdapter.notifyDataSetChanged()
                    }
                    if (radioButtonView.radioRating.isChecked) {
                        Collections.sort(restaurantInfoList, ratingComparator)
                        restaurantInfoList.reverse()
                        dashboardAdapter.notifyDataSetChanged()
                    }
                }
                .setNegativeButton("Cancel")
                { _, _ ->

                }
                .create()
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (restaurantInfoList.isEmpty())
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
