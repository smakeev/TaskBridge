import SwiftUI

struct TemplateNameInputView: View {
    let templateId: String
    
    var body: some View {
        ScreenPlaceholderView(title: "Template Name Input", subtitle: "ID: \(templateId)")
    }
}
