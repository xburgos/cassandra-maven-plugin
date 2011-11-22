require 'java'

include_class 'org.codehaus.mojo.ruby.RubyMojo'

def info(val); puts(val); end
def error(val); $stderr << val.to_s end
def debug(val); puts(val); end # TODO: add debug later

class MojoError < StandardError
end
class Mojo < RubyMojo
  def self.__add_annt( name )
    module_eval "def self.#{name.to_s}(*vals);; end"
  end
  [:description,:goal,:phase,:execute,:requiresDependencyResolution,:parameter,:string,:date,:file].each{|s|__add_annt(s)}

  def set( name, val )
    # if the val is a string, quote, else, don't
    eval "@#{name.to_s} = '#{val}'"
  end

  def setLog(log); end
  def getLog(); end

  def execute
    raise MojoError.new("You must implement 'execute' method!")
  end
end

def run_mojo(mojo)
#  return mojo.new

  o = mojo.new

#  unless $INPUTS.nil? then
#  $INPUTS.each { |k,v|
#    o.instance_variable_set( "@#{k}".to_sym, v)
#  }
#  end
  ret = o.execute

rescue MojoError
  $stderr << $!
rescue Exception # => detail
#  print detail.backtrace.join("\n").to_s
  $stderr << $!
end
