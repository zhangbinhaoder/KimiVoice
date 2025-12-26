// 插件管理：配置AGP插件的仓库（必须包含google()）
pluginManagement {
    repositories {
        google() // 核心：添加Google仓库（AGP插件唯一来源）
        mavenCentral()
        gradlePluginPortal() // Gradle官方插件仓库
    }
    // 可选：统一指定AGP版本，避免每个模块重复配置
    plugins {
        id("com.android.application") version "8.1.3" apply false // 适配AGP 8.1.x（与你的8.13.2兼容）
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Kotlin插件版本
    }
}

// 依赖仓库管理：配置项目依赖的仓库
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // 核心：添加Google仓库
        mavenCentral()
    }
}

// 项目名称和模块配置
rootProject.name = "KimiVoice"
include(":app") // 确保包含app模块