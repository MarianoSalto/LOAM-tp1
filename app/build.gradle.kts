plugins {
    // Plugin principal para desarrollar la aplicación Android.
    id("com.android.application")

    // Plugin necesario para conectar Firebase con Android.
    id("com.google.gms.google-services")

    // Plugin de Safe Args para Navigation.
    alias(libs.plugins.navigation.safeargs)
}


android {

    // Nombre del paquete de nuestra aplicación.
    namespace = "com.example.primertpdeappmoviles"

    // Versión del SDK utilizada para compilar.
    compileSdk {
        version = release(37)
    }

    // Activamos ViewBinding.
    buildFeatures {
        viewBinding = true
    }

    // Configuración general de la aplicación.
    defaultConfig {

        // Identificador único de la aplicación.
        applicationId = "com.example.primertpdeappmoviles"

        // Android mínimo compatible.
        minSdk = 24

        // SDK objetivo.
        targetSdk = 37

        // Número de versión.
        versionCode = 1

        // Nombre de versión.
        versionName = "1.0"

        // Runner utilizado para pruebas.
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configuración de compilación para release.
    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // Configuración de Java.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


dependencies {

    // =================================================
    // FIREBASE
    // =================================================

    // Controla las versiones de las librerías Firebase.
    implementation(
        platform(
            "com.google.firebase:firebase-bom:34.18.0"
        )
    )

    implementation("com.google.firebase:firebase-messaging")

    // Firebase Analytics.
    implementation(
        "com.google.firebase:firebase-analytics"
    )

    // Cloud Firestore.
    implementation(
        "com.google.firebase:firebase-firestore"
    )


    // =================================================
    // COROUTINES
    // =================================================

    // Permite utilizar corrutinas en Android.
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1"
    )

    // Permite utilizar await() con tareas de Firebase.
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1"
    )


    // =================================================
    // ANDROID
    // =================================================

    // Core de Android.
    implementation(libs.androidx.core.ktx)

    // Activity KTX.
    implementation(libs.androidx.activity.ktx)

    // AppCompat.
    implementation(libs.androidx.appcompat)

    // ConstraintLayout.
    implementation(libs.androidx.constraintlayout)

    // Material Design.
    implementation(libs.material)


    // =================================================
    // NAVIGATION
    // =================================================

    // Navigation Fragment.
    implementation(
        libs.androidx.navigation.fragment.ktx
    )

    // Navigation UI.
    implementation(
        libs.androidx.navigation.ui.ktx
    )


    // =================================================
    // MAPAS
    // =================================================

    // Google Maps.
    implementation(libs.play.services.maps)

    // MapLibre.
    implementation(
        "org.maplibre.gl:android-sdk:11.11.0"
    )

    // Lifecycle.
    implementation(
        "androidx.lifecycle:lifecycle-process:2.7.0"
    )

    // Ubicación.
    implementation(
        "com.google.android.gms:play-services-location:21.4.0"
    )

    // =================================================
    // RETROFIT (API CLIMA)
    // =================================================

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")


    // =================================================
    // CÁMARA
    // =================================================

    // Versión de CameraX.
    val camerax_version = "1.5.0-rc01"

    // CameraX Core.
    implementation(
        "androidx.camera:camera-core:$camerax_version"
    )

    // Integración con Camera2.
    implementation(
        "androidx.camera:camera-camera2:$camerax_version"
    )

    // Integración con Lifecycle.
    implementation(
        "androidx.camera:camera-lifecycle:$camerax_version"
    )

    // Vista de cámara.
    implementation(
        "androidx.camera:camera-view:$camerax_version"
    )

    // Extensiones de cámara.
    implementation(
        "androidx.camera:camera-extensions:$camerax_version"
    )

    // Grabación de video.
    implementation(
        "androidx.camera:camera-video:$camerax_version"
    )


    // =================================================
    // TESTS
    // =================================================

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )
}

