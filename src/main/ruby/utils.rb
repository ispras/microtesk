#
# Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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
# MTRubyError is class for desribing errors that can occur 
# in MicroTESK test templates.
#
# TODO: Needs a review.
#
class MTRubyError < StandardError
  def initialize(msg = "You've triggered an MTRuby Error. TODO: avoid these situations and print stack trace")
    super
  end
end

#
# Gets an attribute value from the specified hash. Raises exception if the 
# 'attrs' parameter is not a hash or if it does not contain the specified key. 
#
def get_attribute(attrs, key)
  if !attrs.is_a?(Hash)
    raise MTRubyError, "#{attrs} mush be a Hash!" 
  end

  if !attrs.has_key?(key)
    raise MTRubyError, "The :#{key} attribute is not specified."
  end

  attrs[key]
end
