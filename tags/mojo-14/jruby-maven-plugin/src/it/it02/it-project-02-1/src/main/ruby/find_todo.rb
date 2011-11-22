
# Does not recurse through the dir structure
def execute( basedir )
  todo_regexp = %r"//\W*TODO\W+(.*?)$"
  Dir.foreach( basedir ) { |filename|
    if filename =~ %r"\.java" then
      File.open( "#{basedir}/#{filename}", "r" ) { |file|
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

execute "it02/it-project-02-1/src/main/java/project02"
