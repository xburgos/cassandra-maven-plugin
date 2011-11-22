#include <stdio.h>
#include <log4c.h>

int main()
{
  log4c_init();

  printf("Hello World\n");

  log4c_fini();

  return 0;
}
