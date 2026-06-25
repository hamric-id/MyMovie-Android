plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt)
}

android {
    namespace = "com.hamric.core.network"
    compileSdk = 35

    defaultConfig {
        buildConfigField("String", "TMDB_API_READ_ACCESS_TOKEN", "\"eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1YTU4ZDUwZDhjNmU2MmNjOTU3M2Y2ZDhiNDM5YmNmYiIsIm5iZiI6MTc4MTI1MzE3MC4xNjcsInN1YiI6IjZhMmJjNDMyNDk0MDVjNTA3MmRiZGE0NSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.TSiwQqCD1B9yKH__JcVn24y8H7wa9X9sB7zi0o55EiQ\"")
        buildConfigField("String", "TMDB_API_KEY", "\"5a58d50d8c6e62cc9573f6d8b439bcfb\"")
        //not placed on local.properties because it usually noted on .GITIGNORE. this is just for make HR Team easier to test
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
    }

}

dependencies {
    implementation(project(":core:model"))

    //network
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.logging.interceptor)

    //gson serialization
    implementation(libs.google.code.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    //hilt dagger di
    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
}