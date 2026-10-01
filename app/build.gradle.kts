plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ubeyd.focusguard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ubeyd.focusguard"
        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom =
        platform(
            "androidx.compose:compose-bom:2026.09.00"
        )

    implementation(composeBom)

    implementation(
        "androidx.core:core-ktx:1.18.0"
    )

    implementation(
        "androidx.activity:activity-compose:1.13.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.10.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.10.0"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.foundation:foundation"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.datastore:datastore-preferences:1.2.1"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}
