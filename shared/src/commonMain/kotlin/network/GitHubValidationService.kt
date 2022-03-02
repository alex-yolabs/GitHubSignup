package network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class UsernameValidationResult {
    EMPTY, VALIDATING, OK, WRONG_FORMAT, ALREADY_TAKEN
}

enum class PasswordValidationResult {
    EMPTY, TOO_SHORT, OK
}

enum class RepeatedPasswordValidationResult {
    EMPTY, DIFFERENT, OK
}

interface GitHubValidationService {
    val minPasswordCount: Int
    fun validateUsername(username: String): Flow<UsernameValidationResult>
    fun validatePassword(password: String): PasswordValidationResult
    fun validateRepeatedPassword(password: String, repeatedPassword: String): RepeatedPasswordValidationResult
}

class CloudGitHubValidationService(
    private val gitHubApi: GitHubApi
): GitHubValidationService {

    override val minPasswordCount = 6

    override fun validateUsername(username: String): Flow<UsernameValidationResult> = flow {
        if (username.isEmpty()) {
            emit(UsernameValidationResult.EMPTY)
            return@flow
        }

        if (!username.matches("^[a-zA-Z0-9]*$".toRegex())) {
            emit(UsernameValidationResult.WRONG_FORMAT)
            return@flow
        }

        emit(UsernameValidationResult.VALIDATING)
        if (gitHubApi.isUsernameAvailable(username)) {
            emit(UsernameValidationResult.OK)
        } else {
            emit(UsernameValidationResult.ALREADY_TAKEN)
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

        if (repeatedPassword == password) {
            return RepeatedPasswordValidationResult.OK
        } else {
            return RepeatedPasswordValidationResult.DIFFERENT
        }
    }
}
