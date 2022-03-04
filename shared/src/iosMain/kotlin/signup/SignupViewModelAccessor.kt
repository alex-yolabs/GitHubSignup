package signup

import utilities.asCommonFlow

class SignupViewModelAccessor(private val viewModel: SignupViewModel) {

    val usernameValidationResult = viewModel.usernameValidationResult.asCommonFlow()
    val passwordValidationResult = viewModel.passwordValidationResult.asCommonFlow()
    val repeatedPasswordValidationResult = viewModel.repeatedPasswordValidationResult.asCommonFlow()
    val isSignUpButtonEnabled = viewModel.isSignUpButtonEnabled.asCommonFlow()
    val usernameValidationText = viewModel.usernameValidationText.asCommonFlow()
    val passwordValidationText = viewModel.passwordValidationText.asCommonFlow()
    val repeatedPasswordValidationText = viewModel.repeatedPasswordValidationText.asCommonFlow()
    val usernameValidationLabelIsHidden = viewModel.usernameValidationLabelIsHidden.asCommonFlow()
    val passwordValidationLabelIsHidden = viewModel.passwordValidationLabelIsHidden.asCommonFlow()
    val repeatedPasswordValidationLabelIsHidden = viewModel.repeatedPasswordValidationLabelIsHidden.asCommonFlow()
    val isLoadingViewAnimating = viewModel.isLoadingViewAnimating.asCommonFlow()
    val presentSignupSuccessPopupEvent = viewModel.presentSignupSuccessPopupEvent.asCommonFlow()
    val presentNetworkFailurePopupEvent = viewModel.presentNetworkFailurePopupEvent.asCommonFlow()

    fun onUsernameChanged(username: String) {
        viewModel.onUsernameChanged(username)
    }

    fun onPasswordChanged(password: String) {
        viewModel.onPasswordChanged(password)
    }

    fun onRepeatedPasswordChanged(repeatedPassword: String) {
        viewModel.onRepeatedPasswordChanged(repeatedPassword)
    }

    fun onSignUpButtonClicked() {
        viewModel.onSignUpButtonClicked()
    }
}