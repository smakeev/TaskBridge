# ─────────────────────────────────────────────────────────────────────────────
# TaskBridge — top-level developer Makefile
#
# A Kotlin Multiplatform project with two app front-ends:
#   • iOS     — SwiftUI app, Xcode project GENERATED from iOS/project.yml (XcodeGen)
#   • Android — Compose app, built with the Gradle wrapper (:Android module)
#
# Both share the KMP :Core module (exposed to iOS as the TaskBridgeCore framework).
#
# Run `make` or `make help` for the list of targets.
# ─────────────────────────────────────────────────────────────────────────────

# Use bash so `set -o pipefail`, `[[ ]]`, etc. behave predictably across recipes.
SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c

# ── Project layout / configuration ───────────────────────────────────────────
GRADLE          := ./gradlew

IOS_DIR         := iOS
IOS_PROJECT     := $(IOS_DIR)/TaskBridge.xcodeproj
IOS_SPEC        := $(IOS_DIR)/project.yml
IOS_SCHEME      := TaskBridge
# Override on the command line, e.g.  make build-ios IOS_SIMULATOR='iPhone 16'
IOS_SIMULATOR   ?= iPhone 17 Pro
IOS_DESTINATION := platform=iOS Simulator,name=$(IOS_SIMULATOR)

ANDROID_APP_ID  := com.taskbridge.android
ANDROID_LAUNCH  := $(ANDROID_APP_ID)/com.taskbridge.android.MainActivity

# Homebrew formula that provides the JDK the Gradle build is pinned to
# (see org.gradle.java.home in gradle.properties).
JDK_FORMULA     := openjdk@17

# Make every recipe run as a single shell invocation (lets us use multi-line if/for).
.ONESHELL:

# Targets that are not real files.
.PHONY: help all setup setup-ios setup-android \
        gen-ios open-ios framework \
        build build-ios build-android \
        run-ios run-android install-android uninstall-android \
        test test-ios test-android \
        sync hooks doctor install-tools update-tools \
        clean clean-ios clean-android clean-derived distclean

# Default target.
.DEFAULT_GOAL := help

# ─────────────────────────────────────────────────────────────────────────────
##@ Help
# ─────────────────────────────────────────────────────────────────────────────

help: ## Show this help (list of all targets)
	@awk 'BEGIN {FS = ":.*##"; printf "\nTaskBridge — make targets\n\nUsage:\n  make \033[36m<target>\033[0m\n"} \
		/^##@/ {printf "\n\033[1m%s\033[0m\n", substr($$0, 5); next} \
		/^[a-zA-Z0-9_-]+:.*##/ {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@printf "\nConfigurable variables (override on the command line):\n"
	@printf "  \033[36m%-18s\033[0m %s\n" "IOS_SIMULATOR" "iOS Simulator device name (current: $(IOS_SIMULATOR))"
	@printf "\nExamples:\n"
	@printf "  make setup                 # one-shot setup of both platforms\n"
	@printf "  make run-android           # build, install and launch on a device/emulator\n"
	@printf "  make build-ios IOS_SIMULATOR='iPhone 16'\n\n"

# ─────────────────────────────────────────────────────────────────────────────
##@ Setup  (generate / sync everything needed to build)
# ─────────────────────────────────────────────────────────────────────────────

all: setup ## Alias for `setup`

setup: setup-android setup-ios ## Set up BOTH platforms (tools must already be installed)
	@printf "\n✅ TaskBridge is set up. Open it with 'make open-ios' or build with 'make build-android'.\n"

setup-ios: gen-ios framework ## Generate the Xcode project + build the Core framework for iOS
	@printf "✅ iOS ready — run 'make open-ios' to open Xcode.\n"

setup-android: hooks ## Sync Gradle for Android (downloads deps, installs git hooks)
	@if [ ! -f local.properties ]; then \
		echo "sdk.dir=$$HOME/Library/Android/sdk" > local.properties ; \
		echo "Created local.properties pointing at $$HOME/Library/Android/sdk"; \
	fi
	@echo "Syncing Gradle (resolving dependencies)…"
	$(GRADLE) :Android:dependencies --quiet >/dev/null
	@printf "✅ Android ready — build with 'make build-android'.\n"

# ─────────────────────────────────────────────────────────────────────────────
##@ iOS
# ─────────────────────────────────────────────────────────────────────────────

gen-ios: ## (Re)generate iOS/TaskBridge.xcodeproj from project.yml via XcodeGen
	@command -v xcodegen >/dev/null || { echo "❌ xcodegen not found — run 'make install-tools'"; exit 1; }
	cd $(IOS_DIR) && xcodegen generate
	@echo "Generated $(IOS_PROJECT)"

framework: ## Build & embed the KMP Core framework for Xcode (debug)
	$(GRADLE) :Core:embedAndSignAppleFrameworkForXcode

open-ios: gen-ios ## Generate the project and open it in Xcode
	open $(IOS_PROJECT)

build-ios: gen-ios ## Command-line build of the iOS app for the simulator
	xcodebuild -project $(IOS_PROJECT) -scheme $(IOS_SCHEME) \
		-destination '$(IOS_DESTINATION)' build

