# GitHub 发布指南

## 🚀 快速发布（推荐）

### 方法一：使用自动化脚本

**最简单的方法！** 双击运行 `发布到GitHub.bat`，按照提示操作即可。

脚本会自动完成：
- ✅ 检查Git安装
- ✅ 初始化Git仓库
- ✅ 配置用户信息
- ✅ 添加所有文件
- ✅ 提交代码
- ✅ 推送到GitHub

---

## 📋 准备工作

### 1. 安装Git

如果还没安装Git：

1. 访问 https://git-scm.com/download/win
2. 下载 Git for Windows
3. 安装（保持默认选项即可）
4. 重启命令提示符

### 2. 创建GitHub仓库

1. 登录 GitHub: https://github.com
2. 点击右上角 "+" → "New repository"
3. 填写信息：
   - **Repository name**: `KimiVoice`
   - **Description**: `AI语音助手 - Android系统级语音控制应用`
   - **Public/Private**: 选择 Public（公开）或 Private（私有）
   - ⚠️ **不要**勾选 "Add a README file"
   - ⚠️ **不要**选择 .gitignore 模板
   - ⚠️ **不要**选择 License
4. 点击 "Create repository"

### 3. 获取GitHub Token（如需要）

如果推送时需要身份验证：

1. GitHub 设置 → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. 勾选 `repo` 权限
4. 生成并保存Token

---

## 📝 方法二：手动发布

如果您熟悉Git，可以手动执行：

```bash
# 1. 进入项目目录
cd "d:\KimiVoice源代码@系统级APP\KimiVoice"

# 2. 初始化Git仓库
git init

# 3. 配置用户信息
git config user.name "你的GitHub用户名"
git config user.email "你的邮箱"

# 4. 添加所有文件
git add .

# 5. 提交代码
git commit -m "Initial commit: KimiVoice AI语音助手"

# 6. 添加远程仓库（替换YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/KimiVoice.git

# 7. 设置主分支为main
git branch -M main

# 8. 推送到GitHub
git push -u origin main
```

---

## 🔐 身份验证

### 使用Token推送

如果需要Token认证：

```bash
# 推送时输入用户名和Token
Username: 你的GitHub用户名
Password: 你的Personal Access Token（不是密码！）
```

### 配置Token（一次性）

```bash
# Windows
git config --global credential.helper wincred

# 或者使用Token URL
git remote set-url origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/KimiVoice.git
```

---

## ✅ 发布后检查

发布成功后，访问您的仓库检查：

- ✅ 所有代码文件都已上传
- ✅ README.md 显示正常
- ✅ .gitignore 工作正常（build目录未上传）
- ✅ LICENSE 文件存在

---

## 🎨 美化仓库

### 1. 添加仓库描述

在仓库首页点击 ⚙️ Settings：
- **Description**: `🎤 基于Moonshot AI的Android系统级语音控制应用`
- **Topics**: 添加标签
  - `android`
  - `kotlin`
  - `voice-assistant`
  - `ai`
  - `moonshot`
  - `speech-recognition`

### 2. 创建Release版本

1. 进入仓库的 "Releases" 页面
2. 点击 "Create a new release"
3. 填写信息：
   - **Tag**: `v1.0.0`
   - **Release title**: `v1.0.0 - 首个正式版本`
   - **Description**: 复制以下内容

```markdown
## 🎉 KimiVoice v1.0.0

首个正式版本发布！

### ✨ 主要特性

- 🎯 实时VAD语音检测
- 🤖 AI指令生成（Moonshot AI）
- 🔒 企业级安全加密
- ⚡ 高性能异步处理
- 🛡️ 完善的安全机制
- 📊 详细的日志系统

### 📦 下载

- [app-release.apk](链接到APK文件)

### 📋 系统要求

- Android 7.0+
- 需要系统签名（完整功能）
- Moonshot API Key

### 📖 文档

- [使用指南](使用指南.md)
- [配置示例](配置示例.md)

### 🐛 已知问题

暂无

### 📝 更新日志

- 完整的语音识别和AI指令生成功能
- VAD语音活动检测
- 企业级安全加密（AES-GCM + KeyStore）
- 完善的异常处理和日志系统
```

4. 上传APK文件（可选）：
   - 拖拽 `app-release.apk` 到附件区域
   
5. 点击 "Publish release"

### 3. 添加徽章（可选）

在 README.md 顶部添加：

```markdown
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
```

---

## 🔄 后续更新

当您修改代码后，更新GitHub：

```bash
# 1. 查看修改
git status

# 2. 添加修改的文件
git add .

# 3. 提交
git commit -m "描述您的修改"

# 4. 推送
git push
```

---

## ❓ 常见问题

### Q: 推送失败：remote: Permission denied

**A:** Token权限不足或已过期，重新生成Token。

### Q: 推送失败：fatal: repository not found

**A:** 仓库名称不匹配或不存在，检查仓库地址。

### Q: 文件太大无法推送

**A:** 检查.gitignore，确保不包含build目录和APK文件。

### Q: 需要输入密码但不接受

**A:** GitHub已弃用密码认证，必须使用Personal Access Token。

---

## 📞 需要帮助？

如遇到问题：

1. 检查错误信息
2. 查看Git文档: https://git-scm.com/doc
3. 查看GitHub文档: https://docs.github.com

---

**祝您发布顺利！** 🎉
