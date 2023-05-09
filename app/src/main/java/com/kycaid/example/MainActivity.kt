package com.kycaid.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kycaid.sdk.ui.KycaidConfiguration

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KycaidConfiguration.Builder("", "", null).build()
    }
}
