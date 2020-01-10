# MicroTESK Installation Guide

## System Requirements

Being developed in Java, MicroTESK can be used on Windows, Linux, macOS, and other platforms with
the following software installed:

* JDK 11+ (https://openjdk.java.net);
* Apache Ant 1.8+ (https://ant.apache.org).

To generate test data based on constraints (if required), MicroTESK needs an SMT solver such as
Z3 or CVC4 (see `tools/README.md`).

## Installation Steps

1. Download from http://forge.ispras.ru/projects/microtesk/files and unpack a distribution package
   (the latest `.tar.gz` file).
   The destination directory will be further referred to as `<INSTALL_DIR>`.

1. Set the `MICROTESK_HOME` environment variable to the `<INSTALL_DIR>`+ path
   (see [Setting Environment Variables](#setting_environment_variables)).

1. Add the `<INSTALL_DIR>/bin` path to the `PATH` environment variable.

1. If required, install SMT solver(s) to the `<INSTALL_DIR>/tools` directory
   (see `tools/README.md`).

## Setting Environment Variables

### Windows

1. Start the `Control Panel` dialog.
1. Click on the following items:
  - `System and Security`;
  - `System`;
  - `Advanced system settings`.
1. Click on `Environment Variables`.
1. Click on `New...` under `System Variables`.
1. Specify `Variable name` as `MICROTESK_HOME` and `Variable value` as `<INSTALL_DIR>`.
1. Click `OK` in all open windows.
1. Reopen the command prompt window.


### Linux and macOS

Add the command below to `~/.bash_profile` (Linux) or `~/.profile` (macOS):

```
export MICROTESK_HOME=<INSTALL_DIR>
```

Changes will be applied after restarting the terminal.

## Installation Directory Structure

`<INSTALL_DIR>` contains the following subdirectories:

| Directory | Description |
| :-------- | :-----------|
| `arch`    | Microprocessor specifications and test templates |
| `bin`     | Scripts for model compilation and test generation |
| `doc`     | Documentation |
| `etc`     | Configuration files |
| `gen`     | Generated code of microprocessor models |
| `lib`     | JAR files and Ruby scripts |
| `src`     | Source code |
| `tools`   | SMT solvers |

## Running MicroTESK

To compile a microprocessor model from its specification,
run the following command:

* `sh compile.sh <SPECIFICATION>` (Linux and macOS);
* `compile.bat <SPECIFICATION>` (Windows).

For example, the actions below compile the model from the miniMIPS specification:

```
$ cd $MICROTESK_HOME
$ sh bin/compile.sh arch/minimips/model/minimips.nml arch/minimips/model/mmu/minimips.mmu
```

**NOTE:** Models for all demo specifications are included into the MicroTESK distribution package.
There is no need to compile them.

To generate a test program for a given architecture (model) and a given test template,
run the following command:

* `sh generate.sh <ARCH> <TEMPLATE>` (Linux and macOS);
* `generate.bat <ARCH> <TEMPLATE>` (Windows).

For example, the actions below generate a test program for the miniMIPS model compiled in
the previous example and the `euclid.rb` test template:

```
$ cd $MICROTESK_HOME
$ sh bin/generate.sh minimips arch/minimips/templates/euclid.rb
```

The output file name depends on the `--code-file-prefix` and `--code-file-extension` options.

To get more information on command-line options, run MicroTESK with `--help`.
