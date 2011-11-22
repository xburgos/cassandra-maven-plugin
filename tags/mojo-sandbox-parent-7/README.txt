Releasing the mojo-sandbox-parent
 
 This parent overrules the distributionManagement repository to prevent accidental releases.
 To be able to release this sandbox-parent we need to overrule this by hand.

 To do so, execute from the /target/checkout-directory:
   mvn deploy \
   -DaltDeploymentRepository=codehaus-nexus-staging::default::https://nexus.codehaus.org/service/local/staging/deploy/maven2/ \
   -Pmojo-release
   