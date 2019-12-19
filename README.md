# MicroTESK

## Building from command line

To build the project, the following commands are available:

* `gradle assemble`
  - Building the distribution package
* `gradle test`
  - Running the unit tests
* `gradle build`
  - Building the distribution package
  - Running the unit tests
* `gradle release`
  - Building the distribution package
  - Running the unit tests
  - Creating the tag
  - Publishing the distribution package in the Nexus repository

## Working in IntelliJ IDEA

- Open `build.gradle` as a project

## Working in Eclipse

 - Generate the Eclipse project and classpath files with Gradle:
   ```
   $ gradle eclipse
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
