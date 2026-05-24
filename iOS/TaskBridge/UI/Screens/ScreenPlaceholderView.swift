import SwiftUI

struct ScreenPlaceholderView: View {
    let title: String
    var subtitle: String? = nil
    
    var body: some View {
        VStack(spacing: 8) {
            Text(title)
                .font(.headline)
            if let subtitle {
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
