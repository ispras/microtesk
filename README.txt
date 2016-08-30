--------------------------------------------------------------------------------
| MicroTESK Project                                                            |
--------------------------------------------------------------------------------

 Building MicroTESK from command line:

 To build the project, the following commands can be used:

  gradle assemble - building the distribution package
  gradle test     - running unit tests
  gradle build    - building the distribution package and running unit tests
  gradle release  - building the distribution package, running unit tests,
                    creating tags, and publishing the distribution package
                    into the build repository

--------------------------------------------------------------------------------

 Working with MicroTESK in the Eclipse IDE:

 1. Run command 'gradle eclipse' to generate the Eclipse project and classpath
    files.

 2. Create a workspace located in the "<microtesk>/trunk" directory.

    Use the menu item "File/Switch Workspace...".
    Browse the directory.

 3. Import an existing project called "microtesk" into the workspace.

    Use the menu item "File/Import...".
    Choose "General/Existing Projects into Workspace".
    Select the "<microtesk>/trunk/microtesk" folder as the root directory.
    Select the "microtesk" project located in this directory.
    Press the "Finish" button: the project will be opened in the Eclipse IDE.

--------------------------------------------------------------------------------
