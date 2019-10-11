#
# Copyright 2013-2019 ISP RAS (http://www.ispras.ru)
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

require 'java'
require_relative 'template'

module MicroTESK

  HOME     = ENV["MICROTESK_HOME"]
  TEMPLATE = File.join(HOME, "lib/ruby/template")

  def self.main
    template_file = File.expand_path ARGV[0]

    template_classes = prepare_template_classes(template_file)
    template_classes.each do |template_class, template_class_file|
      if template_class_file.eql?(template_file)
        puts "Processing template #{template_class} defined in #{template_class_file}..."
        template = template_class.new
        template.generate
      end
    end
  end

  def self.prepare_template_classes(template_file)

    if File.file?(template_file)
      ENV['TEMPLATE'] = TEMPLATE
      require template_file
    else
      raise "The #{template_file} file does not exist."
    end

    Template::template_classes
  end

end # MicroTESK

MicroTESK.main
