require_relative "../mtruby"

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

    registered_modes = Hash.new


    instructions.each do |i|

      instruction_name = i.getName.to_s
      inst_arguments = i.getArguments
      inst_situations = i.getSituations

      arg_names = Array.new

      inst_situations.each do |s|
        s1 = s.to_s
        if !Instruction.respond_to?(s1)
          p = lambda do
            $Situation_receiver.situation s1
          end
          Instruction.send(:define_method, s1, p)
        elsif !Instruction.respond_to?("situation_" + s1)
          p = lambda do
            $Situation_receiver.situation s1
          end
          Instruction.send(:define_method, "situation_" + s1, p)
        end
      end

      # -------------------------------------------------------------------------------------------------------------- #
      # Generating convenient shortcuts for addressing modes                                                           #
      # -------------------------------------------------------------------------------------------------------------- #

      inst_arguments.each do |arg|
        arg_names.push(arg.getName())

        modes = arg.getAddressingModes()

        modes.each do |m|
          mode_name = m.getName()

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
              if arguments.first.is_a?(Integer) or arguments.first.is_a?(String)
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
        inst.name = instruction_name

        if(inst_arguments.count != arguments.count)
          raise MTRubyError, caller[0] + "\n" + "MTRuby: wrong number of arguments for instruction '" + instruction_name +
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
          #    raise MTRubyError, caller[2] + "\n" + "MTRuby: unexpected label as argument '" + ind.to_s + "' for instruction '" + instruction_name
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
          #                       "' for instruction '" + instruction_name
          #  end
          #end

          # no check version
          a = arg

          $Situation_receiver = inst
          inst.instance_eval &situations

          inst.arguments[inst_arguments[ind].getName()] = a

        end
        @instruction_receiver.receive(inst)
      end

      # -------------------------------------------------------------------------------------------------------------- #

      # Make sure the method isn't defined already, add a prefix otherwise
      method_name = instruction_name.downcase
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


end