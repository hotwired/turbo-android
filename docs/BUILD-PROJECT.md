## Building from Source

### From Android Studio:

- Open the [project's Gradle file](build.gradle).
- In the menu, choose Build --> Rebuild project.

### From command line:

- Change directories to the project's root directory.
- Run `./gradlew clean assemble -p turbolinks`.

The .aar's will be built at `<project-root>/turbolinks/build/outputs/aar`.

### Running Tests

**From command line:**

- Change directories to the project's root directory.
- Run `./gradlew clean testRelease -p turbolinks`