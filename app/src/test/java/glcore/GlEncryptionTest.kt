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
        val keyPairSerialized = GlEncryption.serializeKeyPair(encryption.powKeyPair)
    }
}
