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
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


// https://blog.csdn.net/duner12138/article/details/112647484

class GlKeyPair {
    companion object {
        const val DEFAULT_KEY_LEN = 2048
        const val ENCRYPT_LIMIT = DEFAULT_KEY_LEN / 8 - 42      // PCKS1
        const val DECRYPT_CHUNK = DEFAULT_KEY_LEN / 8
    }

    var publicKey: PublicKey? = null
        private set
    var privateKey: PrivateKey? = null
        private set

    var signAlgorithm: String = "MD5WithRSA"
    var encryptAlgorithm: String = KeyProperties.KEY_ALGORITHM_RSA
    val encryptTransformation: String = "$encryptAlgorithm/ECB/PKCS1Padding"

    var publicKeyBytes: ByteArray
        set(value) {
            val keyFactory = KeyFactory.getInstance(encryptAlgorithm)
            publicKey = keyFactory.generatePublic(X509EncodedKeySpec(value))
        }
        get() = publicKey?.encoded ?: ByteArray(0)

    var privateKeyBytes: ByteArray
        set(value) {
            val keyFactory = KeyFactory.getInstance(encryptAlgorithm)
            privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(value))
        }
        get() = privateKey?.encoded ?: ByteArray(0)

    var publicKeyString: String
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            publicKeyBytes = Base64.getDecoder().decode(value)
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(publicKeyBytes)

    var privateKeyString: String
        @RequiresApi(Build.VERSION_CODES.O)
        set(value) {
            privateKeyBytes = Base64.getDecoder().decode(value)
        }
        @RequiresApi(Build.VERSION_CODES.O)
        get() = Base64.getEncoder().encodeToString(privateKeyBytes)

    // ---------------------------------------------------------------------------------------------

    fun keyPariMatches() : Boolean {
        return (publicKey != null) && (privateKey != null) &&
                verify("Sleepy".toByteArray(), sign("Sleepy".toByteArray()))
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeyPair() {
        generate(KeyPairGenerator.getInstance(encryptAlgorithm))
    }

    fun generateKeyPair(modulus: BigInteger, publicExponent: BigInteger, privateExponent: BigInteger) {
        // https://stackoverflow.com/a/24547249

        val publicSpec = RSAPublicKeySpec(modulus, publicExponent)
        val privateSpec = RSAPrivateKeySpec(modulus, privateExponent)

        val factory: KeyFactory = KeyFactory.getInstance(encryptAlgorithm)

        publicKey = factory.generatePublic(publicSpec)
        privateKey = factory.generatePrivate(privateSpec)
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateLocalKeyPair(alias: String) {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            encryptAlgorithm, "AndroidKeyStore")

        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY or
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).run {
            setKeySize(DEFAULT_KEY_LEN)

            // setUserAuthenticationRequired(false)
            // setRandomizedEncryptionRequired(true)

            setCertificateSubject(X500Principal("CN=$alias"))
            setCertificateNotBefore(GlDateTime.datetime())
            setCertificateNotAfter(GlDateTime.datetime(100 * 365))

            setDigests(KeyProperties.DIGEST_SHA512,KeyProperties.DIGEST_SHA256)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

            build()
        }
        kpg.initialize(parameterSpec)

        generate(kpg)
    }

    fun loadLocalKeyPair(alias: String) : Boolean {
        return try {
            val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            val entry: KeyStore.Entry = ks.getEntry(alias, null)
            if (entry !is KeyStore.PrivateKeyEntry) {
                throw Exception("Not an instance of a PrivateKeyEntry")
            }

/*            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val entry = keyStore.getEntry(alias, null)*/

            privateKey = entry.privateKey
            publicKey = ks.getCertificate(alias).publicKey
            true
        } catch (e: Exception) {
            GlLog.e("Load key fail: $alias")
            GlLog.e(e.stackTraceToString())
            false
        } finally {

        }
    }

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

    // ---------------------------------------------------------------------------------------------

    // https://stackoverflow.com/questions/30458163/cannot-verify-rsa-signature-on-android

    fun sign(message: ByteArray) : ByteArray {
        return privateKey?.run {
            val signature = Signature.getInstance(signAlgorithm)
            signature.initSign(this)
            signature.update(message)
            signature.sign()
        } ?: byteArrayOf()
    }

    fun verify(message: ByteArray, sign: ByteArray) : Boolean {
        return publicKey?.run {
            val signature = Signature.getInstance(signAlgorithm)
            signature.initVerify(this)
            signature.update(message)
            signature.verify(sign)
        } ?: false
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

        // https://stackoverflow.com/a/19819805
        // https://en.wikipedia.org/wiki/RSA_(cryptosystem)
        // https://crypto.stackexchange.com/questions/79604/private-exponent-on-rsa-key
        // https://crypto.stackexchange.com/questions/18031/how-to-find-modulus-from-a-rsa-public-key
        // https://crypto.stackexchange.com/questions/81615/calculating-rsa-public-modulus-from-private-exponent-and-public-exponent

        val rsaPub = publicKey as RSAPublicKey
        val rsaPrv = privateKey as RSAPrivateKey

        val pubModulus = rsaPub.modulus
        val pubExponent = rsaPub.publicExponent

        val prvModulus = rsaPrv.modulus
        val prvExponent = rsaPrv.privateExponent

        println("===================================================================")
        println("pubModulus")
        println(pubModulus)
        println("..............................")
        println("pubExponent")
        println(pubExponent)
        println("-------------------------------------------------------------------")
        println("prvModulus")
        println(prvModulus)
        println("..............................")
        println("prvExponent")
        println(prvExponent)
        println("===================================================================")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyEncrypt(key: Key, data: ByteArray) : ByteArray =
        keyCipher(key, data, Cipher.ENCRYPT_MODE, ENCRYPT_LIMIT)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyDecrypt(key: Key, data: ByteArray) : ByteArray =
        keyCipher(key, data, Cipher.DECRYPT_MODE, DECRYPT_CHUNK)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun keyCipher(key: Key, data: ByteArray, mode: Int, limit: Int) : ByteArray {
        var offset = 0
        val outputStream = ByteArrayOutputStream()
        val cipher = Cipher.getInstance(encryptTransformation)
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