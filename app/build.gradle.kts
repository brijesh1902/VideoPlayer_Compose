plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.brizz.videoplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brizz.videoplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        defaultConfig {
            setProperty("archivesBaseName", "VideoPlayer_v${versionName}")
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.media3.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // For media playback using ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    // For DASH playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.dash)
    // For HLS playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.hls)
    // For SmoothStreaming playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.smoothstreaming)
    // For building media playback UIs using Compose
    implementation(libs.androidx.media3.ui.compose)

    implementation(libs.hilt.android)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material.icons.extended)

    implementation(libs.coil.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen) // Use the latest stable or beta version

//    implementation(libs.libvlc.all) // Use the latest stable version

    // Room runtime
    implementation("androidx.room:room-runtime:2.6.1")
    // Kotlin extensions + Coroutines support
    implementation("androidx.room:room-ktx:2.6.1")
    // Annotation processor (KSP - recommended)
    ksp("androidx.room:room-compiler:2.6.1")

}