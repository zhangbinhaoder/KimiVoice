@echo off
chcp 65001 >nul
echo ========================================
echo    KimiVoice 快速测试工具
echo ========================================
echo.

:menu
echo 请选择操作：
echo.
echo [1] 安装Debug版本（开发调试）
echo [2] 安装Release版本（正式版本）
echo [3] 卸载应用
echo [4] 启动应用
echo [5] 查看实时日志
echo [6] 清除应用数据
echo [7] 拉取崩溃日志
echo [8] 检查权限状态
echo [9] 退出
echo.
set /p choice=请输入选项 [1-9]: 

if "%choice%"=="1" goto install_debug
if "%choice%"=="2" goto install_release
if "%choice%"=="3" goto uninstall
if "%choice%"=="4" goto start_app
if "%choice%"=="5" goto view_logs
if "%choice%"=="6" goto clear_data
if "%choice%"=="7" goto pull_crash
if "%choice%"=="8" goto check_permission
if "%choice%"=="9" goto end
goto menu

:install_debug
echo.
echo 正在安装Debug版本...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel%==0 (
    echo ✓ 安装成功！
) else (
    echo ✗ 安装失败，请检查设备连接
)
pause
goto menu

:install_release
echo.
echo 正在安装Release版本...
adb install -r app\build\outputs\apk\release\app-release.apk
if %errorlevel%==0 (
    echo ✓ 安装成功！
) else (
    echo ✗ 安装失败，请检查设备连接
)
pause
goto menu

:uninstall
echo.
echo 正在卸载应用...
adb uninstall com.example.kimivoice
if %errorlevel%==0 (
    echo ✓ 卸载成功！
) else (
    echo ✗ 卸载失败
)
pause
goto menu

:start_app
echo.
echo 正在启动应用...
adb shell am start -n com.example.kimivoice/.MainActivity
if %errorlevel%==0 (
    echo ✓ 启动成功！
) else (
    echo ✗ 启动失败
)
pause
goto menu

:view_logs
echo.
echo 正在查看实时日志（按 Ctrl+C 停止）...
echo.
adb logcat -c
adb logcat | findstr "KimiVoice Timber"
pause
goto menu

:clear_data
echo.
echo 正在清除应用数据...
adb shell pm clear com.example.kimivoice
if %errorlevel%==0 (
    echo ✓ 数据清除成功！
) else (
    echo ✗ 清除失败
)
pause
goto menu

:pull_crash
echo.
echo 正在拉取崩溃日志...
if not exist crash_logs mkdir crash_logs
adb pull /sdcard/Android/data/com.example.kimivoice/files/crash/ crash_logs/
if %errorlevel%==0 (
    echo ✓ 日志已保存到 crash_logs 目录
) else (
    echo ✗ 拉取失败，可能没有崩溃日志
)
pause
goto menu

:check_permission
echo.
echo 正在检查应用权限...
echo.
adb shell dumpsys package com.example.kimivoice | findstr "permission"
pause
goto menu

:end
echo.
echo 感谢使用！
exit
