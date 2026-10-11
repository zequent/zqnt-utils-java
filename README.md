# Zequent Utils SDK

A comprehensive Java SDK providing utilities for building gRPC services with Quarkus framework. This SDK includes support for Protocol Buffers, reactive programming with Mutiny, entity mapping, and database integration.

Since the public/internal split of the contracts, this artifact generates only the **internal**
contracts (`src/main/proto/internal/{v2,v3}` of the zqnt-protos submodule). The public contracts,
the telemetry mapping and the DTO/helper classes the SDKs use (`com.zqnt.utils.core`, `*.domains`,
`JsonUtils`) come from [`com.zqnt:zqnt-api-java`](https://github.com/Zequent/zqnt-api-java), a
dependency of this artifact, with unchanged packages.

## Features

- **gRPC Support**: Built-in gRPC service generation and stub creation
- **Protocol Buffers**: Integrated Protocol Buffer compilation
- **Reactive Programming**: Mutiny-based reactive utilities
- **Entity Mapping**: MapStruct-based entity-to-DTO mapping
- **Quarkus Integration**: Optional Quarkus framework support (Spring Boot compatible)
- **Database Support**: Hibernate ORM and reactive Panache integration
- **Validation**: Hibernate Validator support
- **REST API**: SmallRye OpenAPI documentation

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.zequent</groupId>
    <artifactId>zqnt-utils</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Maven Configuration**: Add GitHub Packages to your Maven settings (`~/.m2/settings.xml`):

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>

<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Zequent/zqnt-utils</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

## Requirements

- Java 21 or higher
- Maven 3.8.1 or higher

## Development

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Project Structure

```
src/main/
  ├── java/com/zequent/framework/    # Main source code
  │   ├── common/                    # Common utilities
  │   ├── sdks/                      # SDK implementations
  │   ├── services/                  # Service interfaces
  │   └── utils/                     # Utility classes
  ├── proto/                         # Protocol Buffer definitions
  └── resources/                     # Configuration files
```

## License

This SDK is provided under the **Zequent SDK License**.

**Usage Terms:**
- ✅ Non-commercial use is permitted
- ❌ Modifications and derivative works are NOT permitted
- ❌ Commercial use requires a separate Commercial License

**Commercial Use:**
If you intend to use this SDK in a commercial product or service, you must obtain a Commercial License from Zequent. Please contact: **licensing@zequent.com**

See [LICENSE](LICENSE) file for complete terms and conditions.

## Support

For issues, questions, and commercial licensing:
- GitHub Issues: [zqnt-utils/issues](https://github.com/Zequent/zqnt-utils/issues)
- Commercial Licensing: licensing@zequent.com
- Support: support@zequent.com

You can then execute your native executable with: `./target/utils-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

