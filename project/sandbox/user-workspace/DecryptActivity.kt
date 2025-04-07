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
import com.example.securefile.databinding.ActivityDecryptBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class DecryptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDecryptBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var decryptedContentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecryptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar
        statusTextView = binding.tvStatus
        decryptedContentTextView = binding.tvDecryptedContent

        binding.btnSelectEncryptedFile.setOnClickListener {
            selectEncryptedFile()
        }

        binding.btnDecrypt.setOnClickListener {
            decryptFile()
        }
    }

    private fun selectEncryptedFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.etEncryptedFilePath.setText(uri.toString())
            }
        }
    }

    private fun decryptFile() {
        val filePath = binding.etEncryptedFilePath.text.toString()
        if (filePath.isEmpty()) {
            statusTextView.text = "Please select an encrypted file."
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        statusTextView.text = "Decrypting file..."
        decryptedContentTextView.text = ""

        // Perform decryption in a background thread
        Thread {
            try {
                val file = File(Uri.parse(filePath).path!!)
                val inputStream = FileInputStream(file)
                val outputStream = FileOutputStream(File(file.parent, "decrypted_${file.name}"))

                // Get RSA private key from Android KeyStore
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val rsaKey = keyStore.getKey("RSA_KEY", null) as PrivateKey

                // Decrypt the AES key (in a real app, this would come from secure storage)
                val encryptedKey = binding.etEncryptionKey.text.toString().toByteArray()
                val aesKey = CryptoManager.decryptAESKey(encryptedKey, rsaKey)

                // Decrypt the file using AES
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, aesKey)

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val decryptedData = cipher.update(buffer, 0, bytesRead)
                    if (decryptedData != null) {
                        outputStream.write(decryptedData)
                    }
                }
                val finalData = cipher.doFinal()
                if (finalData != null) {
                    outputStream.write(finalData)
                }

                inputStream.close()
                outputStream.close()

                // Show first 100 characters of decrypted content
                val decryptedContent = File(file.parent, "decrypted_${file.name}").readText()
                val previewContent = if (decryptedContent.length > 100) {
                    decryptedContent.substring(0, 100) + "..."
                } else {
                    decryptedContent
                }

                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    statusTextView.text = "File decrypted successfully!"
                    decryptedContentTextView.text = "Preview:\n$previewContent"
                }
            } catch (e: Exception) {
                Log.e("DecryptActivity", "Decryption failed", e)
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    statusTextView.text = "Decryption failed: ${e.message}"
                }
            }
        }.start()
    }

    companion object {
        private const val FILE_SELECT_CODE = 0
    }
}