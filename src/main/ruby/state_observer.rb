#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# state_observer.rb, May 7, 2014 12:23:01 PM Andrei Tatarnikov
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

#
# Description:
#
# The StateObserver module provides wrapper methods for the model state observer
# object provided by a model (ru.ispras.microtesk.model.api.state.IModelStateObserver).
# The module is included into test templates so that its methods are available in all
# test templates and can be used in their code. The methods are self-explanatory.   
#

module StateObserver

  def self.state_observer=(value)
    @@j_model_state_observer = value
  end

  def self.control_transfer_status
    @@j_model_state_observer.getControlTransferStatus()
  end

  def self.get_loc_value(string, index = nil)
    if index == nil
      @@j_model_state_observer.accessLocation(string).getValue()
    else
      @@j_model_state_observer.accessLocation(string, index).getValue()
    end
  end

  def self.get_loc_size(string, index = nil)
    if index == nil
      @@j_model_state_observer.accessLocation(string).getBitSize()
    else
      @@j_model_state_observer.accessLocation(string, index).getBitSize()
    end
  end

  def self.set_loc_value(value, string, index = nil)
    if index == nil
      @@j_model_state_observer.accessLocation(string).setValue(value)
    else
      @@j_model_state_observer.accessLocation(string, index).setValue(value)
    end
  end

end
