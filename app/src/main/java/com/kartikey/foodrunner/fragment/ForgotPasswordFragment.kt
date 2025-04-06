package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.kartikey.foodrunner.R
import com.kartikey.foodrunner.utils.ConnectionManager
import android.content.DialogInterface

class ForgotPasswordFragment(val contextParam: Context, val mobile_number: String) : Fragment() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmForgotPassword: EditText
    lateinit var forgotPasswordProgressDialog: RelativeLayout
    lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        etOTP = view.findViewById(R.id.etOTP)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmForgotPassword = view.findViewById(R.id.etConfirmForgotPassword)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        forgotPasswordProgressDialog = view.findViewById(R.id.forgotPasswordProgressDialog)

        btnSubmit.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (etOTP.text.isBlank()) {
                    etOTP.error = "OTP missing"
                } else if (etNewPassword.text.isBlank()) {
                    etNewPassword.error = "Password Missing"
                } else if (etConfirmForgotPassword.text.isBlank()) {
                    etConfirmForgotPassword.error = "Confirm Password Missing"
                } else if (etNewPassword.text.toString() != etConfirmForgotPassword.text.toString()) {
                    etConfirmForgotPassword.error = "Passwords don't match"
                } else {
                    if (ConnectionManager().checkConnectivity(activity as Context)) {
                        forgotPasswordProgressDialog.visibility = View.VISIBLE
                        
                        // Simulate network request with delay
                        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                            override fun run() {
                                // Success case - demo implementation
                                Toast.makeText(
                                    contextParam,
                                    "Password changed successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Return to login screen
                                val transaction = fragmentManager?.beginTransaction()
                                transaction?.replace(
                                    R.id.frameLayout,
                                    LoginFragment(contextParam)
                                )
                                transaction?.commit()
                                
                                forgotPasswordProgressDialog.visibility = View.GONE
                            }
                        }, 1500)
                    } else {
                        showNoInternetDialog()
                    }
                }
            }
        })

        return view
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
    
    // Helper class for simplicity
    class LoginFragment(private val context: Context) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_login, container, false)
        }
    }
}