package glcore

import org.junit.Test

internal class GlEncryptionTest {

    @Test
    fun testKeyPairWorkload() {
        val quitFlag = mutableListOf(false)
        GlEncryption().apply {
            val keyPair = this.createKeyPair(128, quitFlag)
            assert(keyPair != null)
            assert(keyPair?.publicKey != null)
            assert(keyPair?.publicKey?.encoded?.let { this.dataSha256(it)[0] } == 0xFF.toByte())
        }
    }
}