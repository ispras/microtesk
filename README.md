# MicroTESK

## Working in command line

To build the project in command line, use [Gradle](https://gradle.org/) (via the wrapper `gradlew`).

The following commands are available:

* `./gradlew clean`
  - Clean the previous build
* `./gradlew assemble`
  - Build the distribution package
* `./gradlew test`
  - Run the unit tests
* `./gradlew build`
  - Build the distribution package
  - Run the unit tests
* `./gradlew release`
  - Build the distribution package
  - Run the unit tests
  - Create the tag
  - Publish the distribution package in [Nexus](https://forge.ispras.ru/nexus/)

## Working in [IntelliJ IDEA](https://www.jetbrains.com/idea/)

- Open `build.gradle` as a project

## Working in [Eclipse IDE](https://www.eclipse.org/ide/)

 - Generate the Eclipse IDE project and classpath files with [Gradle](https://gradle.org/):
   ```
   $ ./gradlew eclipse
   ```

 - Create a workspace located in the directory with `microtesk`

   - Use the menu item `File/Switch Workspace...`
   - Browse the directory

 - Import an existing project called `microtesk` into the workspace

   - Use the menu item `File/Import...`
   - Choose `General/Existing Projects into Workspace`
   - Select the `microtesk` folder as the root directory
   - Select the `microtesk` project located in this directory
   - Press the `Finish` button
