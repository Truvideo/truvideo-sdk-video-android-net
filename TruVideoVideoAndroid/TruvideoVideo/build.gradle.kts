plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.truvideo.video"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
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
    buildFeatures{
        //compose = true
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    composeOptions{
        kotlinCompilerExtensionVersion = "1.5.1"
        //kotlinCompilerExtensionVersion = "1.5.3"
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}

// Create configuration for copyDependencies
configurations {
    create("copyDependencies")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime)
    implementation(libs.truvideo.sdk.android.video)
//    configurations["copyDependencies"].dependencies.add(
//        project.dependencies.create("com.github.Truvideo:truvideo-sdk-android-video:79.1.2")
//    )
    implementation(libs.gson)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.skiko") {
            useTarget("androidx.compose.ui:ui:1.6.1") // Replace with a suitable alternative
        }
    }
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.activity" && requested.name == "activity-compose") {
            useVersion("1.10.0")
        }
    }
}


// Copy dependencies for binding library
project.afterEvaluate {
    tasks.register<Copy>("copyDeps") {
        // Copy both .aar and .jar files
        from(configurations["copyDependencies"].filter {
            it.name.endsWith(".aar") || it.name.endsWith(".jar")
        })
        into("${buildDir}/outputs/deps")
    }
    tasks.named("preBuild") { finalizedBy("copyDeps") }
}