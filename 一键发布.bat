@echo off
chcp 65001 >nul
echo ========================================
echo    KimiVoice GitHub 发布
echo    用户: zhangbinhaoder
echo ========================================
echo.

REM 检查Git
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git未安装！请先安装Git：
    echo    https://git-scm.com/download/win
    echo.
    pause
    exit /b 1
)

echo ✓ Git已就绪
echo.

REM 步骤1: 初始化
if not exist .git (
    echo [1/6] 初始化Git仓库...
    git init
    echo ✓ 完成
) else (
    echo [1/6] Git仓库已存在
)
echo.

REM 步骤2: 配置
echo [2/6] 配置用户信息...
git config user.name "zhangbinhaoder"
git config user.email "zhangbinhaoder@users.noreply.github.com"
echo ✓ 完成
echo.

REM 步骤3: 添加文件
echo [3/6] 添加文件...
git add .
echo ✓ 完成
echo.

REM 步骤4: 提交
echo [4/6] 提交代码...
git commit -m "Initial commit: KimiVoice AI语音助手

🎤 基于Moonshot AI的Android系统级语音控制应用

主要特性:
- ✅ 实时VAD语音检测
- ✅ AI指令生成（Moonshot AI）
- ✅ 企业级安全加密（AES-GCM + KeyStore）
- ✅ Kotlin协程异步处理
- ✅ 完善的安全机制和日志系统
- ✅ 支持Android 7.0+

技术栈:
- Kotlin 2.0.21
- AGP 8.7.3
- OkHttp 4.12.0
- Coroutines 1.9.0"

if %errorlevel% neq 0 (
    echo ⚠️  可能没有新的改动需要提交
)
echo.

REM 步骤5: 添加远程仓库
echo [5/6] 配置远程仓库...
git remote add origin https://github.com/zhangbinhaoder/KimiVoice.git 2>nul
if %errorlevel% neq 0 (
    git remote set-url origin https://github.com/zhangbinhaoder/KimiVoice.git
)
echo ✓ 完成
echo.

REM 步骤6: 推送
echo [6/6] 推送到GitHub...
echo.
echo ⚠️  重要提示：
echo 1. 确保已创建GitHub仓库: https://github.com/zhangbinhaoder/KimiVoice
echo 2. 首次推送需要登录（使用Personal Access Token）
echo.
pause

git branch -M main
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo    🎉 发布成功！
    echo ========================================
    echo.
    echo ✓ 代码已推送到GitHub
    echo ✓ 仓库地址: https://github.com/zhangbinhaoder/KimiVoice
    echo.
    echo 接下来可以：
    echo - 访问仓库查看代码
    echo - 添加仓库描述和标签
    echo - 创建Release发布APK
    echo.
) else (
    echo.
    echo ❌ 推送失败！
    echo.
    echo 可能原因：
    echo 1. 仓库不存在 - 请先在GitHub创建 KimiVoice 仓库
    echo 2. 需要登录 - 输入用户名 zhangbinhaoder 和 Personal Access Token
    echo 3. Token权限不足 - 确保Token有 repo 权限
    echo.
    echo 获取Token: https://github.com/settings/tokens
    echo.
)

pause
