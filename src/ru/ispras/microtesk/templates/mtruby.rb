require_relative 'lib/template'
require_relative 'lib/storage'
require_relative 'lib/situation'

Kernel.send(:define_method, :yes, lambda do true end)
Kernel.send(:define_method, :no, lambda do false end)