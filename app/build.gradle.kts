plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.pembukuanusaha"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pembukuanusaha"
        minSdk = 26
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
}

dependencies {
    // ===== ANDROIDX UI =====
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.core)
    implementation(libs.core.ktx)

    // ===== CHART =====
    implementation(libs.mpandroidchart)

    // ===== EXPORT PDF (WAJIB iText 5 ANDROID) =====
    implementation("com.itextpdf:itextg:5.5.10")

    // ===== EXPORT EXCEL =====
    implementation(libs.poi)

    // ===== FIREBASE =====
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // 🔥 BARU: FIREBASE STORAGE (Untuk Simpan Foto)
    implementation("com.google.firebase:firebase-storage")

    // 🔥 BARU: GLIDE (Untuk Tampilkan Foto)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ===== TESTING =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}