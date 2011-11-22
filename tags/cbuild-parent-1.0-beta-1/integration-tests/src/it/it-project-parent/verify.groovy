/**
    This script installs the CBUILD plugins with checksums the
    integration-tests/target directory.  Validation of creation is
    quickly checked

    @author Lee Thompson <stimpy@codehaus.org>
*/




File sourceArchive = new File( localRepositoryPath, "org/codehaus/mojo/cbuild-parent/it/it-project-parent/1/it-project-parent-1.pom" );

return sourceArchive.exists();

