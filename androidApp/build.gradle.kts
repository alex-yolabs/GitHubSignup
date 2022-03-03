plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    // Integration with activities
    implementation("androidx.activity:activity-compose:1.3.0-alpha08")
    // Compose Material Design
    implementation("androidx.compose.material:material:1.0.1")
    // Animations
    implementation("androidx.compose.animation:animation:1.0.1")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.0.1")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.1")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "wedding.alexkristi.githubsignup.android"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.1"
    }
}