AC_INIT([my_util],
        [0.0.2],
        [Roger Hoover roger.hoover@gmail.com],
        [my_util])
AC_CONFIG_AUX_DIR(config)
AM_CONFIG_HEADER(config.h)
AM_INIT_AUTOMAKE([dist-bzip2])

AC_PROG_CC
AC_PROG_LIBTOOL

AC_MSG_CHECKING(for --with-log4c)
AC_ARG_WITH(log4c,
[  --with-log4c             install prefix for log4c])
log4c_found="no"
if test ! -z "$with_log4c"; then
  if test -d "$with_log4c"; then
    log4c_found="yes"
  fi
fi
AC_MSG_RESULT($with_log4c)
if test "$log4c_found" != "yes"; then
  echo "$as_me: error: log4c not found. Make sure it is installed.  You can use --with-log4c to give a path to it's install prefix" >&2
  exit 1
fi

AC_SUBST(LOG4C_ROOT)
LOG4C_ROOT="${with_log4c}";

AC_CONFIG_FILES([
   Makefile
   README
   doc/Makefile
   m4/Makefile
   src/Makefile
])

AC_OUTPUT
