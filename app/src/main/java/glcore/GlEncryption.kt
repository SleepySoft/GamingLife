package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import glenv.GlKeyPair
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import thirdparty.encodeToBase58String
import java.math.BigInteger
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import kotlin.experimental.and


class GlEncryption {
    var mutex = Mutex()

    var calcLoop = 0
        private set
        get() = runBlocking{ mutex.withLock { field } }

    var powOnLoop = 0
        private set
        get() = runBlocking{ mutex.withLock { field } }

    var keyPairPow = 0
        private set
        get() = runBlocking{ mutex.withLock { field } }

    var powKeyPair = GlKeyPair()
        private set
        get() = runBlocking{ mutex.withLock { field } }

    companion object {
        const val GLID_VERSION = 0
        const val KEYPAIR_VERSION_RSA = 1
        const val KEYPAIR_VERSION_RSA_CRT = 11
        const val KEYPAIR_VERSION_RSA_AUTO = 21

        fun glidFromPublicKey(keyPair: GlKeyPair): String {
            return keyPair.publicKey?.run {
                val pubKeySha = dataSha256(this.encoded)
                glidFromPublicKeyHash(pubKeySha)
            } ?: ""
        }

        fun glidFromPublicKeyHash(pubKeySha: ByteArray): String {
            val pubKeyShaWithVersion = byteArrayOf(GLID_VERSION.toByte()) + pubKeySha
            return pubKeyShaWithVersion.encodeToBase58String()
        }

        fun calcPoW(data: ByteArray) : Int {
            var sum = 0
            for (byte in data) {
                val byteSuffixBit1 = checkByteSuffixBit1(byte)
                sum += byteSuffixBit1
                if (byteSuffixBit1 != 8) {
                    break
                }
            }
            return sum
        }

        fun checkByteSuffixBit1(data: Byte) : Int =
            when {
                (data == 0xFF.toByte()) -> 8
                (data and 0x7F) == 0x7F.toByte() -> 7
                (data and 0x3F) == 0x3F.toByte() -> 6
                (data and 0x1F) == 0x1F.toByte() -> 5
                (data and 0x0F) == 0x0F.toByte() -> 4
                (data and 0x07) == 0x07.toByte() -> 3
                (data and 0x03) == 0x03.toByte() -> 2
                (data and 0x01) == 0x01.toByte() -> 1
                else -> 0
            }

        fun dataSha256(data: ByteArray): ByteArray {
            val md = MessageDigest.getInstance("SHA-256")
            return md.digest(data)
        }

        fun serializeKeyPair(keyPair: GlKeyPair, serializeVersion: Int = KEYPAIR_VERSION_RSA_AUTO) : String =
            if (keyPair.keyPairMatches()) {
                when (serializeVersion) {
                    KEYPAIR_VERSION_RSA -> serializeRSAKeyPair(keyPair)
                    KEYPAIR_VERSION_RSA_CRT -> serializeRSACRTKeyPair(keyPair)
                    KEYPAIR_VERSION_RSA_AUTO -> {
                        when (keyPair.privateKey) {
                            is RSAPrivateCrtKey -> serializeRSACRTKeyPair(keyPair)
                            is RSAPrivateKey -> serializeRSAKeyPair(keyPair)
                            else -> ""
                        }
                    }
                    else -> ""
                }
            } else {
                ""
            }

        private fun serializeRSAKeyPair(keyPair: GlKeyPair) : String {
            println("serializeRSAKeyPair")

            val rsaPub = keyPair.publicKey as RSAPublicKey
            val rsaPrv = keyPair.privateKey as RSAPrivateKey

            val modulus = rsaPrv.modulus                    // n
            val publicExponent = rsaPub.publicExponent      // e
            val privateExponent = rsaPrv.privateExponent    // d

            val versionBase64 = Base64.getEncoder().encodeToString(byteArrayOf(KEYPAIR_VERSION_RSA.toByte()))
            val modulusBase64 = Base64.getEncoder().encodeToString(modulus.toByteArray())
            val privateExponentBase64 = Base64.getEncoder().encodeToString(privateExponent.toByteArray())
            val publicExponentBase64 = Base64.getEncoder().encodeToString(publicExponent.toByteArray())

            // version|n|d|e
            return "$versionBase64|$modulusBase64|$privateExponentBase64|$publicExponentBase64"
        }

        private fun serializeRSACRTKeyPair(keyPair: GlKeyPair) : String {
            println("serializeRSACRTKeyPair")

            // https://stackoverflow.com/a/19819805
            // https://en.wikipedia.org/wiki/RSA_(cryptosystem)
            // https://crypto.stackexchange.com/questions/79604/private-exponent-on-rsa-key
            // https://crypto.stackexchange.com/questions/18031/how-to-find-modulus-from-a-rsa-public-key
            // https://crypto.stackexchange.com/questions/81615/calculating-rsa-public-modulus-from-private-exponent-and-public-exponent
            // https://www.tabnine.com/code/java/methods/java.security.interfaces.RSAPrivateCrtKey/getPrimeQ

            // Note: android.security.keystore2.AndroidKeyStoreRSAPrivateKey cannot be cast to java.security.interfaces.RSAPrivateKey

            val rsaPub = keyPair.publicKey as RSAPublicKey
            val rsaPrv = keyPair.privateKey as RSAPrivateKey
            val rsaPrvCrt = keyPair.privateKey as RSAPrivateCrtKey

            // ----------------------------------------------------------------

            val primeP = rsaPrvCrt.primeP                   // p
            val primeQ = rsaPrvCrt.primeQ                   // q
            val publicExponent = rsaPub.publicExponent      // e
            val privateExponent = rsaPrv.privateExponent    // d

            val versionBase64 = Base64.getEncoder().encodeToString(byteArrayOf(KEYPAIR_VERSION_RSA_CRT.toByte()))
            val primePBase64 = Base64.getEncoder().encodeToString(primeP.toByteArray())
            val primeQBase64 = Base64.getEncoder().encodeToString(primeQ.toByteArray())
            val publicExponentBase64 = Base64.getEncoder().encodeToString(publicExponent.toByteArray())
            val privateExponentBase64 = Base64.getEncoder().encodeToString(privateExponent.toByteArray())

            // version|p|q|d|e
            return "$versionBase64|$primePBase64|$primeQBase64|$privateExponentBase64|$publicExponentBase64"
        }

        fun deserializeKeyPair(keyPairStr: String) : GlKeyPair =
            try {
                val parts = keyPairStr.split("|")
                if (parts.size < 2) {
                    throw Exception("Invalid private key serialize format.")
                }

                val versionBytes = Base64.getDecoder().decode(parts[0])

                when (BigInteger(versionBytes).toInt()) {
                    KEYPAIR_VERSION_RSA -> deserializeRSAKeyPair(parts)
                    KEYPAIR_VERSION_RSA_CRT -> deserializeRSACRTKeyPair(parts)
                    KEYPAIR_VERSION_RSA_AUTO -> when (parts.size) {
                        4 -> deserializeRSAKeyPair(parts)
                        5 -> deserializeRSACRTKeyPair(parts)
                        else -> throw Exception("No recognise KeyPair format.")
                    }
                    else -> throw Exception("Unsupported KeyPair version.")
                }
            } catch (e: Exception) {
                GlLog.e(e.stackTraceToString())
                GlKeyPair()
            } finally {

            }

        private fun deserializeRSAKeyPair(parts: List< String >) : GlKeyPair {
            println("deserializeRSAKeyPair")

            if (parts.size != 4) {
                throw Exception("Error private key serialize format.")
            }

            val modulusBytes = Base64.getDecoder().decode(parts[1])
            val publicExponentBytes = Base64.getDecoder().decode(parts[3])
            val privateExponentBytes = Base64.getDecoder().decode(parts[2])

            val modulus = BigInteger(modulusBytes)
            val publicExponent = BigInteger(publicExponentBytes)
            val privateExponent = BigInteger(privateExponentBytes)

            val keyPair = GlKeyPair().apply {
                generateKeyPair(modulus, publicExponent, privateExponent)
            }

            return keyPair
        }

        private fun deserializeRSACRTKeyPair(parts: List< String >) : GlKeyPair {
            println("deserializeRSACRTKeyPair")

            if (parts.size != 5) {
                throw Exception("Error private key serialize format.")
            }

            val primePBytes = Base64.getDecoder().decode(parts[1])
            val primeQBytes = Base64.getDecoder().decode(parts[2])
            val publicExponentBytes = Base64.getDecoder().decode(parts[4])
            val privateExponentBytes = Base64.getDecoder().decode(parts[3])

            val primeP = BigInteger(primePBytes)
            val primeQ = BigInteger(primeQBytes)
            val publicExponent = BigInteger(publicExponentBytes)
            val privateExponent = BigInteger(privateExponentBytes)

            val keyPair = GlKeyPair().apply {
                generateCrtKeyPair(primeP, primeQ, publicExponent, privateExponent)
            }

            return keyPair
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createKeyPair(workloadProof: Int, quitFlag: List< Boolean >) : GlKeyPair? {
        var loop = 0
        var keyPair: GlKeyPair? = null

        while (quitFlag.isNotEmpty()) {
            if (quitFlag[0]) {
                keyPair = null
                break
            }

            var success = false

            keyPair = keyPair ?: GlKeyPair()
            keyPair.generateKeyPair()

            keyPair.publicKey?.run {
                loop += 1

                runBlocking {
                    mutex.withLock {
                        calcLoop = loop
                    }
                }

                val pubKeySha = dataSha256(this.encoded)
                val workloadVal = calcPoW(pubKeySha)

                if ((workloadVal > keyPairPow) || (workloadProof == 0)) {

                    runBlocking {
                        mutex.withLock {
                            keyPairPow = workloadVal
                            powKeyPair = keyPair
                            powOnLoop = loop
                        }
                    }

                    println("---------------------------------------------------------------")
                    println("Max POW in loop %d: %d".format(loop, keyPairPow))
                    println("  Public: " + keyPair.publicKeyString)
                    println("  Private: " + keyPair.privateKeyString)
                    println("  Pub Sha: " + pubKeySha.toHexString())
                    println("  GL ID  : " + glidFromPublicKeyHash(pubKeySha))
                    println("---------------------------------------------------------------")
                }

                if (workloadVal >= workloadProof) {
                    success = true
                    println("Workload proof reached.")
                    println("Calculation loop: %d".format(loop))
                    println("SHA256 of public key: " + pubKeySha.toHexString())
                }
            }

            if (success) {
                break
            }
        }
        return keyPair
    }
}