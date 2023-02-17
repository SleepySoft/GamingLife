package glcore

import org.junit.Test

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

    @Test
    fun testKeyPairSerializeAndDeserialize() {
        val quitFlag = mutableListOf(false)
        val encryption = GlEncryption().apply {
            createKeyPair(0, quitFlag)
        }

        val originalKeyPair = encryption.powKeyPair
        val keyPairSerialized = GlEncryption.serializeKeyPair(originalKeyPair)
        val keyPairDeserialized = GlEncryption.deserializeKeyPair(keyPairSerialized)

        val challenge = "Sleepy".toByteArray()
        assert(originalKeyPair.verify(challenge, keyPairDeserialized.sign(challenge)))
        assert(keyPairDeserialized.verify(challenge, originalKeyPair.sign(challenge)))

        assert(originalKeyPair.publicKeyBytes.contentEquals(keyPairDeserialized.publicKeyBytes))
        assert(!originalKeyPair.privateKeyBytes.contentEquals(keyPairDeserialized.privateKeyBytes))

        println("=> Original public key string:")
        println(originalKeyPair.publicKeyString)
        println("=> Deserialized public key string:")
        println(keyPairDeserialized.publicKeyString)

        println("=> Original private key string:")
        println(originalKeyPair.privateKeyString)
        println("=> Deserialized private key string:")
        println(keyPairDeserialized.privateKeyString)
    }
}
