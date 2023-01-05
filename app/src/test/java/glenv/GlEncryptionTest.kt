package glcore

import glcore.toHexString
import glenv.GlEncryption
import org.junit.Test
import java.util.*
import kotlin.random.Random

internal class GlEncryptionTest {

    @Test
    fun testKeyPairGeneration() {
        val glEncryption1 = GlEncryption().apply { generateKeyPair() }
        val glEncryption2 = GlEncryption().apply { generateKeyPair() }

        println("----------------------- Encryption 1 -----------------------")
        println(glEncryption1.publicKeyString)
        println(glEncryption1.privateKeyString)

        println("----------------------- Encryption 2 -----------------------")
        println(glEncryption2.publicKeyString)
        println(glEncryption2.privateKeyString)

        assert(glEncryption1.publicKeyString != glEncryption2.publicKeyString)
        assert(glEncryption1.privateKeyString != glEncryption2.privateKeyString)
    }

    @Test
    fun testKeyAttachment() {
        val glEncryption1 = GlEncryption()
        val glEncryption2 = GlEncryption()

        glEncryption1.generateKeyPair()
        glEncryption2.publicKeyString = glEncryption1.publicKeyString
        glEncryption2.privateKeyString = glEncryption1.privateKeyString

        assert(glEncryption1.publicKey == glEncryption2.publicKey)
        assert(glEncryption1.privateKey == glEncryption2.privateKey)
    }

    @Test
    fun testInvalidKey() {
        val glEncryption = GlEncryption()
        assert(glEncryption.publicKey == null)
        assert(glEncryption.publicKeyString == "")
        assert(glEncryption.privateKey == null)
        assert(glEncryption.privateKeyString == "")
    }

    // -----------------------------------------------------------------

    @Test
    fun testPublicKeyEncryptionAndPrivateKeyDecryptionShortData() {
        val testString = "GamingLife by SleepySoft"
        val glEncryption = GlEncryption().apply { generateKeyPair() }
        val encryptedData = glEncryption.publicKeyEncrypt(testString.encodeToByteArray())
        val decryptionData = glEncryption.privateKeyDecrypt(encryptedData)
        assert(decryptionData.decodeToString() == testString)
    }

    @Test
    fun testPrivateKeyEncryptionAndPublicKeyDecryptionShortData() {
        val testString = "GamingLife by SleepySoft"
        val glEncryption = GlEncryption().apply { generateKeyPair() }
        val encryptedData = glEncryption.privateKeyEncrypt(testString.encodeToByteArray())
        val decryptionData = glEncryption.publicKeyDecrypt(encryptedData)
        assert(decryptionData.decodeToString() == testString)
    }

    // -----------------------------------------------------------------

    @Test
    fun testPublicKeyEncryptionAndPrivateKeyDecryptionLongData() {
        val testData = ByteArray(128000).apply { Random.nextBytes(this) }
        val glEncryption = GlEncryption().apply { generateKeyPair() }
        val encryptedData = glEncryption.publicKeyEncrypt(testData)
        val decryptionData = glEncryption.privateKeyDecrypt(encryptedData)

        // println("--------------------------------------------------------------------")
        // println(testData.toHexString())
        // println("--------------------------------------------------------------------")
        // println(decryptionData.toHexString())

        assert(decryptionData.contentEquals(testData))
    }

    @Test
    fun testPrivateKeyEncryptionAndPublicKeyDecryptionLongData() {
        val testData = ByteArray(128000).apply { Random.nextBytes(this) }
        val glEncryption = GlEncryption().apply { generateKeyPair() }
        val encryptedData = glEncryption.privateKeyEncrypt(testData)
        val decryptionData = glEncryption.publicKeyDecrypt(encryptedData)

        // println("--------------------------------------------------------------------")
        // println(testData.toHexString())
        // println("--------------------------------------------------------------------")
        // println(decryptionData.toHexString())

        assert(decryptionData.contentEquals(testData))
    }
}