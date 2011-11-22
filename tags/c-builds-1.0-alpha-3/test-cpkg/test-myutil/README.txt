This example illustrates a simple three level dependency in C, using RPMs and autotools
It employs the Plugins from org.codehaus.mojo.c-builds to build

IMPORTANT NOTE: c-builds/test-cpkg currently assumes that it is pulling in a Source tarball from c-builds/m2-repo. This will eventually change to where the c-builds/test-cpkg POM is building the autotools project in /src -- omitting the src tarball altogether. But for the short term, we can rely on the src tarball as an interim step.

--------------------------------------------
Until we have the Maven lifecycle changes complete, we will follow the steps listed below.

NOTE: Steps 1-5 are essentially setup steps for 6-8. They will be done either once or seldomly. We are essentially focused on what is happening in Step 8.

When the Maven lifecycle changes are complete, we should be able to omit steps 6 and 7, which will happen automagically when building test-myutil in Step 8.

--------------------------------------------
NOTE: Let's assume that you have checked this example out as ~/c-builds/test-cpkg (See step 1)

1) Clear your current local repo (so you're not fooled)
1.1) rm -rf ~/.m2/repository/org/libexpat/expat
2) Make sure that you have checked out and built org.codehaus.mojo.c-builds
2.1) cd ~
2.2) svn co https://svn.codehaus.org/mojo/trunk/mojo/mojo-sandbox/c-builds
2.3) cd ~/c-builds
2.4) mvn clean install

3) Make sure that the RPM database is clean of what we are building. This is particularly true if you have been building stuff in et_src_repo
3.1) su to root
3.2) rpm -e org_libexpat_expat
3.3) rpm -e net_sourceforge_log4c_log4c
3.4) rpm -e com_myco_myutil

4) List ~/test-cpkg/m2-repo onto your repository list in settings.xml using a file:// URL.
       <repository>
        <id>test-repo</id>
        <name>Cpkg Test Repository</name>
        <url>file:///home/myname/test-cpkg/m2-repo/</url>
       </repository>

5) Build the parent Plugins for test-cpkg
5.1) cd ~/test-cpkg/parents
5.2) mvn clean install

6) Build and install expat. NOTE: this will actually install the RPM.
6.1) cd ~/test-cpkg/org/libexpat/expat
6.2) mvn clean install
 
7) Build and install log4c. NOTE: this will actually install the RPM.
7.1) cd ~/test-cpkg/net/sourceforge/log4c/log4c
7.2) mvn clean install
 
8) Build and install my_util. NOTE: this will actually install the RPM.
8.1) cd ~/test-cpkg/test-myutil
8.2) mvn clean install

9) Verify that it worked. NOTE: we are cd-ing to where the RPM has installed the package
9.1) cd /myco/pkgs/linux/intel/myutil/0.0.2.fc4.x86/bin
9.2) my_util

This should print:
  [cberry@cwb test-myutil]$ cd /myco/pkgs/linux/intel/myutil/0.0.2.fc4.x86/bin
  [cberry@cwb bin]$ ls
  total 28
  drwxr-xr-x  2 root root 4096 Dec 28 23:18 .
  drwxr-xr-x  4 root root 4096 Dec 28 23:18 ..
  -rwxr-xr-x  1 root root 7611 Dec 28 23:18 my_util
  [cberry@cwb bin]$ my_util
  Hello World
  
