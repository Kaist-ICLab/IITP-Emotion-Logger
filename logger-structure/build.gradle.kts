plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
//    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "kaist.iclab.loggerstructure"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.android.ktx)
    implementation(libs.appcompat)
    implementation(libs.play.services.wearable)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // RoomDB
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    // To use Kotlin annotation processing tool (kapt)
    ksp(libs.room.compiler)

    //  Dependency for Koin Library
    //  https://insert-koin.io/
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.gson)
}