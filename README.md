MicroTESK
=========

## Building from command line

To build the project, the following commands can be used:

- `gradle assemble` - building the distribution package;
- `gradle test`     - running unit tests;
- `gradle build`    - building the distribution package and running unit tests;
- `gradle release`  - building the distribution package, running unit tests,
                      creating tags, and publishing the distribution package
                      into the build repository.

## Working in IntelliJ IDEA

- Open `build.gradle` as a project.

## Working in Eclipse

 - Generate the Eclipse project and classpath files with Gradle:
   ```
   $ gradle eclipse
   ```

 - Create a workspace located in the directory with `microtesk`.

   - Use the menu item `File/Switch Workspace...`.
   - Browse the directory.

 - Import an existing project called `microtesk` into the workspace.

   - Use the menu item `File/Import...`.
   - Choose `General/Existing Projects into Workspace`.
   - Select the `microtesk` folder as the root directory.
   - Select the `microtesk` project located in this directory.
   - Press the `Finish` button.
