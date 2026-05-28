import SwiftUI

@MainActor
final class ScrollBlinkHighlighter: ObservableObject {
    @Published private(set) var highlightedId: String?
    @Published private(set) var opacity = 0.0

    private var activeId: String?

    func scrollToAndBlink(
        id: String,
        scrollProxy: ScrollViewProxy,
        presentationDelayNanoseconds: UInt64 = 1_200_000_000,
        waitForItem: @MainActor (String) async -> Bool = { _ in true }
    ) async {
        guard activeId != id else { return }
        activeId = id
        defer {
            if activeId == id {
                activeId = nil
            }
        }

        guard await waitForItem(id) else { return }

        highlightedId = id
        opacity = 0.0

        withAnimation(.easeInOut(duration: 0.35)) {
            scrollProxy.scrollTo(id, anchor: .center)
        }
        await uncancellableSleep(nanoseconds: presentationDelayNanoseconds)

        for _ in 0..<2 {
            withAnimation(.easeInOut(duration: 1.0)) {
                opacity = 1.0
            }
            await uncancellableSleep(nanoseconds: 1_000_000_000)
            withAnimation(.easeInOut(duration: 1.0)) {
                opacity = 0.0
            }
            await uncancellableSleep(nanoseconds: 1_000_000_000)
        }

        if highlightedId == id {
            highlightedId = nil
        }
    }
}
