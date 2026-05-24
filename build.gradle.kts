plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

tasks.register("installGitHooks", Exec::class) {
    description = "Installs git hooks from .githooks directory"
    group = "help"
    workingDir = rootDir
    commandLine("git", "config", "core.hooksPath", ".githooks")
}

// Automatically run the task during project build/sync
afterEvaluate {
    tasks.named("prepareKotlinBuildScriptModel") {
        dependsOn("installGitHooks")
    }
}
