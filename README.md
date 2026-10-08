# YSM 1.12.2 — 1.0 独立修改
基于旧开源 LegacyYSM v1.1.9 的 Minecraft Java 1.12.2 / Forge 移植修改版。支持部分新版公开模型数据、纯客户端本地模式、悬浮模型库和动作轮盘。

。本项目独立维护；上游原作者、版权和资源许可证完整保留。

下载本体后必须同时安装前置 **[MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)**。详见 **[文字安装说明](docs/INSTALLATION.md)**。

[下载 Mod 本体：ysm-1.12.2-1.0.jar](https://github.com/mgbxk/ysm-1.12.2/releases/download/v1.0/ysm-1.12.2-1.0.jar) · [发布页面与安装包](https://github.com/mgbxk/ysm-1.12.2/releases/tag/v1.0)

## 安装

- Minecraft Java **1.12.2**；Forge 实测 **14.23.5.2860**；游戏运行使用 **Java 8**。
- 必需前置：[MixinBooter](https://modrinth.com/mod/mixinbooter)。本版本实测 **10.6**；更高前置版本未纳入 modern9 实机验证。
- 将 `ysm-1.12.2-1.0.jar` 与必需前置 **MixinBooter** 放入当前实例的 `mods/`，各只保留一份。
- **Alt+Y** 打开模型库；**Z** 打开动作轮盘，可在游戏“控制”中修改。
- 自定义模型目录：当前游戏实例的 `config/yes_steve_model/custom/`。放入整个模型文件夹或支持的 ZIP 后重新加载模型，或按 F3+T。

连接未安装 YSM 的服务器时自动启用本地模式，只有自己看见自己的模型。单人游戏及装有同版本 YSM 的服务端保留同步模式。服务端联机详情见 [CLIENT_MODE.md](CLIENT_MODE.md)。


### 安装了 VanillaFix 才需要修改配置

**如果你的整合包没有 VanillaFix，可以跳过此项。** 已安装时，打开当前实例的 `config/vanillafix.cfg`，找到 `fixes` 配置区，将其中的 `B:modSupport` 设置为：

```cfg
B:modSupport=false
```

保存后重新启动游戏。这一步关闭 VanillaFix 的模组兼容支持；**MixinBooter 是必需前置，无论是否安装 VanillaFix 都需要安装。**

## 支持与限制

支持公开 `ysm.json` spec 1/2 的未加密文件夹和 ZIP、已实现的动画控制器子集、`blend_transition` 曲线对象、部分 Molang、模型外观配置与分类轮盘。提供动画音效及 OptiFine / 支持实体 LabPBR 的光影接入；默认游戏渲染不具有完整 PBR 效果。

模型库和轮盘悬浮在游戏画面上，面板外继续显示游戏。modern9 修复预览朝向、裁剪、深度遮挡、共享骨骼姿态、变身与动作并行时的缩放覆盖，以及重复播放同名动作。

新版加密 `.ysm`、粒子、未实现的新版模组联动与部分脚本/控制器功能仍不支持。
主要代码采用 [BSD-3-Clause](LICENSE)，嵌入代码和美术资源分别沿用其许可。酒狐及图标为 **CC BY-NC-SA 4.0**；默认模型为 CC0；奶油桃示例为 CC BY 4.0。请保留 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)、licenses/ 和模型作者信息。

上游：https://github.com/RuiXuqi/YesSteveModel 。原始说明见 [docs/UPSTREAM_README.md](docs/UPSTREAM_README.md)。感谢 YSM 开发组、1.7.10 与 1.12.2 移植作者、GeckoLib / SauriaLib 及所有模型作者。

发布步骤和 Release 文案见 [docs/GITHUB_PUBLISH.md](docs/GITHUB_PUBLISH.md) 与 [docs/RELEASE_NOTES_1.0.md](docs/RELEASE_NOTES_1.0.md)。
