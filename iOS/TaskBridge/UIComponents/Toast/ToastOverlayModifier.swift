import SwiftUI

struct ToastOverlayModifier: ViewModifier {
    let text: String?

    func body(content: Content) -> some View {
        ZStack(alignment: .top) {
            content

            if let text {
                ToastView(text: text)
                    .padding(.top, 12)
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: text)
    }
}

extension View {
    func toastOverlay(text: String?) -> some View {
        modifier(ToastOverlayModifier(text: text))
    }
}
