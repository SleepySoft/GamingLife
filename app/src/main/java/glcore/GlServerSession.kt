package glcore

import android.os.Build
import androidx.annotation.RequiresApi
import glenv.GlHttpRequest
import glenv.GlKeyPair
import java.util.Base64


class GlServerSession(
    val httpRequest: GlHttpRequest) {

    var glId: String = ""
    var loginToken: String = ""
    var userNumber: Long = -1
    var proofOfQualifications: Long = 0

    companion object {
        const val REST_ROOT = "rest/"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun register(version: Int, keyPair: GlKeyPair) : Boolean {
        val challenge = getRandomChallenge()
        if (challenge.isEmpty()) {
            return false
        }

        val challengeBytes = keyPair.privateKeyEncrypt(challenge.encodeToByteArray())
        val challengeBase64 = Base64.getEncoder().encodeToString(challengeBytes)

        val request = mapOf< String , String >(
            "version" to version.toString(),
            "public_key" to keyPair.publicKeyString,
            "random_challenge" to challengeBase64
        )

        val response = httpRequest.postParams("$REST_ROOT/register", request)
        val responseDict = GlHttpRequest.responseDict(response)

        return try {
            glId = responseDict["glid"] ?: ""
            loginToken = responseDict["login_ token"] ?: ""
            userNumber = responseDict["user_number"]?.toLong() ?: -1
            proofOfQualifications = responseDict["proof_of_qualifications"]?.toLong() ?: 0
            loginToken.isNotEmpty()
        } catch (e: Exception) {
            println(e)
            e.printStackTrace()
            false
        } finally {

        }
    }

    fun getRandomChallenge(): String {
        val response = httpRequest.get("$REST_ROOT/random_challenge")
        return GlHttpRequest.responseString(response)
    }
}
