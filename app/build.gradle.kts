plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rnx.laranjada"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rnx.laranjada"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"

    productFlavors {
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"

            buildConfigField("String", "APP_ENVIRONMENT", "\"local\"")
            buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:8000\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"http://127.0.0.1:8000\"")

            manifestPlaceholders["usesCleartextTraffic"] = "true"

            resValue(
                type = "string",
                name = "app_name",
                value = "Laranjada Local"
            )
        }

        create("hml") {
            dimension = "environment"
            applicationIdSuffix = ".hml"
            versionNameSuffix = "-hml"

            buildConfigField("String", "APP_ENVIRONMENT", "\"hml\"")
            buildConfigField("String", "API_BASE_URL", "\"https://laranjada.eu\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"https://laranjada.eu\"")

            manifestPlaceholders["usesCleartextTraffic"] = "false"

            resValue(
                type = "string",
                name = "app_name",
                value = "Laranjada HML"
            )
        }

        create("prod") {
            dimension = "environment"

            buildConfigField("String", "APP_ENVIRONMENT", "\"prod\"")
            buildConfigField("String", "API_BASE_URL", "\"https://laranjada.zip\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"https://laranjada.zip\"")

            manifestPlaceholders["usesCleartextTraffic"] = "false"

            resValue(
                type = "string",
                name = "app_name",
                value = "Laranjada"
            )
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.foundation)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}