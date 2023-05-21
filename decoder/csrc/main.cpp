#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <stdlib.h>
#include "common.h"
#include "checknode.h"
#include "simualte.h"
int main(int argc , char* argv[]) {
	sim_init();

	reset(5);
#if TESTMODULE == 1
	checknodetest(1000000);
#endif 
	sim_exit();
	return 0;
}
