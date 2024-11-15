plugins {
    alias(libs.plugins.androidApplication) apply false
    id("com.chaquo.python") version "16.0.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()  // Ensure this line is present for plugin dependencies
    }
}
