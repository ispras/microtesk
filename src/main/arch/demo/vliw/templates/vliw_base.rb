#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

require ENV['TEMPLATE']

#
# Description:
#
# The VliwDemoTemplate test template is a base template to be inherited by
# all other templates for the given architecture. It contains gefinitions 
# of preparators (rules for creating initialization sequences for specific
# resources) and other useful code to be reused.   
#
class VliwBaseTemplate < Template

  #
  # Possible syntax styles to address the VLIW ISA:
  #
  # Style 1:
  #
  # vliw(
  #   (addi r(4), r(0), 5  do situation('overflow') end),
  #   (addi r(5), r(0), 10 do situation('normal') end)
  # )
  #
  # Style 2:
  #
  # vliw(
  #   addi(r(4), r(0), 5)  do situation('overflow') end,
  #   addi(r(5), r(0), 10) do situation('normal') end
  # )
  #
  
  ##############################################################################
  # Initialization Section
  
  def pre
    #
    # Rules for writing preparators of initializing instruction sequences:
    #
    # preparator(:target => '<name>') {
    #   comment 'Initializer for <name>'
    #   vliw(
    #     (lui  target, value(0, 15)),
    #     (addi target, target, value(15, 31))
    #   )
    # }
    #
    # The above code defines an instruction sequence that writes a value
    # to the resource referenced via the <name> addressing mode.
    #
    # Important features:
    #
    # - The ':target' attribute specifies the name of the target addressing
    #   mode.
    # - The 'target' and 'value' methods specify the target addressing mode
    #   with all its arguments set and the value passed to the preparator
    #   respectively. The arguments of the 'value' method specify which part
    #   of the value is used. 
    #
    
    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified general-purpose register (GPR) using the R addressing
    # mode.
    #
    preparator(:target => 'R') {
      comment 'Initializer for R'  
      vliw(
        (lui target, value(16, 31)),
        (addi target, target, value(0, 15))
      )
    }

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified floating-point register (FPR) using the F addressing
    # mode. 
    #
    preparator(:target => 'F') {
      comment 'Initializer for F'
      vliw(
        (lui r(25), value(16, 31)), # GPR[25] holds a temporary value
        (addi r(25), r(25), value(0, 15))
      )
      vliw(
        (mtf r(25), target),
        nop
      )
    }
  end
  
  ##############################################################################
  # Reusable Utility Methods

  #
  # Creates an argument for text printing methods (e.g. trace or text),
  # which refers to the specified GPR register. 
  #
  def gpr(index)
    location('GPR', index)
  end

  #
  # Prints the value stored in the specified general-purpose
  # register (GPR) to the simulator log.
  #
  def trace_gpr(index)
    trace "GPR[%d] = %s", index, gpr(index)
  end

  #
  # Creates an argument for text printing methods (e.g. trace or text),
  # which refers to the specified FPR register. 
  #
  def fpr(index)
    location('FPR', index)
  end

  #
  # Prints the value stored in the specified floating-point
  # register (FPR) to the simulator log.
  #
  def trace_fpr(index)
    trace "FPR[%d] = %s", index, fpr(index)
  end

end
