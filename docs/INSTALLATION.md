# YSM 1.12.2 — 1.0 安装说明

## 必需环境和前置

- Minecraft Java 1.12.2，Forge，游戏运行使用 Java 8。
- **必须安装前置模组 MixinBooter**，将它的 JAR 与 YSM 一起放入当前游戏实例的 `mods/` 文件夹。
- [MixinBooter 官方下载页](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)：选择支持 Minecraft 1.12.2 / Forge 的版本。可从下载页选择适配版本；原交付包记录的实测版本为 10.6，更高版本未在该交付包中验证。
- 移出旧 YSM，只保留一份 YSM 和一份 MixinBooter。

## 安装步骤

1. 完全退出游戏。
2. 下载 `ysm-1.12.2-1.0.jar`，放入当前游戏实例的 `mods/`。
3. 下载并安装必需前置 **MixinBooter**。
4. **只有安装了 VanillaFix 时，才执行下面的配置修改。没有 VanillaFix 就跳过这一步。**
5. 保存配置后启动游戏。

## 已安装 VanillaFix 时

打开当前游戏实例的 `config/vanillafix.cfg` 文件，找到 `fixes` 配置区，将其中的设置改成：

```cfg
B:modSupport=false
```

也就是关闭 VanillaFix 的模组兼容支持。若该配置文件尚未生成，先启动一次游戏，退出后再修改。配置修改与 JAR 文件改名是两件独立的事。

## 基本操作

- **Alt+Y**：打开模型库。
- **Z**：打开动作轮盘。
- 自定义模型目录：`config/yes_steve_model/custom/`。
- 放入模型文件夹或支持的 ZIP 后重新加载模型，或按 **F3+T**。

## 文件名与版本

本次对外发布文件名为 `ysm-1.12.2-1.0.jar`。仅修改文件名，JAR 内容和内部版本标识保持原交付版 `1.1.9-modern.9`，没有重新编译。

本项目是旧开源 LegacyYSM 的独立修改版，请保留原作者署名、许可证和第三方资源声明。完整功能、构建方式和兼容限制见仓库 README。