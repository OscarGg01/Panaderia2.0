plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services") version "4.4.4"
    id("kotlin-kapt") // Necesario para el compilador de Glide
    // AÑADIDO: Plugin de compilador de Compose requerido para Kotlin 2.0+
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.panaderia20"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // 1. CONFIGURACIÓN DE COMPOSE
    buildFeatures {
        // Habilita el soporte para Compose
        compose = true
    }

    composeOptions {
        // AJUSTADO: Se alinea la versión del compilador a la versión de las librerías de UI (1.5.4)
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // --------------------------------------------------------
    // DEPENDENCIAS BASE Y UI
    // --------------------------------------------------------
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView y Coroutines
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --------------------------------------------------------
    // JETPACK COMPOSE
    // --------------------------------------------------------
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // --------------------------------------------------------
    // FIREBASE
    // --------------------------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // --------------------------------------------------------
    // GLIDE
    // --------------------------------------------------------
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    constraints {
        // Forzamos a que cualquier librería que dependa de 'activity' o 'activity-ktx'
        // use la versión 1.8.2, que es compatible con compileSdk 34.
        implementation("androidx.activity:activity:1.8.2") {
            because("La versión 1.11.0+ requiere compileSdk 36")
        }
        implementation("androidx.activity:activity-ktx:1.8.2") {
            because("La versión 1.11.0+ requiere compileSdk 36")
        }

        // Forzamos a que 'core' y 'core-ktx' usen la versión 1.12.0,
        // que es la última versión estable compatible con tu configuración.
        implementation("androidx.core:core:1.12.0") {
            because("La versión 1.13.0+ puede tener dependencias más nuevas")
        }
        implementation("androidx.core:core-ktx:1.12.0") {
            because("La versión 1.13.0+ puede tener dependencias más nuevas")
        }
    }

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.tbuonomo:dotsindicator:5.0")
}