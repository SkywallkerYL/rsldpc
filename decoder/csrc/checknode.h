#ifndef CHECKNODE_H
#define CHECKNODE_H 
/*************************************************************************
	> File Name: checknode.h
	> Author: yangli
	> Mail: 577647772@qq.com 
	> Created Time: 2023年05月21日 星期日 11时06分44秒
 ************************************************************************/
#include "common.h"
#include "simualte.h"
#include <ctime>
#include <cstdlib>
using namespace std;
void Mux32MinSecmin(int *array , int * minval,int*subminval,int * minidx,int * subminidx){
	int absarray [32];
	for(int i=0;i < 32 ; i ++ ){
		absarray[i] = array[i] < 0 ? -array[i]:array[i];
	}
	int localminval = 32 ;
	int localminidx = -1 ;  
	for(int i=0;i < 32 ; i ++ ){
		if (absarray[i] < localminval){
			localminval = absarray[i];
			localminidx = i			 ;
		}
	}
	int localsubminval = 32;
	int localsubminidx = -1;
	for(int i=0;i < 32 ; i ++ ){
		if (absarray[i] < localsubminval && i !=localminidx){
			localsubminval = absarray[i];
			localsubminidx = i			 ;
		}
	}
	*minval = localminval ;
	*minidx = localminidx ;
	*subminval = localsubminval ; 
	*subminidx = localsubminidx ;
}
void checknodetest(int times){
	srand(time(nullptr));
	int *minval = new int (32);
	int *minidx = new int (-1);
	int *subminval = new int (32);
	int *subminidx = new int (-1);
	int array[32];
	while(times --){
		for(int i = 0;i < 32; i++){
			array[i] = rand()%(15*2+1)-15;
		//	printf("i:%d array:%d\n",i,array[i]);
		}
		Mux32MinSecmin(array,minval,subminval,minidx,subminidx);
#include "../build/Table.h"
		clockntimes(1);
		if(*minval !=top->io_minVal||*minidx !=top->io_minIdx
		 ||*subminval !=top->io_subminVal||*subminidx !=top->io_subminIdx){
			printf("minval ref:%d dut:%d\n",*minval,top->io_minVal);
			printf("minidx ref:%d dut:%d\n",*minidx,top->io_minIdx);
			printf("subminval ref:%d dut:%d\n",*subminval,top->io_subminVal);
			printf("subminidx ref:%d dut:%d\n",*subminidx,top->io_subminIdx);
		}

	}
	Log("if there is no other output, it means the module works right");
}


#endif
