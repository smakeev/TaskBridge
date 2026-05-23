import SwiftUI
import TaskBridgeCore

struct ContentView: View {
    let taskCore = TaskCore()
    
    var body: some View {
        VStack {
            Text(taskCore.message)
                .padding()
        }
    }
}
