#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# utils.rb, Apr 30, 2014 3:42:05 PM Andrei Tatarnikov
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

require_relative 'config'

#
# Description:
#
#


class MTRubyError < StandardError
  def initialize(msg = "You've triggered an MTRuby Error. TODO: avoid these situations and print stack trace")
    super
  end
end

#
#
#
#
def get_full_name(file)
  if (Pathname.new file).absolute? then file else File.join(WD, file) end
end

# 
# Defines a method in the target class. If such method is already defined,
# the method type is added to the method name as a prefix to make the name unique.
# If this does not help, an error is reported.  
# 
# Parameters:
#   target_class Target class (Class object)
#   method_name  Method name (String)
#   method_type  Method type (String) 
#   method_body  Body for the method (Proc)
#
def define_method_for(target_class, method_name, method_type, method_body)

  method_name = method_name.downcase
  # puts "Defining method #{target_class}.#{method_name} (#{method_type})..."

  if !target_class.method_defined?(method_name)
    target_class.send(:define_method, method_name, method_body)
  elsif !target_class.method_defined?("#{method_type}_#{method_name}")
    target_class.send(:define_method, "#{method_type}_#{method_name}", method_body)
  else
    puts "Error: Failed to define the #{method_name} method (#{method_type})"
  end

end
