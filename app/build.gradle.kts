plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dagger.android)
}

android {
    namespace = "com.androidbolts.minitiktok"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.androidbolts.minitiktok"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            manifestPlaceholders["appName"] = "Tiktok-D"
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BASE_URL", "\"https://example.com/api/\"")
            buildConfigField("boolean", "USE_FAKE_DATA", "false")

        }

        create("fake") {
//            manifestPlaceholders += mapOf()
            isDebuggable = true
            isMinifyEnabled = false
            manifestPlaceholders["appName"] = "Tiktok-Fake"
            applicationIdSuffix = ".fake"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "USE_FAKE_DATA", "true")
            buildConfigField("String", "BASE_URL", "\"https://example.com/api/\"")
            signingConfig = signingConfigs.getByName("debug")
//            signingConfig = signingConfigs.getByName("releaseConfig")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = false
            manifestPlaceholders["appName"] = "Mini Tiktok"

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "BASE_URL", "\"https://example.com/api/\"")
            buildConfigField("boolean", "USE_FAKE_DATA", "false")
//            signingConfig = signingConfigs.getByName("releaseConfig")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.urlconnection)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.convertor.gson)
    implementation(libs.localebro.okhttp.profiler)

    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)
    implementation (libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    //⚠️------------ ADD RUNTIME DEPENDENCIES ABOVE THIS LINE --------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}