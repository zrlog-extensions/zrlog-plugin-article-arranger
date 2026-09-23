# zrlog-plugin-article-arranger

ZrLog 文章排版插件。按配置选择文章，在站点中生成带目录的文章聚合页，适合系列文章和文档目录。

## 功能

- 选择需要纳入聚合页的文章
- 按文章分类分组展示和批量选择
- 配置聚合页主色和附加 CSS
- 只记录聚合关系，不修改文章正文

## 构建

```shell
export JAVA_HOME=${HOME}/dev/graalvm-jdk-latest
export PATH=${JAVA_HOME}/bin:$PATH
```

## 原生制品发布

Linux amd64/arm64 制品在上传前会调用 `zrlog-artifact-service`，通过与 `plugin-core`
相同的固定版本 `process-artifact` Action 完成压缩和 SHA-256、文件大小校验。
处理成功后才会生成最终制品的 MD5 并上传；处理失败会停止该平台的发布。
服务接收的版本号使用 `bin/build-info.sh` 生成的实际插件版本。

发布前需要配置 Actions Secret `ARTIFACT_SERVICE_TOKEN`，可在仓库中单独设置，
或授权该仓库使用同名组织 Secret。服务地址为 `https://webdav.zrlog.com/artifact`。
