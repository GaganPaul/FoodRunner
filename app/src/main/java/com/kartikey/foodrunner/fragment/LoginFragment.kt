package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.activity.DashboardActivity
import com.kartikey.foodrunner.utils.ConnectionManager

class LoginFragment(val contextParam: Context) : Fragment() {
    lateinit var txtSignUp: TextView
    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var txtForgotPassword: TextView
    lateinit var btnLogin: Button
    lateinit var loginProgressDialog: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etPassword = view.findViewById(R.id.etPassword)
        txtForgotPassword = view.findViewById(R.id.txtForgotPassword)
        txtSignUp = view.findViewById(R.id.txtSignUp)
        btnLogin = view.findViewById(R.id.btnLogin)
        loginProgressDialog = view.findViewById(R.id.loginProgressDialog)

        loginProgressDialog.visibility = View.GONE

        //under line text
        txtForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        txtSignUp.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        txtForgotPassword.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openForgotPasswordInputFragment()
            }
        })

        txtSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openRegisterFragment()
            }
        })

        btnLogin.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                btnLogin.visibility = View.GONE

                if (etMobileNumber.text.isBlank()) {
                    etMobileNumber.error = "Mobile Number Missing"
                    btnLogin.visibility = View.VISIBLE
                } else if (etPassword.text.isBlank()) {
                    btnLogin.visibility = View.VISIBLE
                    etPassword.error = "Missing Password"
                } else {
                    loginUserFun()
                }
            }
        })
        return view
    }

    private fun openForgotPasswordInputFragment() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            ForgotPasswordInputFragment(contextParam)
        )
        transaction?.commit()//apply changes
    }

    private fun openRegisterFragment() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            RegisterFragment(contextParam)
        )
        transaction?.commit()
    }

    private fun loginUserFun() {
        val sharedPreferences = contextParam.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            loginProgressDialog.visibility = View.VISIBLE
            
            // Simulate API call with a delay
            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                override fun run() {
                    // For demo purposes, accept any login with password length >= 4
                    if (etPassword.text.length >= 4) {
                        // Login success
                        val userMobileNumber = etMobileNumber.text.toString()
                        val userName = "Demo User" // Mock data
                        
                        // Save login info to SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("user_logged_in", true)
                        editor.putString("user_id", "123456") // Mock ID
                        editor.putString("name", userName)
                        editor.putString("email", "user@example.com") // Mock email
                        editor.putString("mobile_number", userMobileNumber)
                        editor.putString("address", "123 Main Street") // Mock address
                        editor.apply()

                        Toast.makeText(
                            contextParam,
                            "Welcome " + userName,
                            Toast.LENGTH_LONG
                        ).show()

                        userSuccessfullyLoggedIn()
                    } else {
                        // Login failed
                        btnLogin.visibility = View.VISIBLE
                        Toast.makeText(
                            contextParam,
                            "Invalid Credentials! Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    loginProgressDialog.visibility = View.GONE
                }
            }, 1500) // 1.5 second delay
            
        } else {
            btnLogin.visibility = View.VISIBLE
            showNoInternetDialog()
        }
    }

    private fun userSuccessfullyLoggedIn() {
        val intent = Intent(activity as Context, DashboardActivity::class.java)
        startActivity(intent)
        activity?.finish()
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

    override fun onResume() {
        super.onResume()
        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            showNoInternetDialog()
        }
    }
}