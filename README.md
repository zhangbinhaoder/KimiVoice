# KimiVoice - AI语音助手

<div align="center">
  <h3>🎤 基于Moonshot AI的Android系统级语音控制应用</h3>
  <p>
    <a href="#特性">特性</a> •
    <a href="#技术栈">技术栈</a> •
    <a href="#快速开始">快速开始</a> •
    <a href="#使用说明">使用说明</a> •
    <a href="#许可证">许可证</a>
  </p>
</div>

---

## 📱 项目简介

KimiVoice是一款基于Moonshot AI的Android系统级语音控制应用，可以通过语音指令控制设备。应用采用VAD（语音活动检测）技术实时识别语音，通过AI将自然语言转换为系统命令并执行。

### ✨ 核心特性

- 🎯 **实时VAD语音检测** - 智能识别说话开始和结束
- 🤖 **AI指令生成** - Moonshot AI理解自然语音并生成shell命令
- 🗣️ **TTS语音回复** - AI回复自动语音播报，双向语音对话体验
- 🔒 **企业级安全** - AES-GCM加密 + Android KeyStore硬件保护
- ⚡ **高性能处理** - Kotlin协程异步处理，响应迅速
- 🛡️ **安全机制** - 指令白名单+黑名单双重验证
- 📊 **完整日志** - Timber日志系统 + 崩溃日志保存

### 🎯 支持功能

- 🔊 **语音对话** - 双向语音交互，就像和真人对话
- 📡 **WiFi/蓝牙控制** - 语音开关网络连接
- 🔊 **音量调节** - 静音、增大减小音量
- 📱 **应用启动** - 语音打开任何应用
- ⚙️ **系统设置** - 调整系统配置
- 🎮 **屏幕控制** - 截图、锁屏、点击等
- 🔧 **更多自定义指令** - 可扩展白名单

---

## 🛠️ 技术栈

### 核心技术

- **语言**: Kotlin 2.0.21
- **构建工具**: Gradle 8.13 + AGP 8.7.3
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 15 (API 35)

### 主要依赖

| 库 | 版本 | 用途 |
|---|---|---|
| AndroidX Core KTX | 1.15.0 | Android核心扩展 |
| Lifecycle | 2.8.7 | 生命周期管理 |
| OkHttp | 4.12.0 | 网络请求 |
| Gson | 2.11.0 | JSON解析 |
| Coroutines | 1.9.0 | 协程支持 |
| Timber | 5.0.1 | 日志工具 |
| TextToSpeech | Android内置 | 语音合成 |

### 架构特点

- **LifecycleService** - 生命周期感知的前台服务
- **协程** - 高效的异步处理
- **VAD算法** - 实时能量检测
- **Android KeyStore** - 硬件级密钥保护

---

## 🚀 快速开始

### 前置要求

- ✅ Java 21+
- ✅ Android Studio 2024+
- ✅ Android SDK 35
- ✅ Moonshot API Key
- ✅ Root权限 + Magisk（系统级功能）

### 📦 Magisk模块安装（推荐）

**直接下载安装，无需编译！**

