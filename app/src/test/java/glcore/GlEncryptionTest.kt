package glcore

import glenv.GlKeyPair
import org.junit.Test
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

internal class GlEncryptionTest {

    @Test
    fun testKeyPairWorkload() {
        val quitFlag = mutableListOf(false)
        GlEncryption().apply {
            val keyPair = this.createKeyPair(8, quitFlag)
            assert(keyPair != null)
            assert(keyPair?.publicKey != null)
            assert(keyPair?.publicKey?.encoded?.let { GlEncryption.dataSha256(it)[0] } == 0xFF.toByte())
        }
    }

    fun verifyKeyPairSerializeAndDeserialize() : Boolean {
        val originalKeyPair = GlKeyPair().apply { generateKeyPair() }
        originalKeyPair.dumpRsaKeyPairInfo()

        val keyPairSerialized = GlEncryption.serializeKeyPair(originalKeyPair)
        val keyPairDeserialized = GlEncryption.deserializeKeyPair(keyPairSerialized)

        val challenge = "Sleepy".toByteArray()
        val result =
            originalKeyPair.publicKeyBytes.contentEquals(keyPairDeserialized.publicKeyBytes) &&
            originalKeyPair.privateKeyBytes.contentEquals(keyPairDeserialized.privateKeyBytes) &&
            originalKeyPair.verify(challenge, keyPairDeserialized.sign(challenge)) &&
            keyPairDeserialized.verify(challenge, originalKeyPair.sign(challenge))

        if (!result) {
            println("==========================================================================")
            println("Fail serialize key pair.")

            println("=> KeyPairSerialized:")
            println(keyPairSerialized)

            println("=> Original public key string:")
            println(originalKeyPair.publicKeyString)
            println("=> Deserialized public key string:")
            println(keyPairDeserialized.publicKeyString)

            println("=> Original private key string:")
            println(originalKeyPair.privateKeyString)
            println("=> Deserialized private key string:")
            println(keyPairDeserialized.privateKeyString)

            println("==========================================================================")
        }

        return result
    }

    @Test
    fun testKeyPairSerializeAndDeserialize() {
        var loop = 0
        var failCount = 0
        var successCount = 0
        for (i in 0 until 100000) {
            loop += 1
            println("Loop: $loop")

            if (verifyKeyPairSerializeAndDeserialize()) {
                successCount += 1
            } else {
                failCount += 1
            }
        }
        println("Success: $successCount ; Fail: $failCount .")
    }

    @Test
    fun testCrtParametersFromPQE() {
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        with(glKeyPair) {

            val rsaPub = publicKey as RSAPublicKey
            val rsaPrv = privateKey as RSAPrivateKey
            val rsaPrvCrt = privateKey as RSAPrivateCrtKey

            val pubModulus = rsaPub.modulus
            val pubExponent = rsaPub.publicExponent

            val prvModulus = rsaPrv.modulus
            val prvExponent = rsaPrv.privateExponent

            // ----------------------------------------------------------------

            val primeP = rsaPrvCrt.primeP
            val primeQ = rsaPrvCrt.primeQ
            val primeExponentP = rsaPrvCrt.primeExponentP
            val primeExponentQ = rsaPrvCrt.primeExponentQ
            val crtCoefficient = rsaPrvCrt.crtCoefficient
        }
    }
}
