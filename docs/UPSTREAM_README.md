# 本地新版模型兼容补丁

本目录是基于 LegacyYSM v1.1.9 修改的 `1.1.9-modern.2`，新增公开 `ysm.json`
spec 1/2 的未加密文件夹和 ZIP 模型读取，并加入玩家动画控制器、动画音效及 OptiFine PBR 接入。详细实现范围、限制和编译要求见
[MODERN_COMPAT.md](MODERN_COMPAT.md)。新版加密 .ysm 仍不支持；新版运行时仅支持文档列出的子集。

以下保留上游说明及许可证信息。

---

# 告示

YSM 1.12.2 现已转为闭源，支持加解密 1.2.0 以上版本模型版本将在完善后以混淆 + 原生库的形式发布。

本库仅作为旧开源版本存档，不会再有大更新，仅提供有限的支持。

# Yes Steve Model

[旧版 Yes Steve Model 模组](https://github.com/YesSteveModel/LgeacyYSM) 的 Minecraft 1.12.2 完整移植。

## 概述

Yes Steve Model 是一个修改原版玩家模型的 Minecraft 模组。

它采用了 GeckoLib 作为核心，使用 Minecraft 基岩版模型和动画文件，从而使玩家能够随心所欲的自定义玩家模型和动画。

## 状况

### 兼容性

模组需要 [MixinBooter](https://github.com/CleanroomMC/MixinBooter) 作为前置。

- 模型
  - 兼容 1.1.6 及以下版本三种格式的模型
  - 对 1.2.0 的非加密模型存在兼容问题
  - 暂不兼容 2.2.1 及以上版本文件夹和 ZIP 格式的模型
  - 永远不会兼容 1.2.0 及以上版本 YSM 加密格式的模型

- 模组
  - 兼容 [Aqua Acrobatics](https://github.com/embeddedt/aquaacrobatics) 的游泳动作
  - 兼容 [Oceanic Expanse](https://github.com/SirSquidly/Oceanic-Expanse)、[Trident Mod](https://github.com/jiGGO1/Trident)、[Future MC](https://github.com/thedarkcolour/Future-MC) 的三叉戟动作
  - 兼容 [Crossbows Backport](https://github.com/SmileycorpMC/crossbows-backport)、[Crossbow Mod](https://github.com/jbredwards/Crossbow-Mod) 的弩动作
  - 兼容 [Deeper Depths](https://github.com/SmileycorpMC/Deeper-Depths) 的望远镜动作
  - 兼容 [Mekanism Mixin Help](https://github.com/sddsd2332/MekanismMixinHelp) 的 HDPE 鞘翅

### 已知问题

- 加载模型时可见 Molang 报错，由 Molang 引擎过旧导致
- 模型状态似乎有污染问题，导致有时模型块变换有误
- 视角旋转过快时，头部角度会跳变
- 模型预览界花和草光照不对
- 部分文字颜色不对，这是因为新版文本组件会自动重置颜色，旧版 I18n 处理 String 时不会
- RenderFirstPlayerBackground 未经测试
- 刚进游戏时会有一瞬间报纹理丢失错误，这是因为纹理还未来得及同步

## 协议

### 源码

本项目的源码与上游 YSM 一样，使用 **[BSD-3-Clause](LICENSE)** 协议，您可以自由地使用、修改和分发代码，仅需要保留原始的版权声明。

### 资源

仓库中自带的模型文件采用不同的协议：

- **默认模型**: 采用 **CC0 (Creative Commons Zero)** 协议，完全开放，无任何使用限制
- **酒狐 (Wine Fox) 模型**: 采用 **CC BY-NC-SA 4.0** 协议，允许非商业使用，需要署名，并且衍生作品需要采用相同协议

仓库中的 **[模组图标](src/main/resources/yesstevemodel.png)** 提取自更新版本的 YSM，采用 **CC BY-NC-SA 4.0** 协议。

请在使用仓库内资源时严格遵守对应的协议要求。

## 致谢

- [YSM 开发组](https://github.com/YesSteveModel) 开源了旧版 YSM
- [一只大胡哩](https://github.com/Huli-fox) 和 [凯西](https://github.com/kaixiten) 制作了最初的 [1.7.10 版本](https://github.com/Huli-fox/YesSteveModel-Unofficial)
- [sddsd2332](https://github.com/sddsd2332) 制作了 [1.12.2 移植版](https://github.com/sddsd2332/YesSteveModel-Unofficial)，并为本项目做出了杰出贡献
- [GeckoLib](https://github.com/bernie-g/geckolib) 的所有开发者
- [kappa-maintainer](https://github.com/kappa-maintainer) 制作了 GeckoLib 延续版 [SauriaLib](https://github.com/kappa-maintainer/geckolib)
