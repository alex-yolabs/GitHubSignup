//
//  SignupViewController.swift
//  iosApp
//
//  Created by Alex Huang on 3/2/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import shared
import Auth0

class SignupViewController: UIViewController {

    private let viewModel = SignupViewModelAccessor(
        viewModel: SignupViewModel(
            gitHubApi: gitHubApi,
            gitHubValidationService: gitHubValidationService
        )
    )

    private lazy var usernameTextField: UITextField = {
        let tf = UITextField()
        tf.placeholder = "Username"
        tf.borderStyle = .bezel
        tf.addTarget(self, action: #selector(usernameTextxFieldDidChange(textField:)), for: .editingChanged)
        return tf
    }()

    private lazy var passwordTextField: UITextField = {
        let tf = UITextField()
        tf.placeholder = "Password"
        tf.borderStyle = .bezel
        tf.addTarget(self, action: #selector(passwordTextxFieldDidChange(textField:)), for: .editingChanged)
        return tf
    }()

    private lazy var repeatedPasswordTextField: UITextField = {
        let tf = UITextField()
        tf.placeholder = "Repeated password"
        tf.borderStyle = .bezel
        tf.addTarget(self, action: #selector(repeatedPasswordTextxFieldDidChange(textField:)), for: .editingChanged)
        return tf
    }()

    private lazy var usernameValidationLabel: UILabel = {
        let lb = UILabel()
        lb.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        lb.textColor = .black
        return lb
    }()

    private lazy var passwordValidationLabel: UILabel = {
        let lb = UILabel()
        lb.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        lb.textColor = .black
        return lb
    }()

    private lazy var repeatedPasswordValidationLabel: UILabel = {
        let lb = UILabel()
        lb.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        lb.textColor = .black
        return lb
    }()

    private lazy var signUpButton: UIButton = {
        let btn = UIButton(type: .system)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .bold)
        btn.setTitle("Sign Up", for: .normal)
        btn.addTarget(self, action: #selector(didTapSignUpButton(button:)), for: .touchUpInside)
        return btn
    }()

    private lazy var loadingView: UIActivityIndicatorView = {
        let view = UIActivityIndicatorView()
        return view
    }()

    private lazy var signUpView: UIView = {
        let v = UIView()
        v.addSubview(signUpButton)
        v.addSubview(loadingView)
        signUpButton.snp.makeConstraints {
            $0.top.bottom.equalToSuperview()
            $0.centerX.equalToSuperview()
            $0.left.greaterThanOrEqualToSuperview()
        }
        loadingView.snp.makeConstraints {
            $0.top.bottom.equalToSuperview()
            $0.left.equalTo(signUpButton.snp.right).offset(10)
            $0.right.lessThanOrEqualToSuperview()
        }
        return v
    }()

    private lazy var stackView: UIStackView = {
        let sv = UIStackView(arrangedSubviews: [
            usernameTextField,
            usernameValidationLabel,
            passwordTextField,
            passwordValidationLabel,
            repeatedPasswordTextField,
            repeatedPasswordValidationLabel,
            signUpView
        ])
        sv.axis = .vertical
        sv.distribution = .fill
        sv.alignment = .fill
        sv.spacing = 10
        return sv
    }()

    private lazy var logInButton: UIButton = {
        let btn = UIButton(type: .system)
        btn.setTitle("Log In", for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .regular)
        btn.addTarget(self, action: #selector(didTapLogInButton(button:)), for: .touchUpInside)
        return btn
    }()

    private lazy var logOutButton: UIButton = {
        let btn = UIButton(type: .system)
        btn.setTitle("Log Out", for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .regular)
        btn.addTarget(self, action: #selector(didTapLogOutButton(button:)), for: .touchUpInside)
        return btn
    }()

    private let bag = CloseBag()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
        setupBinding()
    }

    private func setupView() {
        view.backgroundColor = .white
        view.addSubview(stackView)
        view.addSubview(logInButton)
        view.addSubview(logOutButton)
        stackView.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(20)
            $0.left.right.equalToSuperview().inset(20)
        }
        logInButton.snp.makeConstraints {
            $0.center.equalToSuperview()
        }
        logOutButton.snp.makeConstraints {
            $0.centerX.equalToSuperview()
            $0.top.equalTo(logInButton.snp.bottom)
        }
    }

    private func setupBinding() {
        viewModel.usernameValidationText
            .watchString { [weak self] text in
                self?.usernameValidationLabel.text = text
            }
            .closed(by: bag)

        viewModel.passwordValidationText
            .watchString { [weak self] text in
                self?.passwordValidationLabel.text = text
            }
            .closed(by: bag)

        viewModel.repeatedPasswordValidationText
            .watchString { [weak self] text in
                self?.repeatedPasswordValidationLabel.text = text
            }
            .closed(by: bag)

        viewModel.usernameValidationLabelIsHidden
            .watchBool { [weak self] isHidden in
                self?.usernameValidationLabel.isHidden = isHidden
            }
            .closed(by: bag)

        viewModel.passwordValidationLabelIsHidden
            .watchBool { [weak self] isHidden in
                self?.passwordValidationLabel.isHidden = isHidden
            }
            .closed(by: bag)

        viewModel.repeatedPasswordValidationLabelIsHidden
            .watchBool { [weak self] isHidden in
                self?.repeatedPasswordValidationLabel.isHidden = isHidden
            }
            .closed(by: bag)

        viewModel.isSignUpButtonEnabled
            .watchBool { [weak self] isEnabled in
                self?.signUpButton.isEnabled = isEnabled
            }
            .closed(by: bag)

        viewModel.isLoadingViewAnimating
            .watchBool { [weak self] isAnimating in
                if isAnimating {
                    self?.loadingView.startAnimating()
                } else {
                    self?.loadingView.stopAnimating()
                }
            }
            .closed(by: bag)

        viewModel.presentSignupSuccessPopupEvent
            .watchString { [weak self] username in
                let ac = UIAlertController(title: "✅ You have successfully signed up!", message: username, preferredStyle: .alert)
                let action = UIAlertAction(title: "OK", style: .default, handler: nil)
                ac.addAction(action)
                self?.present(ac, animated: true, completion: nil)
            }
            .closed(by: bag)

        viewModel.presentNetworkFailurePopupEvent
            .watchString { [weak self] errorMessage in
                let ac = UIAlertController(title: "❌ Something went wrong, please try again later.", message: errorMessage, preferredStyle: .alert)
                let action = UIAlertAction(title: "OK", style: .default, handler: nil)
                ac.addAction(action)
                self?.present(ac, animated: true, completion: nil)
            }
            .closed(by: bag)

        viewModel.usernameValidationResult
            .watch { [weak self] result in
                guard let result = result else { return }

                switch result {
                case .validating:
                    self?.usernameValidationLabel.textColor = .blue
                case .ok:
                    self?.usernameValidationLabel.textColor = .green
                case .alreadyTaken, .wrongFormat:
                    self?.usernameValidationLabel.textColor = .red
                case .serviceError:
                    self?.usernameValidationLabel.textColor = .orange
                default:
                    self?.usernameValidationLabel.textColor = .clear
                }
            }
            .closed(by: bag)

        viewModel.passwordValidationResult
            .watch { [weak self] result in
                guard let result = result else { return }

                switch result {
                case .ok:
                    self?.passwordValidationLabel.textColor = .green
                case .tooShort:
                    self?.passwordValidationLabel.textColor = .red
                default:
                    self?.passwordValidationLabel.textColor = .clear
                }
            }
            .closed(by: bag)

        viewModel.repeatedPasswordValidationResult
            .watch { [weak self] result in
                guard let result = result else { return }

                switch result {
                case .ok:
                    self?.repeatedPasswordValidationLabel.textColor = .green
                case .different:
                    self?.repeatedPasswordValidationLabel.textColor = .red
                default:
                    self?.repeatedPasswordValidationLabel.textColor = .clear
                }
            }
            .closed(by: bag)
    }

    @objc private func usernameTextxFieldDidChange(textField: UITextField) {
        viewModel.onUsernameChanged(username: textField.text ?? "")
    }

    @objc private func passwordTextxFieldDidChange(textField: UITextField) {
        viewModel.onPasswordChanged(password: textField.text ?? "")
    }

    @objc private func repeatedPasswordTextxFieldDidChange(textField: UITextField) {
        viewModel.onRepeatedPasswordChanged(repeatedPassword: textField.text ?? "")
    }

    @objc private func didTapSignUpButton(button: UIButton) {
        viewModel.onSignUpButtonClicked()
    }

    @objc private func didTapLogInButton(button: UIButton) {
        Auth0
            .webAuth(clientId: "qRlQr289sskc0qkH6iAKT8GbsUDrXU22", domain: "dev-xyz8mlo2.us.auth0.com")
            .start { result in
                switch result {
                case let .success(credentials):
                    NSLog("🎥🙏🙏🙏 credentials: \(credentials)")
                case let .failure(webAuthError):
                    NSLog("🎥🙏🙏🙏 failed to log in with webAuthError: \(webAuthError)")
                }
            }
    }

    @objc private func didTapLogOutButton(button: UIButton) {
        Auth0
            .webAuth(clientId: "qRlQr289sskc0qkH6iAKT8GbsUDrXU22", domain: "dev-xyz8mlo2.us.auth0.com")
            .clearSession { result in
                switch result {
                case .success:
                    NSLog("🎥🙏🙏🙏 logged out successfully")
                case let .failure(webAuthError):
                    NSLog("🎥🙏🙏🙏 failed to log out with webAuthError: \(webAuthError)")
                }
            }
    }

}
