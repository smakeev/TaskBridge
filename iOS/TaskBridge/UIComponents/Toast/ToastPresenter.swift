import SwiftUI

@MainActor
final class ToastPresenter: ObservableObject {
    @Published private(set) var text: String?
    private var dismissTask: Task<Void, Never>?

    func show(_ text: String) {
        dismissTask?.cancel()
        withAnimation {
            self.text = text
        }

        dismissTask = Task { @MainActor in
            try? await Task.sleep(nanoseconds: 2_000_000_000)
            guard !Task.isCancelled else {
                return
            }
            withAnimation {
                self.text = nil
            }
        }
    }
}
