# Demo template time!

require ENV['MTRUBY']

require_relative "./demo_prepost"

class ArmDemo < Template
  def initialize
    super
    @is_executable = yes
  end

  def run

    # exec_debug {
    #   puts "Euclidean algorithm - debug output\n"
    # }

    (1..5).each do |ind|

      i = Random.rand(64)
      j = Random.rand(64)

      # This is what we try to set into the registers - the arguments of the Euclidean algorithm (finding MCD)
      debug {
         puts "Arguments: " + i.to_s + ", " + j.to_s
       }

      # newline
      # text "Setting up arguments"
      # newline

      SUB           blank, setsOff, REG(0), REG(0), register0 # Keep in mind - Ruby thinks a method with no parameters starting in caps is a constant
      ADD_IMMEDIATE blank, setsOff, REG(0), REG(0), IMMEDIATE(0, i)

      SUB           blank, setsOff, REG(1), REG(1), register1
      ADD_IMMEDIATE blank, setsOff, REG(1), REG(1), IMMEDIATE(0, j)

      # This is what is set in the registers at the time of execution
      debug {
        puts
         puts "INPUT: R0: " + get_loc_value("GPR", 0).to_s + ", R1: " + get_loc_value("GPR", 1).to_s
        puts
       }

      label ("cycle" + ind.to_s).to_sym

      CMP blank, REG(0), register1
      SUB greaterThan, setsOff, REG(0), REG(0), register1
      SUB lessThan,    setsOff, REG(1), REG(1), register0

      # This is what REG(0) and REG(1) contain during a single iteration of the cycle
      debug {
         puts
         puts "DEBUG: R0: " + get_loc_value("GPR", 0).to_s + ", R1: " + get_loc_value("GPR", 1).to_s# + ", label code: " + self.send("cycle" + ind.to_s).to_s
          puts
       }

      B   notEqual, ("cycle" + ind.to_s).to_sym

      # Doesn't do anything important, just to snap a nice debug message at the end of each loop
      SUB           blank, setsOff, REG(2), REG(2), register2

      # newline
      debug {
        puts
        puts  "// Simulator heavily implies the result should be " + self.get_loc_value("GPR", 0).to_s
        puts
       }
      # newline

      # This is the result of the algorithm
      # exec_debug {
      #   puts "Result: " + get_loc_value("GPR", 0).to_s
      # }

    end

  end
end