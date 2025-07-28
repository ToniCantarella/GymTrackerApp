plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.example.gymtracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gymtracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = libs.versions.appVersion.get()

        buildConfigField("String", "APP_VERSION", "\"${libs.versions.appVersion.get()}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            versionNameSuffix = ".release"
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.datastore.preferences)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room)
    implementation(libs.room.runtime)
    implementation(libs.compose.charts)
    implementation(libs.compose.calendar)
    annotationProcessor(libs.room.compiler)

    ksp(libs.room.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}