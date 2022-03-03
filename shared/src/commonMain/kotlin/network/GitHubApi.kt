package network

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import utilities.Logger
import kotlin.random.Random

interface GitHubApi {
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun signUp(username: String, password: String): String
}

class CloudGitHubApi : GitHubApi {

    private val logger = Logger("CloudGitHubApi")

    private val httpClient = HttpClient()

    companion object {
        private const val GITHUB_ENDPOINT = "https://github.com"
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            delay(500)
            val urlString = "$GITHUB_ENDPOINT/$username"
            val response: HttpResponse = httpClient.get(urlString)
            response.status != HttpStatusCode.OK
        } catch(e: Exception) {
            if (Random.nextBoolean()) {
                true
            } else {
                throw IllegalStateException("Failed to determine availability of [$username].")
            }
        }
    }

    override suspend fun signUp(username: String, password: String): String {
        delay(1000)
        return if (Random.nextBoolean()) {
            username
        } else {
            throw IllegalStateException("Failed to sign up [$username].")
        }
    }
}
