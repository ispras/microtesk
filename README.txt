----------------------------------------------------------------
 MicroTESK Project
----------------------------------------------------------------

 Building MicroTEST from command line:

 1. To build the project, run the build.sh script:

    sh build.sh

 2. To clean the project folder, run the script with the "clean" key:

    sh build.sh clean 

----------------------------------------------------------------

 Working with MicroTEST in the Eclipse IDE:

 1. Create a workspace located in the "<microtesk>/trunk" directory.

    Use the menu item "File/Switch Workspace...".
    Browse the directory.

 2. Import an existing project called "microtesk" into the workspace.

    Use the menu item "File/Import...".
    Choose "General/Existing Project into Workspace".
    Select the "<microtesk>/trunk/microtesk" folder as the root directory.
    Select the "microtesk" project located in this directory.
    The project will be opened in the Eclipse IDE.

 3. Set up the Ant building tool integrated into Eclipse to enable it 
    to run ANTLR tasks.

    Use the menu item "Window/Preferences...".
    Choose "Ant/Runtime".
    Select the "Classpath" page.
    Select the "Ant Home Entries (Default)" row.
    Click the "Add JARs..." button.
    Select the "microtesk/jars/ant-antlr3.jar" JAR file.

 4. Run the "build.xml" building script.

    Right-click the "build.xml" file in Project Exlorer.
    Select "Run As/Ant Build" from the context menu.
    Ant will run ANRLR to generate the needed code translation classes and
    build the project.

----------------------------------------------------------------
