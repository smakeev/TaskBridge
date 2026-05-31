# TaskBridge — iOS app

SwiftUI app that consumes the shared Kotlin Multiplatform `Core` module (exposed as the
`TaskBridgeCore` framework).

## Project generation (XcodeGen)

The Xcode project is **generated** from [`project.yml`](project.yml) by
[XcodeGen](https://github.com/yonsm/XcodeGen) and is **not** committed to git
(`TaskBridge.xcodeproj/` is gitignored). `project.yml` is the single source of truth, so
there is never a stale or hand-edited `.pbxproj` to drift out of sync.

### First-time setup

```sh
brew install xcodegen      # one-time, if you don't have it
cd iOS
xcodegen generate          # creates TaskBridge.xcodeproj from project.yml
open TaskBridge.xcodeproj   # then build/run the TaskBridge scheme
```

The project's pre-build step runs `./gradlew :Core:embedAndSignAppleFrameworkForXcode`,
so building from Xcode also (re)builds the KMP framework — no separate step needed.

### When to re-run `xcodegen generate`

`sources: [TaskBridge]` globs the whole `TaskBridge/` folder, so XcodeGen picks up any
`.swift` file under it automatically. Just re-run `xcodegen generate` after you **add,
remove, or move** files (or change anything in `project.yml`). Never edit
`TaskBridge.xcodeproj` by hand — it will be overwritten on the next generate.

### Command-line build

```sh
cd iOS
xcodegen generate
xcodebuild -project TaskBridge.xcodeproj -scheme TaskBridge \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build
```
