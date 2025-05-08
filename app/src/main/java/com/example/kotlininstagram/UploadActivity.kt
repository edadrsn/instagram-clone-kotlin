package com.example.kotlininstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlininstagram.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    var selectedPicture: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerLauncher() // İzin ve galeri işlemleri için launcher'ları kaydettim

        // Firebase servislerine erişim sağladım
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
    }

    // Fotoğrafı yükleme işlemi
    fun upload(view: View) {
        val uuid = UUID.randomUUID() // Her fotoğraf için benzersiz bir ID oluşturdum
        val imageName = "$uuid.jpg" // ID ile bir resim ismi belirledim

        // Storage referansına erişiyorum
        val reference = storage.reference
        val imageReference =
            reference.child("images").child(imageName) // "images" klasörünün içine ekleyeceğim

        // Eğer kullanıcı bir resim seçmişse devam et
        if (selectedPicture != null) {
            imageReference.putFile(selectedPicture!!) // Seçilen resmi storage'a yüklüyorum
                .addOnSuccessListener {
                    // Resim başarıyla yüklendiyse, download linkini al
                    val uploadPictureReferences = storage.reference.child("images").child(imageName)
                    uploadPictureReferences.downloadUrl.addOnSuccessListener {
                        val downloadUrl = it.toString() // Download linkini string olarak al

                        // Kullanıcı giriş yapmışsa bilgilerini al
                        if (auth.currentUser != null) {
                            val postMap = hashMapOf<String, Any>()  // Firestore için map oluşturdum
                            postMap.put("downloadUrl", downloadUrl)                 // Resim linki
                            postMap.put(
                                "useremail",
                                auth.currentUser!!.email!!
                            )                                                  // Kullanıcının maili
                            postMap.put(
                                "comment",
                                binding.commentText.text.toString()
                            )                                           // Kullanıcının yazdığı yorum
                            postMap.put("date", Timestamp.now())                   // Gönderi tarihi

                            // Firestore'a gönderiyi ekle
                            firestore.collection("Post").add(postMap)
                                .addOnSuccessListener {
                                    finish() // Başarılıysa aktiviteyi kapat
                                }
                                .addOnFailureListener {
                                    // Hata olursa kullanıcıya göster
                                    Toast.makeText(
                                        this@UploadActivity,
                                        it.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    // Resim yükleme başarısız olursa hata mesajı göster
                    Toast.makeText(
                        this@UploadActivity,
                        it.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // Galeriden resim seçme ve izin kontrolü
    fun selectImage(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 33 ve üstü için READ_MEDIA_IMAGES izni kontrolü
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Kullanıcıya neden izin istediğimizi açıkla
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Snackbar.make(
                        view,
                        "Permission Needed For Gallery", // Açıklama mesajı
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Give Permission", View.OnClickListener {
                            // İzin iste
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    // Direkt izin iste
                    permissionLauncher.launch((Manifest.permission.READ_MEDIA_IMAGES))
                }
            } else {
                // Zaten izin verilmişse galeriyi aç
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            // Android 12 ve altı için izin kontrolü (READ_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(
                        view,
                        "Permission Needed For Gallery",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Give Permission", View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    // Galeriye gitmek ve izin almak için launcher'ları kaydediyorum
    private fun registerLauncher() {
        // Galeriden resim seçtikten sonra ne yapacağımı burada belirtiyorum
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedPicture = intentFromResult.data
                        try {
                            // Android 9 ve üstü için ImageDecoder
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    this@UploadActivity.contentResolver,
                                    selectedPicture!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView3.setImageBitmap(selectedBitmap)
                            } else {
                                // Android 8 ve altı için MediaStore kullanıyorum
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    this@UploadActivity.contentResolver,
                                    selectedPicture
                                )
                                binding.imageView3.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        // İzin alma işlemini burada kontrol ediyorum
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    // İzin verildiyse galeriyi aç
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    // İzin verilmediyse uyarı göster
                    Toast.makeText(
                        this@UploadActivity,
                        "Permission needed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


}