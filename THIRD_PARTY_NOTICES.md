# 第三方代码、模型与资源声明

本项目是基于 RuiXuqi/YesSteveModel（LegacyYSM v1.1.9）的独立修改版，不代表官方 YSM。原始项目源码的 BSD-3-Clause 版权与免责声明保留在 LICENSE；嵌入代码与美术资源分别沿用各自许可，不应把所有内容统一标为 BSD。

| 内容 | 原作者 / 来源 | 许可证及声明 |
| --- | --- | --- |
| 旧版 YSM 及 1.12.2 移植 | Yes Steve Model Dev Group、YS Group、TartaricAcid、AryochiL、TomatoPuddin、RuiXuqi、sddsd2332 | BSD-3-Clause，见 LICENSE 与 docs/UPSTREAM_README.md |
| geckolib3 动画引擎 | Bernie G. / GeckoThePecko、GeckoLib 贡献者 | MIT；源文件版权头原样保留，见 licenses/GeckoLib-1.12-MIT.txt；https://github.com/bernie-g/geckolib |
| mclib 数学表达式与插值代码 | 上游署名 fadookie (mclib)、McHorse / McLib 贡献者 | 保留上游致谢；McLib MIT 原文见 licenses/mchorse-mclib-LICENSE.txt；https://github.com/mchorse/mclib |
| 缓动公式 | Boris Chumichev | EasingManager.java 内保留 Copyright (c) 2015 与 MIT 全文 |
| Cloth 基础数学 Color 类 | shedaniel / cloth-basic-math | Unlicense，源文件保留来源；见 licenses/shedaniel-cloth-basic-math-LICENSE.txt |
| Gradle Wrapper | Gradle 贡献者 | 随附 licenses/Gradle-Apache-2.0.txt；https://github.com/gradle/gradle |
| 默认内置模型 | 各模型 info.json 中原作者，包含哥斯拉、端木、晴路卡、甜粽子、星屑海螺等 | 按上游声明采用 CC0，保留各模型原始作者与 info.json；见 licenses/CC0-1.0.txt |
| Wine Fox / 酒狐 | 哥斯拉、星屑海螺 | CC BY-NC-SA 4.0：署名、非商业、相同方式共享；见 builtin/wine_fox/info.json 与 licenses/CC-BY-NC-SA-4.0.txt |
| 模组图标 yesstevemodel.png | 上游 YSM 资源 | 按上游声明为 CC BY-NC-SA 4.0，非商业、署名、相同方式共享 |
| NaytoTime / 奶油桃 示例模型 | 奶油桃；动画作者龙某兄、奶油桃，其余署名见 ysm.json | CC BY 4.0；原资源未修改，新增说明；见 examples/naytotime/LICENSE、ATTRIBUTION.txt 与 README-upstream.md |
| controller_sound_pbr 示例 | LegacyYSM 默认模型原作者；新增控制器、动画、合成音效及辅助贴图 | CC0；见 examples/controller_sound_pbr/README.txt |
| MixinBooter 前置 | Rongmario / CleanroomMC 及相关贡献者 | LGPL-2.1-only；由整合包启动器从官方 Modrinth CDN 下载；许可证见 licenses/MixinBooter-LGPL-2.1.txt；源码 https://github.com/CleanroomMC/MixinBooter/tree/10.6 |

酒狐和模组图标的非商业条款适用于这些资源，不因此变更其他代码的许可。发布本项目或编译 JAR 时，请同时保留本文件、LICENSE、licenses/ 及资源作者声明。

本包未包含用户上传的蕾米莉亚模型、加密 .ysm、Minecraft 游戏文件、账号文件、登录令牌或私人服务器配置。外层 testing/ 是交付测试资料；其中蕾米莉亚测试截图没有取得独立公开发布授权，建议留在本地。GitHub 发布只需上传 source/ 中的内容及 release/ 成品。
