# Sciko Analysis

A Kotlin Multiplatform library for mathematical analysis operations.

## Features

- Polynomial root finding with Laguerre's method

## Usage

### Gradle

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/mihbor/sciko-analysis")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("ltd.mbor.sciko:sciko-analysis:0.1-SNAPSHOT")
}
```

### Authentication

To use packages from GitHub Packages, you need to authenticate:

1. **Using environment variables:**
   ```bash
   export USERNAME=your-github-username
   export TOKEN=your-github-personal-access-token
   ```

2. **Using gradle.properties:**
   ```properties
   gpr.user=your-github-username
   gpr.key=your-github-personal-access-token
   ```

The personal access token needs the `read:packages` scope.

## Publishing

### Automatic Publishing

The library is automatically published to GitHub Packages on every push to the main branch, when a release is created, or when the workflow is manually triggered.

### Manual Publishing

To publish manually:

```bash
export USERNAME=your-github-username
export TOKEN=your-github-personal-access-token
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.