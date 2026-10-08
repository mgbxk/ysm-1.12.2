# 使用你的 GitHub 账号发布

## 文件夹用途

- source/：可构建的完整源码仓库，含 README、许可证、模型示例、390 份源代码/资源文件、Gradle Wrapper 和 GitHub Actions。将这里面的内容放在仓库根目录。
- release/：已经编译的 modern9 JAR、模组安装 ZIP、可导入 .mrpack 整合包、Release 文案和资源许可声明。适合作为 GitHub Release 附件。
- modpack/：整合包清单与 overrides，供手动安装或调整整合包。
- testing/：个人交付测试资料。可留在本地；其中个人模型测试截图不应自动随公开源码上传。

## 浏览器上传源码

1. 登录 https://github.com/new，仓库名称可用 ysm-1.12.2，按需要选择公开或私有。
2. 新建空仓库，不必让 GitHub 自动生成 README、许可证和 .gitignore；本包已备好。
3. 点击 uploading an existing file 或 Add file → Upload files。
4. 打开解压后的 source/，将里面的文件和子文件夹拖进上传区域。上传后检查根目录直接含 README.md、build.gradle、src/、gradle/，不要多套一层 source/。
5. 点 Commit changes 保存。系统隐藏的 .github/ 和 .gitignore 若没被拖进去，单独上传或用 GitHub 网页编辑器按对应路径新建；Gradle Wrapper 的 gradle-wrapper.jar 必须包含。
6. 项目较多文件时，浏览器上传可能要求分批处理。更省事的方式是在 GitHub Desktop 中将 source/ 作为本地仓库，再 Publish repository。

source/gradle.properties 中的 mod_url 和 mod_issue_tracker_url 当前保留上游地址；将来以你的仓库维护时可改为你的仓库和 Issues 地址，再编译下一版。保留原作者和资源署名，新增维护者名字可使用你自己的 GitHub 用户名。已测试的交付 JAR 仍保留上游元数据。

## 创建 Release

1. 打开你的仓库 Releases → Draft a new release，标签填写 v1.1.9-modern.9，标题填写 YSM 1.12.2 modern9。
2. 将 release/RELEASE_NOTES_MODERN9.md 的内容复制为版本说明。
3. 上传 release/ 下的 JAR、ysm-modern9-install.zip、ysm-modern9-starter.mrpack、SHA256SUMS.txt，以及 THIRD_PARTY_NOTICES.md、LICENSE 与 licenses/。licenses/ 可使用随附 ysm-modern9-license-notices.zip 附件。
4. 检查后点击 Publish release。GitHub 会自动提供该标签的源码 ZIP，不需要将完整交付包重复提交到源码仓库。

也可以从 Actions 手动运行 Create draft release。它先构建/测试，再生成草稿；进入 Release 检查后手动发布，并补传整合包与许可附件。工作流尚未在你的 GitHub 账号执行；构建需要能访问配置的依赖仓库。该流程无需把个人 Token 写进源码。

## 资源许可

BSD-3-Clause 允许发布修改后的主体源码；第三方组件许可、Wine Fox/图标非商业条款和示例模型署名分别保留。详情见 source/THIRD_PARTY_NOTICES.md。用户上传的蕾米莉亚模型不在公开发布材料里，不能将 testing/ 的个人模型截图直接认定为可公开资源。
