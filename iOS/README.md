# XO Arena iOS Shell

This is a thin shell for the iOS application.

## Setup
1. Open Xcode and create a new Project (App).
2. Use SwiftUI for the UI.
3. Add the KMP modules as a framework or use CocoaPods/Swift Package Manager.
4. In your `ContentView.swift`, you can bridge the Compose UI using a `UIViewControllerRepresentable`.

Example:
```swift
import SwiftUI
import UI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return Main_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

(Note: You need to expose a `MainViewController` in the `UI` module's `iosMain` source set.)
