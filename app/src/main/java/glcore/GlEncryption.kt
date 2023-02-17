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
        const val KEYPAIR_VERSION = 1

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

        @RequiresApi(Build.VERSION_CODES.O)
        fun serializeKeyPair(keyPair: GlKeyPair) : String =
            if (keyPair.keyPariMatches()) {
                // https://stackoverflow.com/a/19819805
                // https://en.wikipedia.org/wiki/RSA_(cryptosystem)
                // https://crypto.stackexchange.com/questions/79604/private-exponent-on-rsa-key
                // https://crypto.stackexchange.com/questions/18031/how-to-find-modulus-from-a-rsa-public-key
                // https://crypto.stackexchange.com/questions/81615/calculating-rsa-public-modulus-from-private-exponent-and-public-exponent

                val rsaPub = keyPair.publicKey as RSAPublicKey
                val rsaPrv = keyPair.privateKey as RSAPrivateKey

                // pubModulus == prvModulus
                // val pubModulus = rsaPub.modulus
                val pubExponent = rsaPub.publicExponent

                val prvModulus = rsaPrv.modulus
                val prvExponent = rsaPrv.privateExponent

                val versionBase64 = Base64.getEncoder().encodeToString(byteArrayOf(KEYPAIR_VERSION.toByte()))
                val modulusBase64 = Base64.getEncoder().encodeToString(prvModulus.toByteArray())
                val prvExponentBase64 = Base64.getEncoder().encodeToString(prvExponent.toByteArray())
                val pubExponentBase64 = Base64.getEncoder().encodeToString(pubExponent.toByteArray())

                "$versionBase64|$modulusBase64|$prvExponentBase64|$pubExponentBase64"
            } else {
                ""
            }

        @RequiresApi(Build.VERSION_CODES.O)
        fun deserializeKeyPair(keyPairStr: String) : GlKeyPair =
            try {
                val parts = keyPairStr.split("|")
                if (parts.size != 4) {
                    throw Exception("Error private key serialize format.")
                }

                val versionBytes = Base64.getDecoder().decode(parts[0])

                when (BigInteger(versionBytes).toInt()) {
                    KEYPAIR_VERSION -> {
                        val modulusBytes = Base64.getDecoder().decode(parts[1])
                        val prvExponentBytes = Base64.getDecoder().decode(parts[2])
                        val pubExponentBytes = Base64.getDecoder().decode(parts[3])

                        val modulus = BigInteger(modulusBytes)
                        val pubExponent = BigInteger(pubExponentBytes)
                        val prvExponent = BigInteger(prvExponentBytes)

                        val keyPair = GlKeyPair().apply {
                            generateKeyPair(modulus, pubExponent, prvExponent)
                        }

                        keyPair
                    }
                    else -> throw Exception("Unsupported KeyPair version.")
                }
            } catch (e: Exception) {
                GlLog.e(e.stackTraceToString())
                GlKeyPair()
            } finally {

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