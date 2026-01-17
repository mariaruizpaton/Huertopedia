import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.googleGmsGoogleServices)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    // ESTA LÍNEA ES LA SOLUCIÓN AL ERROR DE "iosMain not found"
    // Crea automáticamente la carpeta iosMain para agrupar tus targets
    applyDefaultHierarchyTemplate()

    androidTarget {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Firebase nativo para Android (Login con Google requiere esto)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.1.0"))
            implementation("com.google.firebase:firebase-auth")
            implementation("com.google.android.gms:play-services-auth:21.2.0")
            implementation("com.google.firebase:firebase-firestore")

            // Ktor Engine para Android (Java)
            implementation("io.ktor:ktor-client-okhttp:2.3.8")
        }
        commonMain.dependencies {
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation("media.kamel:kamel-image:0.9.3")
            implementation(compose.material3)

            // --- IMPORTANTE: SOLO GITLIVE ---
            implementation("dev.gitlive:firebase-auth:1.11.1")
            implementation("dev.gitlive:firebase-firestore:1.11.1")
            implementation("dev.gitlive:firebase-storage:1.11.1")

            implementation(libs.kotlinx.serialization.core)

            // Ktor Core (La base para todos)
            implementation("io.ktor:ktor-client-core:2.3.8")
            // Plugins de Ktor necesarios
            implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
        }

        // IOS (Específico)
        // Usamos 'getting' porque iosMain se crea automáticamente al definir los targets arriba
        val iosMain by getting {
            dependencies {
                // Ktor Engine para iOS (Nativo Apple)
                implementation("io.ktor:ktor-client-darwin:2.3.8")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mariaruiz.huertopedia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mariaruiz.huertopedia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.firebase.storage)
    debugImplementation(compose.uiTooling)
}