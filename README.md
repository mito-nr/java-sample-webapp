# Java Webアプリケーション with New Relic APM

Spring Bootを使用したWebアプリケーションで、New Relic APMの主要機能を実装したデモプロジェクトです。

## 機能

- Spring Boot 3.2.0ベースのWebアプリケーション
- New Relic APM統合
  - カスタムエラー記録（noticeError API）
  - ユーザーID追跡（setUserId）
  - カスタム属性追加
- 負荷テストツール付属

## 必要な環境

- Java 17以上
- Maven 3.6以上
- New Relicアカウント（APM監視用）

## クイックスタート

### 1. リポジトリをクローン

```bash
git clone <repository-url>
cd java-webapp
```

### 2. New Relic設定

環境変数を設定：

```bash
export NEW_RELIC_LICENSE_KEY=your_license_key_here
export NEW_RELIC_APP_NAME="Java Webapp"
```

または `.env.example` をコピーして `.env` を作成し、ライセンスキーを設定してください。

### 3. ビルドと起動

```bash
# ビルド
mvn clean package

# New Relicエージェント付きで起動
java -javaagent:target/newrelic/newrelic.jar \
  -Dnewrelic.config.license_key=$NEW_RELIC_LICENSE_KEY \
  -Dnewrelic.config.app_name="Java Webapp" \
  -jar target/webapp-1.0.0.jar
```

### 4. アクセス

ブラウザで以下にアクセス：
- http://localhost:8080/ - ホーム
- http://localhost:8080/error-test - エラーテスト
- http://localhost:8080/custom-attributes - カスタム属性デモ

## New Relic機能

### 1. エラー記録（noticeError）

`/error-test` エンドポイントでエラーを意図的に発生させ、New Relicに記録します。

```java
newRelicService.noticeError(exception, customAttributes);
```

### 2. ユーザーID追跡（setUserId）

各リクエストでユーザーIDを設定し、New Relicで追跡できます。

```java
newRelicService.setUserId("user_123");
```

### 3. カスタム属性

任意の属性をトランザクションに追加できます。

```java
newRelicService.addCustomAttribute("key", "value");
```

## 負荷テスト

### 継続的な負荷

```bash
./continuous-load.sh http://localhost:8080 5
```

### 並列負荷テスト

```bash
./load-test.sh http://localhost:8080 10 1000
```

### ワンライナー

```bash
while true; do curl -s http://localhost:8080 > /dev/null; echo "$(date '+%H:%M:%S') - リクエスト送信"; sleep 0.2; done
```

## プロジェクト構成

```
.
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/webapp/
│       │       ├── WebApplication.java
│       │       ├── controller/
│       │       │   └── HomeController.java
│       │       └── service/
│       │           └── NewRelicService.java
│       └── resources/
│           ├── application.properties
│           └── templates/
│               ├── index.html
│               ├── error.html
│               └── custom.html
├── pom.xml
├── newrelic.yml
├── continuous-load.sh
└── load-test.sh
```

## New Relic設定

`newrelic.yml` で詳細な設定が可能：
- アプリケーション名
- ログレベル
- トランザクショントレーサー
- エラーコレクター
- 分散トレーシング

## ライセンス

MIT License - 詳細は [LICENSE](LICENSE) を参照してください。

## 参考リンク

- [New Relic Java Agent Documentation](https://docs.newrelic.com/docs/apm/agents/java-agent/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
