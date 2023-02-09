package glenv

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import glcore.GlDateTime
import glcore.GlLog
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


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
    var algorithm: String = KeyProperties.KEY_ALGORITHM_RSA
    val transformation: String = "RSA/ECB/PKCS1Padding"

    var publicKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance(algorithm)
            publicKey = keyFactory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(publicKey?.encoded ?: ByteArray(0))

    var privateKeyString: String = ""
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            field = value
            val keyFactory = KeyFactory.getInstance(algorithm)
            privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(value)))
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(privateKey?.encoded ?: ByteArray(0))

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeyPair() {
        generate(KeyPairGenerator.getInstance(algorithm))
    }

    // --------------------------------------------------------------

    fun deleteLocalKeyPair(alias: String) : Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(alias)
            true
        } catch (e: Exception) {
            GlLog.e("Delete key fail: $alias")
            GlLog.e(e.stackTraceToString())
            false
        } finally {

        }
    }

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
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val spec = KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY).run {

            // https://stackoverflow.com/a/49414593

            setRandomizedEncryptionRequired(false)
            setDigests(
                KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_MD5,
                KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224,
                KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512)
            setKeySize(2048)
            setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE,
                KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)

            setCertificateSubject(X500Principal("CN=Android, O=Android Authority"))
            setCertificateSerialNumber(BigInteger(256, Random()))
            setCertificateNotBefore(GlDateTime.datetime())
            setCertificateNotAfter(GlDateTime.datetime(100 * 365))

            build()
        }

        val generator = KeyPairGenerator.getInstance(
            algorithm, "AndroidKeyStore").apply {
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
        val cipher = Cipher.getInstance(transformation)
        cipher.init(mode, key)
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