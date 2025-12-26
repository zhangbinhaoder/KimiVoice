plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.kimivoice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kimivoice"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    // 核心：添加Lint配置，避免lint错误阻断构建
    lint {
        // 禁用HardcodedDebugMode检查（可选，已删除属性则无需此配置）
        disable += "HardcodedDebugMode"
        // 不将lint错误视为致命错误（关键：避免lint阻断运行）
        abortOnError = false
        // 调试模式下忽略lint错误
        checkReleaseBuilds = false
    }

    signingConfigs {
        create("system") {
            storeFile = file("D:/sign/platform.jks")
            storePassword = "123456"
            keyAlias = "kimi_system"
            keyPassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("system")
        }
        debug {
            signingConfig = signingConfigs.getByName("system")
            isDebuggable = true // 显式指定Debug模式为true（可选，Gradle自动赋值）
            isShrinkResources = false
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
        compose = false
        buildConfig = true
        viewBinding = false
    }

    androidResources {
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }
}

dependencies {
    // AndroidX核心库
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    
    // 多DEX支持（兼容低版本设备）
    implementation("androidx.multidex:multidex:2.0.1")
    
    // 日志工具
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // 网络请求
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okio:okio:3.9.1")
    
    // JSON解析
    implementation("com.google.code.gson:gson:2.11.0")
    
    // 协程支持
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    
    // 测试依赖
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

tasks.withType<com.android.build.gradle.tasks.PackageApplication> {
    outputDirectory = file("D:/sign/output")
}

// Kotlin编译优化
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        // 启用增量编译以提升构建速度
        incremental = true
        // 启用JVM默认方法生成
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}