// Top-level build file with explicit plugin versions
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
    // Applied by :beacon only for the cloud flavor, and only when a google-services.json is
    // present. Declared here so the version is pinned in one place.
    id("com.google.gms.google-services") version "4.4.1" apply false
}
