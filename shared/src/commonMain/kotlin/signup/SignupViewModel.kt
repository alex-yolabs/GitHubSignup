package signup

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import network.GitHubApi
import network.GitHubValidationService
import network.PasswordValidationResult
import network.RepeatedPasswordValidationResult
import network.UsernameValidationResult
import utilities.Logger
import utilities.getOrCatch

@FlowPreview
@ExperimentalCoroutinesApi
class SignupViewModel(
    private val gitHubApi: GitHubApi,
    private val gitHubValidationService: GitHubValidationService
) : ViewModel() {

    private val logger = Logger("SignUpViewModel")
    private val _username = MutableStateFlow(value = "")
    private val _password = MutableStateFlow(value = "")
    private val _repeatedPassword = MutableStateFlow(value = "")
    private val _onSignUpButtonClicked = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _onNetworkFailed = MutableSharedFlow<Throwable>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _isSigningUp = MutableStateFlow(value = false)

    // Inputs
    fun onUsernameChanged(username: String) {
        _username.value = username.trim()
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

    // Outputs
    val username = _username.asStateFlow()

    val password = _password.asStateFlow()

    val repeatedPassword = _repeatedPassword.asStateFlow()

    val isLoadingViewAnimating = _isSigningUp.asStateFlow()

    val usernameValidationResult = _username
        .debounce(500)
        .flatMapLatest { username ->
            gitHubValidationService.validateUsername(username)
                .getOrCatch {
                    _onNetworkFailed.tryEmit(it)
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UsernameValidationResult.EMPTY)

    val passwordValidationResult = _password
        .map(gitHubValidationService::validatePassword)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PasswordValidationResult.EMPTY)

    val repeatedPasswordValidationResult = _password
        .combine(_repeatedPassword, gitHubValidationService::validateRepeatedPassword)
        .stateIn(viewModelScope, SharingStarted.Eagerly, RepeatedPasswordValidationResult.EMPTY)

    val isSignUpButtonEnabled = merge(
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
        })
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val usernameValidationLabelIsHidden = usernameValidationResult
        .map { it == UsernameValidationResult.EMPTY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val passwordValidationLabelIsHidden = passwordValidationResult
        .map { it == PasswordValidationResult.EMPTY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val repeatedPasswordValidationLabelIsHidden = repeatedPasswordValidationResult
        .map { it == RepeatedPasswordValidationResult.EMPTY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val presentSignupSuccessPopupEvent = _onSignUpButtonClicked
        .flatMapConcat {
            _isSigningUp.value = true
            flow {
                gitHubApi.signUp(_username.value, _password.value)
                    .onSuccess {
                        emit(it)
                        _isSigningUp.value = false
                    }
                    .onFailure {
                        _onNetworkFailed.tryEmit(it)
                        _isSigningUp.value = false
                    }
            }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

    val presentNetworkFailurePopupEvent = _onNetworkFailed
        .map { it.message }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 0)
}
