//
//  AppDelegate.swift
//  iosApp
//
//  Created by Alex Huang on 3/2/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import shared
import SnapKit
import FirebaseCore

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = SignupViewController()
        window?.makeKeyAndVisible()
        FirebaseApp.configure()
        return true
    }

}

let gitHubApi = CloudGitHubApi()
let gitHubValidationService = CloudGitHubValidationService(gitHubApi: gitHubApi)
