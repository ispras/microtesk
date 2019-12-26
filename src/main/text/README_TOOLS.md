# Installing Constraint Solver Tools

To generate test data based on constraints, MicroTESK requires external constraint solvers.
The current version supports Z3 (https://github.com/z3prover) and CVC4 (http://cvc4.cs.nyu.edu).
Solver executables should be downloaded and placed to the `<installation dir>/tools` directory.

# Installing Z3

1. Windows users should download Z3 (32 or 64-bit version) from http://z3.codeplex.com/releases and
   unpack the archive to the `<installation dir>/tools/z3/windows` directory.

   **NOTE:** the executable file path is `<windows>/z3/bin/z3.exe`.

2. Linux users should use one of the links below and and unpack the archive to the
   `<installation dir>/tools/z3/unix` directory.

   **NOTE:** the executable file path is `<unix>/z3/bin/z3`.

   | System | Link |
   | :----- | :--- |
   | Debian  x64 | http://z3.codeplex.com/releases/view/101916 |
   | Ubuntu  x86 | http://z3.codeplex.com/releases/view/101913 |
   | Ubuntu  x64 | http://z3.codeplex.com/releases/view/101911 |
   | FreeBSD x64 | http://z3.codeplex.com/releases/view/101907 |

3. OS X users should download Z3 from http://z3.codeplex.com/releases/view/101918 and unpack
   the archive to the `<installation dir>/z3/osx` directory.

   **NOTE:** the executable file path is `<osx>/z3/bin/z3`.

# Installing CVC4

1. Windows users should download the latest version of CVC4 binary from
   http://cvc4.cs.nyu.edu/builds/win32-opt/ and save it to the
   `<installation dir>/tools/cvc4/windows` directory as `cvc4.exe`.

2. Linux users should download the latest version of CVC4 binary from
   http://cvc4.cs.nyu.edu/builds/i386-linux-opt/unstable/ (32-bit version) or
   http://cvc4.cs.nyu.edu/builds/x86_64-linux-opt/unstable/ (64-bit version) and
   save it to the `<installation dir>/tools/cvc4/unix` directory as `cvc4`.

3. OS X users should download the latest version of CVC4 distribution package
   from http://cvc4.cs.nyu.edu/builds/macos/ and install it.
   The CVC4 binary should be copied to `<installation dir>/tools/cvc4/osx` as `cvc4` or
   linked to this file name via a symbolic link.
