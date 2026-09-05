plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    alias(libs.plugins.navigation.safeargs)

}


dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // Dependencia de Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

}


android {
    namespace = "com.example.primertpdeappmoviles"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        viewBinding= true
    }


    defaultConfig {
        applicationId = "com.example.primertpdeappmoviles"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    //implementación propia
    implementation("org.maplibre.gl:android-sdk:11.11.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("com.google.android.gms:play-services-location:21.4.0")

    //Camara
    val camerax_version = "1.5.0-rc01" // o "1.4.0" si prefieres estabilidad
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")


}
