plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") // 👈 Changed from alias to id
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jonathan.portapos"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jonathan.portapos"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Android Core ──────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ── Jetpack Compose (the UI toolkit we'll use) ────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.extended)

    // ── Navigation (moving between screens) ───────────────────
    implementation(libs.navigation.compose)

    // ── Room Database (saving data on-device, offline) ────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── ViewModel (keeps data alive when screen rotates) ──────
    implementation(libs.lifecycle.viewmodel.compose)

    // ── Coil (loading photos from storage into the UI) ────────
    implementation(libs.coil.compose)

    // ── iText (generating PDF receipts) ───────────────────────
    implementation(libs.itext7.core)

    // ── ESC/POS Printer (Hardware integration) ────────────────
    implementation(libs.escpos.printer)

    // ── WindowSizeClass (detecting phone vs tablet) ───────────
    implementation(libs.material3.windowSizeClass)

    // ── Testing (ignore these for now) ────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}