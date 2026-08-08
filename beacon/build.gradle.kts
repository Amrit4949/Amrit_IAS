plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/**
 * The Firebase Gradle plugin hard-fails when `google-services.json` is absent, and that file
 * belongs to whoever ships the app — it cannot be checked in on their behalf. Applying it
 * conditionally means the repository builds for anyone who clones it, and lights up FCM the
 * moment the owner drops their own file into `beacon/`.
 */
val googleServicesConfig = file("google-services.json")
if (googleServicesConfig.exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.lifecycle(
        "beacon: no google-services.json found - the 'cloud' flavor will build but cannot " +
            "receive pushes. See beacon/README.md."
    )
}

android {
    namespace = "com.amrit.beacon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.amrit.beacon"
        // 26 gives us AudioFocusRequest, VibrationEffect and java.util.Base64 unconditionally.
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    /**
     * Two ways to be reached, as separate variants rather than a runtime switch.
     *
     * `lan` is the zero-configuration build: no Firebase dependency is linked in at all, so
     * there is nothing to misconfigure and nothing to crash at startup. `cloud` adds FCM so
     * the phone can be rung from anywhere, at the cost of needing a Firebase project and a
     * deployed relay.
     */
    flavorDimensions += "delivery"
    productFlavors {
        create("lan") {
            dimension = "delivery"
            versionNameSuffix = "-lan"
        }
        create("cloud") {
            dimension = "delivery"
            versionNameSuffix = "-cloud"
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
        // Must stay in lockstep with the Kotlin version pinned in the root build file.
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Only the cloud flavor links Firebase. The lan APK contains none of it.
    "cloudImplementation"(platform("com.google.firebase:firebase-bom:32.7.2"))
    "cloudImplementation"("com.google.firebase:firebase-messaging")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
