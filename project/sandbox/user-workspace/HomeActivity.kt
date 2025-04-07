package com.example.securefile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.securefile.databinding.ActivityHomeBinding
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners for the feature cards
        setupCardClickListener(binding.cardEncrypt, EncryptActivity::class.java)
        setupCardClickListener(binding.cardDecrypt, DecryptActivity::class.java)
        setupCardClickListener(binding.cardAbout, AboutActivity::class.java)

        // Initialize the CryptoManager to generate keys if not exists
        CryptoManager.generateRSAKeyPair()
    }

    private fun setupCardClickListener(card: MaterialCardView, activityClass: Class<*>) {
        card.setOnClickListener {
            startActivity(Intent(this, activityClass))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        // Exit the app when back is pressed from Home
        finishAffinity()
    }
}