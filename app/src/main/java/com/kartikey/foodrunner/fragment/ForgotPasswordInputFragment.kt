package com.kartikey.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
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

class ForgotPasswordInputFragment(val contextParam: Context) : Fragment() {

    lateinit var etMobileNumber: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button
    lateinit var progressDialog: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password_input, container, false)

        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etEmail = view.findViewById(R.id.etEmail)
        btnNext = view.findViewById(R.id.btnNext)
        progressDialog = view.findViewById(R.id.forgotPasswordInputProgressDialog)

        btnNext.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (etMobileNumber.text.isBlank()) {
                    etMobileNumber.error = "Mobile Number Missing"
                } else if (etEmail.text.isBlank()) {
                    etEmail.error = "Email Missing"
                } else {
                    if (ConnectionManager().checkConnectivity(activity as Context)) {
                        // Show progress dialog
                        progressDialog.visibility = View.VISIBLE
                        
                        // Simulate network request with delay
                        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                            override fun run() {
                                // Simulate success response
                                Toast.makeText(
                                    contextParam,
                                    "OTP sent to your email address",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Navigate to password reset screen
                                callChangePasswordFragment()
                                
                                // Hide progress dialog
                                progressDialog.visibility = View.GONE
                            }
                        }, 1500) // 1.5 second delay to simulate network request
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

    fun callChangePasswordFragment() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            ForgotPasswordFragment(contextParam, etMobileNumber.text.toString())
        )
        transaction?.commit()
    }

    override fun onResume() {
        super.onResume()
        if (!ConnectionManager().checkConnectivity(activity as Context)) {
            showNoInternetDialog()
        }
    }
}