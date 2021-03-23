# MicroTESK (Microprocessor TEsting and Specification Kit)

[MicroTESK](http://microtesk.org) is a reconfigurable (retargetable and extendable) model-based test
program generator (TPG) for microprocessors and other programmable devices (such kind of tools are
also called instruction stream generators or ISG). The generator is customized with the help of
instruction-set architecture (ISA) specifications and configuration files, which describe parameters
of the microprocessor subsystems (pipeline, memory and others). The suggested approach eases the
model development and makes it possible to apply the model-based testing in the early design stages
when the microprocessor architecture is frequently modified.

The current version of the tool supports ISA specification (in nML) and manual development of test
program templates (in Ruby). It also implements lightweight methods for automated test program
generation, including random-based and combinatorial techniques. Facilities for describing memory
management units and microprocessor pipelines (microarchitectural networks) are under development,
and so are the methods for advanced test program generation. The framework is applicable to a wide
range of microprocessor architectures including RISC (ARM, MIPS, RISC-V, etc.), CISC (x86, etc.),
and exotic ones (VLIW/EPIC, DSP, GPU, etc.).

## Owner

The owner is [Ivannikov Institute for System Programming of the Russian Academy of Sciences (ISP RAS)](https://ispras.ru/en).
The maintainers are members of Microprocessor Verification Group (MVG).

## Development

Currently, MicroTESK-related source code is stored in the following repositories:

- [MicroTESK for MIPS](https://forge.ispras.ru/projects/microtesk-mips64),
a MicroTESK-based test program generator for MIPS microprocessors.
- [MicroTESK for RISC-V](https://forge.ispras.ru/projects/microtesk-riscv),
a MicroTESK-based test program generator for RISC-V microprocessors.

## Licensing

The MicroTESK package (consisting of MicroTESK core and sample ISA models) is distributed under
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0), which implies the
freedom to use the software for any purpose (to distribute it, to modify it and to distribute
modified versions of the software) under the terms of the license, but requires preservation of
the copyright notice and disclaimer.

## Contacts

For more information, contact microtesk-support@ispras.ru.
