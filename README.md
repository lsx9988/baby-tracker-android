# Baby Tracker · 育儿成长记录（Android）

本地优先、隐私默认的育儿成长记录安卓应用。数据先存本机数据库，云同步/家庭共享作为后续可选模块；不接入第三方统计、广告、崩溃、推送 SDK。

> 本项目为原创实现，功能分类与本地优先架构思路参考公开产品设计，代码、数据模型、UI 均独立编写，未复制任何第三方源码。

## 技术栈

| 项 | 选型 |
| --- | --- |
| 语言 / UI | Kotlin 1.9.24 + Jetpack Compose + Material 3 |
| 架构 | MVVM：ViewModel + Repository + Room DAO，手写 DI |
| 存储 | Room 2.6.1 + DataStore（规划中） |
| 后台 | WorkManager（提醒 / 备份 / 可选同步，规划中） |
| 最低版本 | minSdk 26（Android 8.0） / compileSdk 34 |
| 构建 | Gradle 8.10.2 · AGP 8.6.1 · JDK 17 |

## 当前已实现（P1–P2 基础）

- 宝宝档案：新建、多宝宝切换、备注、归档（表结构预留）
- 喂养记录：奶瓶 / 母乳左 / 母乳右 / 辅食 / 喝水 / 吸奶，分量与时长
- 健康记录：体温 / 体重 / 身高 / 头围 / 黄疸，数值与单位
- 本地 Room 持久化，首页最近记录列表（按时间倒序）
- 月龄描述、记录时间格式化
- 纯本地运行，无联网权限

## 规划增量（本项目相对同类工具的增强方向）

1. 成长里程碑时间线：抬头 / 翻身 / 坐 / 爬 / 站 / 走 / 叫爸妈等节点打卡回顾
2. 周报 / 月报自动汇总：奶量、睡眠、尿布、体温趋势，生成可分享卡片
3. 成长对比影像墙：按月龄存照片，自动拼对比图
4. 月龄辅食库 + 食材过敏标记与避坑提醒
5. 局域网直连同步：家人同 WiFi 下互传，不强制自有服务端
6. 端到端加密本地备份，导出可选密码保护
7. 可自定义健康预警阈值（发热 / 黄疸 / 奶量不足 / 睡眠过少）
8. 可选 AI 育儿日报（接入自有 LLM 网关，离线可关）
9. 脱敏导出：分享/导出可去除姓名、头像等身份信息
10. 更多一键记录磁贴：尿布 / 睡眠 / 体温控制中心直达

## 目录结构

```
app/src/main/java/com/babytracker/app/
├── BabyTrackerApp.kt        # Application + 容器初始化
├── MainActivity.kt          # 单 Activity 入口
├── di/AppContainer.kt        # 手写 DI 容器
├── data/
│   ├── local/               # Room：Database / Entity / DAO
│   └── repository/           # Baby / Record 仓库
├── domain/model/            # 记录类型枚举
├── ui/
│   ├── theme/               # 配色与主题
│   ├── home/                # 首页、宝宝档案、ViewModel
│   ├── record/              # 喂养录入
│   └── health/              # 健康录入
└── util/                    # 时间与月龄工具
```

## 本地构建

```bash
# 需要 JDK 17、Android SDK 34
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

## 隐私说明

- 记录默认存本机私有目录；卸载应用随私有目录清除。
- 本版本不申请网络权限；后续云同步、AI 日报等联网能力均为显式可选，关闭时代码不含联网调用。
- 详见 [docs/PRIVACY.md](docs/PRIVACY.md)。

## 路线图

见 [docs/ROADMAP.md](docs/ROADMAP.md)。

## 协议

代码以 [MIT License](LICENSE) 发布。记录数据归使用者本人所有。
