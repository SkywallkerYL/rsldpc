#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <stdlib.h>
#include "common.h"
#include "testfunc.h"
#include "simualte.h"
int main(int argc , char* argv[]) {
	sim_init();

	reset(5);
#if TESTMODULE == 1
	checknodetest(1000000);
#elif TESTMODULE == 2 
	variablenodetest(1000000);
#elif TESTMODULE == 3
	ProcessUnitTest(100000);
#elif TESTMODULE == 4
	DecoderTest();
#endif 
	sim_exit();
	return 0;
}
