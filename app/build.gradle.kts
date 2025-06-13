plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.core.talita"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.core.talita"
        minSdk = 23
        targetSdk = 34
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
        jvmTarget = "11" // Set to 1.8 or 11 based on your preference
    }

    buildFeatures {
        dataBinding = true
    }
}


dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.osmdroidAndroid)
    implementation(libs.kotlin)
    implementation(libs.zxing.android.embedded)
    implementation(libs.androidx.databinding.runtime)
    implementation("com.google.dagger:dagger:2.51.1")
    implementation(libs.androidx.appcompat)
    kapt("com.google.dagger:dagger-compiler:2.51.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
