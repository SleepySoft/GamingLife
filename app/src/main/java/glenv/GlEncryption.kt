package glenv

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


// https://blog.csdn.net/duner12138/article/details/112647484

class GlEncryption {
    companion object {
        const val ENCRYPT_SIZE_MAX = 245
        const val DECRYPT_SIZE_MAX = 256
    }

    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null

    var publicKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance("RSA")
            privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(publicKey?.encoded ?: ByteArray(0))

    var privateKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(privateKey?.encoded ?: ByteArray(0))

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeyPair() {
        val generator = KeyPairGenerator.getInstance("RSA")
        val keyPair = generator.genKeyPair()
        privateKey = keyPair.private
        publicKey = keyPair.public
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun publicKeyEncrypt(input: ByteArray) : String {
        return publicKey?.run {
            if (input.size <= ENCRYPT_SIZE_MAX) {
                val cipher = Cipher.getInstance("RSA")
                cipher.init(Cipher.ENCRYPT_MODE, this)
                val encrypt = cipher.doFinal(input)
                String(Base64.getEncoder().encode(encrypt))
            } else {
                ""
            }
        } ?: ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun privateKeyEncrypt(input: ByteArray) : String {
        return privateKey?.run {
            if (input.size <= ENCRYPT_SIZE_MAX) {
                val cipher = Cipher.getInstance("RSA")
                cipher.init(Cipher.ENCRYPT_MODE, this)
                val encrypt = cipher.doFinal(input)
                String(Base64.getEncoder().encode(encrypt))
            } else {
                ""
            }
        } ?: ""
    }
}