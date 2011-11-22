To install into test repository, run:

mvn clean package install:install-file clean -f one-to-one-factory/pom.xml -Dfile=target/one-to-one-factory-1.0.jar -DpomFile=pom.xml -DlocalRepositoryId=test -DlocalRepositoryPath=../../repository
mvn clean package install:install-file clean -f many-to-one-factory/pom.xml -Dfile=target/many-to-one-factory-1.0.jar -DpomFile=pom.xml -DlocalRepositoryId=test -DlocalRepositoryPath=../../repository