1. **下载最新模块**
   - [KimiVoice_TTS.zip](https://github.com/zhangbinhaoder/KimiVoice/raw/main/KimiVoice_TTS.zip) - **v1.2 TTS语音回复版** 🆕
   - [KimiVoice更新模块.zip](https://github.com/zhangbinhaoder/KimiVoice/raw/main/KimiVoice%E6%9B%B4%E6%96%B0%E6%A8%A1%E5%9D%97.zip) - v1.1 基础版

2. **Magisk安装**
   - 打开 Magisk Manager
   - 点击「模块」 → 「从本地安装」
   - 选择下载的 zip 文件
   - 重启手机

3. **模块特性**
   - ✅ 自动安装为系统应用（priv-app）
   - ✅ 自动授予必要权限
   - ✅ 系统级签名，完整功能
   - ✅ 卸载模块自动删除

### 🔧 自行编译（可选）

```bash
# 1. 克隆仓库
git clone https://github.com/zhangbinhaoder/KimiVoice.git
cd KimiVoice

# 2. 配置签名（如有系统签名）
# 将 platform.jks 放到 D:/sign/ 目录

# 3. 编译Debug版本
gradlew assembleDebug

# 4. 编译Release版本
gradlew assembleRelease

# 5. 安装到设备
adb install app/build/outputs/apk/release/app-release.apk
```

### 配置要求

1. **系统签名** (可选)
   - 完整功能需要系统签名
   - 普通签名功能受限

2. **Moonshot API Key**
   - 注册地址: https://platform.moonshot.cn/
   - 在应用中配置API Key

---

## 📖 使用说明

### 1. 配置API Key

打开应用后：
1. 在第一个输入框输入Moonshot API Key
2. 设置音频能量阈值（建议2000-5000）
3. 点击"保存配置"

### 2. 启动服务

1. 授予录音、通知权限
2. 点击"启动监听服务"
3. 看到通知栏显示"正在监听..."

### 3. 语音控制

清晰说出指令，AI会语音回复并执行：
- "打开WiFi" → AI: "好的，正在为您打开WiFi"
- "关闭蓝牙" → AI: "正在为您关闭蓝牙"
- "打开设置" → AI: "正在为您打开设置应用"
- "静音" → AI: "正在设置静音模式"

### 4. 工作流程

```
用户说话 → VAD检测 → 录音 → 语音转文字 → AI生成回复 → TTS语音播报 → 执行指令
```

---

## ⚙️ 配置说明

### 音频能量阈值

| 环境 | 推荐值 |
|------|--------|
| 安静室内 | 1500-2500 |
| 普通环境 | 2500-3500 |
| 嘈杂环境 | 3500-5000 |

### VAD参数调整

编辑 `ListenService.kt`:

```kotlin
// 静音帧数阈值（默认15）
private val maxSilenceFrames = 15

// 语音帧数阈值（默认5）
private val minSpeechFrames = 5
```

### 自定义指令白名单

编辑 `KimiConstants.kt`:

```kotlin
val ALLOWED_COMMANDS = listOf(
    "svc wifi",
    "settings put",
    "am start",
    // 添加更多...
)
```

---

## 📊 性能指标

| 指标 | 数值 |
|------|------|
| 服务启动时间 | < 2秒 |
| VAD响应延迟 | < 100ms |
| API调用时间 | 2-5秒 |
| 内存占用 | 30-50 MB |
| APK大小 | 5.19 MB (Release) |

---

## 🔐 安全特性

- ✅ **API Key加密存储** - AES-GCM + KeyStore
- ✅ **指令白名单验证** - 只执行允许的命令前缀
- ✅ **危险指令拦截** - 黑名单防止危险操作
- ✅ **隐私保护** - 音频文件处理后自动删除
- ✅ **崩溃日志** - 本地保存，便于调试

---

## 📁 项目结构

```
KimiVoice/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/kimivoice/
│   │   │   │   ├── MainActivity.kt          # 主界面
│   │   │   │   ├── ListenService.kt         # 监听服务
│   │   │   │   ├── NetworkUtils.kt          # 网络请求
│   │   │   │   ├── AudioUtils.kt            # 音频处理
│   │   │   │   ├── SecurityUtils.kt         # 安全加密
│   │   │   │   ├── CmdReceiver.kt           # 指令接收
│   │   │   │   ├── KimiConstants.kt         # 常量定义
│   │   │   │   └── App.kt                   # 应用初始化
│   │   │   ├── res/                         # 资源文件
│   │   │   └── AndroidManifest.xml          # 清单文件
│   │   └── test/                            # 测试代码
│   ├── build.gradle.kts                     # 构建配置
│   └── proguard-rules.pro                   # 混淆规则
├── gradle/                                   # Gradle配置
├── 使用指南.md                               # 详细使用教程
├── 配置示例.md                               # 配置说明
├── 测试工具.bat                              # 测试脚本
└── README.md                                 # 项目说明
```

---

## 🔔 版本更新

### v1.2 - TTS语音回复版 (2025-12-27) 🆕

✨ **新增功能**
- 集成 TextToSpeech 语音合成引擎
- AI回复自动语音播报
- 双向语音对话体验（类似Kimi语音通话）
- 智能提取和执行shell命令

🔧 **改进**
- 扩展指令白名单（支持蓝牙、数据、按键等）
- 优化CmdReceiver执行逻辑（使用sh -c）
- 改为通过广播执行指令
- 完善错误处理和日志输出

### v1.1 - 基础功能修复 (2025-12-26)

🐛 **修复**
- 修复蓝牙控制无效问题
- 修复启动APP无响应问题
- 添加更多系统命令支持

### v1.0 - 首个版本 (2025-12-25)

🎉 **初始发布**
- 实时VAD语音检测
- Moonshot AI语音识别
- 系统指令执行
- 安全加密存储

---

## 📊 功能对比

| 功能 | v1.0 | v1.1 | v1.2 |
|------|------|------|------|
| VAD语音检测 | ✅ | ✅ | ✅ |
| AI指令解析 | ✅ | ✅ | ✅ |
| WiFi控制 | ✅ | ✅ | ✅ |
| 蓝牙控制 | ❌ | ✅ | ✅ |
| 启动APP | ❌ | ✅ | ✅ |
| **TTS语音回复** | ❌ | ❌ | ✅ |
| **双向对话** | ❌ | ❌ | ✅ |
| 扩展白名单 | 基础 | 中等 | 完整 |

---

## 📚 文档资源

### Q: 服务启动失败？
**A:** 检查API Key是否正确配置，格式至少20字符。

### Q: 无法录音？
**A:** 确保已授予录音权限，进入系统设置手动授权。

### Q: 一直在"正在监听"？
**A:** 环境太安静或阈值太高，尝试降低能量阈值。

### Q: 指令执行失败？
**A:** 查看日志确认指令是否在白名单中，是否被安全机制拦截。

更多问题请查看 [使用指南.md](使用指南.md)

---

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

---

## 📄 许可证

本项目采用 MIT License - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 🙏 致谢

- [Moonshot AI](https://www.moonshot.cn/) - 提供强大的AI能力
- [OkHttp](https://square.github.io/okhttp/) - 网络请求框架
- [Timber](https://github.com/JakeWharton/timber) - 日志工具
- [Kotlin](https://kotlinlang.org/) - 现代化的编程语言

---

## 📞 联系方式

- 项目Issues: [GitHub Issues](https://github.com/YOUR_USERNAME/KimiVoice/issues)
- 文档: [使用指南](使用指南.md) | [配置示例](配置示例.md)

---

<div align="center">
  <p>如果这个项目对您有帮助，请给个⭐️吧！</p>
  <p>Made with ❤️ by KimiVoice Team</p>
</div>