run-ios: open-ios ## Open the iOS app in Xcode to run it (Cmd-R)
	@echo "Press Cmd-R in Xcode to run on the $(IOS_SIMULATOR) simulator."

test-ios: gen-ios ## Run the iOS app's unit tests on the simulator
	xcodebuild -project $(IOS_PROJECT) -scheme $(IOS_SCHEME) \
		-destination '$(IOS_DESTINATION)' test

# ─────────────────────────────────────────────────────────────────────────────
##@ Android
# ─────────────────────────────────────────────────────────────────────────────

build-android: ## Build the Android debug APK
	$(GRADLE) :Android:assembleDebug

install-android: ## Install the debug APK on a connected device/emulator
	$(GRADLE) :Android:installDebug

uninstall-android: ## Uninstall the Android app from the device/emulator
	$(GRADLE) :Android:uninstallDebug

run-android: install-android ## Build, install and launch the Android app
	@command -v adb >/dev/null || { echo "❌ adb not found — install Android platform-tools"; exit 1; }
	adb shell am start -n "$(ANDROID_LAUNCH)"

test-android: ## Run the Core + Android JVM unit tests
	$(GRADLE) :Core:testDebugUnitTest :Android:testDebugUnitTest

# ─────────────────────────────────────────────────────────────────────────────
##@ Shared / cross-platform
# ─────────────────────────────────────────────────────────────────────────────

build: build-android build-ios ## Build both apps

test: test-android test-ios ## Run all tests on both platforms

sync: ## Resolve/refresh all Gradle dependencies for every module
	$(GRADLE) dependencies --quiet >/dev/null
	@echo "✅ Gradle dependencies resolved."

hooks: ## Install the repo's git hooks (.githooks via Gradle task)
	$(GRADLE) installGitHooks

# ─────────────────────────────────────────────────────────────────────────────
##@ Clean
# ─────────────────────────────────────────────────────────────────────────────

clean: clean-android clean-ios ## Clean EVERYTHING generated on both platforms
	@printf "✅ Clean complete.\n"

clean-android: ## Gradle clean + remove all module build/ output
	-$(GRADLE) clean
	rm -rf build Core/build Android/build Platform_Handlers/build
	@echo "Removed Gradle build output."

clean-ios: clean-derived ## Remove the generated Xcode project + Core iOS framework output
	rm -rf $(IOS_PROJECT)
	rm -rf Core/build/xcode-frameworks Core/build/bin Core/build/cocoapods
	@echo "Removed generated Xcode project and iOS framework artifacts."

clean-derived: ## Delete this project's Xcode DerivedData
	@dd="$$HOME/Library/Developer/Xcode/DerivedData"; \
	if [ -d "$$dd" ]; then \
		find "$$dd" -maxdepth 1 -type d -name 'TaskBridge-*' -exec rm -rf {} + ; \
		echo "Removed TaskBridge DerivedData."; \
	else \
		echo "No DerivedData directory found."; \
	fi

distclean: clean ## clean + drop Gradle/Kotlin caches (.gradle, .kotlin)
	rm -rf .gradle .kotlin
	@echo "Removed .gradle and .kotlin caches. (Next build will re-download.)"

# ─────────────────────────────────────────────────────────────────────────────
##@ Tooling
# ─────────────────────────────────────────────────────────────────────────────

doctor: ## Check that all required dev tools are present
	@echo "Checking required tools…"
	@ok=0; \
	check() { if command -v $$1 >/dev/null; then printf "  ✅ %-12s %s\n" "$$1" "$$($$2 2>&1 | head -1)"; else printf "  ❌ %-12s MISSING — %s\n" "$$1" "$$3"; ok=1; fi; }; \
	check brew      "brew --version"        "install from https://brew.sh"; \
	check xcodegen  "xcodegen --version"     "run 'make install-tools'"; \
	check java      "java -version"          "run 'make install-tools'"; \
	check xcodebuild "xcodebuild -version"   "install Xcode from the App Store"; \
	check adb       "adb --version"          "install Android platform-tools / Android Studio"; \
	[ -d "$$HOME/Library/Android/sdk" ] && printf "  ✅ %-12s %s\n" "android-sdk" "$$HOME/Library/Android/sdk" || printf "  ⚠️  %-12s not at ~/Library/Android/sdk\n" "android-sdk"; \
	exit $$ok

install-tools: ## Install all required dev tools via Homebrew (xcodegen, JDK 17)
	@command -v brew >/dev/null || { echo "❌ Homebrew is required. Install it from https://brew.sh first."; exit 1; }
	@echo "Installing/ensuring developer tools via Homebrew…"
	brew install xcodegen $(JDK_FORMULA)
	@echo "Ensuring Xcode command-line tools are present…"
	@xcode-select -p >/dev/null 2>&1 || xcode-select --install || true
	@printf "✅ Tools installed. Note: Xcode itself and the Android SDK are installed separately.\n"

update-tools: ## Update Homebrew + all dev tools (incl. make itself) to latest
	@command -v brew >/dev/null || { echo "❌ Homebrew is required. Install it from https://brew.sh first."; exit 1; }
	brew update
	brew upgrade xcodegen $(JDK_FORMULA) make || true
	@printf "✅ Tools updated.\n"
