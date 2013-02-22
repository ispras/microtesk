MicroTESK Ruby readme
=====================

MicroTESK Ruby (abbreviated as MTRuby) is a reconfigurable template-based generator of tests for microprocessor architectures. It serves as a front-end for the actual MicroTESK reconfigurable microprocessor testing suite written in Java.

At this moment, the only supported interpreter is JRuby due to the extensive Java integration. Ruby syntax corresponds to CRuby 1.9. CRuby support is planned. JRE is also required to use this front-end since both JRuby and MicroTESK require it to execute.

Configuration
---------------------

To configure MTRuby edit the `config.rb` configuration file.

You will have to provide the location of MicroTESK library (most likely called `microtesk.jar`), a Sim-NML description of the target architecture and the files that contain your custom templates for test generation.

MTRuby can automatically detect the templates in the provided files, so there is no need to explicitly state the template classes anywhere.

Template creation
---------------------

To create a template, you must ``require 'mtruby'`` in the template file. Then you should subclass 'Template' and override its 'run' method like this:

    require 'mtruby'
    # You should provide a full path to mtruby.rb or use require_relative with a relative one

    class MyTemplate < Template
        def run
            # Your code here
        end
    end

In the `run` method, using whatever constructions that are available in Ruby, you can write instruction much like you would in a basic assembler. Assuming the target architecture supports instructions `add` and `addi`, an instruction could look like:

    add r, m

or

    addi r, 1000

Depending on the description of the instruction in the Sim-NML file, the method will support either data storage names or immediate numeric values (or, in fact, their String representations as well) as arguments.

If the data storage consists of multiple cells, the target cell can be defined as an argument like this:

    add r[15], m

In fact, definitions like `r` and `r[0]` are interchangeable, the first cell (#0) is assumed by default.

If you want to request MicroTESK to generate some of the arguments of the instruction so that it would create a certain exceptions, or situations, in the processor, you can provide the exception names in a Ruby block given to the instruction. Make sure to use parentheses if you intend to use curly braces due to the way Ruby handles their precedence.

    add r[10], nil          do overflow end
    addi(r[15], nil)        { overflow }

If the exception can accept arguments, you can provide them as a Hash (the exceptions, in fact, work like methods in MTRuby):

    add nil, nil            do overflow(:op1 => 1000, :op2 => 2000) end

If you want to write multiple templates using the same wrapper code (pre- and post-conditions), you can define a wrapper Template class like this:

    # mywrapper.rb

    require 'mtruby'

    class MyWrapper < Template
        def initialize
            super
            @is_executable = no # So that the wrapper won't be parsed as a template
        end

        def pre
            # Pre-condition instructions
        end

        def post
            # Post-condition instructions
        end
    end

And then subclass your template class from the wrapper class:

    require 'mtruby'
    require 'mywrapper'

    class MyTemplate < MyWrapper
        def initialize
            super
            @is_executable = yes # Because @is_executable is inherited by default
        end

        def run
            # Your code here
        end
    end

And that's it on the basics of MTRuby. The rest of the functionality is being developed.

Running templates
---------------------

To parse the templates, mention the files that contain them in the `config.rb` configuration file and start `parse_templates.rb` with JRuby

    > jruby parse_templates

By default MTRuby will output results to the directories listed in `config.rb`. The default filename will be the name of the template class (e.g. `MyTemplate.asm`), but you can override it by setting `@target_file` in the `initialize` function of the template:

    require 'mtruby'

    class MyTemplate
        def initialize
            super
            @target_file = "test_situation_alpha.mips"
        end

        def run
            # Your code here
        end
    end

In the configuration files you can select whether you need file output, debugging output to the console or both by setting `$TO_FILES` and `$TO_STDOUT` to `true` or `false` as appropriate.

Features in development
---------------------

* Blocks that distribute modifications of the same template to files with different suffixes
* Restrictions on generated arguments (e.g. `(1..5)` or `r[1..5]` instead of `nil`)
* String-based argument generation like `"0b1****0101"`
* CRuby 1.9 (common Ruby interpreter) support
* Hot-swapping configuration files
* Executing a single template by using the interpreter on it (if technically feasible)
* More cool stuff in both MTRuby and the MicroTESK library


---------------------
*Institute for System Programming of Russian Academy of Sciences, 2012*