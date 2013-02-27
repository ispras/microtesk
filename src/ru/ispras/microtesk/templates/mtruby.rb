require_relative 'lib/template'
require_relative 'lib/argument'
require_relative 'lib/instruction'
require_relative 'lib/instruction_block'


#require_relative 'lib/storage'
#require_relative 'lib/situation'

Kernel.send(:define_method, :yes, lambda do true end)
Kernel.send(:define_method, :no, lambda do false end)