# DominionBedrockUI

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%2B-62b47a?logo=minecraft)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Dominion](https://img.shields.io/badge/Dominion-4.9.3-3068b7.svg)](https://github.com/LunaDeerMC/Dominion)

[Dominion](https://github.com/LunaDeerMC/Dominion) 领地插件的基岩版表单界面扩展。

为通过 Geyser / floodgate 联入 Java 版服务器的基岩玩家提供完整的表单式 GUI，覆盖 Dominion 领地管理的核心功能。**不修改 Dominion 本体任何代码**，Java 版玩家体验完全不变，双端数据通过 DominionAPI 天然互通。

## 功能特性

- **主菜单**：我的领地 / 我参与管理的领地 / 创建领地 / 当前所在领地 / 使用帮助
- **领地管理**：传送、重命名、进入与离开消息、设置传送点（当前位置）、转让、删除
- **旗标设置**：环境旗标、访客权限旗标、成员权限旗标、权限组旗标
- **成员管理**：添加 / 移出成员、设置成员所属权限组
- **权限组管理**：创建 / 重命名 / 删除组、组成员增删
- **创建领地**：基于圈地工具已选好的两个对角点，名称校验后直通 `/dominion create`，未选点、区域重叠、数量上限、经济扣费等由 Dominion 原生反馈
- **基岩选区边框补显**：Dominion 本体使用 BlockDisplay 实体渲染选区边框，基岩客户端无法显示；本扩展改用蓝色粒子沿长方体 12 条棱描边约 10 秒，颜色与时长和本体保持一致
- **命令自动分发**：基岩玩家执行无参 `/dominion` `/dom` 自动打开表单主菜单；Java 玩家与带子命令的调用一律放行，零干扰
- **Folia 兼容**：调度统一使用 GlobalRegionScheduler，兼容 Paper / Purpur / Leaf / Folia

## 环境要求

| 组件 | 要求 |
| --- | --- |
| 服务端 | Paper / Purpur / Leaf / Folia 1.20.1+ |
| Dominion | 4.9.3（硬依赖） |
| floodgate | 2.2.4+（软依赖，缺失时扩展自动停用、不影响服务器） |
| Geyser | 基岩玩家联入所需 |
| JDK | 21+（仅构建时需要） |

## 安装

1. 确认服务器已安装 [Dominion](https://github.com/LunaDeerMC/Dominion) 与 [floodgate](https://github.com/GeyserMC/Floodgate)；
2. 将 `DominionBedrockUI-1.0.0.jar` 放入服务器 `plugins/` 目录，重启服务器；
3. 无需额外配置，开箱即用。

> 从带审核功能的旧版本升级时：审核与 EasyBot 联动功能已移除，可删除残留的 `plugins/DominionBedrockUI/pending.yml`，并卸载 EasyBot 侧的 `dominion-approval` 插件。

## 使用

- **基岩玩家**：输入 `/dominion`（或 `/dom`）→ 自动打开表单主菜单；
- **任意玩家**：输入 `/dbui`（别名 `/dominionbedrock`、`/bedrockui`）→ 基岩玩家打开表单界面，Java 玩家自动转发至 Dominion 原界面；
- **权限**：`dominionbedrockui.use`（默认所有玩家可用）。

## 配置说明

配置文件位于 `plugins/DominionBedrockUI/config.yml`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `intercept-dominion-command` | `true` | 是否为基岩玩家拦截无参 Dominion 主命令并打开表单 |
| `intercept-commands` | `dominion`, `dom` | 触发拦截的命令列表（不含斜杠，仅无参数时拦截） |
| `create.name-regex` | `^[\w一-龥-]{1,32}$` | 表单创建领地时的名称校验正则（中英文/数字/下划线/连字符，1-32 字符） |

## 从源码构建

```bash
git clone https://github.com/AppleCraft-Dev/DominionBedrockUI.git
cd DominionBedrockUI
```

构建前需提供 Dominion API（运行时由服务器上的 Dominion 插件提供，编译期任选其一）：

- 将 Dominion 插件 jar（内含 API 类，可从 [Hangar](https://hangar.papermc.io/zhangyuheng/Dominion) / [Modrinth](https://modrinth.com/plugin/lunadeer-dominion) 下载）或 DominionAPI jar 放入项目根目录 `libs/` 文件夹；
- 或构建时用 `-PdominionJar=<jar 路径>` 显式指定。

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

产物：`build/libs/DominionBedrockUI-1.0.0.jar`

## 项目结构

```
src/main/java/cn/lunadeer/dominion/bedrockui/
├── DominionBedrockUI.java        # 主类：floodgate 检测、命令与监听器注册
├── platform/                     # 平台检测（floodgate 类型隔离，避免 NoClassDefFoundError）
├── form/Forms.java               # Cumulus 表单封装（SimpleForm / CustomForm / ModalForm）
├── service/DominionService.java  # 数据读写，全部通过 DominionAPI + Providers
├── menu/                         # 各级表单菜单（主菜单 / 领地 / 旗标 / 成员 / 权限组）
├── dispatch/
│   ├── UiDispatchListener.java   # 拦截无参 /dominion /dom，按客户端类型分发界面
│   └── SelectionBorderListener.java  # 基岩选区粒子边框（补显本体 BlockDisplay 边框）
├── command/BedrockUiCommand.java # /dbui 命令
└── util/                         # 文案（Lang）/ 配置 / 主线程调度（Sync）
```

## 已知限制

- 基岩客户端无法渲染 Display 实体，Dominion 原生的发光箱体边框（圈地选区与已建成领地查看）在基岩端不可见。本扩展目前为**圈地选区**提供粒子描边替代；已建成领地的边框显示仍依赖 Java 端。
- 基岩表单按钮为白底，按钮文字统一使用深色颜色代码以保证可读性，内容区则可正常使用浅色代码。

## 致谢

- [Dominion](https://github.com/LunaDeerMC/Dominion) —— 领地 / 防熊插件本体，作者 [LunaDeerMC](https://github.com/LunaDeerMC)
- [floodgate](https://github.com/GeyserMC/Floodgate) 与 [Geyser](https://github.com/GeyserMC/Geyser) —— 基岩版跨平台联入方案，作者 GeyserMC

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
