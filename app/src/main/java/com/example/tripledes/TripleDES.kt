import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKey
import java.util.Arrays
import java.security.MessageDigest

class TripleDES {
    @Throws(Exception::class)
    fun encrypt(message: String): ByteArray {
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
        val key: SecretKey = SecretKeySpec(keyBytes, "DESede")
        val iv = IvParameterSpec(ByteArray(8))
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val plainTextBytes = message.toByteArray(charset("utf-8"))
        // final String encodedCipherText = new sun.misc.BASE64Encoder()
        // .encode(cipherText);
        return cipher.doFinal(plainTextBytes)
    }

    fun saveKey() {

    }

    fun generateKey() {

    }

    @Throws(Exception::class)
    fun decrypt(message: ByteArray?): String {
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
        val key: SecretKey = SecretKeySpec(keyBytes, "DESede")
        val iv = IvParameterSpec(ByteArray(8))
        val decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        decipher.init(Cipher.DECRYPT_MODE, key, iv)

        // final byte[] encData = new
        // sun.misc.BASE64Decoder().decodeBuffer(message);
        val plainText = decipher.doFinal(message)
        return String(plainText)
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val text = "knowledgefactory.net"
            val codedtext = TripleDES().encrypt(text)
            val decodedtext = TripleDES().decrypt(codedtext)
            println(codedtext)
            println(decodedtext)
            // This correctly shows "knowledgefactory.net"
        }
    }
}