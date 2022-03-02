//
//  SignupViewController.swift
//  iosApp
//
//  Created by Alex Huang on 3/2/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import shared

class SignupViewController: UIViewController {

    private let viewModel = SignupViewModel(
        gitHubApi: gitHubApi,
        gitHubValidationService: gitHubValidationService
    )

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .red
    }

}
