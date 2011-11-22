import static org.codehaus.mojo.unix.maven.plugin.ShittyUtil.*
import static org.codehaus.mojo.unix.sysvpkg.PkgchkUtil.*
import org.codehaus.mojo.unix.sysvpkg.PkginfoFile
import org.codehaus.mojo.unix.sysvpkg.PkginfoUtil
import static fj.data.Option.*
import fj.data.Option
import org.joda.time.LocalDateTime

Option<LocalDateTime> ldtNone = Option.none()

boolean success = true

File main = new File((File) basedir, "target/project-sysvpkg-3-1.1-2.pkg")
pkginfo = PkginfoUtil.getPackageInfoForDevice(main);

println "************************************************************************"
println "Hudson Master"

// No <description> inside <project>, no <description> inside <package> => null

success &= assertRelaxed(
        new PkginfoFile( "all", "application", "Hudson Master", "project-sysvpkg-3", "1.1-2"),
        PkginfoUtil.getPackageInfoForDevice(main).some(), packageInfoEqual);

success &= assertSysvPkgEntries(main, [
        directory("/usr", "17777777777", "?", "?", ldtNone),
        directory("/usr/share", "0755", "hudson", "hudson", ldtNone),
        directory("/usr/share/hudson", "0755", "root", "bin", ldtNone),
        directory("/usr/share/hudson/lib", "0755", "hudson", "hudson", ldtNone),
        regularFile("/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, ldtNone),
        directory("/usr/share/hudson/license", "0755", "root", "bin", ldtNone),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, ldtNone),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, ldtNone),
        regularFile("/usr/share/hudson/LICENSE-downstream.txt", "0644", "hudson", "hudson", 15, 1367, ldtNone),
        directory("/usr/share/hudson/server", "0755", "root", "bin", ldtNone),
        regularFile("/usr/share/hudson/server/README.txt", "0644", "hudson", "hudson", 35, 3223, ldtNone),
//        new PkgchkUtil.RegularFile("/usr/share/hudson/server/README.txt", "0644", "root", "bin", 1544, 59072, null),
        installationFile("pkginfo", 154, 0, ldtNone),
        installationFile("postinstall", 36+38, 0, ldtNone),
])

println "************************************************************************"
println "Hudson Slave"

// This has the <description> set on <package> too
File slave = new File((File) basedir, "target/project-sysvpkg-3-slave-1.1-2.pkg")
success &= assertRelaxed(
        new PkginfoFile( "all", "application", "Hudson Slave", "project-sysvpkg-3-slave", "1.1-2").desc(some("Hudson slave node")),
        PkginfoUtil.getPackageInfoForDevice(slave).some(),
        packageInfoEqual);

success &= assertSysvPkgEntries(slave, [
        directory("/usr", "17777777777", "?", "?", ldtNone),
        directory("/usr/share", "0755", "nobody", "nogroup", ldtNone),
        directory("/usr/share/hudson", "0755", "root", "bin", ldtNone),
        directory("/usr/share/hudson/lib", "0755", "nobody", "nogroup", ldtNone),
        regularFile("/usr/share/hudson/lib/slave.jar", "0644", "nobody", "nogroup", 158615, 48565, ldtNone),
        directory("/usr/share/hudson/license", "0755", "root", "bin", ldtNone),
        regularFile("/usr/share/hudson/license/atom-license.txt", "0644", "root", "bin", 49, 4473, ldtNone),
        regularFile("/usr/share/hudson/license/dc-license.txt", "0644", "root", "bin", 1544, 59072, ldtNone),
        regularFile("/usr/share/hudson/LICENSE-downstream.txt", "0644", "nobody", "nogroup", 15, 1367, ldtNone),
        directory("/usr/share/hudson/slave", "0755", "root", "bin", ldtNone),
        regularFile("/usr/share/hudson/slave/README.txt", "0644", "nobody", "nogroup", 34, 3099, ldtNone),
        installationFile("pkginfo", 176, 0, ldtNone),
        installationFile("postinstall", 36+37, 0, ldtNone),
])

return success
