import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "ru.netology.nework"
    compileSdk = 34

    val localApiKeyProperties = Properties().apply {
        val localApiKeyFile = rootProject.file("API_KEY.properties")
        if (localApiKeyFile.exists()) {
            localApiKeyFile.inputStream().use(::load)
        }
    }
    val apiKey = localApiKeyProperties.getProperty("API_KEY")
        ?: project.findProperty("API_KEY") as? String
        ?: ""
    val baseUrl = localApiKeyProperties.getProperty("BASE_URL")
        ?: project.findProperty("BASE_URL") as? String
        ?: "http://94.228.125.136:8080/api/"
    val yandexMapKitApiKey = localApiKeyProperties.getProperty("YANDEX_MAPKIT_API_KEY")
        ?: project.findProperty("YANDEX_MAPKIT_API_KEY") as? String
        ?: localApiKeyProperties.getProperty("MAPS_API_KEY")
        ?: project.findProperty("MAPS_API_KEY") as? String
        ?: ""

    defaultConfig {
        applicationId = "ru.netology.nework"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField(
            "String",
            "API_KEY",
            "\"$apiKey\""
        )
        buildConfigField("String", "YANDEX_MAPKIT_API_KEY", "\"$yandexMapKitApiKey\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        disable += setOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "ObsoleteLintCustomCheck",
            "OldTargetApi",
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.yandex.android:maps.mobile:4.7.0-lite")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
}

kapt {
    correctErrorTypes = true
}
