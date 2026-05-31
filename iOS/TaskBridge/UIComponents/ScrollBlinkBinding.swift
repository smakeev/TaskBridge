import SwiftUI
import TaskBridgeCore

/// Describes how one screen feeds the shared `ScrollBlinkHighlighter`. Domain-neutral:
/// used by the Tasks root/details screens, the Reminders screen, and any future list
/// that wants the "scroll to a row and blink it" behaviour.
///
/// Two highlight sources, both reduced to *the id of the row to blink*:
/// - a live stream of "something was created" app messages (blink in place), and
/// - a pending navigation-destination message consumed on appear (blink after we
///   navigate here).
struct ScrollBlinkBinding {
    /// The navigation-destination-message scope this screen consumes.
    let scopeId: String
    /// The live stream of created messages to observe while the screen is visible.
    let createdMessages: () -> AsyncStream<AppMessage>
    /// Maps a created message to the row id to blink, or nil to ignore it. This is
    /// also where a screen gates on "am I the current screen" / "is it in my scope".
    let createdTargetId: (AppMessage) async -> String?
    /// Consumes (and clears) the pending navigation-destination message for `scopeId`.
    let consumePending: (String) async throws -> NavigationDestinationMessage?
    /// Maps a consumed pending message to the row id to blink, or nil if not ours.
    let pendingTargetId: (NavigationDestinationMessage) -> String?
    /// Polls until the row for the id exists in this screen's list (or times out).
    /// Defaults to "always ready" for flat lists that render every row immediately.
    let waitForItem: @MainActor (String) async -> Bool

    init(
        scopeId: String,
        createdMessages: @escaping () -> AsyncStream<AppMessage>,
        createdTargetId: @escaping (AppMessage) async -> String?,
        consumePending: @escaping (String) async throws -> NavigationDestinationMessage?,
        pendingTargetId: @escaping (NavigationDestinationMessage) -> String?,
        waitForItem: @escaping @MainActor (String) async -> Bool = { _ in true }
    ) {
        self.scopeId = scopeId
        self.createdMessages = createdMessages
        self.createdTargetId = createdTargetId
        self.consumePending = consumePending
        self.pendingTargetId = pendingTargetId
        self.waitForItem = waitForItem
    }
}

extension View {
    /// Wires a `ScrollBlinkBinding` to a `ScrollBlinkHighlighter`: blink on live
    /// created-messages, and blink the pending navigation target on appear.
    /// Apply inside a `ScrollViewReader` so `scrollProxy` is in scope.
    func scrollBlinkHighlighting(
        highlighter: ScrollBlinkHighlighter,
        binding: ScrollBlinkBinding,
        scrollProxy: ScrollViewProxy
    ) -> some View {
        modifier(ScrollBlinkHighlightingModifier(
            highlighter: highlighter,
            binding: binding,
            scrollProxy: scrollProxy
        ))
    }
}

private struct ScrollBlinkHighlightingModifier: ViewModifier {
    let highlighter: ScrollBlinkHighlighter
    let binding: ScrollBlinkBinding
    let scrollProxy: ScrollViewProxy

    func body(content: Content) -> some View {
        content
            .task { await observeCreatedMessages() }
            .task { await consumePendingNavigationMessage() }
    }

    private func observeCreatedMessages() async {
        for await message in binding.createdMessages() {
            guard let targetId = await binding.createdTargetId(message) else { continue }
            await highlighter.scrollToAndBlink(
                id: targetId,
                scrollProxy: scrollProxy,
                waitForItem: binding.waitForItem
            )
        }
    }

    private func consumePendingNavigationMessage() async {
        guard let message = try? await binding.consumePending(binding.scopeId),
              let targetId = binding.pendingTargetId(message) else {
            return
        }
        await highlighter.scrollToAndBlink(
            id: targetId,
            scrollProxy: scrollProxy,
            presentationDelayNanoseconds: 1_200_000_000,
            waitForItem: binding.waitForItem
        )
    }
}
