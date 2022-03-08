package network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import utilities.Logger

enum class UsernameValidationResult {
    EMPTY, VALIDATING, OK, WRONG_FORMAT, ALREADY_TAKEN, SERVICE_ERROR
}

enum class PasswordValidationResult {
    EMPTY, TOO_SHORT, OK
}

enum class RepeatedPasswordValidationResult {
    EMPTY, DIFFERENT, OK
}

interface GitHubValidationService {
    val minPasswordCount: Int
    fun validateUsername(username: String): Flow<Result<UsernameValidationResult>>
    fun validatePassword(password: String): PasswordValidationResult
    fun validateRepeatedPassword(password: String, repeatedPassword: String): RepeatedPasswordValidationResult
}

class CloudGitHubValidationService(
    private val gitHubApi: GitHubApi
): GitHubValidationService {

    private val logger = Logger("CloudGitHubValidationService")

    override val minPasswordCount = 6

    override fun validateUsername(username: String): Flow<Result<UsernameValidationResult>> = flow {
        if (username.isEmpty()) {
            emit(Result.success(UsernameValidationResult.EMPTY))
            return@flow
        }

        if (!username.matches("^[a-zA-Z0-9]*$".toRegex())) {
            emit(Result.success(UsernameValidationResult.WRONG_FORMAT))
            return@flow
        }

        emit(Result.success(UsernameValidationResult.VALIDATING))
        gitHubApi.isUsernameAvailable(username)
            .onSuccess {
                if (it) {
                    emit(Result.success(UsernameValidationResult.OK))
                } else {
                    emit(Result.success(UsernameValidationResult.ALREADY_TAKEN))
                }
            }
            .onFailure {
                logger.log("exception: $it")
                emit(Result.success(UsernameValidationResult.SERVICE_ERROR))
            }
    }

    override fun validatePassword(password: String): PasswordValidationResult {
        return when (password.count()) {
            0 -> PasswordValidationResult.EMPTY
            in 0 until minPasswordCount -> PasswordValidationResult.TOO_SHORT
            else -> PasswordValidationResult.OK
        }
    }

    override fun validateRepeatedPassword(password: String, repeatedPassword: String): RepeatedPasswordValidationResult {
        if (repeatedPassword.isEmpty()) {
            return RepeatedPasswordValidationResult.EMPTY
        }

        return if (repeatedPassword == password) {
            RepeatedPasswordValidationResult.OK
        } else {
            RepeatedPasswordValidationResult.DIFFERENT
        }
    }
}
