package signup

import utilities.asCommonFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import network.GitHubApi
import network.GitHubValidationService
import network.PasswordValidationResult
import network.RepeatedPasswordValidationResult
import network.UsernameValidationResult

class SignupViewModel(
    private val gitHubApi: GitHubApi,
    private val gitHubValidationService: GitHubValidationService
): ViewModel() {

    private val _username = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _repeatedPassword = MutableStateFlow("")
    private val _onSignUpButtonClicked = MutableSharedFlow<Unit>()
    private val _signingUp = MutableStateFlow(false)

    private val usernameValidationResult = _username.flatMapLatest { username ->
        gitHubValidationService.validateUsername(username)
    }
    private val passwordValidationResult = _password.map { password ->
        gitHubValidationService.validatePassword(password)
    }
    private val repeatedPasswordValidationResult = _password.combine(_repeatedPassword) { password, repeatedPassword ->
        gitHubValidationService.validateRepeatedPassword(password, repeatedPassword)
    }

    val isSignInButtonEnabled = combine(
        usernameValidationResult,
        passwordValidationResult,
        repeatedPasswordValidationResult,
        _signingUp
    ) { result1, result2, result3, signingUp ->
        result1 == UsernameValidationResult.OK &&
                result2 == PasswordValidationResult.OK &&
                result3 == RepeatedPasswordValidationResult.OK &&
                !signingUp
    }
        .distinctUntilChanged()
        .asCommonFlow()

    val usernameValidationText = usernameValidationResult.map { result ->
        when (result) {
            UsernameValidationResult.EMPTY ->
                ""
            UsernameValidationResult.VALIDATING ->
                "Validating username"
            UsernameValidationResult.OK ->
                "Username available"
            UsernameValidationResult.WRONG_FORMAT ->
                "Username can only contain numbers or digits"
            UsernameValidationResult.ALREADY_TAKEN ->
                "Username already taken"
        }
    }
        .asCommonFlow()

    val passwordValidationText = passwordValidationResult.map { result ->
        when (result) {
            PasswordValidationResult.EMPTY ->
                ""
            PasswordValidationResult.OK ->
                "Password acceptable"
            PasswordValidationResult.TOO_SHORT ->
                "Password must be at least ${gitHubValidationService.minPasswordCount} characters"
        }
    }
        .asCommonFlow()

    val repeatedPasswordValidationText = repeatedPasswordValidationResult.map { result ->
        when (result) {
            RepeatedPasswordValidationResult.EMPTY ->
                ""
            RepeatedPasswordValidationResult.OK ->
                "Password repeated"
            RepeatedPasswordValidationResult.DIFFERENT ->
                "Password different"
        }
    }
        .asCommonFlow()

    val isUsernameValidationLabelHidden = usernameValidationText.map { it.isEmpty() }.asCommonFlow()
    val isPasswordValidationLabelHidden = passwordValidationText.map { it.isEmpty() }.asCommonFlow()
    val isRepeatedPasswordValidationLabelHidden = repeatedPasswordValidationText.map { it.isEmpty() }.asCommonFlow()

    val signedIn = _onSignUpButtonClicked.flatMapLatest {
        flow {
            _signingUp.value = true
            emit(gitHubApi.signUp(_username.value, _password.value))
            _signingUp.value = false
        }
    }
        .asCommonFlow()

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onRepeatedPasswordChanged(repeatedPassword: String) {
        _repeatedPassword.value = repeatedPassword
    }

    fun onSignInButtonClicked() {
        _onSignUpButtonClicked.tryEmit(Unit)
    }

}
