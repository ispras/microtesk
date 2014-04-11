require_relative 'lib/template'
require_relative 'lib/constructs/argument'
require_relative 'lib/constructs/instruction'
require_relative 'lib/constructs/instruction_block'

Kernel.send(:define_method, :yes, lambda do true end)
Kernel.send(:define_method, :no, lambda do false end)
