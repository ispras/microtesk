--------------------------------------------------------------------------------
| MicroTESK Project                                                            |
--------------------------------------------------------------------------------

 Building MicroTESK from command line:

 1. To build the project, run the following command:

    ant

 2. To clean the project folder, run the same command with the "clean" key:

    ant clean 

--------------------------------------------------------------------------------

 Working with MicroTESK in the Eclipse IDE:

 1. Create a workspace located in the "<microtesk>/trunk" directory.

    Use the menu item "File/Switch Workspace...".
    Browse the directory.

 2. Import an existing project called "microtesk" into the workspace.

    Use the menu item "File/Import...".
    Choose "General/Existing Projects into Workspace".
    Select the "<microtesk>/trunk/microtesk" folder as the root directory.
    Select the "microtesk" project located in this directory.
    Press the "Finish" button: the project will be opened in the Eclipse IDE.
 
 3. Run the "build.xml" building script.

    Run 'ant -f setup.xml' from the "<microtesk>/trunk/microtesk" folder.
    Right-click the "build.xml" file in Package Explorer.
    Select "Run As/Ant Build" from the context menu.
    Ant will run ANRLR to generate the needed code translation classes and
    build the project.

--------------------------------------------------------------------------------
