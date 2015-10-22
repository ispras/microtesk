#
# Copyright 2015 ISP RAS (http://www.ispras.ru)
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
# MmuPlugin provides runtime methods to create objects that
# allow describing test templates for the MMU subsystem.
#
module MmuPlugin

  def eq(variable_name, value)
  end

  def dom(variable_name, values)
  end

  def dist(variable_name, *ranges)
  end

  def hit(buffer_name)
  end

  def miss(buffer_name)
  end

  def event(buffer_name, attrs = {})
  end

end
