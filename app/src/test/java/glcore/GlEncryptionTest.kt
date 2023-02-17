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
    }
}
