//
//  NSRecursiveLock+PerformLocked.swift
//  iosApp
//
//  Created by Alex Huang on 3/2/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

extension NSRecursiveLock {
    final func performLocked<T>(_ action: () -> T) -> T {
        lock()
        defer { unlock() }
        return action()
    }
}
