package com.example.kotlininstagram

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlininstagram.databinding.ActivityFeedBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRegistrar
import com.google.firebase.auth.auth

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        auth= Firebase.auth

    }


    //MENÜYÜ MAİNE BAĞLADIK
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       val menuInflater=menuInflater
        menuInflater.inflate(R.menu.insta_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    //MENÜYE TIKLAYINCA NE YAPICAZ
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.add_post){
            val  intent=Intent(this,UploadActivity::class.java)
            startActivity(intent)
        }else if(item.itemId==R.id.signOut){
            //ÇIKIŞ YAP VE GİRİŞ SAYFASINA GİT
            auth.signOut()
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()

        }else{

        }
        return super.onOptionsItemSelected(item)
    }


}