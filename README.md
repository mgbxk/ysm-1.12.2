# YSM 1.12.2

Minecraft Java 1.12.2 / Forge 的 YSM 独立修改版。

## 最新发布：1.1

**更新内容：支持新版 YSM 格式。**

[下载 Mod 本体：ysm-1.12.2-1.1.jar](https://github.com/mgbxk/ysm-1.12.2/releases/download/v1.1/ysm-1.12.2-1.1.jar) · [1.1 发布页面与安装包](https://github.com/mgbxk/ysm-1.12.2/releases/tag/v1.1) · [历史版本 1.0](https://github.com/mgbxk/ysm-1.12.2/releases/tag/v1.0)

本次发布使用提供的 `ysm-1.12.2-modern10-v3.jar`，仅将下载文件名改为 `ysm-1.12.2-1.1.jar`；内部版本为 `1.1.9-modern.10`。

## 安装

1. 使用 Minecraft Java **1.12.2 / Forge**，游戏运行使用 **Java 8**。
2. 完全退出游戏，移出旧 YSM，把 `ysm-1.12.2-1.1.jar` 放入当前实例的 `mods/`，只保留一个 YSM。
3. **必须安装前置 [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)**，选择支持 Minecraft 1.12.2 / Forge 的版本，一起放进 `mods/`。
4. 只有安装了 **VanillaFix** 时，打开 `config/vanillafix.cfg`，将 `fixes` 配置区中的设置改为：

```cfg
B:modSupport=false
```

没有 VanillaFix 就跳过配置修改；MixinBooter 无论如何都需要安装。详见 [文字安装说明](docs/INSTALLATION.md) 与 [1.1 更新说明](docs/RELEASE_NOTES_1.1.md)。

## MCG 整合包安装

[完整教程与配置下载](docs/MCG_INSTALLATION.md)：先用提供的 anillafix.cfg 替换 .minecraft/versions/MinecraftGensoukyo/config/vanillafix.cfg，再将 **MixinBooter 前置和 YSM 本体**放进同一实例的 .minecraft/versions/MinecraftGensoukyo/mods/。

## 基本操作

- **Alt+Y**：模型库。
- **Z**：动作轮盘。
- 自定义模型目录：`config/yes_steve_model/custom/`。

## 仓库源码与验证范围

新版对应源码未随 1.1 JAR 提供。本仓库源码和下方功能、构建、测试报告仍对应此前 **1.0** 交付版本；本次没有重新编译或运行游戏测试。GitHub 自动生成的 Source code 附件来自现有仓库源码。

## 此前 1.0 源码的支持与限制

支持公开 `ysm.json` spec 1/2 的未加密文件夹和 ZIP、已实现的动画控制器子集、`blend_transition` 曲线对象、部分 Molang、模型外观配置与分类轮盘。提供动画音效及 OptiFine / 支持实体 LabPBR 的光影接入；默认游戏渲染不具有完整 PBR 效果。

新版加密 `.ysm`、粒子、未实现的新版模组联动与部分脚本/控制器功能仍不支持。
主要代码采用 [BSD-3-Clause](LICENSE)，嵌入代码和美术资源分别沿用其许可。酒狐及图标为 **CC BY-NC-SA 4.0**；默认模型为 CC0；奶油桃示例为 CC BY 4.0。请保留 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)、licenses/ 和模型作者信息。

上游：https://github.com/RuiXuqi/YesSteveModel 。原始说明见 [docs/UPSTREAM_README.md](docs/UPSTREAM_README.md)。感谢 YSM 开发组、1.7.10 与 1.12.2 移植作者、GeckoLib / SauriaLib 及所有模型作者。

发布步骤和 Release 文案见 [docs/GITHUB_PUBLISH.md](docs/GITHUB_PUBLISH.md) 与 [docs/RELEASE_NOTES_1.0.md](docs/RELEASE_NOTES_1.0.md)。
