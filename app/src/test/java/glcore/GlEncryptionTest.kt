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

    fun verifyKeyPairSerializeAndDeserialize() : Boolean {
        val quitFlag = mutableListOf(false)
        val encryption = GlEncryption().apply {
            createKeyPair(0, quitFlag)
        }

        val originalKeyPair = encryption.powKeyPair
        val keyPairSerialized = GlEncryption.serializeKeyPair(originalKeyPair)
        // val keyPairSerialized = "AQ==|AL5/+Xo4qCxUduokaojuv3Gz9cJZZmT29qFKHojsufZFWVr7SfRQ9VK6MmDSzSsBzkCMXkYJk75pugasJw3EkCHJWQ9Bf+g4B37USjLnbUkfk5bldEiaq+ldXb1GjVSVNTXdYvcUreb2ndA2HA+g2X98ruk24N9UZB6qeE727wVbg4NCWyrH1IiCtpf2aJt16sDg2AX+G1ykiw+GNwI+8Z2qU14cY2cFMCCbxs47013uBrABieDzC2K40MioSPmTDUATRbj63o3Kppqv9s2J1VTZSUX35TG4duVk5CEN92sXop4bUzZ7hhG+ePekECm9SdV9ofgsmQeA2e09P3wvLi8=|AOjWgmPHD1/sQ2we40sUhR2OJLpNivNDTfwItXzZdHINt6mWFE49cmj3xZuScpkEtZOV/aQ2qhGSKE1l6e+dN1p62N/mSIlzJU/F9oR0jDiHRipC45Fz4atAjJ9Ltgwr7JDQIKYSIHvfJZ4+HkFiPttHChw13aSzzhITsF86C+uX7LJxILzbxjUJC/TA8Gi7Gzf8XpVFPNUKDkFNQYdcpOlL1MNXRxt+7b/GxkgqKEMfcqE/hy06S/55skmXLFGixFhbhN0oe2S3476oHqLCuz0qr1HN61JQh0+sN69lVw33d90SBl/Vy+HvtenqxcIBhzH406EXFyydd59PW+UpeQ==|AQAB"
        val keyPairDeserialized = GlEncryption.deserializeKeyPair(keyPairSerialized)

        val challenge = "Sleepy".toByteArray()
        val result =
            originalKeyPair.verify(challenge, keyPairDeserialized.sign(challenge)) &&
            keyPairDeserialized.verify(challenge, originalKeyPair.sign(challenge)) &&
            originalKeyPair.publicKeyBytes.contentEquals(keyPairDeserialized.publicKeyBytes) &&
            originalKeyPair.privateKeyBytes.contentEquals(keyPairDeserialized.privateKeyBytes)

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
}
