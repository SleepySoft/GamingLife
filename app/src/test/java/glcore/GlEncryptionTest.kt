package glcore

import glenv.GlKeyPair
import glenv.KeyPairUtility
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPairGenerator
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
        KeyPairUtility.dumpRsaKeyPairInfo(originalKeyPair.toJavaKeyPair())

        val keyPairSerialized = KeyPairUtility.serializeKeyPair(originalKeyPair.toJavaKeyPair())
        val keyPairDeserialized = KeyPairUtility.deserializeKeyPair(keyPairSerialized)
        val glKeyPairDeserialized = GlKeyPair().apply { fromJavaKeyPair(keyPairDeserialized) }

        val challenge = "Sleepy".toByteArray()
        val result =
            originalKeyPair.publicKey!!.encoded.contentEquals(glKeyPairDeserialized.publicKey!!.encoded) &&
            originalKeyPair.privateKey!!.encoded.contentEquals(glKeyPairDeserialized.privateKey!!.encoded) &&
            originalKeyPair.verify(challenge, glKeyPairDeserialized.sign(challenge)) &&
            glKeyPairDeserialized.verify(challenge, originalKeyPair.sign(challenge))

        if (!result) {
            println("==========================================================================")
            println("Fail serialize key pair.")

            println("=> KeyPairSerialized:")
            println(keyPairSerialized)

            println("=> Original public key string:")
            println(originalKeyPair.publicKeyString)
            println("=> Deserialized public key string:")
            println(glKeyPairDeserialized.publicKeyString)

            println("=> Original private key string:")
            println(originalKeyPair.privateKeyString)
            println("=> Deserialized private key string:")
            println(glKeyPairDeserialized.privateKeyString)

            println("==========================================================================")
        }

        return result
    }

    @Test
    fun testKeyPairSerializeAndDeserialize() {
        var loop = 0
        var failCount = 0
        var successCount = 0
        for (i in 0 until 10) {
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

    private fun computeCarmichaelLambda(p: BigInteger, q: BigInteger): BigInteger {
        return lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE))
    }

    private fun lcm(x: BigInteger, y: BigInteger): BigInteger {
        return x.multiply(y).divide(x.gcd(y))
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

            // ----------------------------------------------------------------

            val p = primeP
            val q = primeQ
            val e = pubExponent

            // ----------------------------------------------------------------

            val n = p.multiply(q)
            assert(n == pubModulus)
            assert(n == prvModulus)

            val p_1 = p.subtract(BigInteger.ONE)
            val q_1 = p.subtract(BigInteger.ONE)
            val phi = computeCarmichaelLambda(p, q)

            assert(e == BigInteger.valueOf(65537))
            assert(e.gcd(phi).equals(BigInteger.ONE))
            assert(prvExponent.multiply(e).mod(phi) == BigInteger.ONE)

            val d = e.modInverse(phi)

            assert(d.multiply(e).mod(phi) == BigInteger.ONE)
            // assert(d == prvExponent)

            val dp = d.mod(p_1)
            // assert(dp == primeExponentP)

            val dq = d.mod(q_1)
            // assert(dq == primeExponentQ)

            val coeff = q.modInverse(p)
            // assert(coeff == crtCoefficient)
        }
    }

    @Test
    fun testRSAKeyPairAndRSACRTKeypair() {
        val generator = KeyPairGenerator.getInstance("RSA")
        val keyPair = generator.genKeyPair()
        KeyPairUtility.verifyKeyPair(keyPair)
    }
}
