################################################################################
#                                                                              #
#                MicroTESK Test Templates (Ruby Libraries)                     #
#                                                                              #
#                         Configuration File                                   #
#                                                                              #
################################################################################

# Folders and Packages 

WD               = Dir.pwd
HOME             = ENV['MICROTESK_HOME']

JARS             = File.join(HOME, "lib/jars")
TOOLS            = File.join(HOME, "tools")

FORTRESS_JAR     = File.join(JARS, "fortress.jar")
MICROTESK_JAR    = File.join(JARS, "microtesk.jar")
MODELS_JAR       = File.join(JARS, "models.jar")

MTRUBY           = File.join(HOME, "lib/ruby/mtruby")

MODEL_CLASS_FRMT = "ru.ispras.microtesk.model.%s.Model"

# Debugging features 

$TO_STDOUT = true # Write results to stdout?
$TO_FILES  = true # Write results to files?
