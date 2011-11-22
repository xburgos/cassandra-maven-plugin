#!/bin/sh

rpm="rpm -e --nodeps"

$rpm com_myco_foo
$rpm com_myco_blah

$rpm com_myco_myutil
$rpm net_sourceforge_log4c_log4c
$rpm org_libexpat_expat

$rpm com_ibm_alphaworks_decNumber
