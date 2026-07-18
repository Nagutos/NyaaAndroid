// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // AGP 9 compiles Kotlin itself (built-in Kotlin), so the kotlin-android plugin is gone.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
}