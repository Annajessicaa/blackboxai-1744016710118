package com.example.securefile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.securefile.databinding.ActivityEncryptBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore

class EncryptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEncryptBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncryptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar
        statusTextView = binding.tvStatus

        binding.btnSelectFile.setOnClickListener {
            selectFile()
        }

        binding.btnEncrypt.setOnClickListener {
            encryptFile()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.etFilePath.setText(uri.toString())
            }
        }
    }

    private fun encryptFile() {
        val filePath = binding.etFilePath.text.toString()
        if (filePath.isEmpty()) {
            statusTextView.text = "Please select a file."
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        statusTextView.text = "Encrypting file..."

        // Perform encryption in a background thread
        Thread {
            try {
                val file = File(Uri.parse(filePath).path!!)
                val inputStream = FileInputStream(file)
                val outputStream = FileOutputStream(File(file.parent, "encrypted_${file.name}"))

                // Encrypt the file using AES
                val aesKey = CryptoManager.generateAESKey()
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, aesKey)

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val encryptedData = cipher.update(buffer, 0, bytesRead)
                    if (encryptedData != null) {
                        outputStream.write(encryptedData)
                    }
                }
                val finalData = cipher.doFinal()
                if (finalData != null) {
                    outputStream.write(finalData)
                }

                inputStream.close()
                outputStream.close()

                // Encrypt the AES key with RSA
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val rsaKey = keyStore.getKey("RSA_KEY", null) as PrivateKey
                val encryptedKey = CryptoManager.encryptAESKey(aesKey, rsaKey)

                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    statusTextView.text = "File encrypted successfully!"
                }
            } catch (e: Exception) {
                Log.e("EncryptActivity", "Encryption failed", e)
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    statusTextView.text = "Encryption failed: ${e.message}"
                }
            }
        }.start()
    }

    companion object {
        private const val FILE_SELECT_CODE = 0
    }
}