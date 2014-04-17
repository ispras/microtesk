#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# template_builder.rb, Apr 17, 2014 4:03:42 PM Andrei Tatarnikov
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require 'set'
require_relative 'template'

$Situation_receiver = nil

module TemplateBuilder

  # TODO:
  # make errors display file/line numbers (there's an example of that in the last version)

  # TODO:
  # try to make sense of all this metaprogramming, it's only this convoluted
  # because of the way the DSL is supposed to work - injecting methods makes
  # life and code sort of complicated

  def build_template_class(j_metamodel)

    instructions = j_metamodel.getInstructions()
    instructions.each do |i|

      inst_name = i.getName.to_s

      # Defines methods for test situations (added to the Instruction class)
      define_situation_methods i.getSituations

      # -------------------------------------------------------------------------------------------------------------- #
      # Generating convenient shortcuts for addressing modes                                                           #
      # -------------------------------------------------------------------------------------------------------------- #
      
      inst_arguments  = i.getArguments
      
      arg_names        = Array.new
      args_for_mode    = Hash.new
      registered_modes = Hash.new

      inst_arguments.each_with_index do |arg, index|
        arg_names.push(arg.getName())
        
        modes = arg.getAddressingModes()

        args_for_mode[index] = Array.new

        modes.each do |m|
          mode_name = m.getName()
          
          args_for_mode[index].push mode_name

          # Make sure we didn't add this mode before and then add it
          if(!registered_modes.keys.include?(mode_name))

            names = m.getArgumentNames().to_a
            registered_modes[mode_name] = names

            # - The addr-mode shortcut itself ------------------------------------------------------------------------ #

            p = lambda do |*arguments|
              arg = Argument.new
              arg.mode = mode_name

              #if arguments.is_a?(Integer) or arguments.is_a?(String)
              #  if(names.count == 1)
              #    arg.values = {names.first => arguments}
              #  else
              #    raise "MTRuby: wrong amount of arguments for addressing mode '" + mode_name + "'"
              #  end
              #els
              if arguments.first.is_a?(Integer) or arguments.first.is_a?(String) or arguments.first.is_a?(NoValue)
                if(arguments.count != names.count)
                  raise MTRubyError, caller[0] + "\n" + "MTRuby: wrong number of arguments for addressing mode '" + mode_name +
                                     "', expected: " + names.count.to_s + ", got: " + arguments.count.to_s
                end
                arg.values = {}
                arguments.each_with_index do |n, ind|
                  arg.values[names[ind]] = n
                end
              elsif(arguments.first.is_a?(Hash))
                argumentss = arguments.first
                if(argumentss.count != names.count)
                  raise MTRubyError, caller[0] + "\n" + "MTRuby: wrong number of arguments for addressing mode '" + mode_name +
                                     "', expected: " + names.count.to_s + ", got: " + arguments.count.to_s
                end
                argumentss.keys.each do |n|
                  if(!names.include?(n.to_s))
                    raise MTRubyError, caller[0] + "\n" + "MTRuby: wrong argument '" + n + "'for addressing mode '" + mode_name + "'"
                  end
                end
                arg.values = argumentss
              end
              #arg.values.keys.each {|n| puts n.to_s + " " + arg.values[n].to_s}
              #puts '------'
              return arg
            end

            # -------------------------------------------------------------------------------------------------------- #

            method_name = mode_name.downcase
            while Template.respond_to?(method_name)
              method_name = "mode_" + method_name
            end

            # Define the instruction method in the class
            # This is the core idea behind the DSL
            Template.send(:define_method, method_name, p)

            # -------------------------------------------------------------------------------------------------------- #

          end
        end

      end

      # -------------------------------------------------------------------------------------------------------------- #
      # Generating convenient shortcuts for instructions                                                               #
      # -------------------------------------------------------------------------------------------------------------- #

      p = lambda do  |*arguments, &situations|

        inst = Instruction.new
        inst.name = inst_name

        if(inst_arguments.count != arguments.count)
          raise MTRubyError, caller[0] + "\n" + "MTRuby: wrong number of arguments for instruction '" + inst_name +
              "', expected: " + inst_arguments.count.to_s + ", got: " + arguments.count.to_s
        end
        arguments.each_with_index do |arg, ind|

          #if(arg.is_a?(Symbol))
          #  a = arg
          #
          #  match = false
          #  inst_arguments[ind].getAddressingModes().each do |am|
          #    if(am.name == "#IMM")
          #      match = true
          #      break
          #    end
          #  end
          #
          #  if(!match)
          #    raise MTRubyError, caller[2] + "\n" + "MTRuby: unexpected label as argument '" + ind.to_s + "' for instruction '" + inst_name
          #    #raise "MTRuby: instruction arguments wrong (label) TODO proper error"
          #  end
          #
          #else
          #  a = arg
          #  if(arg.is_a?(Integer) or arg.is_a?(String))
          #    a = Argument.new
          #    a.mode = "#IMM"
          #    a.values[registered_modes["#IMM"].first] = arg
          #  end
          #  match = false
          #  inst_arguments[ind].getAddressingModes().each do |am|
          #    if(am.name == a.mode)
          #      match = true
          #      break
          #    end
          #  end
          #
          #  if(!match)
          #    raise MTRubyError, caller[2] + "\n" + "MTRuby: unexpected argument " + ind.to_s + ": '" + a.mode +
          #                       "' for instruction '" + inst_name
          #  end
          #end

          # no check version
          if arg.is_a? NoValue and !arg.is_immediate
            a = Argument.new
            a.mode = args_for_mode[ind].sample
            #puts registered_modes.to_s
            #puts nvl.mode
            registered_modes[a.mode].each do |mode|
              if arg.aug_value != nil
                a.values[mode] = arg.aug_value
              else
                a.values[mode] = NoValue.new
              end
            end
          else
            a = arg
          end


          # TODO: The situation/composite must come at the end of a block for this to work.
          # Is there a way to apply it if it's before the attributes?
          $Situation_receiver = inst
          if situations != nil
            result = inst.instance_eval &situations
            inst.situation(result)
          end

          inst.arguments[inst_arguments[ind].getName()] = a

        end
        @instruction_receiver.receive(inst)
      end

      # -------------------------------------------------------------------------------------------------------------- #

      # Make sure the method isn't defined already, add a prefix otherwise
      method_name = inst_name.downcase
      while Template.respond_to?(method_name)
        method_name = "op_" + method_name
      end

      # Define the instruction method in the class
      # This is the core idea behind the DSL
      Template.send(:define_method, method_name, p)

      # -------------------------------------------------------------------------------------------------------------- #
      # Finale                                                                                                         #
      # -------------------------------------------------------------------------------------------------------------- #

    end

    p = lambda do registered_modes end

    Template.send(:define_method, :registered_modes, p)
    
  end

  # ------------------------------------------------------------------------------------------------------------- #
  # Defines methods for test situations (added to the Instruction class)                                          #
  # ------------------------------------------------------------------------------------------------------------- #

  def define_situation_methods(meta_situations)
    defined_situations = Set.new
    meta_situations.each do |situation|

      name = situation.getName.to_s
      if defined_situations.add?(name)
        if !Instruction.method_defined?(name)
          p = lambda do Situation.new(name) end
          Instruction.send(:define_method, name, p)
        elsif !Instruction.method_defined?("situation_" + name)
          p = lambda do Situation.new(name) end
          Instruction.send(:define_method, "situation_" + name, p)
        end
      end

    end
  end

end
