----------------------------------------------------------------
 MicroTESK Project
----------------------------------------------------------------

 Configuration of the Eclipse IDE:

 1. Create a workspace located in this directory.

    Use the menu item "File/Switch Workspace...".
    Browse the directory.

 2. Create a project called "microtesk".

    Use the menu item "File/New/Project...".
    Choose "Java Project".
    Type the project name.    

 3. Change the compiler compliance level to 6.0.

    Use the menu item "Window/Preferences...".
    Choose "Java/Compiler".
    Select the compliance level.

 4. Add a dependency from the external libraries.

    Use the context menu item "Build path/Configure build path...".
    Add the external jars jars/*.jar.

 The demo generator project (optionally):

 1. Create the project called "microtesk-demo".

    Use the menu item "File/New/Project...".
    Choose "Java Project".
    Type the project name.    

 2. Change the compiler compliance level to 6.0.

    Use the menu item "Window/Preferences...".
    Choose "Java/Compiler".
    Select the compliance level.

 3. Configure the build path for the project.

    Use the context menu item "Build Path/Configure Build Path...".
    
    Add the project "test-fusion"

 For integration with TeSLa (obsolete):

 1. Install the ECLiPSE tool.

 2. Add the TeSLa library.

    Use the context menu item "Build path/Configure build path...".
    Add the external jar tesla-*.*.jar

 3. Add the ECLiPSe library.

    Use the context menu item "Build path/Configure build path...".
    Add the external jar eclipse.jar

 4. Add the ANTLR runtime library.

    Use the context menu item "Build path/Configure build path...".
    Add the external jar antlr-runtime-3.*.*.jar.

 5. Specify the ECLiPSe directory.

    -Declipse.directory="C:\Program Files\EcLiPSe 5.10"

----------------------------------------------------------------
