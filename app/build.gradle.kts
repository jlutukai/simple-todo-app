import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.lutukai.simpletodoapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.lutukai.simpletodoapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.lutukai.simpletodoapp.HiltTestRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
       compilerOptions{
           jvmTarget.set(JvmTarget.JVM_17)
       }
    }
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room testing - in-memory database
    androidTestImplementation(libs.androidx.room.testing)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    // Core test utilities
    androidTestImplementation(libs.androidx.core.testing)

    // Truth - fluent assertions
    testImplementation(libs.truth)
    androidTestImplementation(libs.truth)

    // MockK - mocking for Repository unit tests
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    // Robolectric for JVM-based UI testing
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.fragment.testing)
    debugImplementation(libs.androidx.fragment.testing.manifest)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    //navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
}