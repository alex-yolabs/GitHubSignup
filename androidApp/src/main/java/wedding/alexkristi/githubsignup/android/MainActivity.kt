package wedding.alexkristi.githubsignup.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import network.CloudGitHubApi
import network.CloudGitHubValidationService
import network.PasswordValidationResult
import network.RepeatedPasswordValidationResult
import network.UsernameValidationResult
import signup.SignupViewModel
import utilities.Logger

@FlowPreview
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    private val gitHubApi = CloudGitHubApi()
    private val gitHubValidationService = CloudGitHubValidationService(gitHubApi)
    private val viewModel = SignupViewModel(gitHubApi, gitHubValidationService)
    private val logger = Logger("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val root = findViewById<ComposeView>(R.id.root)
        root.setContent {
            Column {
                val username: String by viewModel.username.collectAsState()
                val password: String by viewModel.password.collectAsState()
                val repeatedPassword: String by viewModel.repeatedPassword.collectAsState()
                val usernameValidationLabelIsHidden by viewModel.usernameValidationLabelIsHidden.collectAsState(initial = true)
                val passwordValidationLabelIsHidden by viewModel.passwordValidationLabelIsHidden.collectAsState(initial = true)
                val repeatedPasswordValidationLabelIsHidden by viewModel.repeatedPasswordValidationLabelIsHidden.collectAsState(initial = true)
                val usernameValidationText by viewModel.usernameValidationText.collectAsState(initial = "")
                val passwordValidationText by viewModel.passwordValidationText.collectAsState(initial = "")
                val repeatedPasswordValidationText by viewModel.repeatedPasswordValidationText.collectAsState(initial = "")
                val usernameValidationTextColor by viewModel.usernameValidationResult
                    .map {
                        when (it) {
                            UsernameValidationResult.EMPTY ->
                                android.graphics.Color.TRANSPARENT
                            UsernameValidationResult.OK ->
                                android.graphics.Color.GREEN
                            UsernameValidationResult.VALIDATING ->
                                android.graphics.Color.BLUE
                            UsernameValidationResult.WRONG_FORMAT, UsernameValidationResult.ALREADY_TAKEN ->
                                android.graphics.Color.RED
                            UsernameValidationResult.SERVICE_ERROR ->
                                android.graphics.Color.MAGENTA
                        }
                    }
                    .collectAsState(initial = android.graphics.Color.TRANSPARENT)
                val passwordValidationTextColor by viewModel.passwordValidationResult
                    .map {
                        when (it) {
                            PasswordValidationResult.EMPTY ->
                                android.graphics.Color.TRANSPARENT
                            PasswordValidationResult.OK ->
                                android.graphics.Color.GREEN
                            PasswordValidationResult.TOO_SHORT ->
                                android.graphics.Color.RED
                        }
                    }
                    .collectAsState(initial = android.graphics.Color.TRANSPARENT)
                val repeatedPasswordValidationTextColor by viewModel.repeatedPasswordValidationResult
                    .map {
                        when (it) {
                            RepeatedPasswordValidationResult.EMPTY ->
                                android.graphics.Color.TRANSPARENT
                            RepeatedPasswordValidationResult.OK ->
                                android.graphics.Color.GREEN
                            RepeatedPasswordValidationResult.DIFFERENT ->
                                android.graphics.Color.RED
                        }
                    }
                    .collectAsState(initial = android.graphics.Color.TRANSPARENT)
                val isSignUpButtonEnabled by viewModel.isSignUpButtonEnabled.collectAsState(initial = false)
                val isLoadingViewAnimating by viewModel.isLoadingViewAnimating.collectAsState(initial = false)
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        viewModel.onUsernameChanged(it)
                    },
                    label = {
                        Text("Username")
                    }
                )
                if (!usernameValidationLabelIsHidden) {
                    Text(
                        text = usernameValidationText,
                        style = TextStyle(
                            color = Color(usernameValidationTextColor)
                        )
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        viewModel.onPasswordChanged(it)
                    },
                    label = {
                        Text("Password")
                    }
                )
                if (!passwordValidationLabelIsHidden) {
                    Text(
                        text = passwordValidationText,
                        style = TextStyle(
                            color = Color(passwordValidationTextColor)
                        )
                    )
                }
                OutlinedTextField(
                    value = repeatedPassword,
                    onValueChange = {
                        viewModel.onRepeatedPasswordChanged(it)
                    },
                    label = {
                        Text("Repeated password")
                    }
                )
                if (!repeatedPasswordValidationLabelIsHidden) {
                    Text(
                        text = repeatedPasswordValidationText,
                        style = TextStyle(
                            color = Color(repeatedPasswordValidationTextColor)
                        )
                    )
                }
                Row {
                    Button(
                        enabled = isSignUpButtonEnabled,
                        onClick = {
                            viewModel.onSignUpButtonClicked()
                        }
                    ) {
                        Text("Sign Up")
                    }
                    if (isLoadingViewAnimating) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        viewModel.presentSignupSuccessPopupEvent
            .onEach {
                Toast.makeText(this, "✅ You have successfully signed up!", Toast.LENGTH_LONG).run {
                    show()
                }
            }
            .launchIn(MainScope())

        viewModel.presentNetworkFailurePopupEvent
            .onEach {
                Toast.makeText(this, "❌ Something went wrong, please try again later.", Toast.LENGTH_LONG).run {
                    show()
                }
            }
            .launchIn(MainScope())
    }
}
