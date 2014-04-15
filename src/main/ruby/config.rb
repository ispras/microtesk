#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# config.rb, Apr 15, 2014 1:55:38 PM
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
# This is the configuration file for Test Templates subsystem.
# It contains information related to package and folder structure
# and other global settings for the subsystem. 
#

# Folders and Packages 

WD               = Dir.pwd
HOME             = ENV["MICROTESK_HOME"]

JARS             = File.join(HOME, "lib/jars")
TOOLS            = File.join(HOME, "tools")

FORTRESS_JAR     = File.join(JARS, "fortress.jar")
MICROTESK_JAR    = File.join(JARS, "microtesk.jar")
MODELS_JAR       = File.join(JARS, "models.jar")

TEMPLATE         = File.join(HOME, "lib/ruby/template")

MODEL_CLASS_FRMT = "ru.ispras.microtesk.model.%s.Model"

# Debugging features 

$TO_STDOUT = true # Write results to stdout?
$TO_FILES  = true # Write results to files?
