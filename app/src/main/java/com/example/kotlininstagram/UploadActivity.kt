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

        registerLauncher()

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage


    }


    fun upload(view: View) {

        val uuid=UUID.randomUUID()
        val imageName="$uuid.jpg"

        //Reference ile storage eriştik
        val reference = storage.reference
        val imageReference =
            reference.child("images").child(imageName)  //images klasörü aç içerisine image.jpg resmini koy

        if (selectedPicture != null) {
            imageReference.putFile(selectedPicture!!)
                .addOnSuccessListener {
                    //download url yi al -> firestore a kaydet

                }
                .addOnFailureListener {
                    Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
                }
        }


    }


    //Resim seçme ve kamera izni
    fun selectImage(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Android 33+ -> READ MEDIA IMAGES
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin isteme mantığını kullanıcıya açıkla
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    Snackbar.make(view, "Permission Needed For Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //İzin iste
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    //İzin iste
                    permissionLauncher.launch((Manifest.permission.READ_MEDIA_IMAGES))
                }
            }
            //İzin verildi
            else {
                //Galeriye git
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        } else {
            //Android 32 - -> READ_EXTERNAL_STORAGE
            //Daha önceden izin verilmiş mi kontrol et

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {//İzin verilmedi
                //İzin isteme mantığını kullanıcıya açıkla
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(view, "Permission Needed For Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //İzin iste
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    //İzin iste
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            //İzin verildi
            else {
                //Galeriye git
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }
        }
    }


    //RegisterLauncher:Intent sonucu almak
    //PermissionLauncher:İzin istemek
    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedPicture = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    this@UploadActivity.contentResolver,
                                    selectedPicture!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView3.setImageBitmap(selectedBitmap)
                            } else {
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

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(this@UploadActivity, "Permission needed!", Toast.LENGTH_SHORT)
                        .show()

                }
            }
    }


}