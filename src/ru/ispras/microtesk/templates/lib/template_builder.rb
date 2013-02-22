#                              #
#   MicroTESK Ruby front-end   #
#                              #
#     Template configurator    #
#                              #

# This module provides the functions necessary to fill the Template class
# with functions and constants that mirror the instructions, situations
# and data storages in the given model

# It's pretty much the meat of MTRuby

require_relative "../mtruby"
require_relative "situation_definition"
require_relative "storage_definition"
require_relative "storage_range"

# To add some kind of "unit testing" this will need some refactoring later

module TemplateBuilder

  # -------------------------------------------------- #
  # Instructions                                       #
  # -------------------------------------------------- #

  # Add all instructions to Template class and define the logic of
  # parsing instructions
  def build_instructions (model)
    # Get all instructions from the model
    instructions = model.getInstructions()

    # Make the instructions accessible from the Template class
    # so that the DSL wil be able to call them
    instructions.each do |i|
      instruction_name = i.getName.to_s
      inst_arguments = i.getArguments

      # Instruction method body
      # Probably one of the most important code blocks in the entire front-end
      # Should be moved to its own module eventually
      # Uses a lot of questionable and convoluted metaprogramming

      p = lambda do  |*arguments, &situations|

        # Get the instruction attribute if it's provided (must be a hash)
        # Doesn't do anything useful just yet
        instruction_attribute = nil
        if arguments.first.class <= Hash
          instruction_attribute = arguments.shift
        end
        if instruction_attribute
          puts "Debug: instruction attribute accepted: " +
               instruction_attribute.to_s
        end

        # Fail if the number of arguments is wrong
        if arguments.count != inst_arguments.count
          raise "MTRuby: error: " + caller[0] + ": " + self.class.name +
                ": " + instruction_name + ": Expected " +
                inst_arguments.count.to_s +
                " parameters, given " + arguments.count.to_s
        end

        # Asks the model to create an instruction with a given name
        builder = model.createCall(instruction_name)

        # Fills the instruction with arguments
        arguments.each_with_index do |a, ind|
          # Workaround for the "a == a[0]" gimmick in the DSL
          if a.class <= StorageDefinition
            arg = a[0]
          # Fail if the index is out of bounds
          elsif a.class <= Storage
            if self.send(a.name.downcase.to_sym).capacity <= a.index ||
                a.index < 0
              raise "MTRuby: error: " + caller[1] + ": " + self.class.name +
                    ": " + instruction_name + ": Argument \#" + ind.to_s +
                    ": index '" + a.index.to_s +
                    "' exceeds storage capacity '" +
                    self.send(a.name.downcase.to_sym).capacity.to_s + "'"
            end
            arg = a
          elsif a.class <= StorageRange
            capacity = self.send(a.name.downcase.to_sym).capacity
            if capacity <= a.begin || capacity <= a.end || a.begin < 0 || a.end < 0
              raise "MTRuby: error: " + caller[1] + ": " + self.class.name +
                    ": " + instruction_name + ": Argument \#" + ind.to_s +
                    ": range [" + a.begin.to_s + ', ' + a.end.to_s +
                    "] exceeds storage capacity '" +
                    self.send(a.name.downcase.to_sym).capacity.to_s + "'"
            end
            arg = a
          else
            arg = a
          end

          # In fact this will transmit a "null" value to the model later
          # As will ranges and other methods of setting generating values
          # This is a major TODO

          if !arg || arg.class <= StorageRange || arg.class <= Range
            builder.setArgument(nil, nil)
          else
            # Fail if the argument is not something the instruction expects
            if inst_arguments[ind].getType.to_s == "VALUE"
              if arg.class <= Storage
                raise "MTRuby: error: " + caller[2] + ": " + self.class.name +
                      ": " + instruction_name + ": Argument \#" + ind.to_s +
                      ": immediate value expected, did you mean to use '" +
                      + arg.name + ".value' instead?"
              elsif !(arg.class <= Numeric || arg.class <= String)
                raise "MTRuby: error: " + caller[2] + ": " + self.class.name +
                      ": " + instruction_name + ": Argument \#" + ind.to_s +
                      ": immediate value expected, got '" + arg.class.name + "'"
              end
            elsif inst_arguments[ind].getType.to_s == "ALLOCATION"
              if !(arg.class <= Storage)
                raise "MTRuby: error: " + caller[2] + ": " + self.class.name +
                      ": " + instruction_name + ": Argument \#" + ind.to_s +
                      ": Storage identifier expected, got '" + arg.class.name +
                      "'"
              end
            end

            # Add the arguments to the instruction
            if inst_arguments[ind].getType.to_s == "VALUE"
              builder.setArgument(nil, arg.to_s)
            elsif inst_arguments[ind].getType.to_s == "ALLOCATION"
              builder.setArgument(arg.name, arg.index.to_s)
            end
          end
        end

        # Clear the situation list
        # (workaround for the situations-in-block gimmick)
        @current_situations.clear

        # Fill the situation list with situations from the attached block
        if situations
          situations.call
        end

        # Add the situations to the instruction
        @current_situations.each do |s|
          sbuilder = builder.addSituation(s.name)
          s.arguments.each_pair do |key, value|
            sbuilder.setArgument(key, value.to_s)
          end
        end

        # Call the instruction in the simulator via the model
        instruction_caller = builder.getCall
        instruction_caller.execute

        # DEBUG: This will later write to an array in the class that the
        # template belongs to

        # Prints the instruction text received from the model
        # It's supposed to be formatted according to the rules written in
        # the Sim-nml description
        text = instruction_caller.getText

        #puts text if $TO_STDOUT

        @instruction_list.push text

      end

      # Make sure the method isn't defined already, add a prefix otherwise
      method_name = instruction_name.downcase
      while Template.respond_to?(method_name)
        method_name = "op_" + method_name
      end

      # Define the instruction method in the class
      # This is the core idea behind the DSL
      Template.send(:define_method, instruction_name, p)
    end
  end

    # -------------------------------------------------- #
    # Data storage                                       #
    # -------------------------------------------------- #

  # Add all storage to Template class and make it available to the instructions
  def build_storage (model)
    # Get all available storage units from the model
    storage = model.getAllocationStores()

    # For every storage unit make it accessible through the Template methods
    storage.each do |s|
      st = StorageDefinition.new

      st.name = s.getName.to_s
      st.capacity = s.getCount

      # Has a side-effect of returning StorageDefinition when you use it
      # without an index, but it's dealt with in the instruction call method
      # listed above
      p = lambda do
        return st
      end

      # Make sure the method isn't defined already, add a prefix otherwise
      method_name = st.name.downcase
      while Template.respond_to?(method_name)
        method_name = "st_" + method_name
      end

      # Define the storage "accessor" method in the Template class
      Template.send(:define_method, method_name, p)
    end
  end

  # Add all situations to Template class and make them visible to the instructions
  def build_situations (model)
    # -------------------------------------------------- #
    # Situations                                         #
    # -------------------------------------------------- #

    # Get all situations - a.k.a. part of the testing knowledge - from the model
    situations = model.getSituations()

    # For each situation, make it available to the DSL through the Template class
    situations.each do |s|
      si = SituationDefinition.new

      si.name = s.getName.to_s

      arguments = s.getArguments
      #attributes = s.getAttributes

      arguments.each do |a|
        si.arguments[a.getName.to_s] = a.getType.to_s
      end

      # This is pretty straightforward thanks to using blocks for situations
      # in the DSL and the parameters being a Hash
      p = lambda do |args = {}|
        @current_situations.push si.getSituation(args)
      end

      # Make sure the method isn't defined already, add a prefix otherwise
      method_name = si.name.downcase
      while Template.respond_to?(method_name)
        method_name = "si_" + method_name
      end

      # Define the situation "accessor" method in the Template class
      Template.send(:define_method, method_name, p)
    end
  end

  # Build template class
  def build_template_class (model)
    build_instructions(model)
    build_storage(model)
    build_situations(model)
  end
end