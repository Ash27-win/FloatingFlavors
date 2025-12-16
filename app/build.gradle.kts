plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

buildDir = file("build2")

android {
    namespace = "com.example.floatingflavors"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.floatingflavors"
        minSdk = 23
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // If you have kotlin compiler extension version set in libs.versions, you can set it here.
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.3" // only if needed for your Compose/Kotlin setup
    // }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.play.services.cast.framework)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation, Splashscreen
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    // -----------------------------
    // Compose BOM (single source of truth for Compose versions)
    // -----------------------------
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))

    // Compose artifacts (NO versions here — BOM provides them)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit & Gson converter (unchanged)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Lifecycle ViewModel (for Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Coil for images
    implementation("io.coil-kt:coil-compose:2.6.0")
}


//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
//}
//
//buildDir = file("build2")
//
//android {
//    namespace = "com.example.floatingflavors"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.example.floatingflavors"
//        minSdk = 23
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//    buildFeatures {
//        compose = true
//    }
//}
//
//dependencies {
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    //Navigation dependencies
//    implementation(libs.androidx.navigation.compose)
//    implementation(libs.androidx.core.splashscreen)
//    // Jetpack Compose UI
//    implementation(libs.androidx.compose.ui)
//    implementation(libs.androidx.compose.material3)
//    implementation(libs.androidx.compose.ui.tooling.preview)
//    debugImplementation(libs.androidx.compose.ui.tooling)
//    implementation(libs.androidx.compose.material.icons.extended)
//
//    // Retrofit
//    implementation("com.squareup.retrofit2:retrofit:2.11.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
//// OkHttp (optional but good)
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
//// Coroutines (for ViewModel)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
//// Lifecycle ViewModel (for Compose)
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
//
//    // Coil for image loading (REQUIRED)
//    implementation("io.coil-kt:coil-compose:2.6.0")
//
//    // Coroutines (android already present, add core if you need non-android coroutine code)
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
//
//    // Material3 (you use libs alias; ensure it's up-to-date and compatible with compose version)
//    implementation("androidx.compose.material3:material3:1.1.0")  // verify with your Compose BOM / kotlin plugin
//
//    // Compose foundation + text (required for KeyboardOptions)
//    implementation("androidx.compose.foundation:foundation:1.6.7")
//    implementation("androidx.compose.ui:ui-text:1.6.7")
//}
