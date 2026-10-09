# YSM 1.12.2 — 1.1 安装说明

## 更新内容

**支持新版 YSM 格式。**

## 下载与安装

- `ysm-1.12.2-1.1.jar`：新版 Mod 本体，放入 Minecraft 1.12.2 / Forge 实例的 `mods/`。
- `ysm-1.1-install.zip`：相同 Mod 本体、纯文字安装说明和许可证。
- `ysm-1.1-license-notices.zip`：许可证及第三方声明，包括新版 JAR 自带的许可文件。
- `SHA256SUMS.txt`：下载附件的 SHA-256 校验值。

退出游戏后移出旧 YSM，仅保留一份 YSM。游戏运行使用 Java 8。

**必需前置：[MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)。** 选择适用于 Minecraft 1.12.2 / Forge 的版本，与 YSM 一起放进 `mods/`。

## VanillaFix：只有已安装时才修改

如果安装了 **VanillaFix**，打开当前实例的 `config/vanillafix.cfg`，在 `fixes` 配置区将设置改为：

```cfg
B:modSupport=false
```

保存后重启游戏。没有 VanillaFix 就跳过这一步；无论是否安装 VanillaFix，都必须安装 MixinBooter。

## 使用

**Alt+Y** 打开模型库；**Z** 打开动作轮盘。模型目录为 `config/yes_steve_model/custom/`。

## 发布说明

本体来自用户提供的 `ysm-1.12.2-modern10-v3.jar`，对外发布文件名改为 `ysm-1.12.2-1.1.jar`。文件内容未改变，JAR 内部版本为 `1.1.9-modern.10`。

本次更新内容由发布者提供；未重新编译或运行游戏测试。新版对应源码未随 JAR 提供，仓库源码与原有测试报告仍对应此前的 1.0 交付版本，GitHub 自动生成的 Source code 附件也来自该仓库源码。

安装说明使用文字，未附带聊天截图。原作者、主体许可证及第三方组件声明予以保留。

## MCG 整合包

使用 MCG（MinecraftGensoukyo）时，请按 [MCG 安装教程](MCG_INSTALLATION.md) 先替换提供的 VanillaFix 配置，再把 MixinBooter 和 YSM 本体放入该实例的 mods 文件夹。
