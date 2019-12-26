# MicroTESK Builds

## 2019/12/26 - MicroTESK 2.5

### 2019/12/26 - MicroTESK 2.5.0 beta

 * Introduced new internal representation, so-called *MIR* (*Middle-level* [or *MicroTESK*] *IR*)
   - Redesigned the constraint generator (for mark-based situations)
   - Redesigned the symbolic executor (for binary code analysis)
 * Unified the directives (alignment, data definition, and labels) for `.text` and `.data` sections:
   - Enabled a possibility to define data in `.text`
   - Implemented new directives: `.balign`, `.p2align`, and `.option`
   - Refactored the code/data allocation logic
 * Implemented a simple instruction-level coverage tracker (experimental)
   - Introduced a new option `--coverage-log` for tracking test coverage
   - Enabled a possibility to generate test coverage reports in Aspectrace format
     (see https://forge.ispras.ru/projects/aspectrace)
 * Used the `QEMU4V 0.3.4` simulator for running tests

## 2016/10/28 - MicroTESK 2.4

### 2019/10/25 - MicroTESK 2.4.46 beta

 * Updated the `TestBase` library
 * Used the `QEMU4V 0.3.3` simulator for running tests

### 2019/01/29 - MicroTESK 2.4.45 beta

 * Supported block-level register allocation constraints
 * Updated the register allocation engine
 * Changed mmuSL interpretation: structures' fields are listed from upper to lower bits

### 2018/11/16 - MicroTESK 2.4.44 beta

 * Supported separate-file sections: it is useful for boot generation
 * Supported the `decode` attribute of nML primitives
 * Enabled bit selection for non-immediate arguments in the decoder generator

### 2018/08/20 - MicroTESK 2.4.43 beta

 * Test Templates: the `nitems` generator was established
 * Test Templates: blocks w/o attributes are now allowed (`block {}`)
 * Code generation facilities moved to the Castle library

### 2018/07/24 - MicroTESK 2.4.42 beta

 * Improved template facilities: support for addressing modes (registers) in printing functions
    (such as `trace`)
 * Improved template facilities: support for the `_SLL` operation
 * Improved register allocation: support for the `retain` attribute

### 2018/07/19 - MicroTESK 2.4.41 beta

 * Support for floating-point types in data sections
 * Support for formatted printing of values in data sections
 * Support for register reservation
 * Support for basic operations (`_AND`, `_OR`, `_XOR`, `_ADD`, `_SUB`, `_PLUS`, `_MINUS`, `_NOT`)
    w/ dynamic immediate values in test templates
 * Improved register allocation strategies
 * New permutator `exhaustive` aliased as `full`
 * Support for i386 in x86 specifications
 * Support for the NASM and GNU assembler in x86 specifications and test templates
 * miniMIPS test programs are compiled w/ GCC and simulated in QEMU
 * x86 test programs are compiled w/ the GNU and NASM assemblers and simulated in QEMU
 * New option `--model-name` to allow building several ISA models from the same specifications
 * New option `--base-template-path` for automated template generation

### 2018/05/04 - MicroTESK 2.4.40 beta

 - Autogen: support for automated generation of test templates
 * Templates: methods `rev_id` and `is_rev` to get information on revisions
   supported by the model
 * Templates: support for global labels (the `global_label` method)
 * Templates: support for numeric labels
 * Templates: support for weak symbols (the `weak` method)
 * Templates: new method `set_default_mode_allocator`
 * Templates: runtime methods to trace the state of registers and memory

### 2018/02/19 - MicroTESK 2.4.39 beta

 * Test data iterator functionality was implemented
 * New demo template `testdata.rb` (miniMIPS) demonstrating test data iterator
 * Refactoring in generation logic

### 2018/02/05 - MicroTESK 2.4.38 beta

 * Enhancements in the branch engine
 * Refactoring in the memory engine
 * New demo specifications and templates: Vmem ISA aimed to demonstrate
   the facilities of the memory engine
 * Changes in the nML language translator: special constructs to specify
   label-based instruction operands
 * Improvements in the simulator: correct handling of jump instructions
 * Improvements in the symbolic executor: correct SMT-formulae

### 2017/09/18 - MicroTESK 2.4.37 beta

 * Bug fix in constraint-based test data generation

### 2017/09/15 - MicroTESK 2.4.36 beta

 * Enhancements in the branch engine

### 2017/09/08 - MicroTESK 2.4.35 beta

 * Support for excluded elements in revision configuration files

### 2017/09/01 - MicroTESK 2.4.34 beta

 * Bug fixes and code improvements

### 2017/08/25 - MicroTESK 2.4.33 beta

 * Support for configuration files describing relations between revisions
 * Support for operation templates in nML
 * Supported new nML operators: is_type, type_of, and size_of
 * Support for bitfields in MMU constraints (test templates)

### 2017/08/18 - MicroTESK 2.4.32 beta

 * Support for revisions in nML and MMU specifications
 * Integration of generation engines (branch and memory)
 * Test template demonstrating how to mix ALU, BPU and MMU constraints in a single sequence (miniMIPS)

### 2017/08/03 - MicroTESK 2.4.31 beta

 * Test template demonstrating how to mix BPU and ALU constraints (miniMIPS)
 * Test template demonstrating how to mix MMU and ALU constraints (miniMIPS)

### 2017/08/01 - MicroTESK 2.4.30 beta

 * Example templates demonstrating how to create tests for BPU (miniMIPS)

### 2017/07/14 - MicroTESK 2.4.29 beta

 * Bug fixes and code improvements

### 2017/07/07 - MicroTESK 2.4.28 beta

 * Support for generation of GCC linker scripts for test programs

### 2017/06/26 - MicroTESK 2.4.27 beta

 * Improved support for sections in test templates
   (constructs `section {}`, `section_data {}`, and `section_text {}`)

### 2017/06/09 - MicroTESK 2.4.26 beta

 * Bug fixes and code improvements

### 2017/06/05 - MicroTESK 2.4.25 beta

 * Support for sections (code and data can be placed into regions defined by
   the `.section` directive)
 * Changes in generator (common mechanism of test situation processing for
   all generation engines)

### 2017/05/26 - MicroTESK 2.4.24 beta

 * Bug fixes and code improvements

### 2017/05/19 - MicroTESK 2.4.23 beta

 * Changes in generator: external code w/ fixed origin is generated and allocated first

### 2017/05/12 - MicroTESK 2.4.22 beta

 * Bug fixes and code improvements

### 2017/05/05 - MicroTESK 2.4.21 beta

 * Bug fixes and code improvements

### 2017/04/28 - MicroTESK 2.4.20 beta

 * Bug fixes and code improvements

### 2017/04/21 - MicroTESK 2.4.19 beta

 * Option `jruby-thread-pool-max` to limit the number of threads JRuby can create

### 2017/04/14 - MicroTESK 2.4.18 beta

 * Bug fixes and code improvements

### 2017/04/07 - MicroTESK 2.4.17 beta

 * Demo specification and test templates for x86

### 2017/03/24 - MicroTESK 2.4.16 beta

 * Bug fixes and code improvements

### 2017/03/17 - MicroTESK 2.4.15 beta

 * Accurate state modeling during pre-simulation

### 2017/03/06 - MicroTESK 2.4.14 beta

 * Support for assertions in nML was implemented
 * New option `--debug-print` was introduced

### 2017/02/17 - MicroTESK 2.4.13 beta

 * Bug fixes and code improvements

### 2017/02/10 - MicroTESK 2.4.12 beta

 * Support for jumps over instruction blocks (unstable)
 * Support for generation of tests running on multiple processing elements (unstable)
 * Bug fixes in the memory engine

### 2017/02/03 - MicroTESK 2.4.11 beta

 * Performance optimization of the memory engine

### 2017/01/27 - MicroTESK 2.4.10 beta

 * The SAT4J solver has been adapted to be used in the memory engine
 * The `.text` directive is automatically inserted into code

### 2017/01/20 - MicroTESK 2.4.9 beta

 * Refactoring of simulation logic

### 2017/01/16 - MicroTESK 2.4.8 beta

 * Refactoring of simulation logic

### 2016/12/30 - MicroTESK 2.4.7 beta

 * Refactoring of generation and simulation logic

### 2016/12/21 - MicroTESK 2.4.6 beta

 * Improved disassembler
 * Improved floating-point support

### 2016/12/09 - MicroTESK 2.4.5 beta

 * Improved floating-point support

### 2016/12/05 - MicroTESK 2.4.4 beta

 * Basic implementation of disassembler
 * Basic support for 16-bit floating point data types

### 2016/11/18 - MicroTESK 2.4.3 beta

 * Support for binary masks in preparators (test templates)
 * The `internal` modifier for nML operation was established

### 2016/11/11 - MicroTESK 2.4.2 beta

 * Support for structures (the `struct` keyword) in nML
 * Improved timing statistics

### 2016/11/03 - MicroTESK 2.4.1 beta

 * Performance optimization: variables are not reset
 * Simulation on multiple instances is debugged
 * Improvements in exception handling facilities

### 2016/10/28 - MicroTESK 2.4.0 alpha

 * Basic facilities for multicore generation (unstable, untested)
 * nML: the `init` attribute was made public
 * Refactoring in the ISA simulator

## 2015/09/04 - MicroTESK 2.3

### 2016/10/21 - MicroTESK 2.3.52 beta

 * Refactoring in the ISA simulator

### 2016/09/30 - MicroTESK 2.3.51 beta

 * Refactoring: meta model as a separate entity

### 2016/09/16 - MicroTESK 2.3.50 beta

 * Bug fix in `ExprFactory`

### 2016/09/09 - MicroTESK 2.3.49 beta

 * Bug fixes and code improvements

### 2016/09/02 - MicroTESK 2.3.48 beta

 * Bug fixes and code improvements

### 2016/08/26 - MicroTESK 2.3.47 beta

 * Documentation on Ruby code of test templates included into distribution
 * Possibility to override configuration options from test templates by using the
   `set_option_value` function
 * Updated configuration options (see MicroTESK help)
 * New configuration option `--reserve-explicit` that specifies whether explicitly specified
   registers must be reserved
 * New test template `random_sequence.rb` that combines sequences based on distributions

### 2016/08/12 - MicroTESK 2.3.46 beta

 * Improved functionality of the `prologue` and `epilogue` constructs

### 2016/08/06 - MicroTESK 2.3.45 beta

 * ANTLR was updated to version 3.5.2

### 2016/08/01 - MicroTESK 2.3.44 beta

 * New documentation file on simulator's memory settings
 * JRuby was updated to version 1.7.25

### 2016/07/22 - MicroTESK 2.3.43 beta

 * Test Templates: support for global code sections, global labels and control transfers between
   global sections
 * New test template `external_labels.rb` for miniMIPS

### 2016/07/15 - MicroTESK 2.3.42 beta

 * Test Templates: new rearranger strategy `sample`
 * Test Templates: improvements in the `prepare` function
 * Test Templates: the `rand` function can be parameterized w/ a distribution
 * Test Generator: new engine `trivial`

### 2016/07/08 - MicroTESK 2.3.41 beta

 * Improvements in the nML translator

### 2016/07/01 - MicroTESK 2.3.40 beta

 * New option `--default-test-data` that enables generation of default test data for instructions
   w/o explicitly specified test situations
 * External access to `mode` and `op` arguments (nML)

### 2016/06/24 - MicroTESK 2.3.39 beta

 * New option `--time-statistics` for printing time statistics
 * New option `--no-simulation` for disabling simulation

### 2016/06/17 - MicroTESK 2.3.38 beta

 * Refactoring of the nML translator

### 2016/06/03 - MicroTESK 2.3.37 beta

 * Bug fixes and code improvements

### 2016/05/27 - MicroTESK 2.3.36 beta

 * Refactoring of the nML translator and the ISA model

### 2016/05/20 - MicroTESK 2.3.35 beta

 * Registers used for streams are excluded from random selection
 * Bug fixes and code improvements

### 2016/05/13 - MicroTESK 2.3.34 beta

 * Bug fixes and code improvements

### 2016/05/06 - MicroTESK 2.3.33 beta

 * Bug fixes and code improvements

### 2016/04/22 - MicroTESK 2.3.32 beta

 * Bug fix in coverage extractor for nML

### 2016/04/16 - MicroTESK 2.3.31 beta

 * Possibility to specify exclusions for random register allocation was implemented
 * Possibility to free reserved registers was implemented
 * Bug fix in `sign_extend` and `zero_extend` nML functions

### 2016/04/11 - MicroTESK 2.3.30 beta

 * Fixed a bug related to simulation of aligned instruction calls

### 2016/04/08 - MicroTESK 2.3.29 beta

 * Support for handling control transfers to invalid addresses was implemented

### 2016/04/01 - MicroTESK 2.3.28 beta

 * Possibility to specify strategy for register allocation was implemented

### 2016/03/18 - MicroTESK 2.3.27 beta

 * The `atomic` and `iterate` block types were established
 * The `block` structure now can be used only to combine sequences returned by nested blocks
 * The `rearranger` parameter was supported by blocks

### 2016/03/11 - MicroTESK 2.3.26 beta

 * Bug fixes and code improvements

### 2016/03/04 - MicroTESK 2.3.25 beta

 * Bug fixes and code improvements

### 2016/02/26 - MicroTESK 2.3.24 beta

 * Support for test case level data sections
 * Support for relative origins (`.org :delta => 0xXXXX`)

### 2016/02/20 - MicroTESK 2.3.23 beta

 * Bug fixes and code improvements

### 2016/02/12 - MicroTESK 2.3.22 beta

 * Support for assigning default test situations to instructions and instruction groups
 * Support for preparator overriding
 * New instruction permulation methods (`permutator` and `obfuscator`)
 * Data are now printed in the end of a test program

### 2016/02/05 - MicroTESK 2.3.21 beta

 * Branch Engine: parameter `limit` was renamed to `branch_exec_limit`
 * Branch Engine: new parameter `trace_count_limit` that bounds the number of execution traces
   was introduced
 * Test Templates: support for named preparators was implemented
 * Test Templates: `atomic` was renamed to `sequence`
 * nML Translator: general improvements were made

### 2016/01/29 - MicroTESK 2.3.20 beta

 * Support for randomizing preparators (random selection between several preparator variants)
 * Bug fixes in the branch engine

### 2016/01/22 - MicroTESK 2.3.19 beta

 * Support for instruction permutations in test template blocks was implemented

### 2016/01/18 - MicroTESK 2.3.18 beta

 * Bug fixes and code improvements

### 2016/01/11 - MicroTESK 2.3.17 beta

 * Bug fixes and code improvements

### 2015/12/25 - MicroTESK 2.3.16 beta

 * Possibility to print buffer state to simulation log was implemented
 * ISA simulator API was improved

### 2015/12/17 - MicroTESK 2.3.15 beta

 * Support for explicit invocation of preparators in test templates
 * Support for MMU-related constraints in test situations

### 2015/12/11 - MicroTESK 2.3.14 beta

 * Support for block-level prologue and epilogue was implemented

### 2015/12/04 - MicroTESK 2.3.13 beta

 * Bug fixes and code improvements

### 2015/11/27 - MicroTESK 2.3.12 beta

 * Possibility of initialize address fields depending on the operation that
   performs memory access was implemented in mmuSL
 * Cases of incorrect use of test template blocks are now reported to the user

### 2015/11/20 - MicroTESK 2.3.11 beta

 * Test template blocks were made ``passive`` (processed by the generation engine only on request)
 * Support for reuse (composition, repetition) of test template blocks was implemented
 * Support for new floating-point functions `is_nan` and `is_signaling_nan` in nML was implemented

### 2015/11/13 - MicroTESK 2.3.10 beta

 * Improvements in Tarmac logging: support for memory accesses and register writes were implemented
 * Handling of let-expressions (MMU) was improved
 * Ruby engine was switched to JRuby 1.7.22

### 2015/11/06 - MicroTESK 2.3.9 beta

 * Support for functions in mmuSL
 * Support for expressions based on extern variables in let-constructs in mmuSL
 * Support for `if-then-else` expressions in let-constructs in mmuSL
 * JRuby was updated to version 9.0.3.0

### 2015/10/30 - MicroTESK 2.3.8 beta

 * Bug fixes and code improvements

### 2015/10/23 - MicroTESK 2.3.7 beta

 * Possibility to describe memory-related test situations in test templates was implemented
 * Support for bit mask operations in MMU specifications was implemented

### 2015/10/16 - MicroTESK 2.3.6 beta

 * Possibility to use values of registers defined in ISA specifications as parameters while
   generating tests for MMU (the `extern` construct)
 * Possibility to map buffers to virtual memory was implemented (the `memory buffer` construct)
 * Possibility to map buffers to registers defined in ISA specifications was implemented
   (the `register buffer` construct)
 * Support for shift operations in mmuSL was implemented

### 2015/10/09 - MicroTESK 2.3.5 beta

 * Support for floating-point calculations was improved (integration w/ JSoftFloat and additional
   nML functions for type conversion)
 * Support for checking addresses using memory region settings was implemented

### 2015/10/02 - MicroTESK 2.3.4 beta

 * Some bug fixes in the MMU simulator were made
 * Some optimizations of the MMU test engine were implemented

### 2015/09/25 - MicroTESK 2.3.3 beta

- Bug fixes and code improvements

### 2015/09/18 - MicroTESK 2.3.2 beta

 * MMU-simulator was implemented

### 2015/09/11 - MicroTESK 2.3.1 beta

 * Support for MMU-directed test program generation: improvements

### 2015/09/04 - MicroTESK 2.3.0 alpha

 * Basic support for MMU-directed test program generation was implemented

## 2015/03/24 - MicroTESK 2.2

### 2015/08/28 - MicroTESK 2.2.17 beta

 * Bug fixes and code improvements

### 2015/08/21 - MicroTESK 2.2.16 beta

 * Bug fixes and code improvements

### 2015/08/14 - MicroTESK 2.2.15 beta

 * Bug fixes and code improvements

### 2015/08/07 - MicroTESK 2.2.14 beta

 * Support for self-checking test program generation was implemented

### 2015/07/30 - MicroTESK 2.2.13 beta

 * Bug fixes and code improvements

### 2015/07/24 - MicroTESK 2.2.12 beta

 * Data generated for test programs (defined in `data` blocks) now can be placed into a separate file

### 2015/07/17 - MicroTESK 2.2.11 beta

 * Bug fixes and code improvements

### 2015/07/15 - MicroTESK 2.2.10 beta

 * Possibility to specify test case level prologue and epilogue in test templates was implemented
 * Possibility to specify calls of pseudo instructions in test templates was implemented
 * The functionality of `align` and `org` methods (directives) was improved
 * CVC4 is now used as the default SMT solver
 * `Z3_PATH` and `CVC4_PATH` environment variables can now be used to specify path to corresponding
  SMT solver executable

### 2015/07/10 - MicroTESK 2.2.9 beta

 * Support for `.org` and `.align` directives in test templates
 * Better handling of the `undefined` and `unpredicted` situations

### 2015/07/06 - MicroTESK 2.2.8 beta

 * Support for preprocessor directives
 * Support for exception handling
 * Support for tracking instruction addresses during simulation
   (including tracking addresses in indirect branches)

### 2015/06/26 - MicroTESK 2.2.7 beta

 * Data file generation feature was implemented

### 2015/06/19 - MicroTESK 2.2.6 beta

 * Support for large memory addresses (e.g 48 and 64 bits) was improved
 * Support for overloaded preparators was implemented
 * Support for operation and addressing mode groups in test templates was implemented
 * Support for user-defined test data generator extensions (in Java)
 * Improved console output format (use the --verbose option)

### 2015/06/11 - MicroTESK 2.2.5 beta

 * Basic support for Tarmac was implemented (logging is enabled w/ the `--tarmac-log` option)
 * Support for large memory addresses (e.g 48 and 64 bits) was implemented

### 2015/06/05 - MicroTESK 2.2.4 beta

 * Possibility to select test situations applied to instruction on random manner was implemented
 * Randomization of operands of instructions w/ unspecified situations was supported
 * Architecture-specific settings (set up w/ `--arch-dirs` option) were established
 * Support for random register allocation was implemented
 * Options `--comments-enabled` and `--comments-enabled` for enabling/disabling printing comments to
   test programs were added
 * Option `--solver-debug` for enabling debug output of SMT solvers was added
 * Functionality of the `include` directive was improved
 * Format of generated test programs was enhanced
 * If a preparator for an addressing mode is not specified, test generation is aborted

### 2015/05/29 - MicroTESK 2.2.3 beta

 * Test data is generated taking into account data generated for other instructions
 * Statistics on test program size and test generator performance is collected
 * Test programs are split into parts if they exceed the specified limits on instruction number

### 2015/05/22 - MicroTESK 2.2.2 alpha

 * Simple mechanism for detecting loops in test templates was implemented:
   if the number of branch executions exceeds the specified limit, test generation is halted
 * New attribute `init` was supported for `mode` and `op` nML primitives:
   it specifies initialization actions to be performed each time before code of other standard
   attributes like `syntax` and `image` is executed
 * The repetition operator was implemented in nML:
   `{N}X` - concatenates the specified location `X` w/ itself `N` times
 * Branches of logic in nML specifications which raise exceptions are automatically marked as named
   test situations
 * Syntax of the `reg`, `mem` and `var` nML constructs was extended to support array-based aliases
 * Functionality of `format` and `trace` nML functions was improved:
   now they support conditional string-based expressions as arguments
 * Default values for some command-line options are now stored in a configuration file

### 2015/04/23 - MicroTESK 2.2.1 alpha

 * Support for the CVC4 constraint solver

### 2015/03/24 - MicroTESK 2.2.0 alpha

 * Support for automated extraction of coverage information that allows building constraints for
   named instruction execution paths
 * Possibility to process test templates in a stream manner (block by block) was implemented,
   which allows processing larger test templates
 * The `trace` method (test templates) now accepts objects created by the `rand` method

## 2014/10/18 - MicroTESK 2.1

### 2015/03/05 - MicroTESK 2.1.5 beta

 * The `random_seed` setting was added to test templates
 * Biased random generation was supported
 * The miniMIPS specification was improved
 * More test templates for miniMIPS were added

### 2015/02/19 - MicroTESK 2.1.4 beta

 * The logic of test sequence building and processing has been improved
 * The format of generated test programs has been improved

### 2015/02/17 - MicroTESK 2.1.3 beta

 * Bug fixes and code improvements

### 2014/12/30 - MicroTESK 2.1.2 beta

 * Facilities to describe the data segment in test templates were implemented
 * Function `trace` (nML) was supported
 * Possibility to create instances of modes and ops in nML code was implemented
 * Aliases for memory locations (`mem`, `reg`, `var`) were supported
 * Specification of the miniMIPS ISA was added to examples

### 2014/10/31 - MicroTESK 2.1.1 beta

 * Issue related to processing test templates under Windows was fixed

### 2014/10/18 - MicroTESK 2.1.0 beta

 * Support for VLIW was implemented
 * Support for floating-point types was implemented
 * Ability to describe initialization code (the `preparator` construct) was implemented
 * New test data generators were implemented
 * The test templates library was improved (including new text printing facilities and ability to
   specify unknown immediate values)
 * New examples of test templates demonstrating features of MicroTESK were added
 * Ability to use labels rather than addresses in the generated code was implemented

## 2011 - MicroTESK 2.0

### 2014/04/18 - MicroTESK 2.0.2 beta

 * An ability to manually specify initialization code was implemented
 * The test templates library was improved
 * Test template examples for ARM were updated
 * Several bugs related to MicroTESK installation were fixed
 * Default test situations `Random` and `Zero` were provided

### 2014/03/20 - MicroTESK 2.0.1 beta

 * Initial build of MicroTESK 2.0 based on nML and Ruby

## 2008 - MicroTESK 1.0

### 2009/08/20 - MicroTESK 1.0.0 beta

 * Demo build of MicroTESK w/ predefined ISA specifications and situations (MIPS)

## 2007 - TestFusion4M

 * Closed test program generator for a specific MIPS64-compatible microprocessor
