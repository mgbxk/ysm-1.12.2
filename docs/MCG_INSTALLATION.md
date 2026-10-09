# MCG 整合包安装 YSM 教程

适用于 MinecraftGensoukyo（MCG）Minecraft 1.12.2 整合包。

## 准备文件

- [下载 vanillafix.cfg 配置文件](https://github.com/mgbxk/ysm-1.12.2/releases/download/v1.1/vanillafix.cfg)。
- [下载 YSM 1.1 本体：ysm-1.12.2-1.1.jar](https://github.com/mgbxk/ysm-1.12.2/releases/download/v1.1/ysm-1.12.2-1.1.jar)。
- **必需前置：[MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter)**，选择适用于 Minecraft 1.12.2 / Forge 的版本。

## 安装步骤

1. 完全退出游戏。
2. 打开 MCG 整合包目录，找到：

   ```text
   .minecraft/versions/MinecraftGensoukyo/config/vanillafix.cfg
   ```

   用上面下载的 `vanillafix.cfg` **替换这个同名文件**。

3. 把 **MixinBooter 前置 JAR** 和 **YSM 本体 `ysm-1.12.2-1.1.jar`** 一起放入：

   ```text
   .minecraft/versions/MinecraftGensoukyo/mods/
   ```

   如果已有旧版 YSM，先移出旧版，只保留一份 YSM；MixinBooter 也只保留一份。

4. 启动整合包即可。

## 配置说明

提供的配置已经将 VanillaFix 的 `fixes` 配置区设置为：

```cfg
B:modSupport=false
```

替换后无需再手动修改这一项。MixinBooter 是必需前置。

安装位置应以 MCG 实例目录为准：替换的是 `config/vanillafix.cfg`，两个 Mod JAR 放在同一实例的 `mods/` 文件夹。

## 基本操作

- **Alt+Y**：打开模型库。
- **Z**：打开动作轮盘。
- 模型目录：`.minecraft/versions/MinecraftGensoukyo/config/yes_steve_model/custom/`。