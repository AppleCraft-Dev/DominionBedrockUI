# DominionBedrockUI

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%2B-62b47a?logo=minecraft)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Dominion](https://img.shields.io/badge/Dominion-4.9.3-3068b7.svg)](https://github.com/LunaDeerMC/Dominion)

[Dominion](https://github.com/LunaDeerMC/Dominion) 领地插件的基岩版表单界面扩展。

为基岩玩家提供表单式 GUI，覆盖 Dominion 领地管理的核心功能。

## 安装

1. 确认服务器已安装 [Dominion](https://github.com/LunaDeerMC/Dominion) 与 [floodgate](https://github.com/GeyserMC/Floodgate)；
2. 将 `DominionBedrockUI-x.x.x.jar` 放入服务器 `plugins/` 目录，重启服务器；
3. 无需额外配置，开箱即用。

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

## 致谢

- [Dominion](https://github.com/LunaDeerMC/Dominion)
- [floodgate](https://github.com/GeyserMC/Floodgate) 与 [Geyser](https://github.com/GeyserMC/Geyser)

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
