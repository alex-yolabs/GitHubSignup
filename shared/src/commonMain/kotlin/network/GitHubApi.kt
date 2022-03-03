package network

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import utilities.Logger
import kotlin.random.Random

interface GitHubApi {
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun signUp(username: String, password: String): Boolean
}

class CloudGitHubApi: GitHubApi {

    private val logger = Logger("CloudGitHubApi")

    private val httpClient = HttpClient()

    companion object {
        private const val GITHUB_ENDPOINT = "https://github.com"
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        val urlString = "$GITHUB_ENDPOINT/$username"
        return try {
            delay(500)
            val response: HttpResponse = httpClient.get(urlString)
            response.status.value !in 200..299
        } catch(e: Exception) {
            true
        }
    }

    override suspend fun signUp(username: String, password: String): Boolean {
        delay(1000)
        return Random.nextBoolean()
    }
}
