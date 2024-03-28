plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.apollo)
    alias(libs.plugins.supabase.serialization)
}

android {
    namespace = "com.tsundoku"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tsundoku"
        minSdk = 33
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
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    apollo {
        service("service") {
            packageName.set("com.tsundoku")
            introspection {
                endpointUrl.set("https://graphql.anilist.co")
                schemaFile.set(file("src/main/graphql/schema.graphqls"))
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore)
    implementation(libs.gson)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.postgres.kt)
    implementation(libs.ktor)

    // Retrofit2
    implementation(libs.com.squareup.retrofit2)
    implementation(libs.com.squareup.retrofit2.converterGson)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    // implementation(libs.appauth)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // implementation(libs.hilt.lifecycle.viewmodel)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android.compiler)

    // Apollo GraphQL
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.sqlite)
    implementation(libs.apollo.cache)

    // Coil
    implementation(libs.coil.compose)

    // Compose Destinations
    implementation(libs.ramamcosta.compose.destinations.core)
    ksp(libs.ramamcosta.compose.destinations.ksp)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}