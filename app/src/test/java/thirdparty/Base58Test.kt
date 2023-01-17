package thirdparty

import org.junit.Test

internal class Base58Test {

    fun doTestEncoding(data: ByteArray, base58: String) {
        val encoded = data.encodeToBase58String()
        if (encoded != base58) {
            println("Encoded string %s does match expect %s".format(encoded, base58))
            assert(false)
        }
    }

    fun doTestStringEncoding(text: String, base58: String) {
        doTestEncoding(text.toByteArray(), base58)
    }

    @Test
    fun testBase58BasicEncoding() {
        doTestStringEncoding("SleepySoft", "5gqfQHSRrRQ8hq")
        doTestStringEncoding("05d38226-d2f2-4304-b8fa-046ce7b07c30", "NERXrxemo7ozjwWDHxXq1TxqH71U3z8VwiDEfTstxDoPS7Kyy")
        doTestStringEncoding("0aac8ac7-735b-44ff-888c-b05210695639", "NJp7CT1GRMgXWHVJwwLejZTVN56Bggv4WsQ5vbZMAcpPBaEMN")
        println("testBase58BasicEncoding DONE")
    }

    fun doTestDecoding(base58: String, data: ByteArray) {
        val decoded = base58.decodeBase58()
        if (!decoded.contentEquals(data)) {
            println("Decoded data for %s does match expect".format(base58))
            assert(false)
        }
    }

    fun doTestStringDecoding(base58: String, text: String) {
        doTestDecoding(base58, text.toByteArray())
    }

    @Test
    fun testBase58BasicDecoding() {
        doTestStringDecoding("5gqfQHSRrRQ8hq", "SleepySoft")
        doTestStringDecoding("NERXrxemo7ozjwWDHxXq1TxqH71U3z8VwiDEfTstxDoPS7Kyy", "05d38226-d2f2-4304-b8fa-046ce7b07c30")
        doTestStringDecoding("NJp7CT1GRMgXWHVJwwLejZTVN56Bggv4WsQ5vbZMAcpPBaEMN", "0aac8ac7-735b-44ff-888c-b05210695639")
        println("testBase58BasicDecoding DONE")
    }
}