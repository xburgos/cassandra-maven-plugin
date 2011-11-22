class RubyTest < Mojo
  goal :test
  description "This is a mojo description"
  phase :validate
  requiresDependencyResolution :compile

  string :prop, :expression=>"${someExpression}", :default=>"nothing", :alias=>"prop3", :required=>true

  def execute
    print "The following String was passed to prop: '#{$prop}'"
    return Hash.new
  end
end

run_mojo RubyTest
#return RubyTest.new