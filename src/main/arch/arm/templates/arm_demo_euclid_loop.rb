#
# Copyright (c) 2014 ISPRAS
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# arm_demo_euclid_loop.rb, Apr 14, 2014 5:16:20 PM
#

require ENV["TEMPLATE"]

require_relative "./demo_prepost"

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described program calculates the greatest common
# divisor of two 5-bit random numbers ([1..63]) by using the Euclidean
# algorithm. This template is an extended version of the arm_demo_euclid.rb
# template. It repeats the code of the test program five times to
# demonstrate the use of loops in test templates.  
#

class ArmDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    debug { puts "Euclidean Algorithm: Debug Output\n" }
      
    (1..5).each do |it|

      i = Random.rand(63) + 1 # [1..63], zero is excluded (no solution) 
      j = Random.rand(63) + 1 # [1..63], zero is excluded (no solution)

      debug { puts "\nInput parameter values (iteration #{it}): #{i}, #{j}\n\n" }

      EOR           blank, setsOff, REG(0), REG(0), register0
      ADD_IMMEDIATE blank, setsOff, REG(0), REG(0), IMMEDIATE(0, i)

      EOR           blank, setsOff, REG(1), REG(1), register1
      ADD_IMMEDIATE blank, setsOff, REG(1), REG(1), IMMEDIATE(0, j)

      debug { puts "\nInitial register values (iteration #{it}): R0 = #{getGPR(0)}, R1 = #{getGPR(1)}\n\n" }

      label ("cycle" + it.to_s).to_sym

      CMP blank, REG(0), register1
      SUB greaterThan, setsOff, REG(0), REG(0), register1
      SUB lessThan,    setsOff, REG(1), REG(1), register0

      debug { puts "\nCurrent register values (iteration #{it}): R0 = #{getGPR(0)}, R1 = #{getGPR(1)}\n\n" } 

      B notEqual, ("cycle" + it.to_s).to_sym

      MOV blank, setsOff, REG(2), register0

      debug {puts "\nResult stored in R2 (iteration #{it}): #{getGPR(2)}\n"}

    end
  end

  def getGPR(index)
    self.get_loc_value("GPR", index).to_s 
  end

end
