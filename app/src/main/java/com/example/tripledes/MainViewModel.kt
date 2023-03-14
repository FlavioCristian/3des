package com.example.tripledes

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tripledes.data.KeyRoomDB
import java.io.IOException
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import com.example.tripledes.data.Key
import javax.crypto.spec.SecretKeySpec

class MainViewModel : ViewModel() {

    val _privateKey: MutableLiveData<PrivateKey> by lazy { MutableLiveData() }
    val privateKey: LiveData<PrivateKey> = _privateKey

    val _publicKey: MutableLiveData<PublicKey> by lazy { MutableLiveData() }
    val publicKey: LiveData<PublicKey> = _publicKey

    var isKeysGenerated = false

    @Throws(GeneralSecurityException::class, IOException::class)
    fun loadPublicKeyRSA(stored: String): PublicKey {
        val formattedStored = stored.replace(" ", "+")
        val data: ByteArray = Base64.getDecoder().
        decode(formattedStored.toByteArray())
        val spec = X509EncodedKeySpec(data)
        val fact = KeyFactory.getInstance("RSA")
        val publicKey = fact.generatePublic(spec)
        return publicKey
    }

    @Throws(Exception::class)
    fun encryptMessageRSA(plainText: String, publickey: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, loadPublicKeyRSA(publickey))
        return Base64.getEncoder().encodeToString(cipher.doFinal
            (plainText.toByteArray()))
    }

    @Throws(Exception::class)
    fun decryptMessageRSA(encryptedText: String?, privatekey: String):
            String {
        val formattedEncryptedText = encryptedText?.replace(" ", "+")
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, loadPrivateKeyRSA(privatekey))
        return String(cipher.doFinal(
            Base64.getDecoder().
        decode(formattedEncryptedText)))
    }

    @Throws(GeneralSecurityException::class)
    fun loadPrivateKeyRSA(key64: String): PrivateKey {
        val formattedStored = key64.replace(" ", "+")
        val clear: ByteArray = Base64.getDecoder().
        decode(formattedStored.toByteArray())
        val keySpec = PKCS8EncodedKeySpec(clear)
        val fact = KeyFactory.getInstance("RSA")
        val priv = fact.generatePrivate(keySpec)
        Arrays.fill(clear, 0.toByte())
        return priv
    }

    fun generateKeyRSA() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(1024)
        val pair = keyGen.generateKeyPair()
        _privateKey.postValue(pair.private)
        _publicKey.postValue(pair.public)
        isKeysGenerated = true
    }

    @Throws(Exception::class)
    fun encryptDES(message: String, secretKey: SecretKey): ByteArray {
        val iv = IvParameterSpec(ByteArray(8))
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
        val plainTextBytes = message.toByteArray(charset("utf-8"))
        return cipher.doFinal(plainTextBytes)
    }

    fun fromSecretKeyToString(key: SecretKey): String {
        val byteDES = key.encoded
        return Base64.getEncoder().encodeToString(byteDES)
    }

    fun fromStringToSecretKey(key: String): SecretKey {
        val byteDES = Base64.getDecoder().decode(key)
        return SecretKeySpec(byteDES, "DESede")
    }

    suspend fun saveKeyDES(context: Context, key: SecretKey) {
        var keyString = ""

        KeyRoomDB.getDatabase(context).keyDao().insert(Key("des", keyString))
    }

    fun generateKeyDES(): SecretKey {
        val md = MessageDigest.getInstance("md5")
        val digestOfPassword = md.digest(
            "HG58YZ3CR9"
                .toByteArray(charset("utf-8"))
        )
        val keyBytes = Arrays.copyOf(digestOfPassword, 24)
        var j = 0
        var k = 16
        while (j < 8) {
            keyBytes[k++] = keyBytes[j++]
        }
        return SecretKeySpec(keyBytes, "DESede")
    }

    @Throws(Exception::class)
    fun decryptDES(message: String, key: SecretKey): String {

        val formattedMessage = message.replace(" ", "+")
        val data: ByteArray = Base64.getDecoder().
        decode(formattedMessage.toByteArray())
        val iv = IvParameterSpec(ByteArray(8))
        val decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        decipher.init(Cipher.DECRYPT_MODE, key, iv)
        val plainText = decipher.doFinal(data)
        return String(plainText)
    }
}
