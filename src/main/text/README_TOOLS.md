# Installing SMT Solvers

To generate test data based on constraints, MicroTESK uses external SMT solvers.
There are supported [Z3](https://github.com/z3prover/) and [CVC4](https://github.com/CVC4/CVC4/).
Solver executables should be placed to `<INSTALL_DIR>/tools`.

## Specifying paths

If solvers are already installed, MicroTESK can find them by using the `Z3_PATH` and `CVC4_PATH`
environment variables. Each `_PATH` variable specifies the path to the corresponding executable.

## Installing Z3

* Build Z3 as it is described in https://github.com/Z3Prover/z3.
* Move the executable file to the following path (or create a symbolic link):
  - `<INSTALL_DIR>/tools/z3/windows/z3.exe` (Windows);
  - `<INSTALL_DIR>/tools/z3/unix/z3` (Linux);
  - `<INSTALL_DIR>/tools/z3/osx/z3` (macOS).

## Installing CVC4

* Build CVC4 as it is described in https://github.com/CVC4/CVC4/.
* Move the executable file to the following path (or create a symbolic link):
  - `<INSTALL_DIR>/tools/cvc4/windows/cvc4.exe` (Windows);
  - `<INSTALL_DIR>/tools/cvc4/unix/cvc4` (Linux);
  - `<INSTALL_DIR>/tools/cvc4/osx/cvc4` (macOS).
