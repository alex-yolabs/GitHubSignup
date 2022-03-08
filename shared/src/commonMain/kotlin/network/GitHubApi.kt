package network

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import utilities.Logger
import kotlin.random.Random

interface GitHubApi {
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    suspend fun signUp(username: String, password: String): Result<String>
}

class CloudGitHubApi : GitHubApi {

    private val logger = Logger("CloudGitHubApi")

    private val httpClient = HttpClient()

    companion object {
        private const val GITHUB_ENDPOINT = "https://github.com"
    }

    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return try {
            delay(500)
            val urlString = "$GITHUB_ENDPOINT/$username"
            val response: HttpResponse = httpClient.get(urlString)
            val isAvailable = response.status != HttpStatusCode.OK
            Result.success(isAvailable)
        } catch(e: Exception) {
            if (Random.nextBoolean()) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Failed to determine availability of [$username]."))
            }
        }
    }

    override suspend fun signUp(username: String, password: String): Result<String> {
        delay(1000)
        return if (Random.nextBoolean()) {
            Result.success(username)
        } else {
            Result.failure(IllegalStateException("Failed to sign up [$username]."))
        }
    }
}
