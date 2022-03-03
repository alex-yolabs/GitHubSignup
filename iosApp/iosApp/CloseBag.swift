//
//  CloseBag.swift
//  iosApp
//
//  Created by Alex Huang on 3/2/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import shared

class CloseBag {

    private let lock = NSRecursiveLock()

    // state
    private var closables = [Ktor_ioCloseable]()
    private var isClosed = false

    public func insert(_ closable: Ktor_ioCloseable) {
        _insert(closable)?.close()
    }

    private func _insert(_ closable: Ktor_ioCloseable) -> Ktor_ioCloseable? {
        lock.performLocked {
            if isClosed {
                return closable
            }

            closables.append(closable)
            return nil
        }
    }

    private func close() {
        let oldClosables = self._close()

        for closable in oldClosables {
            closable.close()
        }
    }

    private func _close() -> [Ktor_ioCloseable] {
        lock.performLocked {
            let closables = self.closables

            self.closables.removeAll(keepingCapacity: false)
            isClosed = true

            return closables
        }
    }

    deinit {
        close()
    }

}

extension Ktor_ioCloseable {
    func closed(by bag: CloseBag) {
        bag.insert(self)
    }
}
