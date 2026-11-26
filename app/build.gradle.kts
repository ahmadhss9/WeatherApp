plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.weatherapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 24           // Step 91: Android 7.0 (API 24) minimum
        targetSdk = 37        // Step 91: latest stable target
        versionCode = 1       // Step 91: bump on every release to Play Store
        versionName = "1.0"   // Step 91: human-readable version shown in About

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true  // Step 91: generate BuildConfig so VERSION_NAME is accessible in Java
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
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // Network requests
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}