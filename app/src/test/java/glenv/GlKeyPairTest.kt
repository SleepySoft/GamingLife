package glcore

import glenv.GlKeyPair
import glenv.KeyPairUtility
import org.junit.Test
import java.util.*
import kotlin.random.Random

internal class GlKeyPairTest {

    @Test
    fun testKeyPairGeneration() {
        val glKeyPair1 = GlKeyPair().apply { generateKeyPair() }
        val glKeyPair2 = GlKeyPair().apply { generateKeyPair() }

        println("----------------------- Encryption 1 -----------------------")
        println(glKeyPair1.publicKeyString)
        println(glKeyPair1.privateKeyString)

        println("----------------------- Encryption 2 -----------------------")
        println(glKeyPair2.publicKeyString)
        println(glKeyPair2.privateKeyString)

        assert(glKeyPair1.publicKeyString != glKeyPair2.publicKeyString)
        assert(glKeyPair1.privateKeyString != glKeyPair2.privateKeyString)
    }

    @Test
    fun testKeyAttachment() {
        val glKeyPair1 = GlKeyPair()
        val glKeyPair2 = GlKeyPair()

        glKeyPair1.generateKeyPair()
        glKeyPair2.publicKeyString = glKeyPair1.publicKeyString
        glKeyPair2.privateKeyString = glKeyPair1.privateKeyString

        assert(glKeyPair1.publicKey == glKeyPair2.publicKey)
        assert(glKeyPair1.privateKey == glKeyPair2.privateKey)
    }

    @Test
    fun testInvalidKey() {
        val glKeyPair = GlKeyPair()
        assert(glKeyPair.publicKey == null)
        assert(glKeyPair.publicKeyString == "")
        assert(glKeyPair.privateKey == null)
        assert(glKeyPair.privateKeyString == "")
    }

    // https://medium.com/@wujingwe/write-unit-test-which-has-androidkeystore-dependency-f12181ae6311

/*    @Test
    fun testLocalKeyPair() {
        val glKeyPair1 = GlKeyPair().apply { generateLocalKeyPair("TestLocalKey") }
        val glKeyPair2 = GlKeyPair().apply { loadLocalKeyPair("TestLocalKey") }

        assert(glKeyPair1.publicKey == glKeyPair2.publicKey)
        assert(glKeyPair1.privateKey == glKeyPair2.privateKey)
    }*/

    // -----------------------------------------------------------------

    @Test
    fun testPublicKeyEncryptionAndPrivateKeyDecryptionShortData() {
        val testString = "GamingLife by SleepySoft"
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        val encryptedData = glKeyPair.publicKeyEncrypt(testString.encodeToByteArray())
        val decryptionData = glKeyPair.privateKeyDecrypt(encryptedData)
        assert(decryptionData.decodeToString() == testString)
    }

    @Test
    fun testPrivateKeyEncryptionAndPublicKeyDecryptionShortData() {
        val testString = "GamingLife by SleepySoft"
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        val encryptedData = glKeyPair.privateKeyEncrypt(testString.encodeToByteArray())
        val decryptionData = glKeyPair.publicKeyDecrypt(encryptedData)
        assert(decryptionData.decodeToString() == testString)
    }

    // -----------------------------------------------------------------

    @Test
    fun testPublicKeyEncryptionAndPrivateKeyDecryptionLongData() {
        val testData = ByteArray(128000).apply { Random.nextBytes(this) }
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        val encryptedData = glKeyPair.publicKeyEncrypt(testData)
        val decryptionData = glKeyPair.privateKeyDecrypt(encryptedData)

        // println("--------------------------------------------------------------------")
        // println(testData.toHexString())
        // println("--------------------------------------------------------------------")
        // println(decryptionData.toHexString())

        assert(decryptionData.contentEquals(testData))
    }

    @Test
    fun testPrivateKeyEncryptionAndPublicKeyDecryptionLongData() {
        val testData = ByteArray(128000).apply { Random.nextBytes(this) }
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        val encryptedData = glKeyPair.privateKeyEncrypt(testData)
        val decryptionData = glKeyPair.publicKeyDecrypt(encryptedData)

        // println("--------------------------------------------------------------------")
        // println(testData.toHexString())
        // println("--------------------------------------------------------------------")
        // println(decryptionData.toHexString())

        assert(decryptionData.contentEquals(testData))
    }

    @Test
    fun testSignAndVerify() {
        val message = ByteArray(128000).apply { Random.nextBytes(this) }
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        val sign = glKeyPair.sign(message)
        val verified = glKeyPair.verify(message, sign)

        message[0] = (message[0] - 1).toByte()
        val notVerified = !glKeyPair.verify(message, sign)

        assert(verified)
        assert(notVerified)
    }

    // -------------------------------------------------------------------------------------

    @Test
    fun testKeyPairInfo() {
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }
        KeyPairUtility.dumpRsaKeyPairInfo(glKeyPair.toJavaKeyPair())
    }

    @Test
    fun testDumpDataForPythonSide() {
        val data = "SleepySoft".toByteArray()
        val glKeyPair = GlKeyPair().apply { generateKeyPair() }

        println("============================================================================")

        println("Public key serialized:")
        println(glKeyPair.publicKeyString)

        println("Test data base64: ")
        println(Base64.getEncoder().encodeToString(data))

        val dataEncrypted = glKeyPair.privateKeyEncrypt(data)
        println("Encrypted data base64:")
        println(Base64.getEncoder().encodeToString(dataEncrypted))

        val dataSigned = glKeyPair.sign(data)
        println("Signed data base64:")
        println(Base64.getEncoder().encodeToString(dataSigned))

        println("============================================================================")
    }
}