plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("org.jetbrains.kotlin.android")
//    id("kotlin-android")
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "kaist.iclab.wearablelogger"
    compileSdk = 35

    defaultConfig {
        applicationId = "kaist.iclab.wearablelogger"
        minSdk = 30
        targetSdk = 30
        versionCode = 1
        versionName = "2023-11-08"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

//  Default libraries for use of Android and Kotlin.
    implementation(libs.android.ktx)
    implementation(libs.play.services.wearable)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(project(":logger-structure"))

//  Include privileged SDK
    implementation(fileTree("libs"))

//  Jetpack Compose is a modern declarative UI Toolkit for Android
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.material.icons.extended)

//  Horologist is a group of libraries that aim to supplement Wear OS developers with features that are commonly required by developers but not yet available.
//  https://github.com/google/horologist
    implementation(libs.horologist.compose.tools)

//  Dependency for Koin Library
//  https://insert-koin.io/
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.datastore.preferences)
    implementation(libs.datastore.preferences.core)

    // RoomDB
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

//  Dependency for testing.
//  2023-11-08: It is not required for current development progress, but should be added for future testing.
//    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//    debugImplementation("androidx.compose.ui:ui-tooling")
//    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.android)

    // For periodic upload
    implementation(libs.work.runtime.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //    implementation("com.google.android.horologist:horologist-tiles:0.1.5")
//    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.1.1")
    //    implementation("androidx.wear.tiles:tiles:1.2.0")
//    implementation("androidx.wear.tiles:tiles-material:1.2.0")
    //    implementation("androidx.percentlayout:percentlayout:1.0.0")
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
//    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.gson)
}