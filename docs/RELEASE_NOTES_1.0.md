# YSM 1.12.2 — 1.0

Minecraft Java 1.12.2 / Forge 的 YSM 独立修改版。支持部分公开新版模型数据、纯客户端本地模式、悬浮模型库和动作轮盘，并包含原交付版的模型预览及变身显示修复。

## 下载

- `ysm-1.12.2-1.0.jar`：Mod 本体，放入当前游戏实例的 `mods/`。
- `ysm-1.0-install.zip`：包含相同 Mod 本体、文字安装说明与许可证的安装包。
- `ysm-1.0-license-notices.zip`：完整许可证与第三方声明。
- `SHA256SUMS.txt`：附件校验值。

## 必需前置

**必须安装 [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)**，选择支持 Minecraft 1.12.2 / Forge 的版本，和 YSM 一起放进 `mods/`。游戏运行使用 Java 8；原交付包记录的实测环境为 Forge 14.23.5.2860 / MixinBooter 10.6。

## VanillaFix：仅已安装时修改

如果有 **VanillaFix**，打开 `config/vanillafix.cfg`，在 `fixes` 配置区改为：

```cfg
B:modSupport=false
```

保存后重启游戏。**没有 VanillaFix 就跳过这一步；MixinBooter 无论如何都必须安装。**

## 使用

移出旧 YSM，只保留一份 YSM 与一份 MixinBooter。**Alt+Y** 打开模型库，**Z** 打开动作轮盘。模型目录为 `config/yes_steve_model/custom/`；服务端未安装 YSM 时仅自己可见。

完整文字安装说明收录在安装 ZIP 和仓库的 `docs/INSTALLATION.md` 中。

## 版本说明与许可证

文件名按发布要求改为 `ysm-1.12.2-1.0.jar`；内容未改变，内部版本仍为 `1.1.9-modern.9`，本次没有重新编译或运行游戏测试。原交付材料中的测试记录见源码仓库的 `docs/TEST_REPORT_MODERN9.md`。

新版加密 `.ysm`、部分控制器、粒子和模组联动仍不支持。原作者与第三方资源署名保留，见 LICENSE 和 THIRD_PARTY_NOTICES.md；部分模型和图标带有非商业使用条款。