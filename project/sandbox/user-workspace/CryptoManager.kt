import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                "AES_KEY",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
        )
        return keyGenerator.generateKey()
    }

    fun generateRSAKeyPair() {
        val keyGen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        keyGen.initialize(
            KeyGenParameterSpec.Builder(
                "RSA_KEY",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setKeySize(2048)
            .setUserAuthenticationRequired(true)
            .build()
        )
        keyGen.generateKeyPair()
    }

    fun encryptAESKey(aesKey: SecretKey, rsaPublicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        return cipher.doFinal(aesKey.encoded)
    }

    fun decryptAESKey(encryptedKey: ByteArray, rsaPrivateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey)
        val decryptedKey = cipher.doFinal(encryptedKey)
        return SecretKeySpec(decryptedKey, KeyProperties.KEY_ALGORITHM_AES)
    }
}