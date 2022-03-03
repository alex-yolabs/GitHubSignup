package signup

import utilities.asCommonFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import network.GitHubApi
import network.GitHubValidationService
import network.PasswordValidationResult
import network.RepeatedPasswordValidationResult
import network.UsernameValidationResult
import utilities.Logger
import utilities.throttleFirst

@FlowPreview
@ExperimentalCoroutinesApi
class SignupViewModel(
    private val gitHubApi: GitHubApi,
    private val gitHubValidationService: GitHubValidationService
) : ViewModel() {

    private val logger = Logger("SignUpViewModel")
    private val _username = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _repeatedPassword = MutableStateFlow("")
    private val _onSignUpButtonClicked = MutableSharedFlow<Unit>(0, 1, BufferOverflow.DROP_OLDEST)
    private val _onNetworkFailed = MutableSharedFlow<Throwable>(0, 1, BufferOverflow.DROP_OLDEST)
    private val _isSigningUp = MutableStateFlow(false)

    val usernameValidationResult = _username
        .debounce(500)
        .flatMapLatest { username ->
            gitHubValidationService.validateUsername(username)
                .catch {
                    _onNetworkFailed.tryEmit(it)
                }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        .asCommonFlow()

    val passwordValidationResult = _password
        .map { password ->
            gitHubValidationService.validatePassword(password)
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        .asCommonFlow()

    val repeatedPasswordValidationResult = _password
        .combine(_repeatedPassword) { password, repeatedPassword ->
            gitHubValidationService.validateRepeatedPassword(password, repeatedPassword)
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
        .asCommonFlow()

    val isSignUpButtonEnabled =
        merge(
            _username.map { false },
            combine(
                usernameValidationResult,
                passwordValidationResult,
                repeatedPasswordValidationResult,
                _isSigningUp
            ) { result1, result2, result3, signingUp ->
                result1 == UsernameValidationResult.OK &&
                        result2 == PasswordValidationResult.OK &&
                        result3 == RepeatedPasswordValidationResult.OK &&
                        !signingUp
            }
        )
            .distinctUntilChanged()
            .asCommonFlow()

    val usernameValidationText = usernameValidationResult
        .map { result ->
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
                UsernameValidationResult.SERVICE_ERROR ->
                    "Something went wrong, please try again later."
            }
        }
        .asCommonFlow()

    val passwordValidationText = passwordValidationResult
        .map { result ->
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

    val repeatedPasswordValidationText = repeatedPasswordValidationResult
        .map { result ->
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

    val usernameValidationLabelIsHidden = usernameValidationResult
        .map { it == UsernameValidationResult.EMPTY }
        .asCommonFlow()
    val passwordValidationLabelIsHidden = passwordValidationResult
        .map { it == PasswordValidationResult.EMPTY }
        .asCommonFlow()
    val repeatedPasswordValidationLabelIsHidden = repeatedPasswordValidationResult
        .map { it == RepeatedPasswordValidationResult.EMPTY }
        .asCommonFlow()

    val isLoadingViewAnimating = _isSigningUp.asCommonFlow()

    val presentSignupSuccessPopupEvent = _onSignUpButtonClicked
        .throttleFirst(500)
        .flatMapLatest {
            _isSigningUp.value = true
            flow {
                val username = gitHubApi.signUp(_username.value, _password.value)
                emit(username)
                _isSigningUp.value = false
            }.catch {
                _onNetworkFailed.tryEmit(it)
                _isSigningUp.value = false
            }
        }
        .asCommonFlow()

    val presentNetworkFailurePopupEvent = _onNetworkFailed
        .map { it.message }
        .asCommonFlow()

    fun onUsernameChanged(username: String) {
        _username.value = username.lowercase()
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onRepeatedPasswordChanged(repeatedPassword: String) {
        _repeatedPassword.value = repeatedPassword
    }

    fun onSignUpButtonClicked() {
        _onSignUpButtonClicked.tryEmit(Unit)
    }

}
