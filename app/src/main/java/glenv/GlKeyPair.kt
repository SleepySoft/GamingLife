package glenv

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import glcore.GlLog
import java.io.ByteArrayOutputStream
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


// https://blog.csdn.net/duner12138/article/details/112647484

class GlKeyPair {
    companion object {
        const val ENCRYPT_SIZE_MAX = 245
        const val DECRYPT_SIZE_MAX = 256
    }

    var publicKey: PublicKey? = null
        private set
    var privateKey: PrivateKey? = null
        private set

    @RequiresApi(Build.VERSION_CODES.M)
    var transformation: String = KeyProperties.KEY_ALGORITHM_RSA

    var publicKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance(transformation)
            publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(publicKey?.encoded ?: ByteArray(0))

    var privateKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance(transformation)
            privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(privateKey?.encoded ?: ByteArray(0))

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeyPair() {
        generate(KeyPairGenerator.getInstance(transformation))
    }

    // --------------------------------------------------------------

    fun loadLocalKeyPair(alias: String) : Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val entry = keyStore.getEntry(alias, null)
            privateKey = (entry as KeyStore.PrivateKeyEntry).privateKey
            publicKey = keyStore.getCertificate(alias).publicKey
            true
        } catch (e: Exception) {
            GlLog.e("Load key fail: $alias")
            GlLog.e(e.stackTraceToString())
            false
        } finally {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateLocalKeyPair(alias: String) {
        val spec = KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).run {
            build()
        }

        val generator = KeyPairGenerator.getInstance(
            transformation, "AndroidKeyStore").apply {
                initialize(spec)
        }

        generate(generator)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun publicKeyEncrypt(data: ByteArray) : ByteArray = publicKey?.run {
            keyEncrypt(this, data)
        } ?: byteArrayOf()

    @RequiresApi(Build.VERSION_CODES.O)
    fun privateKeyEncrypt(data: ByteArray) : ByteArray = privateKey?.run {
            keyEncrypt(this, data)
        } ?: byteArrayOf()

    @RequiresApi(Build.VERSION_CODES.O)
    fun publicKeyDecrypt(data: ByteArray) : ByteArray = publicKey?.run {
        keyDecrypt(this, data)
    } ?: byteArrayOf()

    @RequiresApi(Build.VERSION_CODES.O)
    fun privateKeyDecrypt(data: ByteArray) : ByteArray = privateKey?.run {
        keyDecrypt(this, data)
    } ?: byteArrayOf()

    // ---------------------------------------------------------------------------------------------

    private fun generate(generator: KeyPairGenerator) {
        val keyPair = generator.genKeyPair()
        privateKey = keyPair.private
        publicKey = keyPair.public
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyEncrypt(key: Key, data: ByteArray) : ByteArray =
        keyCipher(key, data, Cipher.ENCRYPT_MODE, ENCRYPT_SIZE_MAX)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyDecrypt(key: Key, data: ByteArray) : ByteArray =
        keyCipher(key, data, Cipher.DECRYPT_MODE, DECRYPT_SIZE_MAX)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyCipher(key: Key, data: ByteArray, mode: Int, limit: Int) : ByteArray {
        var offset = 0
        val outputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance(transformation).apply { init(mode, key) }
        while (offset < data.size) {
            val remainingLen = data.size - offset
            val processLen = if (remainingLen > limit) limit else remainingLen
            val encryptedData = cipher.doFinal(data, offset, processLen)
            outputStream.write(encryptedData)
            offset += processLen
        }
        outputStream.close()
        return outputStream.toByteArray()
        // return Base64.getEncoder().encode(outputStream.toByteArray()).toString()
    }
}