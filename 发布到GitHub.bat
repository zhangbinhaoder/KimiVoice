@echo off
chcp 65001 >nul
echo ========================================
echo    KimiVoice GitHub 发布工具
echo ========================================
echo.

REM 检查Git是否安装
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未检测到Git！
    echo.
    echo 请先安装Git:
    echo 1. 访问 https://git-scm.com/download/win
    echo 2. 下载并安装Git for Windows
    echo 3. 重启命令提示符后再运行此脚本
    echo.
    pause
    exit /b 1
)

echo ✓ Git已安装
echo.

REM 获取GitHub用户名
set /p GITHUB_USERNAME=请输入您的GitHub用户名: 

if "%GITHUB_USERNAME%"=="" (
    echo ❌ 错误: GitHub用户名不能为空！
    pause
    exit /b 1
)

echo.
echo 您的GitHub用户名: %GITHUB_USERNAME%
echo 仓库地址将是: https://github.com/%GITHUB_USERNAME%/KimiVoice
echo.
set /p CONFIRM=确认无误？(Y/N): 

if /i not "%CONFIRM%"=="Y" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo ========================================
echo    开始发布流程
echo ========================================
echo.

REM 步骤1: 检查是否已初始化Git
if not exist .git (
    echo [1/7] 初始化Git仓库...
    git init
    if %errorlevel% neq 0 (
        echo ❌ Git初始化失败！
        pause
        exit /b 1
    )
    echo ✓ Git仓库初始化成功
) else (
    echo [1/7] Git仓库已存在，跳过初始化
)
echo.

REM 步骤2: 配置Git用户信息
echo [2/7] 配置Git用户信息...
git config user.name "%GITHUB_USERNAME%"
git config user.email "%GITHUB_USERNAME%@users.noreply.github.com"
echo ✓ Git用户信息配置完成
echo.

REM 步骤3: 添加所有文件
echo [3/7] 添加文件到暂存区...
git add .
if %errorlevel% neq 0 (
    echo ❌ 添加文件失败！
    pause
    exit /b 1
)
echo ✓ 文件添加成功
echo.

REM 步骤4: 查看状态
echo [4/7] 查看Git状态...
git status
echo.

REM 步骤5: 提交代码
echo [5/7] 提交代码...
git commit -m "Initial commit: KimiVoice AI语音助手

- 完整的语音识别和AI指令生成功能
- VAD语音活动检测
- 企业级安全加密（AES-GCM + KeyStore）
- 完善的异常处理和日志系统
- 支持Android 7.0+
- 使用Moonshot AI API"

if %errorlevel% neq 0 (
    echo ❌ 代码提交失败！
    pause
    exit /b 1
)
echo ✓ 代码提交成功
echo.

REM 步骤6: 添加远程仓库
echo [6/7] 添加GitHub远程仓库...
echo.
echo 正在添加远程仓库: https://github.com/%GITHUB_USERNAME%/KimiVoice.git
git remote add origin https://github.com/%GITHUB_USERNAME%/KimiVoice.git
if %errorlevel% neq 0 (
    echo.
    echo ⚠️  远程仓库可能已存在，尝试更新...
    git remote set-url origin https://github.com/%GITHUB_USERNAME%/KimiVoice.git
)
echo ✓ 远程仓库配置完成
echo.

REM 步骤7: 推送到GitHub
echo [7/7] 推送到GitHub...
echo.
echo ⚠️  重要提示：
echo 1. 请确保已在GitHub创建了名为 "KimiVoice" 的空仓库
echo 2. 如果还没创建，请访问: https://github.com/new
echo 3. 仓库名称必须是: KimiVoice
echo 4. 创建时不要添加README、.gitignore或LICENSE（已包含）
echo.
set /p READY=已创建好GitHub仓库？(Y/N): 

if /i not "%READY%"=="Y" (
    echo.
    echo 操作已暂停。请完成以下步骤：
    echo 1. 访问 https://github.com/new
    echo 2. 创建名为 KimiVoice 的仓库
    echo 3. 重新运行此脚本
    pause
    exit /b 0
)

echo.
echo 正在推送代码到GitHub...
echo （首次推送可能需要输入GitHub账号密码或Token）
echo.

git branch -M main
git push -u origin main

if %errorlevel% neq 0 (
    echo.
    echo ❌ 推送失败！
    echo.
    echo 可能的原因：
    echo 1. 仓库不存在或名称不匹配
    echo 2. 需要身份验证（请配置GitHub Token）
    echo 3. 网络连接问题
    echo.
    echo 手动推送命令：
    echo   git push -u origin main
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo    🎉 发布成功！
echo ========================================
echo.
echo ✓ 代码已成功推送到GitHub
echo ✓ 仓库地址: https://github.com/%GITHUB_USERNAME%/KimiVoice
echo.
echo 接下来您可以：
echo 1. 访问仓库查看代码
echo 2. 添加仓库描述和标签
echo 3. 创建Release版本
echo 4. 分享给其他人
echo.
pause
