require_relative "../mtruby"

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

      arg_names = Array.new

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

            names = m.getArgumentNames()
            registered_modes[mode_name] = names

            # - The addr-mode shortcut itself ------------------------------------------------------------------------ #

            p = lambda do |arguments = {}|
              arg = Argument.new
              arg.mode = mode_name

              if arguments.is_a?(Integer) or arguments.is_a?(String)
                if(names.count == 1)
                  arg.values = {names.first => arguments}
                else
                  raise "MTRuby: wrong amount of arguments for addressing mode '" + mode_name + "'"
                end
              else

                if(arguments.count != names.count)
                  raise "MTRuby: wrong amount of arguments for addressing mode '" + mode_name + "'"
                end
                arguments.keys.each do |n|
                  if(!names.include?(n.to_s))
                    raise "MTRuby: wrong argument '" + n + "'for addressing mode '" + mode_name + "'"
                  end
                end
                arg.values = arguments
              end
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
          raise "MTRuby: wrong number of arguments for instruction '" + instruction_name + "'"
        end
        arguments.each_with_index do |arg, ind|
          a = arg
          if(arg.is_a?(Integer) or arg.is_a?(String))
            a = Argument.new
            a.mode = "IMM"
            a.values[registered_modes["IMM"].first] = arg
          end
          match = false
          inst_arguments[ind].getAddressingModes().each do |am|
            if(am.name == a.mode)
              match = true
              break
            end
          end

          if(!match)
            raise "MTRuby: instruction arguments wrong TODO proper error"
          end

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



  end


end