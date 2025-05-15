# TruthGuardian项目说明文档

## 项目概述

TruthGuardian是一个Android应用项目，目前处于初始开发阶段。该应用采用了现代Android应用架构，使用Java语言开发，并遵循了MVVM（Model-View-ViewModel）设计模式。

## 技术栈

- **开发语言**：Java（JDK 11）
- **构建系统**：Gradle（Kotlin DSL）
- **最低SDK版本**：24（Android 7.0 Nougat）
- **目标SDK版本**：35

## 项目结构

```
TruthGuardian/
├── app/                  # 主应用模块
│   ├── src/              # 源代码目录
│   │   ├── main/         # 主要源代码
│   │   │   ├── java/     # Java代码
│   │   │   │   └── com/example/truthguardian/  # 包结构
│   │   │   │       ├── MainActivity.java       # 主活动
│   │   │   │       └── ui/                     # UI组件
│   │   │   │           ├── home/               # 首页相关组件
│   │   │   │           ├── dashboard/          # 仪表盘相关组件
│   │   │   │           └── notifications/      # 通知相关组件
│   │   │   ├── res/     # 资源文件
│   │   │   │   ├── layout/      # 布局文件
│   │   │   │   ├── navigation/  # 导航配置
│   │   │   │   ├── values/      # 字符串、颜色等资源
│   │   │   │   └── ...          # 其他资源
│   │   │   └── AndroidManifest.xml  # 应用清单
│   │   ├── androidTest/  # Android测试代码
│   │   └── test/         # 单元测试代码
│   ├── build.gradle.kts  # 应用模块的构建配置
│   └── proguard-rules.pro # ProGuard混淆规则
├── build.gradle.kts      # 顶级构建配置
└── settings.gradle.kts   # 项目设置
```

## 主要组件

### 1. 核心架构

该项目采用Navigation Component架构，使用BottomNavigationView实现底部导航栏，包含三个主要的Fragment：

- **Home**：首页视图
- **Dashboard**：仪表盘视图
- **Notifications**：通知视图

### 2. MVVM架构

每个页面都遵循MVVM架构模式：

- **Model**：数据层（尚未实现）
- **View**：Fragment（如HomeFragment）
- **ViewModel**：数据持有者（如HomeViewModel）

### 3. 主要组件与依赖

- **UI组件**：
  - BottomNavigationView：底部导航栏
  - NavHostFragment：导航宿主
  - ConstraintLayout：约束布局

- **技术依赖**：
  - AndroidX：Android扩展库
  - ViewModel和LiveData：用于MVVM架构
  - Navigation Component：用于页面导航
  - Material Design：材料设计组件

## 功能模块

### 1. Home模块
当前实现为简单的文本展示页面，显示"欢迎来到truth guard app"。

### 2. Dashboard模块
仪表盘功能，当前实现为简单的文本展示页面。

### 3. Notifications模块
通知功能，当前实现为简单的文本展示页面。

## 构建与运行

### 环境要求

- Android Studio Iguana或更高版本
- JDK 11+
- Android SDK 35

### 构建步骤

1. 克隆项目到本地
2. 在Android Studio中打开项目
3. 点击"Run"按钮或使用`./gradlew assembleDebug`命令构建应用

## 当前状态

项目目前处于初始开发阶段，提供了基本的导航结构和MVVM架构框架，但具体业务功能尚未实现。

## 未来发展

项目未来可能需要实现以下功能：

1. 数据层实现（Room数据库、远程API等）
2. 用户认证功能
3. 内容真实性验证功能（根据项目名称推测）
4. 优化UI/UX设计
5. 添加单元测试和UI测试 