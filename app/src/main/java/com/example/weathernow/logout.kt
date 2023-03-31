package com.example.weathernow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weathernow.databinding.ActivityLogoutBinding
import com.google.firebase.auth.FirebaseAuth

class logout : AppCompatActivity() {
    private lateinit var binding: ActivityLogoutBinding
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = FirebaseAuth.getInstance()

        if (user.currentUser != null) {
            user.currentUser?.let {
                binding.name.text = it.email
            }
        }

        binding.save.setOnClickListener {
            user.signOut()
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
        }
    }
}