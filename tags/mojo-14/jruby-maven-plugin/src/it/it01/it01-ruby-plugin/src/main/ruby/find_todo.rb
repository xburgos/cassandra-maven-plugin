class FindTodo < Mojo
  goal :todo
  description "Finds the TODO comments for files in the requested directory"

  string :extension, :default=>"java"
  string :delimiter, :default=>"//"
  file :basedir, :required=>true, :expression=>"${basedir}", :description=>"The project's base directory."

  # Does not recurse through the dir structure
  def execute
    todo_regexp = %r"#{$delimiter}\W*TODO\W+(.*?)$"
    info "basedir = #{$basedir}"
    Dir.foreach( $basedir ) { |filename|
      if filename =~ %r"\.#{$extension}" then
        File.open( "#{$basedir}/#{filename}", "r" ) { |file|
          # iterate through the lines of the file
          count = 0
          file.each_line { |line|
            count += 1
            line.scan( todo_regexp ) {
              puts "#{filename}, line #{count}: #{$1}"
            }
          }
        }
      end
    }
  end
end

run_mojo FindTodo
#return FindTodo.new
