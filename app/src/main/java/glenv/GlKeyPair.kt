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
import java.security.spec.*
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
            publicKey = try {
                if (value.isNotEmpty()) {
                    val keyFactory = KeyFactory.getInstance(encryptAlgorithm)
                    keyFactory.generatePublic(X509EncodedKeySpec(value))
                } else {
                    // Not enter exception when empty
                    null
                }
            } catch (e: Exception) {
                GlLog.e(e.stackTraceToString())
                null
            } finally {

            }
        }
        get() = publicKey?.encoded ?: ByteArray(0)

    var privateKeyBytes: ByteArray
        set(value) {
            privateKey = try {
                if (value.isNotEmpty()) {
                    val keyFactory = KeyFactory.getInstance(encryptAlgorithm)
                    keyFactory.generatePrivate(PKCS8EncodedKeySpec(value))
                } else {
                    // Not enter exception when empty
                    null
                }
            } catch (e: Exception) {
                GlLog.e(e.stackTraceToString())
                null
            } finally {

            }
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

    fun keyPairValid() : Boolean = (!keyPairEmpty() && keyPairMatches())

    fun keyPairEmpty() : Boolean = ((publicKey == null) && (privateKey == null))

    fun keyPairMatches() : Boolean = verify("Sleepy".toByteArray(), sign("Sleepy".toByteArray()))

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeyPair() {
        generate(KeyPairGenerator.getInstance(encryptAlgorithm))
    }

    fun generateKeyPair(modulus: BigInteger, publicExponent: BigInteger, privateExponent: BigInteger) {
        // https://stackoverflow.com/a/24547249

        val publicSpec = RSAPublicKeySpec(modulus, publicExponent)
        // val privateSpec = RSAPrivateKeySpec(modulus, privateExponent)

        // Use RSAPrivateCrtKeySpec avoiding error:04000090:RSA routines:OPENSSL_internal:VALUE_MISSING
        // https://stackoverflow.com/questions/67613519/using-rsaprivatekey-and-not-rsaprivatecrtkey-to-save-an-rsa-private-key-to-and
        // https://stackoverflow.com/questions/34932367/java-generating-rsa-key-pair-from-5-crt-components

        val params = calculatePKCS1Parameters(modulus, privateExponent, publicExponent)
        if (params.size == 5) {
            val privateSpec = RSAPrivateCrtKeySpec(
                modulus, publicExponent, privateExponent,
                params[0], params[1], params[2], params[3], params[4])
            val factory: KeyFactory = KeyFactory.getInstance(encryptAlgorithm)

            publicKey = factory.generatePublic(publicSpec)
            privateKey = factory.generatePrivate(privateSpec)
        }
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

    // https://stackoverflow.com/a/29837139
    // Return value: [primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient]

    fun calculatePKCS1Parameters(
        mod: BigInteger, privExponent: BigInteger, pubExponent: BigInteger) : List< BigInteger >
    {
        val n = mod
        val d = privExponent
        val e = pubExponent

        val zero = BigInteger.ZERO
        val one = BigInteger.ONE
        val two = BigInteger.valueOf(2)
        val four = BigInteger.valueOf(4)


        val de = e*d
        val modulusplus1 = n + one
        val deminus1 = de - one
        var p = zero
        var q = zero

        val kprima = de/n

        val ks = arrayOf(kprima, kprima - one, kprima + one)

        var found = false
        for (k in ks)
        {
            val fi = deminus1/k
            val pplusq = modulusplus1 - fi
            val delta = pplusq*pplusq - n*four

            val sqrt = sqrt(delta)
            p = (pplusq + sqrt)/two
            if (n % p != zero) {
                continue
            }
            q = (pplusq - sqrt)/two
            found = true
            break
        }

        return if (found)
        {
            val dp = d % (p - one)
            val dq = d % (q - one)
            val inverseq = q.modInverse(p)

            listOf< BigInteger >(p, q, dp, dq, inverseq)
        } else {
            listOf()
        }
    }

    // https://stackoverflow.com/a/71540415

    // A fast square root by Ryan Scott White. (MIT License)
    fun newtonPlusSqrt(x: BigInteger): BigInteger {
        if (x.compareTo(BigInteger.valueOf(144838757784765629L)) < 0) {
            val xAsLong = x.toLong()
            var vInt = Math.sqrt(xAsLong.toDouble()).toLong()
            if (vInt * vInt > xAsLong) vInt--
            return BigInteger.valueOf(vInt)
        }
        val xAsDub = x.toDouble()
        var result: BigInteger
        if (xAsDub < 2.1267e37) // 2.12e37 largest here since sqrt(long.max*long.max) > long.max
        {
            val vInt = Math.sqrt(xAsDub).toLong()
            result = BigInteger.valueOf(vInt + x.divide(BigInteger.valueOf(vInt)).toLong() shr 1)
        } else if (xAsDub < 4.3322e127) {
            // Convert a double to a BigInteger
            val bits = java.lang.Double.doubleToLongBits(Math.sqrt(xAsDub))
            val exp = ((bits shr 52).toInt() and 0x7ff) - 1075
            result = BigInteger.valueOf(bits and (1L shl 52) - 1 or (1L shl 52)).shiftLeft(exp)
            result = x.divide(result).add(result).shiftRight(1)
            if (xAsDub > 2e63) {
                result = x.divide(result).add(result).shiftRight(1)
            }
        } else  // handle large numbers over 4.3322e127
        {
            val xLen = x.bitLength()
            val wantedPrecision = (xLen + 1) / 2
            val xLenMod = xLen + (xLen and 1) + 1

            //////// Do the first Sqrt on Hardware ////////
            val tempX = x.shiftRight(xLenMod - 63).toLong()
            val tempSqrt1 = Math.sqrt(tempX.toDouble())
            var valLong = java.lang.Double.doubleToLongBits(tempSqrt1) and 0x1fffffffffffffL
            if (valLong == 0L) valLong = 1L shl 53

            //////// Classic Newton Iterations ////////
            result = BigInteger.valueOf(valLong).shiftLeft(53 - 1)
                .add(x.shiftRight(xLenMod - 3 * 53).divide(BigInteger.valueOf(valLong)))
            var size = 106
            while (size < 256) {
                result =
                    result.shiftLeft(size - 1).add(x.shiftRight(xLenMod - 3 * size).divide(result))
                size = size shl 1
            }
            if (xAsDub > 4e254) // 4e254 = 1<<845.77 
            {
                val numOfNewtonSteps = 31 - Integer.numberOfLeadingZeros(wantedPrecision / size) + 1

                ////// Apply Starting Size ////////
                val wantedSize = (wantedPrecision shr numOfNewtonSteps) + 2
                val needToShiftBy = size - wantedSize
                result = result.shiftRight(needToShiftBy)
                size = wantedSize
                do {
                    //////// Newton Plus Iteration ////////
                    val shiftX = xLenMod - 3 * size
                    val valSqrd = result.multiply(result).shiftLeft(size - 1)
                    val valSU = x.shiftRight(shiftX).subtract(valSqrd)
                    result = result.shiftLeft(size).add(valSU.divide(result))
                    size *= 2
                } while (size < wantedPrecision)
            }
            result = result.shiftRight(size - wantedPrecision)
        }

        // Detect a round ups. This function can be further optimized - see article.
        // For a ~7% speed bump the following line can be removed but round-ups will occur.
        if (result.multiply(result).compareTo(x) > 0) result = result.subtract(BigInteger.ONE)

        // // Enabling the below will guarantee an error is stopped for larger numbers.
        // // Note: As of this writing, there are no known errors.
        // BigInteger tmp = val.multiply(val);
        // if (tmp.compareTo(x) > 0)  {
        //     System.out.println("val^2(" + val.multiply(val).toString() + ") >=  x(" + x.toString() + ")"); 
        //     System.console().readLine();
        //     //throw new Exception("Sqrt function had internal error - value too high");   
        // }
        // if (tmp.add(val.shiftLeft(1)).add(BigInteger.ONE).compareTo(x) <= 0) {
        //     System.out.println("(val+1)^2(" + val.add(BigInteger.ONE).multiply(val.add(BigInteger.ONE)).toString() + ") >=  x(" + x.toString() + ")"); 
        //     System.console().readLine();
        //     //throw new Exception("Sqrt function had internal error - value too low");    
        // }
        return result
    }

    fun sqrt(x: BigInteger): BigInteger {
        var half = BigInteger.ZERO.setBit(x.bitLength() / 2)
        var cur = half
        while (true) {
            val tmp = half.add(x.divide(half)).shiftRight(1)
            if (tmp == half || tmp == cur) return tmp
            cur = half
            half = tmp
        }
    }
}