package com.example.kotlininstagram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlininstagram.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Authu initialize etmenin 2 yolu var
        //1-auth=FirebaseAuth.getInstance()
        //2-auth= Firebase.auth

        auth = Firebase.auth
        //Authentication kullanarak güncel kullanıcı var mı yok mu alabiliriz
        //Eğer uygulamaay giriş yaptıysak tekrar bize giriş sayfası gelmez ana sayfa gelir
        val currentUser=auth.currentUser
        if(currentUser!=null){ //Güncel kullanıco varsa
            val intent=Intent(this@MainActivity,FeedActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun signInClicked(view: View) {
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if (email.equals("") || password.equals("")) {
            Toast.makeText(this@MainActivity, "Enter email and password", Toast.LENGTH_SHORT).show()
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val intent = Intent(this@MainActivity, FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }

        }

    }

    fun signUpClicked(view: View) {
        //KAYDOL
        //SIGN UP NEW USER:Yeni kullanıcı oluşturma
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if (email.equals("") || password.equals("")) {
            Toast.makeText(this@MainActivity, "Enter email and password", Toast.LENGTH_SHORT).show()
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {   //Sadece başarılıysa çağırılır
                    val intent = Intent(this@MainActivity, FeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {    //Sadece hata varsa çağırılır
                    Toast.makeText(
                        this@MainActivity,
                        it.localizedMessage,  //Hata mesajını kullanıcının anlayacağı dilde yazdır
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


}