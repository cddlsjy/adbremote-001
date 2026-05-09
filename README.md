[English](README_en.md) / [中文](README.md)

# ADB Remote ATV
计划增加IP扫描功能

Android TV 的遥控器，基于 ADB Shell 命令

ADB Remote ATV 是一个 Android TV 的遥控器，基于 [ADB Shell](https://github.com/cgutman/AdbLib) 命令，泛用性更高。

下面的 shell 命令，是软件的基本原理，通过 shell 命令可模拟物理遥控器的基本按键，此外还可以快捷启动指定APP、借助手机软键盘输入中/英字符等。

```shell
# 输入事件
adb shell input text <string>   # 向设备输入字符
adb shell input keyevent <key_code>   # 向设备输入按键事件
# 启动软件
adb shell am start <package/activity>	# 启动设备上的软件
shell ime set <app>	# 切换输入法
shell settings get secure default_input_method	# 获取当前输入法
```



## 软件功能

### 按键

对于的 shell 命令如下：

```shell
adb shell input keyevent <key_code>   # 向设备输入按键事件
```

其中的 keycode如下表：

| 功能               | 字符常量                   | 数字键值 |
| ------------------ | -------------------------- | -------- |
| 返回键             | KEYCODE_BACK               | 4        |
| 按键Home           | KEYCODE_HOME               | 3        |
| 菜单键             | KEYCODE_MENU               | 82       |
| 扬声器静音键       | KEYCODE_VOLUME_MUTE        | 164      |
| 音量增加键         | KEYCODE_VOLUME_UP          | 24       |
| 音量减小键         | KEYCODE_VOLUME_DOWN        | 25       |
| 导航键 向上        | KEYCODE_DPAD_UP            | 19       |
| 导航键 向下        | KEYCODE_DPAD_DOWN          | 20       |
| 导航键 向左        | KEYCODE_DPAD_LEFT          | 21       |
| 导航键 向右        | KEYCODE_DPAD_RIGHT         | 22       |
| 导航键 确定键      | KEYCODE_DPAD_CENTER        | 23       |
| 数字按键           | KEYCODE_0 - KEYCODE_9      | 7 - 16   |
| 退格键             | KEYCODE_DEL                | 67       |
| TV 键              | KEYCODE_TV                 | 170      |
| 电源键             | KEYCODE_TV_POWER           | 177      |
| 多媒体键 播放/暂停 | KEYCODE_MEDIA_PLAY_PAUSE   | 85       |
| 多媒体键 快进      | KEYCODE_MEDIA_FAST_FORWARD | 95       |
| 多媒体键 快退      | KEYCODE_MEDIA_REWIND       | 89       |
| 多媒体键 上一首    | KEYCODE_MEDIA_PREVIOUS     | 88       |
| 多媒体键 下一首    | KEYCODE_MEDIA_NEXT         | 87       |



### 字符输入

支持中/英字符，借助手机软键盘将字符输入到 Android TV 中。注意中文字符需要[ADBKeyboard](https://github.com/senzhk/ADBKeyBoard?tab=readme-ov-file)支持。

英文字符的 shell 命令如下：

```shell
shell input text <string>  # 向设备输入按键事件
```

中文字符的 shell 命令如下，需要[ADBKeyboard](https://github.com/senzhk/ADBKeyBoard?tab=readme-ov-file)的支持：

```shell
shell am start -a android.intent.action.VIEW -d <string>
```

切换输入法命令如下：

```shell
shell ime set <app>

# 例如
shell ime set com.android.adbkeyboard/.AdbIME  # 切换到ADBKeyboard
```

获取当前输入法：

```shell
shell settings get secure default_input_method
```



### 快捷启动

对应的 shell 命令如下：

```shell
shell am start <package/activity>  # 向设备输入按键事件

# 例如
shell am start com.github.tvbox.osc/.ui.activity.HomeActivity	# 启动TVBox
```

快捷启动软件可从软件仓库中添加，仓库从在线地址中解析，该 json 文件为项目根目录中的`apps.json`。例如

```json
[
    {
        "name": "TVBox",
        "icon": "https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/tvbox.png",
        "url": "com.github.tvbox.osc/.ui.activity.HomeActivity"
    },
    {
        "name": "TVBox UI美化版",
        "icon": "https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/tvbox.png",
        "url": "com.github.tvbox.osc.tk/com.github.tvbox.osc.ui.activity.HomeActivity"
    },
]
```

其中：

- `name`：为该软件的名称
- `icon`：为该软件的图标，便于展示
- `url`：为该软件的启动路径。格式参考`com.github.tvbox.osc/.ui.activity.HomeActivity`



**如何获取一个 APP 的启动路径**，可从该软件的 AndroidManifest.xml 文件中获取，下面是一个示例文件，

- 从`manifest`标签中找到`package`属性，为启动路径的包 package
- 从带有`LAUNCHER`的`activity`中找到`android:name`属性，为启动路径的 activity
- `package/activity`组合起来就是启动命令的路径参数。

```xml
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" 
          android:compileSdkVersion="33" 
          android:compileSdkVersionCodename="13" 
          package="tech.simha.androidtvremote" 
          platformBuildVersionCode="33" platformBuildVersionName="13">
    
    <application 
         android:appComponentFactory="androidx.core.app.CoreComponentFactory" 
         android:hardwareAccelerated="true" 
         android:icon="@mipmap/launcher_icon" 
         android:label="Remote ATV" 
         android:name="android.app.Application">
        
        <activity 
          android:exported="true" 
          android:hardwareAccelerated="true" 
          android:launchMode="singleTop" 
          android:name="tech.simha.androidtvremote.MainActivity" 
          android:screenOrientation="portrait" 
          android:theme="@style/LaunchTheme" 
          android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
</manifest>
```





## 软件界面

![](https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/icons/appscreen.jpg)



## 鸣谢

**ADBlib**：https://github.com/cgutman/AdbLib

**ADBKeyboard**：https://github.com/senzhk/ADBKeyBoard?tab=readme-ov-file

**圆形菜单按键**：https://github.com/D10NGYANG/DL10RoundMenuView

**数字进度条**：https://github.com/daimajia/NumberProgressBar
