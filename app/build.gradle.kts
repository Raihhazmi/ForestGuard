plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.forestguard.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.forestguard.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Versi compiler ini aman untuk kebanyakan Android Studio baru
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- INTI ANDROID ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- JETPACK COMPOSE (UI) ---
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // --- NAVIGASI ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- ICON ---
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // --- APPWRITE (DATABASE) ---
    implementation("io.appwrite:sdk-for-android:5.1.0")

    // --- COIL (GAMBAR) ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- PETA OSMDROID (Pengganti MapLibre) ---
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // DATASTORE (Untuk simpan setting Mode Gelap)
    implementation("androidx.datastore:datastore-preferences:1.0.0")


    // --- TESTING (Wajib ada biar gak error) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}