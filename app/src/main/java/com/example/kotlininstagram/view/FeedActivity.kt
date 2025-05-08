package com.example.kotlininstagram.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.kotlininstagram.R
import com.example.kotlininstagram.databinding.ActivityFeedBinding
import com.example.kotlininstagram.model.Post
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postArrayList:ArrayList<Post>


    // MENÜYÜ MAİN'E BAĞLADIK
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater // Menü oluşturmada kullanılıyor
        menuInflater.inflate(
            R.menu.insta_menu,
            menu
        ) // Oluşturduğum menü XML dosyasını (insta_menu.xml) bağladım
        return super.onCreateOptionsMenu(menu) // Menü görünmeye devam etsin diye true dönüyorum
    }

    // MENÜYE TIKLAYINCA NE OLACAĞINI BURADA YAZIYORUM
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_post) {
            // Eğer kullanıcı "Add Post" butonuna tıklarsa -> UploadActivity'e geçiş yap
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)

        } else if (item.itemId == R.id.signOut) {
            // Eğer kullanıcı "Sign Out" seçeneğine tıklarsa
            auth.signOut() // Firebase üzerinden çıkış yap
            val intent = Intent(this, MainActivity::class.java) // Giriş ekranına dön
            startActivity(intent)
            finish() // Bu aktiviteyi kapattım, geri gelmesin
        }
        return super.onOptionsItemSelected(item) // Diğer menü işlemleri için sistemin kendi davranışını da çağırdım
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        auth = Firebase.auth
        db = Firebase.firestore
        postArrayList=ArrayList<Post>()
        getData()

    }

    //Verileri almak için getData fonksiyonu oluşturdum
    private fun getData() {
        db.collection("Posts")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (value != null) {
                        if (!value.isEmpty) {
                            val documents = value.documents
                            for (document in documents) {
                                val comment = document.get("comment") as String
                                val userEmail = document.get("userEmail") as String
                                val downloadUrl=document.get("downloadUrl") as String

                                val posts=Post(userEmail,comment,downloadUrl)
                                postArrayList.add(posts)
                            }
                        }
                    }
                }
            }
        }
    }