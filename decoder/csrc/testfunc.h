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
#if TESTMODULE == 1
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
#elif TESTMODULE == 2
void variablesim(int *checkin, int llrin, int *checkout, int* appout){
	int sum = 0;
	for(int i = 0 ; i < 6;i++){
		sum+=checkin[i];  
	}
	for (int i = 0; i < 6 ; i++){
		checkout[i] = sum - checkin[i]; 
	}
	*appout = llrin + sum; 
}
void variablenodetest(int times){
	srand(time(nullptr));
	int checkin[6];
	int checkout[6];
	int *appout = new int(0);
	while(times --){
		for(int i = 0;i < 6; i++){
			checkin[i] = rand()%(15*2+1)-15;
		//	printf("i:%d array:%d\n",i,array[i]);
		}
		int llrin = rand()%(15*2+1)-15;
		variablesim(checkin,llrin,checkout,appout);
#include "../build/Table.h"
		top->io_LLrin = llrin;
		clockntimes(1);
		if((top->io_APPout&0x7f) != (*appout&0x7f)) {
			printf("ref:%d dut:%d\n",*appout, top->io_APPout);
		}
		if((top->io_Checkout_0&0x7f) !=( checkout[0]&0x7f)) printf("1 dut:%x ref:%x\n",top->io_Checkout_0,checkout[0]);
		if((top->io_Checkout_1&0x7f) !=( checkout[1]&0x7f)) printf("2 dut:%x ref:%x\n",top->io_Checkout_1,checkout[1]);
		if((top->io_Checkout_2&0x7f) !=( checkout[2]&0x7f)) printf("3 dut:%x ref:%x\n",top->io_Checkout_2,checkout[2]);
		if((top->io_Checkout_3&0x7f) !=( checkout[3]&0x7f)) printf("4 dut:%x ref:%x\n",top->io_Checkout_3,checkout[3]);
		if((top->io_Checkout_4&0x7f) !=( checkout[4]&0x7f)) printf("5 dut:%x ref:%x\n",top->io_Checkout_4,checkout[4]);
		if((top->io_Checkout_5&0x7f) !=( checkout[5]&0x7f)) printf("6 ref:%x ref:%x\n",top->io_Checkout_5,checkout[5]);
	}
	Log("if there is no other output, it means the module works right");
}
#endif 

#endif
