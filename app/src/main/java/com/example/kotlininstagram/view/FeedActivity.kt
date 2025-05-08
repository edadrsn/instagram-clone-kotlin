package com.example.kotlininstagram.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlininstagram.R
import com.example.kotlininstagram.adapter.FeedRecyclerAdapter
import com.example.kotlininstagram.databinding.ActivityFeedBinding
import com.example.kotlininstagram.model.Post
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postArrayList:ArrayList<Post>
    private lateinit var feedAdapter:FeedRecyclerAdapter

    // MENÜYÜ MAİN'E BAĞLADIK
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.insta_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // MENÜYE TIKLAYINCA NE OLACAK
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_post) {
            // Eğer kullanıcı "Add Post" butonuna tıklarsa -> UploadActivity'e geçiş yap
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)

        } else if (item.itemId == R.id.signOut) {
            // Eğer kullanıcı "Sign Out" seçeneğine tıklarsa, Firebase'den çıkış yap
            auth.signOut()  // Firebase auth üzerinden çıkış yapıyoruz
            val intent = Intent(this, MainActivity::class.java)  // Giriş ekranına dön
            startActivity(intent)
            finish()  // Bu aktiviteyi kapat, geri gelmesin
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        auth = Firebase.auth  // Firebase Authentication nesnesini alıyoruz
        db = Firebase.firestore  // Firestore veritabanı nesnesini alıyoruz
        postArrayList = ArrayList<Post>()  // Boş bir ArrayList oluşturuyoruz
        getData()  // Veriyi almak için getData() fonksiyonunu çağırıyoruz

        // RecyclerView ayarlarını yapıyoruz
        binding.recyclerView.layoutManager = LinearLayoutManager(this@FeedActivity)  // Layout manager ile her bir item'ın düzenini belirliyoruz
        feedAdapter = FeedRecyclerAdapter(postArrayList)  // Adapter'ı oluşturuyoruz ve postArrayList ile bağlıyoruz
        binding.recyclerView.adapter = feedAdapter  // Adapter'ı RecyclerView'a bağlıyoruz
    }

    // Verileri almak için getData fonksiyonu oluşturdum
    private fun getData() {
        // Firestore'dan "Posts" koleksiyonunu alıyoruz ve "date" alanına göre azalan sırayla sıralıyoruz
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->  // Verinin değişimlerini dinliyoruz
                if (error != null) {
                    // Hata olursa kullanıcıya gösteriyoruz
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (value != null) {
                        if (!value.isEmpty) {
                            val documents = value.documents  // Verileri listeliyoruz

                            postArrayList.clear()  // Önceki verileri temizliyoruz

                            for (document in documents) {
                                // Firestore'dan gelen her dokümandan veri alıyoruz
                                val comment = document.get("comment") as String  // "comment" alanını alıyoruz
                                val useremail = document.get("userEmail") as String  // "userEmail" alanını alıyoruz
                                val downloadUrl = document.get("downloadUrl") as String  // "downloadUrl" alanını alıyoruz

                                // Post objesi oluşturuyoruz
                                val post = Post(useremail, comment, downloadUrl)

                                // Yeni post'u listeye ekliyoruz
                                postArrayList.add(post)
                            }

                            // RecyclerView'a veri ekledikten sonra adapter'ı güncelliyoruz
                            feedAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
