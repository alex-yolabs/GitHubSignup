package network

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay

interface GitHubApi {
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun signUp(username: String, password: String): Boolean
}

class CloudGitHubApi: GitHubApi {
    private val httpClient = HttpClient()

    companion object {
        private const val GITHUB_ENDPOINT = "https://github.com"
    }

    override suspend fun isUsernameAvailable(username: String): Boolean {
        val urlString = "$GITHUB_ENDPOINT/$username"
        val response: HttpResponse = httpClient.get(urlString)
        return response.status.value !in 200..299
    }

    override suspend fun signUp(username: String, password: String): Boolean {
        delay(1000)
        return true
    }
}
