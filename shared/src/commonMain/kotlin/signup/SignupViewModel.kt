package signup

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import network.GitHubApi
import network.GitHubValidationService

class SignupViewModel(
    private val gitHubApi: GitHubApi,
    private val gitHubValidationService: GitHubValidationService
): ViewModel() {

    private val _username = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _passwordRepeat = MutableStateFlow("")

    val username = _username.asStateFlow()
    val password = _password.asStateFlow()
    val passwordRepeat = _passwordRepeat.asStateFlow()
    val isUsernameValidationHidden = username.map { it.isEmpty() }
    val isPasswordValidationHidden = password.map { it.isEmpty() }
    val isPasswordRepeatValidationHidden = passwordRepeat.map { it.isEmpty() }

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onPasswordRepeatChanged(passwordRepeat: String) {
        _passwordRepeat.value = passwordRepeat
    }

}
