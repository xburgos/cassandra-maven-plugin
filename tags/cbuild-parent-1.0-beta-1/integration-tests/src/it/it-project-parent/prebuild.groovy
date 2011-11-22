/**
    This script creates a user writeable RPM database in the
    integration-tests/target directory.  Validation of creation is quickly
    checked along with a quick check of the local repo for the CBUILDS
    plugins.

    @author <a href="mailto:stimpy@codehaus.org">Lee Thompson</a>
*/

String cwd = new File( '.' ).getCanonicalPath();
myLocalRepo = cwd + "/target/it-repo"
rpmDir = cwd + "/target/rpm-db"
myBase = cwd + "/.."
println ( "prebuild.groovy running at " + myBase)

// switched to groovy and gpath since couldn't get @project.version@ to work
File mypom = new File(myBase, "/pom.xml")
def project = new XmlSlurper().parse(mypom);
String ver = project.version.text();
println "version is ${ver}"


class Rpm 
{
  def mkDb ( String rpmDir )
  {
    def rpmcmd = "mkdir -p ${rpmDir}" 
    println "command: ${rpmcmd}"
    Process proc = rpmcmd.execute()
    proc.waitFor()
    println "stdout: ${proc.text}"

    rpmcmd  = "rpm --initdb --dbpath " + rpmDir
    println "command: ${rpmcmd}"
    proc = rpmcmd.execute()
    proc.waitFor()
    println "stdout: ${proc.text}"
  }
}

rpm = new Rpm();
rpm.mkDb( rpmDir );

// The rpm --initdb makes a Packages file
File sourceArchive = new File( myLocalRepo, "../rpm-db/Packages" );

// The CBUILD plugins should have sha1 checksum files
File pluginSHA1 = new File( myLocalRepo, 
  "org/codehaus/mojo/cbuild-parent/${ver}/cbuild-parent-${ver}.pom.sha1" );

return ( sourceArchive.exists() && pluginSHA1.exists() );
