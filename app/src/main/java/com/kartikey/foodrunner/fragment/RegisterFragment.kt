package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.activity.DashboardActivity
import com.kartikey.foodrunner.utils.ConnectionManager

class RegisterFragment(val contextParam: Context) : Fragment() {

    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etDeliveryAddress: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var registerProgressDialog: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etDeliveryAddress = view.findViewById(R.id.etDeliveryAddress)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnSubmit)
        registerProgressDialog = view.findViewById(R.id.registerProgressdialog)

        btnRegister.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                registerUserFun()
            }
        })
        return view
    }

    private fun userSuccessfullyRegistered() {
        openDashBoard()
    }

    private fun openDashBoard() {
        val intent = Intent(activity as Context, DashboardActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun registerUserFun() {
        val sharedPreferences = contextParam.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        sharedPreferences.edit().putBoolean("user_logged_in", false).apply()

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (checkForErrors()) {
                registerProgressDialog.visibility = View.VISIBLE
                
                // Simulate network request with delay
                Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                    override fun run() {
                        // Registration success
                        val name = etName.text.toString()
                        val email = etEmail.text.toString()
                        val mobileNumber = etMobileNumber.text.toString()
                        val address = etDeliveryAddress.text.toString()
                        
                        // Save user info to SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("user_logged_in", true)
                        editor.putString("user_id", "123456") // Mock ID
                        editor.putString("name", name)
                        editor.putString("email", email)
                        editor.putString("mobile_number", mobileNumber)
                        editor.putString("address", address)
                        editor.apply()

                        Toast.makeText(
                            contextParam,
                            "Registered successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        userSuccessfullyRegistered()
                        
                        registerProgressDialog.visibility = View.GONE
                    }
                }, 1500) // 1.5 second delay
            }
        } else {
            showNoInternetDialog()
        }
    }

    private fun showNoInternetDialog() {
        val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
        alterDialog.setTitle("No Internet")
        alterDialog.setMessage("Internet Connection can't be established!")
        alterDialog.setPositiveButton("Open Settings", 
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(settingsIntent)
                }
            }
        )
        alterDialog.setNegativeButton("Exit", 
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    ActivityCompat.finishAffinity(activity as Activity)
                }
            }
        )
        alterDialog.setCancelable(false)
        alterDialog.create().show()
    }

    private fun checkForErrors(): Boolean {
        //errorPassCount determines if there are any errors or not
        var errorPassCount = 0
        if (etName.text.isBlank()) {
            etName.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etMobileNumber.text.isBlank()) {
            etMobileNumber.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etEmail.text.isBlank()) {
            etEmail.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etDeliveryAddress.text.isBlank()) {
            etDeliveryAddress.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etConfirmPassword.text.isBlank()) {
            etConfirmPassword.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etPassword.text.isBlank()) {
            etPassword.error = "Field Missing!"
        } else {
            errorPassCount++
        }
        if (etPassword.text.isNotBlank() && etConfirmPassword.text.isNotBlank()) {
            if (etPassword.text.toString() == etConfirmPassword.text.toString()) {
                errorPassCount++
            } else {
                etConfirmPassword.error = "Confirmed Password doesn't match"
            }
        }
        return errorPassCount == 7
    }

    override fun onResume() {
        super.onResume()
        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            showNoInternetDialog()
        }
    }
}