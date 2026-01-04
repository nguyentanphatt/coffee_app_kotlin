package com.example.coffeeapp.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.coffeeapp.Helper.CloudinaryConfig
import com.example.coffeeapp.R
import com.example.coffeeapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var selectedImageUri: Uri? = null

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.btn_5)
                    .into(binding.profileAvatar)

                uploadToCloudinary(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        initCloudinary()
        checkAuthAndLoadProfile()
        setupListeners()
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME,
                "api_key" to CloudinaryConfig.API_KEY,
                "api_secret" to CloudinaryConfig.API_SECRET
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {

        }
    }

    private fun uploadToCloudinary(imageUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        binding.editAvatarBtn.isEnabled = false

        val tempFile = copyUriToTempFile(imageUri)
        if (tempFile == null) {
            Toast.makeText(this, "Cannot process image", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            binding.editAvatarBtn.isEnabled = true
            return
        }

        val userId = currentUser?.uid ?: return

        MediaManager.get().upload(tempFile.absolutePath)
            .option("folder", "coffee_app/avatars")
            .option("public_id", "user_$userId")
            .option("overwrite", true)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Uploading avatar...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {

                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {

                    tempFile.delete()

                    val imageUrl = resultData["secure_url"] as? String

                    runOnUiThread {
                        if (!imageUrl.isNullOrEmpty()) {
                            saveAvatarUrlToDatabase(imageUrl)
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.editAvatarBtn.isEnabled = true
                            Toast.makeText(
                                this@ProfileActivity,
                                "Upload failed: No URL returned",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {

                    tempFile.delete()

                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.editAvatarBtn.isEnabled = true
                        Toast.makeText(
                            this@ProfileActivity,
                            "Upload failed: ${error.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Upload rescheduled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            .dispatch()
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveAvatarUrlToDatabase(imageUrl: String) {
        val userId = currentUser?.uid ?: return

        val database = FirebaseDatabase.getInstance(
            "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
        val userRef = database.getReference("users").child(userId)

        val updates = mapOf(
            "avatarUrl" to imageUrl,
            "avatarUpdatedAt" to System.currentTimeMillis()
        )

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.editAvatarBtn.isEnabled = true
                Toast.makeText(
                    this,
                    "Avatar updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.btn_5)
                    .into(binding.profileAvatar)
            }
            .addOnFailureListener { error ->
                binding.progressBar.visibility = View.GONE
                binding.editAvatarBtn.isEnabled = true
                Toast.makeText(
                    this,
                    "Failed to save avatar URL: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkAuthAndLoadProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        currentUser?.let { user ->
            binding.profileName.text = user.displayName ?: "User"
            binding.profileEmail.text = user.email ?: "No email"
            binding.profileUid.text = user.uid

            if (!user.phoneNumber.isNullOrEmpty()) {
                binding.phoneCard.visibility = View.VISIBLE
                binding.profilePhone.text = user.phoneNumber
            } else {
                binding.phoneCard.visibility = View.GONE
            }

            val creationTime = user.metadata?.creationTimestamp
            if (creationTime != null) {
                val date = Date(creationTime)
                val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                binding.profileMemberSince.text = "Member since ${formatter.format(date)}"
            }

            loadAvatarFromDatabase(user.uid)
        }

        binding.progressBar.visibility = View.GONE
    }

    private fun loadAvatarFromDatabase(userId: String) {
        val database = FirebaseDatabase.getInstance(
            "https://coffee-app-dcd84-default-rtdb.asia-southeast1.firebasedatabase.app"
        )
        val userRef = database.getReference("users").child(userId).child("avatarUrl")

        userRef.get().addOnSuccessListener { snapshot ->
            val avatarUrl = snapshot.getValue(String::class.java)
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.btn_5)
                    .error(R.drawable.btn_5)
                    .into(binding.profileAvatar)
            } else {
                currentUser?.photoUrl?.let { photoUrl ->
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.btn_5)
                        .into(binding.profileAvatar)
                }
            }
        }.addOnFailureListener {
            currentUser?.photoUrl?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.btn_5)
                    .into(binding.profileAvatar)
            }
        }
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.editAvatarBtn.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        binding.editProfileBtn.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.changePasswordBtn.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun showChangePasswordDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("We'll send you a password reset email to ${currentUser?.email}")
            .setPositiveButton("Send Email") { _, _ ->
                sendPasswordResetEmail()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail() {
        val email = currentUser?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No email associated with this account", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Password reset email sent to $email",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { error ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Failed to send email: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}