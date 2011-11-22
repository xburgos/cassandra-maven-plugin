This example illustrates a simple "harvest" use case
It employs the Plugins from org.codehaus.mojo.c-builds to build

IMPORTANT NOTE: c-builds/test-harvest assumes that it is pulling in a Source tarball from c-builds/m2-repo!! 

--------------------------------------------
NOTE: Let's assume that you have checked this example out as ~/c-builds/test-cpkg (See step 1)

1) Clear your current local repo (so you're not fooled)
1.1) rm -rf ~/.m2/repository/com/myco

2) Make sure that you have checked out and built org.codehaus.mojo.c-builds
2.1) cd ~
2.2) svn co https://svn.codehaus.org/mojo/trunk/mojo/mojo-sandbox/c-builds
2.3) cd ~/c-builds
2.4) mvn clean install

3) Make sure that the RPM database is clean of what we are building. This is particularly true if you have been building stuff in et_src_repo
3.1) su to root
3.2) rpm -e com_myco_foo

4) List ~/test-cpkg/m2-repo onto your repository list in settings.xml using a file:// URL.
       <repository>
        <id>test-repo</id>
        <name>Cpkg Test Repository</name>
        <url>file:///home/myname/test-cpkg/m2-repo/</url>
       </repository>

5) Build the parent Plugins for test-cpkg
5.1) cd ~/test-cpkg/parents
5.2) mvn clean install

6) Build and install my_util. NOTE: this will actually install the RPM.
6.1) cd ~/test-cpkg/test-jrpm
6.2) mvn clean install

7) Verify that it worked. NOTE: we are cd-ing to where the RPM has installed the package
7.1) cd /myco/pkgs/linux/intel/foo/1.2.3/bin
7.2) myscript.sh

This should print:
  [cberry@cwb test-harvest]$ /myco/pkgs/linux/intel/foo/1.2.3/bin/myscript.sh
  Hello there. You have called foo/bin/myscript.sh
  Congratulations (harvest worked)
[  
