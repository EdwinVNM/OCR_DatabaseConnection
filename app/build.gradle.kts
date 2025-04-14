plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ocr_databaseconnection"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ocr_databaseconnection"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.text.recognition.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Dependencies for Connection
    implementation (libs.appcompat.v131)
    implementation (libs.constraintlayout.v204)
    implementation (libs.core.ktx)
    implementation (libs.material.v140)

    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)

    // Dependencies for OCR
    implementation (libs.play.services.vision)// For OCR
    implementation (libs.picasso) // For loading images (optional, but useful)
    implementation (libs.activity.ktx) // For activity results handling
    implementation (libs.text.recognition) // Latest version

    // Dependency for Responsiveness
    implementation (libs.constraintlayout.v214)

    // Dependency for Chart
    implementation (libs.mpandroidchart)


}