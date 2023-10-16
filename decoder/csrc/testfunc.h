#ifndef CHECKNODE_H
#define CHECKNODE_H 
/*************************************************************************
	> File Name: checknode.h
	> Author: yangli
	> Mail: 577647772@qq.com 
	> Created Time: 2023年05月21日 星期日 11时06分44秒
 ************************************************************************/
#include "common.h"
#include "channel.h"
#include "simualte.h"
#include <ctime>
#include <fstream>
#include <string>
#include <sstream>
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
		if (absarray[i] <= localsubminval && i !=localminidx){
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
		top->io_input_0 = array[0];
		top->io_input_1 = array[1];
		top->io_input_2 = array[2];
		top->io_input_3 = array[3];
		top->io_input_4 = array[4];
		top->io_input_5 = array[5];
		top->io_input_6 = array[6];
		top->io_input_7 = array[7];
		top->io_input_8 = array[8];
		top->io_input_9 = array[9];
		top->io_input_10 = array[10];
		top->io_input_11 = array[11];
		top->io_input_12 = array[12];
		top->io_input_13 = array[13];
		top->io_input_14 = array[14];
		top->io_input_15 = array[15];
		top->io_input_16 = array[16];
		top->io_input_17 = array[17];
		top->io_input_18 = array[18];
		top->io_input_19 = array[19];
		top->io_input_20 = array[20];
		top->io_input_21 = array[21];
		top->io_input_22 = array[22];
		top->io_input_23 = array[23];
		top->io_input_24 = array[24];
		top->io_input_25 = array[25];
		top->io_input_26 = array[26];
		top->io_input_27 = array[27];
		top->io_input_28 = array[28];
		top->io_input_29 = array[29];
		top->io_input_30 = array[30];
		top->io_input_31 = array[31];	
		clockntimes(1);

		if(*minval !=top->io_minVal||*minidx !=top->io_minIdx
		 ||*subminval !=top->io_subminVal||*subminidx !=top->io_subminIdx){
			printf("minval ref:%d dut:%d\n",*minval,top->io_minVal);
			printf("minidx ref:%d dut:%d\n",*minidx,top->io_minIdx);
			printf("subminval ref:%d dut:%d\n",*subminval,top->io_subminVal);
			printf("subminidx ref:%d dut:%d\n",*subminidx,top->io_subminIdx);
			for(int i = 0;i < 32; i++){
			//	array[i] = rand()%(15*2+1)-15;
				printf("i:%d array:%d\n",i,array[i]);
			}
				Log("You should check the minval and subminval, the Idx may be different!");
			break;
		}
	

	}
	Log("if there is no other output, it means the module works right");
}
#elif TESTMODULE == 2
//更改了硬件逻辑  这的个暂时不能用了  
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
		top->io_Checkin_0 = checkin[0];
		top->io_Checkin_1 = checkin[1];
		top->io_Checkin_2 = checkin[2];
		top->io_Checkin_3 = checkin[3];
		top->io_Checkin_4 = checkin[4];
		top->io_Checkin_5 = checkin[5];
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
#elif TESTMODULE == 3
//更改了硬件逻辑 ，这的个暂时不能用了 
void ProcessUnitTest(int times){
	srand(time(nullptr));
	int writedata [6*64];
	int writecnt = 0 ; 
	while ( times -- ){
		//Initial 
		for(int i = 0 ; i < 384 ; i ++ ){
			writedata[i] = rand()%(127*2+1)-127; 
		}
		top->io_LLrin = 0;
	//V2C write Test  
		top->io_V2CWriteEn = 1;
		top->io_V2CReadEn  = 0;
		top->io_C2VWriteEn = 0;
		top->io_C2VReadEn  = 0;
		while(writecnt < 64) {
			top->io_V2CWriteAddr_0 = writecnt;
            top->io_V2CWriteAddr_1 = writecnt;
            top->io_V2CWriteAddr_2 = writecnt;
            top->io_V2CWriteAddr_3 = writecnt;
            top->io_V2CWriteAddr_4 = writecnt;
            top->io_V2CWriteAddr_5 = writecnt;
			top->io_V2CWriteData_0 = writedata[writecnt*6+0];
            top->io_V2CWriteData_1 = writedata[writecnt*6+1];
            top->io_V2CWriteData_2 = writedata[writecnt*6+2];
            top->io_V2CWriteData_3 = writedata[writecnt*6+3];
            top->io_V2CWriteData_4 = writedata[writecnt*6+4];
            top->io_V2CWriteData_5 = writedata[writecnt*6+5];
			clockntimes(1);
			writecnt = writecnt+1;
		}
		writecnt = 0;
	//V2C read  Test 
		top->io_V2CWriteEn = 0;	
        top->io_V2CReadEn  = 1;
        top->io_C2VWriteEn = 0;
        top->io_C2VReadEn  = 0;
		int rowcounter = 0;
		while (rowcounter < 6){
			top->io_RowCounter = rowcounter;
			writecnt = 0;
			while(writecnt < 64){
				top->io_V2CReadAddr = writecnt ;
    	        //top->io_V2CReadAddr_1 = writecnt ;
    	        //top->io_V2CReadAddr_2 = writecnt ;
    	        //top->io_V2CReadAddr_3 = writecnt ;
    	        //top->io_V2CReadAddr_4 = writecnt ;
    	        //top->io_V2CReadAddr_5 = writecnt ;
				clockntimes(1);
				if(top->io_V2CReadData!=(writedata[writecnt*6+rowcounter]&0xff)) printf("V2CRead ref:%x dut:%x\n",top->io_V2CReadData,writedata[writecnt*6+rowcounter]);
			//	if(top->io_V2CReadData_1!=(writedata[writecnt*6+1]&0xff)) printf("V2CRead or Write fail\n");
			//	if(top->io_V2CReadData_2!=(writedata[writecnt*6+2]&0xff)) printf("V2CRead or Write fail\n");
			//	if(top->io_V2CReadData_3!=(writedata[writecnt*6+3]&0xff)) printf("V2CRead or Write fail\n");
			//	if(top->io_V2CReadData_4!=(writedata[writecnt*6+4]&0xff)) printf("V2CRead or Write fail\n");
			//	if(top->io_V2CReadData_5!=(writedata[writecnt*6+5]&0xff)) printf("V2CRead or Write fail\n");
				writecnt = writecnt + 1;
			}
			rowcounter++;
		}
		writecnt = 0;
		rowcounter = 0;
	//C2V write Test 	
		top->io_V2CWriteEn = 0;
		top->io_V2CReadEn  = 0;
		top->io_C2VWriteEn = 1;
		top->io_C2VReadEn  = 0;
		for(int i = 0 ; i < 384 ; i ++ ){
			writedata[i] = rand()%(7*2+1)-7; 
		}
		while (rowcounter < 6) {
			top->io_RowCounter = rowcounter;
			writecnt = 0 ;
			while(writecnt < 64) {
				top->io_C2VWriteAddr = writecnt;
        	    //top->io_C2VWriteAddr_1 = writecnt;
        	    //top->io_C2VWriteAddr_2 = writecnt;
        	    //top->io_C2VWriteAddr_3 = writecnt;
        	    //top->io_C2VWriteAddr_4 = writecnt;
        	    //top->io_C2VWriteAddr_5 = writecnt;
				top->io_C2VWriteData = writedata[writecnt*6+rowcounter];
        	    //top->io_C2VWriteData_1 = writedata[writecnt*6+1];
        	    //top->io_C2VWriteData_2 = writedata[writecnt*6+2];
        	    //top->io_C2VWriteData_3 = writedata[writecnt*6+3];
        	    //top->io_C2VWriteData_4 = writedata[writecnt*6+4];
        	    //top->io_C2VWriteData_5 = writedata[writecnt*6+5];
				clockntimes(1);
				writecnt = writecnt+1;
			}
			rowcounter++;
		}
		writecnt = 0;

	//C2V read Test  
		top->io_V2CWriteEn = 0;	
        top->io_V2CReadEn  = 0;
        top->io_C2VWriteEn = 0;
        top->io_C2VReadEn  = 1;
		while(writecnt < 64){
			top->io_C2VReadAddr_0 = writecnt ;
            top->io_C2VReadAddr_1 = writecnt ;
            top->io_C2VReadAddr_2 = writecnt ;
            top->io_C2VReadAddr_3 = writecnt ;
            top->io_C2VReadAddr_4 = writecnt ;
            top->io_C2VReadAddr_5 = writecnt ;
			clockntimes(1);
			int sum = 0;
			for (int i = 0;i < 6 ;i++){
				sum+=writedata[writecnt*6+i];
			}
			if((sum&0x7f) != (top->io_Appout&0x7f)) printf("ref:%x dut:%x\n",sum,top->io_Appout);
			//if(top->io_C2VReadData_0!=writedata[writecnt*6+0]) printf("C2VRead or Write fail\n");
			//if(top->io_C2VReadData_1!=writedata[writecnt*6+1]) printf("C2VRead or Write fail\n");
			//if(top->io_C2VReadData_2!=writedata[writecnt*6+2]) printf("C2VRead or Write fail\n");
			//if(top->io_C2VReadData_3!=writedata[writecnt*6+3]) printf("C2VRead or Write fail\n");
			//if(top->io_C2VReadData_4!=writedata[writecnt*6+4]) printf("C2VRead or Write fail\n");
			//if(top->io_C2VReadData_5!=writedata[writecnt*6+5]) printf("C2VRead or Write fail\n");
			writecnt = writecnt + 1;
		}
		writecnt = 0;

		top->io_V2CWriteEn = 0;
		top->io_V2CReadEn  = 0;
		top->io_C2VWriteEn = 0;
		top->io_C2VReadEn  = 0;
		clockntimes(10);
	// Varialbe Out
	}
	Log("if there is no other output, it means the module works right");
}
#elif TESTMODULE == 4 
bool decodeonetime(double sigma){
	top->io_Start = 1;
	top->io_IterInput = ITERMAX ;
	clockntimes(1);
	top->io_Start = 0;
	for(int i = 0 ; i < 64 ; i ++ ){
		top->io_LLrin_0 = LLrInitial( (sigma));
		top->io_LLrin_1 = LLrInitial( (sigma));
		top->io_LLrin_2 = LLrInitial( (sigma));
		top->io_LLrin_3 = LLrInitial( (sigma));
		top->io_LLrin_4 = LLrInitial( (sigma));
		top->io_LLrin_5 = LLrInitial( (sigma));
		top->io_LLrin_6 = LLrInitial( (sigma));
		top->io_LLrin_7 = LLrInitial( (sigma));
		top->io_LLrin_8 = LLrInitial( (sigma));
		top->io_LLrin_9 = LLrInitial( (sigma));
		top->io_LLrin_10 = LLrInitial( (sigma));
		top->io_LLrin_11 = LLrInitial( (sigma));
		top->io_LLrin_12 = LLrInitial( (sigma));
		top->io_LLrin_13 = LLrInitial( (sigma));
		top->io_LLrin_14 = LLrInitial( (sigma));
		top->io_LLrin_15 = LLrInitial( (sigma));
		top->io_LLrin_16 = LLrInitial( (sigma));
		top->io_LLrin_17 = LLrInitial( (sigma));
		top->io_LLrin_18 = LLrInitial( (sigma));
		top->io_LLrin_19 = LLrInitial( (sigma));
		top->io_LLrin_20 = LLrInitial( (sigma));
		top->io_LLrin_21 = LLrInitial( (sigma));
		top->io_LLrin_22 = LLrInitial( (sigma));
		top->io_LLrin_23 = LLrInitial( (sigma));
		top->io_LLrin_24 = LLrInitial( (sigma));
		top->io_LLrin_25 = LLrInitial( (sigma));
		top->io_LLrin_26 = LLrInitial( (sigma));
		top->io_LLrin_27 = LLrInitial( (sigma));
		top->io_LLrin_28 = LLrInitial( (sigma));
		top->io_LLrin_29 = LLrInitial( (sigma));
		top->io_LLrin_30 = LLrInitial( (sigma));
		top->io_LLrin_31 = LLrInitial( (sigma));
		clockntimes(1);
	}
//	printf("hhh\n");
	//int i = 0;
	//for (int i = 0 ; i < 2000; i ++) {
	//	clockntimes(1);
	//}
	//return true;
	while(!top->io_OutValid) {
		clockntimes(1);
	//	i++;
	}
    //	printf("i:%d\n",i);
	bool flag = top->io_Success; 
//	if(flag) printf("success\n");
	//printf("Iter remain:%d\n",top->io_IterOut);
	clockntimes(1);
	return flag ;
}



void DecoderTest(){
	for (double sigma = sigmastart; sigma >= sigmaend ; sigma = sigma-sigmastep ){
		int frame = 0 ;
		int errorframe = 0; 
		while(frame < maxtime || errorframe < maxerrortime) {
			bool success = decodeonetime(sigma);
			if(!success) errorframe++;
			frame++;
		}
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)2048;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
	}
}


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
void cppdecodeonetime(double sigma, int Itermax,int c2vind[384][32], int v2cind[2048][6] ){
	int llrin[2048];
	int c2vmin1[384];
	int c2vmin2[384];
	int c2vaddr[384];
	int c2v ;
	bool c2vsign[2048];
	int v2cmsg[2048*6];
	int appout[2048];
	//Initial 与硬件类似的形式 
	for(int i = 0 ; i < 64;i ++ ){
		for (int j = 0; j < 32 ; j ++ ){
			llrin[i*32 + j ] = LLrInitial( (sigma)); 
			for (int k = 0 ; k < 6 ; k ++ ){
				v2cmsg [i*32+j*6+k] = llrin[i*32+j];
			}
		}
	}
	int iter = Itermax ;
	while(iter > 0){
		// C2V   

		// V2C  
	

		// appout  decision 
	

	}
}
#elif TESTMODULE == 5 
void gngwrappertest(int times, double ber,uint64_t p0,uint64_t p1, uint64_t p2,uint64_t p3,uint64_t p4 ){
	top->io_p0_0 = p0;
	top->io_p0_1 = p1;
	top->io_p0_2 = p2; 
	top->io_p0_3 = p3;
	top->io_p0_4 = p4;
	top->io_dinvalid = 1 ;
	int n = times ;
	int wrongword = 0;
	int totalword = 0;
	int din = 0;
	while ( n --) {
		top->io_din_0 = din;
		top->io_din_1 = din;
		top->io_din_2 = din;
		top->io_din_3 = din;
		top->io_din_4 = din;
		top->io_din_5 = din;
		top->io_din_6 = din;
		top->io_din_7 = din;
		top->io_din_8 = din;
		top->io_din_9 = din;
		top->io_din_10 =din;
		top->io_din_11 =din;
		top->io_din_12 =din;
		top->io_din_13 =din;
		top->io_din_14 =din;
		top->io_din_15 =din;
		top->io_din_16 =din;
		top->io_din_17 =din;
		top->io_din_18 =din;
		top->io_din_19 =din;
		top->io_din_20 =din;
		top->io_din_21 =din;
		top->io_din_22 =din;
		top->io_din_23 =din;
		top->io_din_24 =din;
		top->io_din_25 =din;
		top->io_din_26 =din;
		top->io_din_27 =din;
		top->io_din_28 =din;
		top->io_din_29 =din;
		top->io_din_30 =din;
		top->io_din_31 =din;
		clockntimes(1);
		//保证app是8的时候才是对的   
		if	(	(top->io_dout_0 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_1 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_2 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_3 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_4 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_5 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_6 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_7 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_8 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_9 &0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_10&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_11&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_12&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_13&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_14&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_15&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_16&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_17&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_18&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_19&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_20&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_21&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_22&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_23&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_24&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_25&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_26&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_27&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_28&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_29&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_30&0x80)==0x80)wrongword ++;
		if	(	(top->io_dout_31&0x80)==0x80)wrongword ++;
		//printf("top->io_dout_0 : %x\n",top->io_dout_0);
	totalword+=32;
	}
	double realber = (double)(wrongword)/(double)totalword ;
	Log("Hope ber: %f  Real ber:%f ", ber,realber);
}
#elif TESTMODULE == 6 
void toptest(){
	uint64_t p_table[11][5] = {
    {0x00F6DE59DA609D40, 0x03D8972667FFD320, 0x08D62EDC6F866400, 0x123A4AC0DA34DA00, 0x2AE15EECABCB0000},
    {0x00D4C6F967D95498, 0x0378FF6FC977A6E0, 0x0832B202D6961F80, 0x114CE6ADF916BC00, 0x29BE9DB8C1EDA000},
    {0x00B5EF2F3C2BA5D8, 0x031E5FFD9697D1E0, 0x07937AFFCE63FD00, 0x106088159C106300, 0x28963B58EF860200},
    {0x009A3B0E7A344CD8, 0x02C8D9C2F5A5AEE0, 0x06F8E2BFE25A4640, 0x0F75935D7D441100, 0x2768358ADCF0CC00},
    {0x00818A4B91FA10C8, 0x027888440213C440, 0x066342AD1E391000, 0x0E8C74A939941900, 0x26348F382605D600},
    {0x006BB86D7E965E34, 0x022D80CE2913B4C0, 0x05D2F3E0B2761640, 0x0DA5A00395E54C00, 0x24FB51500C437400},
    {0x00589D178E071C04, 0x01E7D1AFAB37FF30, 0x05484E31CBDD5180, 0x0CC191717C245180, 0x23BC8BBDB73F7400},
    {0x00480C6D08D793E0, 0x01A781736CABADE0, 0x04C3A71F8E22AA40, 0x0BE0CCE87A553580, 0x2278567E19FCAC00},
    {0x0039D78F447547B6, 0x016C8E27681DABA0, 0x04455094D8222CC0, 0x0B03DE21828A1900, 0x212ED2D8B793B400},
    {0x002DCD35A4BF3130, 0x0136ECB54E6E5210, 0x03CD9785821A3AE0, 0x0A2B583F8CB28D80, 0x1FE02CBE92C1AA00},
	{0x0023BA5DD079D4D0, 0x010688560D870250, 0x035CC266342E88C0, 0x0957D54093BBD180, 0x1E8C9C528874BA00},

};
	double  rbertable[11] = {
    0.034520,0.032020,0.029600,0.027240,0.024960,0.022760,0.020640,0.018620,0.016680,0.014860,
0.013140};
	double  sigmatable[11] = {
    0.55,0.54,0.53,0.52,0.51,0.50,0.49,0.48,0.47,0.46,0.45
  };
	for (int i = 0; i < 11 ; i ++) {
		double sigma = sigmatable[i];
		top->io_IterInput = ITERMAX;
		top->io_maxError  = maxerrortime; 
		top->io_p0_0 = p_table[i][4];
		top->io_p0_1 = p_table[i][3];
		top->io_p0_2 = p_table[i][2];
		top->io_p0_3 = p_table[i][1];
		top->io_p0_4 = p_table[i][0];
		top->io_start = 1;
		clockntimes(1); 
		top->io_start = 0;
		while(!top->io_Framevalid) {
			clockntimes(1); 
		}
		int frame =(int) top->io_totalframe ;
		int errorframe = (int )top->io_errorframe ; 
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)2048;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
		clockntimes(10);
	}
  
}
#elif TESTMODULE == 7 
bool decodeonetime(double sigma){
	top->io_Start = 1;
	top->io_postvalid = 1;
	//top->io_strongMessage = 5;
	//top->io_weakMessage = 3;
	top->io_postIterInput = 6;
	top->io_IterInput = ITERMAX ;
	clockntimes(1);
	top->io_Start = 0;
	//printf("hhh\n");
	for(int i = 0 ; i < 16 ; i ++ ){
		top->io_LLrin_0 = LLrInitial( (sigma));
		top->io_LLrin_1 = LLrInitial( (sigma));
		top->io_LLrin_2 = LLrInitial( (sigma));
		top->io_LLrin_3 = LLrInitial( (sigma));
		top->io_LLrin_4 = LLrInitial( (sigma));
		top->io_LLrin_5 = LLrInitial( (sigma));
		top->io_LLrin_6 = LLrInitial( (sigma));
		top->io_LLrin_7 = LLrInitial( (sigma));
		top->io_LLrin_8 = LLrInitial( (sigma));
		top->io_LLrin_9 = LLrInitial( (sigma));
		top->io_LLrin_10 = LLrInitial( (sigma));
		top->io_LLrin_11 = LLrInitial( (sigma));
		top->io_LLrin_12 = LLrInitial( (sigma));
		top->io_LLrin_13 = LLrInitial( (sigma));
		top->io_LLrin_14 = LLrInitial( (sigma));
		top->io_LLrin_15 = LLrInitial( (sigma));
		top->io_LLrin_16 = LLrInitial( (sigma));
		top->io_LLrin_17 = LLrInitial( (sigma));
		top->io_LLrin_18 = LLrInitial( (sigma));
		top->io_LLrin_19 = LLrInitial( (sigma));
		top->io_LLrin_20 = LLrInitial( (sigma));
		top->io_LLrin_21 = LLrInitial( (sigma));
		top->io_LLrin_22 = LLrInitial( (sigma));
		top->io_LLrin_23 = LLrInitial( (sigma));
		top->io_LLrin_24 = LLrInitial( (sigma));
		top->io_LLrin_25 = LLrInitial( (sigma));
		top->io_LLrin_26 = LLrInitial( (sigma));
		top->io_LLrin_27 = LLrInitial( (sigma));
		top->io_LLrin_28 = LLrInitial( (sigma));
		top->io_LLrin_29 = LLrInitial( (sigma));
		top->io_LLrin_30 = LLrInitial( (sigma));
		top->io_LLrin_31 = LLrInitial( (sigma));
		top->io_LLrin_32 = LLrInitial( (sigma));
		top->io_LLrin_33 = LLrInitial( (sigma));
		top->io_LLrin_34 = LLrInitial( (sigma));
		top->io_LLrin_35 = LLrInitial( (sigma));
		top->io_LLrin_36 = LLrInitial( (sigma));
		top->io_LLrin_37 = LLrInitial( (sigma));
		top->io_LLrin_38 = LLrInitial( (sigma));
		top->io_LLrin_39 = LLrInitial( (sigma));
		top->io_LLrin_40 = LLrInitial( (sigma));
		top->io_LLrin_41 = LLrInitial( (sigma));
		top->io_LLrin_42 = LLrInitial( (sigma));
		top->io_LLrin_43 = LLrInitial( (sigma));
		top->io_LLrin_44 = LLrInitial( (sigma));
		top->io_LLrin_45 = LLrInitial( (sigma));
		top->io_LLrin_46 = LLrInitial( (sigma));
		top->io_LLrin_47 = LLrInitial( (sigma));
		top->io_LLrin_48 = LLrInitial( (sigma));
		top->io_LLrin_49 = LLrInitial( (sigma));
		top->io_LLrin_50 = LLrInitial( (sigma));
		top->io_LLrin_51 = LLrInitial( (sigma));
		top->io_LLrin_52 = LLrInitial( (sigma));
		top->io_LLrin_53 = LLrInitial( (sigma));
		top->io_LLrin_54 = LLrInitial( (sigma));
		top->io_LLrin_55 = LLrInitial( (sigma));
		top->io_LLrin_56 = LLrInitial( (sigma));
		top->io_LLrin_57 = LLrInitial( (sigma));
		top->io_LLrin_58 = LLrInitial( (sigma));
		top->io_LLrin_59 = LLrInitial( (sigma));
		top->io_LLrin_60 = LLrInitial( (sigma));
		top->io_LLrin_61 = LLrInitial( (sigma));
		top->io_LLrin_62 = LLrInitial( (sigma));
		top->io_LLrin_63 = LLrInitial( (sigma));
		top->io_LLrin_64 = LLrInitial( (sigma));
		top->io_LLrin_65 = LLrInitial( (sigma));
		top->io_LLrin_66 = LLrInitial( (sigma));
		top->io_LLrin_67 = LLrInitial( (sigma));
		top->io_LLrin_68 = LLrInitial( (sigma));
		top->io_LLrin_69 = LLrInitial( (sigma));
		top->io_LLrin_70 = LLrInitial( (sigma));
		top->io_LLrin_71 = LLrInitial( (sigma));
		top->io_LLrin_72 = LLrInitial( (sigma));
		top->io_LLrin_73 = LLrInitial( (sigma));
		top->io_LLrin_74 = LLrInitial( (sigma));
		top->io_LLrin_75 = LLrInitial( (sigma));
		top->io_LLrin_76 = LLrInitial( (sigma));
		top->io_LLrin_77 = LLrInitial( (sigma));
		top->io_LLrin_78 = LLrInitial( (sigma));
		top->io_LLrin_79 = LLrInitial( (sigma));
		top->io_LLrin_80 = LLrInitial( (sigma));
		top->io_LLrin_81 = LLrInitial( (sigma));
		top->io_LLrin_82 = LLrInitial( (sigma));
		top->io_LLrin_83 = LLrInitial( (sigma));
		top->io_LLrin_84 = LLrInitial( (sigma));
		top->io_LLrin_85 = LLrInitial( (sigma));
		top->io_LLrin_86 = LLrInitial( (sigma));
		top->io_LLrin_87 = LLrInitial( (sigma));
		top->io_LLrin_88 = LLrInitial( (sigma));
		top->io_LLrin_89 = LLrInitial( (sigma));
		top->io_LLrin_90 = LLrInitial( (sigma));
		top->io_LLrin_91 = LLrInitial( (sigma));
		top->io_LLrin_92 = LLrInitial( (sigma));
		top->io_LLrin_93 = LLrInitial( (sigma));
		top->io_LLrin_94 = LLrInitial( (sigma));
		top->io_LLrin_95 = LLrInitial( (sigma));
		top->io_LLrin_96 = LLrInitial( (sigma));
		top->io_LLrin_97 = LLrInitial( (sigma));
		top->io_LLrin_98 = LLrInitial( (sigma));
		top->io_LLrin_99 = LLrInitial( (sigma));
		top->io_LLrin_100 = LLrInitial( (sigma));
		top->io_LLrin_101 = LLrInitial( (sigma));
		top->io_LLrin_102 = LLrInitial( (sigma));
		top->io_LLrin_103 = LLrInitial( (sigma));
		top->io_LLrin_104 = LLrInitial( (sigma));
		top->io_LLrin_105 = LLrInitial( (sigma));
		top->io_LLrin_106 = LLrInitial( (sigma));
		top->io_LLrin_107 = LLrInitial( (sigma));
		top->io_LLrin_108 = LLrInitial( (sigma));
		top->io_LLrin_109 = LLrInitial( (sigma));
		top->io_LLrin_110 = LLrInitial( (sigma));
		top->io_LLrin_111 = LLrInitial( (sigma));
		top->io_LLrin_112 = LLrInitial( (sigma));
		top->io_LLrin_113 = LLrInitial( (sigma));
		top->io_LLrin_114 = LLrInitial( (sigma));
		top->io_LLrin_115 = LLrInitial( (sigma));
		top->io_LLrin_116 = LLrInitial( (sigma));
		top->io_LLrin_117 = LLrInitial( (sigma));
		top->io_LLrin_118 = LLrInitial( (sigma));
		top->io_LLrin_119 = LLrInitial( (sigma));
		top->io_LLrin_120 = LLrInitial( (sigma));
		top->io_LLrin_121 = LLrInitial( (sigma));
		top->io_LLrin_122 = LLrInitial( (sigma));
		top->io_LLrin_123 = LLrInitial( (sigma));
		top->io_LLrin_124 = LLrInitial( (sigma));
		top->io_LLrin_125 = LLrInitial( (sigma));
		top->io_LLrin_126 = LLrInitial( (sigma));
		top->io_LLrin_127 = LLrInitial( (sigma));
	
		clockntimes(1);
	}
//	printf("hhh\n");
	//int i = 0;
	//for (int i = 0 ; i < 2000; i ++) {
	//	clockntimes(1);
	//}
	//return true;
	while(!top->io_OutValid) {
		clockntimes(1);
		//printf("jjj\n");
	//	i++;
	}
    //	printf("i:%d\n",i);
	bool flag = top->io_Success; 
//	if(flag) printf("success\n");
	//printf("Iter remain:%d\n",top->io_IterOut);
	clockntimes(1);
	return flag ;
}
bool ReadllrDecoder(int * llrin,int & errorbit,int &checknum);
void ReadfileDecode(){
	
	int frame = 0 ;
	int errorframe = 0; 
	ifstream file(WRONG_FILENAME);
	if(!file) {
		Log("NO file\n");
		return ;
	}
	string line;
	int llrin[2048];
	int targetframe =1;
	while(getline(file,line)){
		//if(frame != targetframe-1) {
		//frame++;
		//continue;
		//}
		for (int i = 0;i < 2048;i++){
			string hex = line.substr(i,1);
			int value = stoi(hex,nullptr,16);
			if(value&0x8){
				value |= 0xfffffff0;
			}
			llrin[i] = value;
		}
		int errorbit = 0;
		int checknum = 0;
		bool success = ReadllrDecoder(llrin,errorbit,checknum);
		if(!success) errorframe++;
		frame++;
		double fer = (double) errorframe / (double) frame;
		//double rate = (double)errorframe/(double)frame;
		//double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("check:%d errorbit:%d errorframe:%d frame:%d rate:%f",checknum,errorbit,errorframe,frame,fer);
	}
	//Log("if there is no other output, it means the module works right");
}

void DecoderTest(){
	for (double sigma = sigmastart; sigma >= sigmaend ; sigma = sigma-sigmastep ){
		int frame = 0 ;
		int errorframe = 0; 
		while(frame < maxtime || errorframe < maxerrortime) {
			bool success = decodeonetime(sigma);
			if(!success) errorframe++;
			frame++;
		}
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)2048;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
	}

	//Log("if there is no other output, it means the module works right");
}
void update_min_vss(Message &min1, Message &min2,int & min1_addr , int & min2_addr ,Message val ,int addr ){
	if(addr == min1_addr) {
		if(val <= min2) {
			min1 = val ; 
		}else {
			min1 = min2 ; 
			min1_addr = min2_addr ;
			min2 = val ;//<= C2VMAX ? val : C2VMAX ; 
			min2_addr = addr ;
		}
	}else if (addr == min2_addr) {
		if (val <= min1) {
			min2 = min1 ;
			min2_addr = min1_addr ;
			min1 = val ; 
			min1_addr =addr;
		}else {
			min2 = val ;//<= C2VMAX ? val : C2VMAX; 
		}
	}else {
		if(val <= min1) {
			min2 = min1 ; 
			min2_addr = min1_addr ; 
			min1 = val ;
			min1_addr = addr ;
		}else if(val <= min2) {
			min2 = val ;
			min2_addr = addr ;
		}
	}

}
void choose_true_min(Message &min1, Message &min2,
					 Message &submin1, Message &submin2,
		int & min_addr1 , int & min_addr2 ,
		int & submin_addr1 , int & submin_addr2 ,
		Message &minval ,Message & subminval,
		int &minaddr,int &subminaddr ){

	bool mindecide[6];
	mindecide[0] = min1<min2 ;
	mindecide[1] = min1<submin1 ;
	mindecide[2] = min1<submin2 ;
	mindecide[3] = min2<submin1 ;
	mindecide[4] = min2<submin2 ;
	mindecide[5] = submin1<submin2 ;
	subminval = submin2;
	subminaddr = submin_addr2;
	if(mindecide[0] & mindecide[1]&mindecide[2]){
		minval = min1;
		minaddr = min_addr1;
		if(mindecide[3]&mindecide[4]){
			subminval = min2;
			subminaddr = min_addr2;
		}else if(mindecide[5]){
			subminval = submin1;
			subminaddr = submin_addr1;
		}
	}else if(mindecide[3]&mindecide[4]){
		minval = min2;
		minaddr = min_addr2;
		if(mindecide[1]&mindecide[2]){
			subminval = min1;
			subminaddr = min_addr1;
		}else if(mindecide[5]){
			subminval = submin1;
			subminaddr = submin_addr1;
		}
	}else if(mindecide[5]){
		minval = submin1;
		minaddr = submin_addr1;
		if(mindecide[0]&mindecide[2]){
			subminval = min1;
			subminaddr = min_addr1;
		}else if(mindecide[4]){
			subminval = submin2;
			subminaddr = submin_addr2;
		}
	}else {
		minval = submin2;
		minaddr = submin_addr2;
		if(mindecide[0]&mindecide[1]){
			subminval = min1;
			subminaddr = min_addr1;
		}else if(mindecide[3]){
			subminval = min2;
			subminaddr = min_addr2;
		}else {
			subminval = submin1;
			subminaddr = submin_addr1;
		}
	}

}
bool cppdecodekernelFast2col(vector<vector<int>> VofC,vector<vector<int>> CofV, Message * llrin, Message * appout , vector<vector<bool>> v2c, 
		Message *min1, Message *submin1,int * min_addr1, int *submin_addr1, 
		Message *min2, Message *submin2,int * min_addr2, int *submin_addr2, 
		bool * sign , bool * sign0 ,Message * c2v,Message * c2v0 
		){
	// Initialization   
	for(int i = 0; i < ROWNUMBER; i ++) {
		min1[i] = min2[i] = C2VMAX; 
		submin1[i] = submin2[i] = C2VMAX;
		min_addr1[i] = min_addr2[i] = 0 ;
		submin_addr1[i] = submin_addr2[i] = 0 ;
		sign[i]  = 0;
		sign0[i]  = 0;
	}
	for(int i = 0; i < COLNUMBER; i ++) {
		//update checknode min1 min2   
		for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
			//if(H[j][i] == 1) {
				int j = CofV[i][jindex];
				
				if(llrin[i] < 0) {
					if((i/BLKSIZE)%2==0){	
						sign[j] = sign[j]^1;
					}else {
						sign0[j] = sign0[j]^1;
					}
										
				}
				Message llr_abs = llrin[i] < 0 ? -llrin[i] : llrin[i];
				//if(llr_abs > C2VMAX) printf("aaaaa\n");
//#if TYPE
				llr_abs = (llr_abs <= C2VMAX ? llr_abs : C2VMAX) ;
//#endif
				v2c[i][j] = llrin[i] < 0;
				//update_min(min1[j],min2[j],min1_addr[j],llr_abs,i);
				//两列同时做  相当于偶数列存min1  奇数列存Min2  从0开始
				if((i/BLKSIZE)%2 == 0){
					update_min_vss(min1[j],submin1[j],min_addr1[j],submin_addr1[j],llr_abs,i/BLKSIZE);
				}else {
					update_min_vss(min2[j],submin2[j],min_addr2[j],submin_addr2[j],llr_abs,i/BLKSIZE);
				}
			//}
		}
		appout[i] = llrin[i];
	}
	//Iteration 
	bool success = 1; 
	for(int iter = 0 ; iter < ITERMAX; iter++) {
		
		for (int iindex = 0; iindex < 16; iindex ++) {
			//CNU 
			for (int id = 0 ; id < 128 ; id++ ){
				int i = iindex*128+id;
				Message appval = llrin[i];
				for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
					//if(H[j][i] == 1) {
						int j = CofV[i][jindex];
						Message minval ;Message subminval;
						int minaddr;int subminaddr;
						//if(min1_addr[j] == -1) exit(0);
						choose_true_min(min1[j],min2[j],submin1[j],submin2[j],
						min_addr1 [j],min_addr2[j] ,submin_addr1[j] ,submin_addr2[j] ,
						minval ,subminval,
							minaddr,subminaddr );
						//printf("minval:%d minaddr:%d\n",minval,minaddr);
						Message c2v_val = ((i/BLKSIZE == minaddr) ? subminval : minval);
						//if(c2v[j]>=1000) 
						//{
						//printf("iter:%d j:%d c2v:%f min1:%f min2:%f \n",iter,j,c2v[j],min1[j],min2[j]);
						//printf("addr:%d col:%d c2v_val:%f\n",min1_addr[j],i,c2v_val);
						////	exit(0);
						//}
						c2v_val = c2v_val * 0.75;//3/4;
						//c2v_val = (c2v_val+c2v_val+c2v_val) / 4;
						if((i/BLKSIZE)%2==0){	
							if(sign[j] != v2c[i][j]){
								c2v_val = -c2v_val;	
							}	
							c2v[j] = c2v_val;
						}else {
							if(sign0[j] != v2c[i][j]){
								c2v_val = -c2v_val;	
							}	
							c2v0[j] = c2v_val;
						}
						//c2v[j] = c2v[j] <= C2VMAX ? c2v[j] : C2VMAX ;	
						//c2v[j] = c2v_val;
						appval += c2v_val;
					//}
				}
				appout[i] = appval;
			}
			//appout[i] = appval;
			//if(appval<=-10) 
			//{
			//	printf("/********app check*********/\n");
			//	printf("iter:%d app:%f llr:%f\n",iter,appval,llrin[i] );
			//	for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
			//	//if(H[j][i] == 1) {
			//		int j = CofV[i][jindex];
			//		printf("iter:%d j:%d c2v:%f min1:%f min2:%f \n",iter,j,c2v[j],min1[j],min2[j]);
			//	}
			////	exit(0);
			//}
			//VNU   update v2c 
			for (int id = 0 ; id < 128 ; id++ ){
				int i = iindex*128+id;
				Message appval = appout[i];
				for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
					//if(H[j][i] == 1) {
						int j = CofV[i][jindex];
						Message v2c_abs;
						if((i/BLKSIZE)%2==0){
							v2c_abs = appval - c2v[j];
						}else{
							v2c_abs = appval - c2v0[j];
						}
						int v2csign = 0 ; 
						if(v2c_abs < 0 ) {
							v2c_abs = -v2c_abs ;
							v2csign = 1; 
						}
						//if(v2c_abs>=40) 
						//{
						//	printf("/********v2c_abs check*********/\n");
						//	printf("iter:%d app:%f llr:%f v2c:%f c2v:%f sign:%d\n",iter,appval,llrin[i],v2c_abs,c2v[j],v2csign );
						//}

//#if TYPE
						v2c_abs = v2c_abs <= C2VMAX ? v2c_abs : C2VMAX ;
//#endif
						if((i/BLKSIZE)%2==0){	
							if(v2csign != v2c[i][j]) {
								sign[j] = sign[j] ^ 1; 
							}
						}else {
							if(v2csign != v2c[i][j]) {
								sign0[j] = sign0[j] ^ 1; 
							}	
						}
						v2c[i][j] = v2csign ;
						if((i/BLKSIZE)%2 == 0){
							//printf("%d\n",i);
							update_min_vss(min1[j],submin1[j],min_addr1[j],submin_addr1[j],v2c_abs,i/BLKSIZE);
						}else {
							//printf("hhh%d\n",i);
							update_min_vss(min2[j],submin2[j],min_addr2[j],submin_addr2[j],v2c_abs,i/BLKSIZE);
						} 
						//update_min_vss(min1[j],min2[j],min1_addr[j],min2_addr[j],v2c_abs,i);
						//update_min(min1[j],min2[j],min1_addr[j],v2c_abs,i);
					//}
				}
			}
			
		}
		success = 1; 
		for (int i = 0 ; i < COLNUMBER ; i ++) {
			if(appout[i] < 0 ){
				success = 0 ;
				break;
			}
		}
		if(success) {
			return success ; 
		}
	}
	return success ;
}
void cppDecoderTest(){

	//int H[ROWNUMBER][COLNUMBER];
	//这里直接定义这个大矩阵会超内存
	//导致C程序错误退出  
	//用vector动态分配
	vector<vector<int>> H;
	H.resize(ROWNUMBER);
	for (int i = 0 ; i < ROWNUMBER; i ++ ){
		H[i].resize(COLNUMBER);
	}
	ifstream inr(BASE_MATRIX_FILENAME);
	if(!inr) {
		Log("No file %s",BASE_MATRIX_FILENAME) ;
		return ; 
	}
	string r ;
	for(int i = 0 ; i < ROWNUMBER; i ++) {
		string number ; 
		getline(inr,r);
		istringstream stream(r);
		for (int j = 0 ; j < COLNUMBER ; j ++) {
			stream >> number ; 
			H[i][j] = stoi(number);
		}
	}

	vector<vector<int>> VofC ;
	VofC.resize(ROWNUMBER);
	for(int i = 0; i < ROWNUMBER; i++){
		VofC[i].resize(COLNUMBER/BLKSIZE);
		for(int j = 0 ; j < COLNUMBER; j ++) {
			if(H[i][j]==1) {
				VofC[i][j/BLKSIZE] = j ;
			}
		}

	}
	vector<vector<int>> CofV ;
	CofV.resize(COLNUMBER);
	for(int i = 0; i < COLNUMBER; i++){
		CofV[i].resize(ROWNUMBER/BLKSIZE);
		for(int j = 0 ; j < ROWNUMBER; j ++) {
			if(H[j][i]==1) {
				CofV[i][j/BLKSIZE] = j ;
			}
		}

	}
	//printf("aaa\n");
	for (double sigma = sigmastart; sigma >= sigmaend ; sigma = sigma-sigmastep ){
		int frame = 0 ;
		int errorframe = 0; 
		//Log("hhhh");
		while((frame < maxtime) || (errorframe < maxerrortime)) {
			Message llrin[COLNUMBER]; 
			Message appout[COLNUMBER];
			//bool v2c[COLNUMBER][6]; 

			vector<vector<bool>> v2c;
			v2c.resize(COLNUMBER);
			for (int i = 0 ; i < COLNUMBER; i ++ ){
				v2c[i].resize(ROWNUMBER);
			}
			Message c2v[ROWNUMBER] ;

			Message min1 [ROWNUMBER];
			Message min2 [ROWNUMBER]; 
			bool sign[ROWNUMBER] ;
			
			//Log("hhhh");
			for (int i = 0 ; i < COLNUMBER;i++ ){
				llrin[i] = LLrInitial((sigma)); 
//#if TYPE
				llrin[i] = llrin[i] <= -APPMAX ? -APPMAX : llrin[i] >= APPMAX ? APPMAX : llrin[i];
//#endif
			}

		Message submin1 [ROWNUMBER];
		Message submin2 [ROWNUMBER];
		int min_addr1[ROWNUMBER];
		int min_addr2[ROWNUMBER]; 
		int submin_addr1[ROWNUMBER];
		int submin_addr2[ROWNUMBER];
		bool sign0[ROWNUMBER] ;
		Message c2v0[ROWNUMBER] ;
		bool success = cppdecodekernelFast2col(VofC, CofV,  llrin, appout ,v2c, 
		 min1, submin1,min_addr1, submin_addr1, 
		 min2, submin2,min_addr2, submin_addr2, 
				sign ,sign0, c2v,c2v0 
		);
			//printf("aaa\n");
			if(!success) {
				errorframe++;
				//Log("fail frame %d",errorframe);
			}
			//else Log("success frame%d",frame);
			frame++;
		}
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)COLNUMBER;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
	}

	//Log("if there is no other output, it means the module works right");
}
bool ReadllrDecoder(Message * llrin ,int & errorbit,int &checknum
		){
	//Decoder Initialization
	top->io_Start = 1;
	top->io_IterInput = ITERMAX ;
#if POSTPROCESS 
	top->io_postvalid = 1;

	top->io_strongMessage_0 = strongMessage_0 ;
	top->io_weakMessage_0   =   weakMessage_0 ;
	top->io_strongMessage_1 = strongMessage_1 ;
	top->io_weakMessage_1   =   weakMessage_1 ;
	top->io_strongMessage_2 = strongMessage_2 ;
	top->io_weakMessage_2   =   weakMessage_2 ;
	top->io_postIterInput = postInter;
#endif
	clockntimes(1);
	top->io_Start = 0;
	//
	for(int i = 0; i < 16;i++) {
		top->io_LLrin_0 = llrin[i*128+0];
		//printf("llrin:%x appout:%x\n",top->io_LLrin_0,top->io_appout_0);
		top->io_LLrin_1 = llrin[i*128+1];
		top->io_LLrin_2 = llrin[i*128+2];
		top->io_LLrin_3 = llrin[i*128+3];
		top->io_LLrin_4 = llrin[i*128+4];
		top->io_LLrin_5 = llrin[i*128+5];
		top->io_LLrin_6 = llrin[i*128+6];
		top->io_LLrin_7 = llrin[i*128+7];
		top->io_LLrin_8 = llrin[i*128+8];
		top->io_LLrin_9 = llrin[i*128+9];
		top->io_LLrin_10 = llrin[i*128+10];
		top->io_LLrin_11 = llrin[i*128+11];
		top->io_LLrin_12 = llrin[i*128+12];
		top->io_LLrin_13 = llrin[i*128+13];
		top->io_LLrin_14 = llrin[i*128+14];
		top->io_LLrin_15 = llrin[i*128+15];
		top->io_LLrin_16 = llrin[i*128+16];
		top->io_LLrin_17 = llrin[i*128+17];
		top->io_LLrin_18 = llrin[i*128+18];
		top->io_LLrin_19 = llrin[i*128+19];
		top->io_LLrin_20 = llrin[i*128+20];
		top->io_LLrin_21 = llrin[i*128+21];
		top->io_LLrin_22 = llrin[i*128+22];
		top->io_LLrin_23 = llrin[i*128+23];
		top->io_LLrin_24 = llrin[i*128+24];
		top->io_LLrin_25 = llrin[i*128+25];
		top->io_LLrin_26 = llrin[i*128+26];
		top->io_LLrin_27 = llrin[i*128+27];
		top->io_LLrin_28 = llrin[i*128+28];
		top->io_LLrin_29 = llrin[i*128+29];
		top->io_LLrin_30 = llrin[i*128+30];
		top->io_LLrin_31 = llrin[i*128+31];
		top->io_LLrin_32 = llrin[i*128+32];
		top->io_LLrin_33 = llrin[i*128+33];
		top->io_LLrin_34 = llrin[i*128+34];
		top->io_LLrin_35 = llrin[i*128+35];
		top->io_LLrin_36 = llrin[i*128+36];
		top->io_LLrin_37 = llrin[i*128+37];
		top->io_LLrin_38 = llrin[i*128+38];
		top->io_LLrin_39 = llrin[i*128+39];
		top->io_LLrin_40 = llrin[i*128+40];
		top->io_LLrin_41 = llrin[i*128+41];
		top->io_LLrin_42 = llrin[i*128+42];
		top->io_LLrin_43 = llrin[i*128+43];
		top->io_LLrin_44 = llrin[i*128+44];
		top->io_LLrin_45 = llrin[i*128+45];
		top->io_LLrin_46 = llrin[i*128+46];
		top->io_LLrin_47 = llrin[i*128+47];
		top->io_LLrin_48 = llrin[i*128+48];
		top->io_LLrin_49 = llrin[i*128+49];
		top->io_LLrin_50 = llrin[i*128+50];
		top->io_LLrin_51 = llrin[i*128+51];
		top->io_LLrin_52 = llrin[i*128+52];
		top->io_LLrin_53 = llrin[i*128+53];
		top->io_LLrin_54 = llrin[i*128+54];
		top->io_LLrin_55 = llrin[i*128+55];
		top->io_LLrin_56 = llrin[i*128+56];
		top->io_LLrin_57 = llrin[i*128+57];
		top->io_LLrin_58 = llrin[i*128+58];
		top->io_LLrin_59 = llrin[i*128+59];
		top->io_LLrin_60 = llrin[i*128+60];
		top->io_LLrin_61 = llrin[i*128+61];
		top->io_LLrin_62 = llrin[i*128+62];
		top->io_LLrin_63 = llrin[i*128+63];
		top->io_LLrin_64 = llrin[i*128+64];
		top->io_LLrin_65 = llrin[i*128+65];
		top->io_LLrin_66 = llrin[i*128+66];
		top->io_LLrin_67 = llrin[i*128+67];
		top->io_LLrin_68 = llrin[i*128+68];
		top->io_LLrin_69 = llrin[i*128+69];
		top->io_LLrin_70 = llrin[i*128+70];
		top->io_LLrin_71 = llrin[i*128+71];
		top->io_LLrin_72 = llrin[i*128+72];
		top->io_LLrin_73 = llrin[i*128+73];
		top->io_LLrin_74 = llrin[i*128+74];
		top->io_LLrin_75 = llrin[i*128+75];
		top->io_LLrin_76 = llrin[i*128+76];
		top->io_LLrin_77 = llrin[i*128+77];
		top->io_LLrin_78 = llrin[i*128+78];
		top->io_LLrin_79 = llrin[i*128+79];
		top->io_LLrin_80 = llrin[i*128+80];
		top->io_LLrin_81 = llrin[i*128+81];
		top->io_LLrin_82 = llrin[i*128+82];
		top->io_LLrin_83 = llrin[i*128+83];
		top->io_LLrin_84 = llrin[i*128+84];
		top->io_LLrin_85 = llrin[i*128+85];
		top->io_LLrin_86 = llrin[i*128+86];
		top->io_LLrin_87 = llrin[i*128+87];
		top->io_LLrin_88 = llrin[i*128+88];
		top->io_LLrin_89 = llrin[i*128+89];
		top->io_LLrin_90 = llrin[i*128+90];
		top->io_LLrin_91 = llrin[i*128+91];
		top->io_LLrin_92 = llrin[i*128+92];
		top->io_LLrin_93 = llrin[i*128+93];
		top->io_LLrin_94 = llrin[i*128+94];
		top->io_LLrin_95 = llrin[i*128+95];
		top->io_LLrin_96 = llrin[i*128+96];
		top->io_LLrin_97 = llrin[i*128+97];
		top->io_LLrin_98 = llrin[i*128+98];
		top->io_LLrin_99 = llrin[i*128+99];
		top->io_LLrin_100 = llrin[i*128+100];
		top->io_LLrin_101 = llrin[i*128+101];
		top->io_LLrin_102 = llrin[i*128+102];
		top->io_LLrin_103 = llrin[i*128+103];
		top->io_LLrin_104 = llrin[i*128+104];
		top->io_LLrin_105 = llrin[i*128+105];
		top->io_LLrin_106 = llrin[i*128+106];
		top->io_LLrin_107 = llrin[i*128+107];
		top->io_LLrin_108 = llrin[i*128+108];
		top->io_LLrin_109 = llrin[i*128+109];
		top->io_LLrin_110 = llrin[i*128+110];
		top->io_LLrin_111 = llrin[i*128+111];
		top->io_LLrin_112 = llrin[i*128+112];
		top->io_LLrin_113 = llrin[i*128+113];
		top->io_LLrin_114 = llrin[i*128+114];
		top->io_LLrin_115 = llrin[i*128+115];
		top->io_LLrin_116 = llrin[i*128+116];
		top->io_LLrin_117 = llrin[i*128+117];
		top->io_LLrin_118 = llrin[i*128+118];
		top->io_LLrin_119 = llrin[i*128+119];
		top->io_LLrin_120 = llrin[i*128+120];
		top->io_LLrin_121 = llrin[i*128+121];
		top->io_LLrin_122 = llrin[i*128+122];
		top->io_LLrin_123 = llrin[i*128+123];
		top->io_LLrin_124 = llrin[i*128+124];
		top->io_LLrin_125 = llrin[i*128+125];
		top->io_LLrin_126 = llrin[i*128+126];
		top->io_LLrin_127 = llrin[i*128+127];
		clockntimes(1);
		
	} 
	//top->io_Start = 0;
	//clockntimes(1);
	while(!top->io_OutValid) {
		clockntimes(1);
	//	i++;
	}
    //	printf("i:%d\n",i);
	bool flag = top->io_Success; 
	errorbit = top->io_errorbit;
	checknum = top->io_check;
//	if(flag) printf("success\n");
	//printf("Iter remain:%d\n",top->io_IterOut);
	clockntimes(1);
	return flag ;
	//Decoder退出了initial态 进入decode
	//printf("counter:%d\n",top->io_counter);
	
}

#ifdef DIFFTEST 
#include "difftest.h"
//更新除了appout以外的所有值
void valuefetch(int *dutmin1,int *dutmin2,int *dutsubmin1,int *dutsubmin2,
	int* dutminaddr1,int * dutsubminaddr1,int * dutminaddr2,int* dutsubminaddr2,
	bool* dutsign , bool * dutsign0,int * dutc2v,int* dutc2v0,bool dutv2c[2048][6]
){
	c2vfetch (dutc2v,dutc2v0)  ; 
	minfetch(dutmin1, dutmin2) ;
	subminfecth(dutsubmin1 ,dutsubmin1) ;
	minaddrfetch(dutminaddr1 ,dutminaddr2) ;
	subminaddrfetch(dutsubminaddr1 ,dutsubminaddr2) ;
	signfetch(dutsign,dutsign0) ; 
	v2csignfetch(dutv2c) ;
}
//比较除了appout,c2v以外的所有数值
bool valuediff(int *dutmin1,int *dutmin2,int *dutsubmin1,int *dutsubmin2,
	int* dutminaddr1,int * dutsubminaddr1,int * dutminaddr2,int* dutsubminaddr2,
	bool* dutsign , bool * dutsign0,//int * dutc2v,int* dutc2v0,
	bool dutv2c[2048][6],
	bool v2c[2048][6], 
	Message *min1, Message *submin1,int * min_addr1, int *submin_addr1, 
	Message *min2, Message *submin2,int * min_addr2, int *submin_addr2, 
	bool * sign , bool * sign0 //,Message * c2v,Message * c2v0 
){
	bool diff = 1;
	// min 
	diff = mindiff(min1,dutmin1);
	if(!diff) return false ;
	diff = mindiff(min2,dutmin2);
	if(!diff) return false ;
	//submin 
	diff = submindiff(submin1,dutsubmin1);
	if(!diff) return false ;
	diff = submindiff(submin2,dutsubmin2);
	if(!diff) return false ;
	//minaddr 
	diff = minaddrdiff(min_addr1,dutminaddr1);
	if(!diff) return false ;
	diff = minaddrdiff(min_addr2,dutminaddr2);
	if(!diff) return false ;
	diff = minaddrdiff(submin_addr1,dutsubminaddr1);
	if(!diff) return false ;
	diff = minaddrdiff(submin_addr2,dutsubminaddr2);
	if(!diff) return false ;
	diff = signdiff(sign,dutsign) ;
	if(!diff) return false ;
	diff = signdiff(sign0,dutsign0) ;
	if(!diff) return false ;
	diff = v2csigndiff(v2c,dutv2c) ;
	if(!diff) return false ;
	return diff;
}

bool cppdecodekernelFast2coldiff(vector<vector<int>> VofC,vector<vector<int>> CofV, Message * llrin, Message * appout , bool v2c[2048][6], 
		Message *min1, Message *submin1,int * min_addr1, int *submin_addr1, 
		Message *min2, Message *submin2,int * min_addr2, int *submin_addr2, 
		bool * sign , bool * sign0 ,Message * c2v,Message * c2v0 
		){
	// dut array 
	//int dutllrin[2048]; 
	int dutappout[2048];
	bool dutv2c[2048][6]; 
	int dutmin1 [384];int dutsubmin1 [384];
	int dutmin2 [384];int dutsubmin2 [384];
	int dutminaddr1[384];int dutsubminaddr1[384];
	int dutminaddr2[384];int dutsubminaddr2[384]; 
	bool dutsign[384] ;bool dutsign0[384] ;
	int dutc2v[384] ; int dutc2v0[384] ;
	//
	// Initialization   
	for(int i = 0; i < ROWNUMBER; i ++) {
		min1[i] = min2[i] = C2VMAX; 
		submin1[i] = submin2[i] = C2VMAX;
		min_addr1[i] = min_addr2[i] = 0 ;
		submin_addr1[i] = submin_addr2[i] = 0 ;
		sign[i]  = 0;
		sign0[i]  = 0;
	}
	for(int i = 0; i < COLNUMBER; i ++) {
		//update checknode min1 min2   
		for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
			int j = CofV[i][jindex];
			
			if(llrin[i] < 0) {
				if((i/BLKSIZE)%2==0){	
					sign[j] = sign[j]^1;
				}else {
					sign0[j] = sign0[j]^1;
				}
									
			}
			Message llr_abs = llrin[i] < 0 ? -llrin[i] : llrin[i];
			llr_abs = (llr_abs <= C2VMAX ? llr_abs : C2VMAX) ;
			v2c[i][j] = llrin[i] < 0;
			//update_min(min1[j],min2[j],min1_addr[j],llr_abs,i);
			//两列同时做  相当于偶数列存min1  奇数列存Min2  从0开始
			if((i/BLKSIZE)%2 == 0){
				update_min_vss(min1[j],submin1[j],min_addr1[j],submin_addr1[j],llr_abs,i/BLKSIZE);
			}else {
				update_min_vss(min2[j],submin2[j],min_addr2[j],submin_addr2[j],llr_abs,i/BLKSIZE);
			}
		}
		appout[i] = llrin[i];
	}
	//Decoder Initialization
	top->io_Start = 1;
	top->io_IterInput = ITERMAX ;
	//clockntimes(1);
	//top->io_Start = 0;
	//
	for(int i = 0; i < 16;i++) {
		top->io_LLrin_0 = llrin[i*128+0];
		//printf("llrin:%x appout:%x\n",top->io_LLrin_0,top->io_appout_0);
		top->io_LLrin_1 = llrin[i*128+1];
		top->io_LLrin_2 = llrin[i*128+2];
		top->io_LLrin_3 = llrin[i*128+3];
		top->io_LLrin_4 = llrin[i*128+4];
		top->io_LLrin_5 = llrin[i*128+5];
		top->io_LLrin_6 = llrin[i*128+6];
		top->io_LLrin_7 = llrin[i*128+7];
		top->io_LLrin_8 = llrin[i*128+8];
		top->io_LLrin_9 = llrin[i*128+9];
		top->io_LLrin_10 = llrin[i*128+10];
		top->io_LLrin_11 = llrin[i*128+11];
		top->io_LLrin_12 = llrin[i*128+12];
		top->io_LLrin_13 = llrin[i*128+13];
		top->io_LLrin_14 = llrin[i*128+14];
		top->io_LLrin_15 = llrin[i*128+15];
		top->io_LLrin_16 = llrin[i*128+16];
		top->io_LLrin_17 = llrin[i*128+17];
		top->io_LLrin_18 = llrin[i*128+18];
		top->io_LLrin_19 = llrin[i*128+19];
		top->io_LLrin_20 = llrin[i*128+20];
		top->io_LLrin_21 = llrin[i*128+21];
		top->io_LLrin_22 = llrin[i*128+22];
		top->io_LLrin_23 = llrin[i*128+23];
		top->io_LLrin_24 = llrin[i*128+24];
		top->io_LLrin_25 = llrin[i*128+25];
		top->io_LLrin_26 = llrin[i*128+26];
		top->io_LLrin_27 = llrin[i*128+27];
		top->io_LLrin_28 = llrin[i*128+28];
		top->io_LLrin_29 = llrin[i*128+29];
		top->io_LLrin_30 = llrin[i*128+30];
		top->io_LLrin_31 = llrin[i*128+31];
		top->io_LLrin_32 = llrin[i*128+32];
		top->io_LLrin_33 = llrin[i*128+33];
		top->io_LLrin_34 = llrin[i*128+34];
		top->io_LLrin_35 = llrin[i*128+35];
		top->io_LLrin_36 = llrin[i*128+36];
		top->io_LLrin_37 = llrin[i*128+37];
		top->io_LLrin_38 = llrin[i*128+38];
		top->io_LLrin_39 = llrin[i*128+39];
		top->io_LLrin_40 = llrin[i*128+40];
		top->io_LLrin_41 = llrin[i*128+41];
		top->io_LLrin_42 = llrin[i*128+42];
		top->io_LLrin_43 = llrin[i*128+43];
		top->io_LLrin_44 = llrin[i*128+44];
		top->io_LLrin_45 = llrin[i*128+45];
		top->io_LLrin_46 = llrin[i*128+46];
		top->io_LLrin_47 = llrin[i*128+47];
		top->io_LLrin_48 = llrin[i*128+48];
		top->io_LLrin_49 = llrin[i*128+49];
		top->io_LLrin_50 = llrin[i*128+50];
		top->io_LLrin_51 = llrin[i*128+51];
		top->io_LLrin_52 = llrin[i*128+52];
		top->io_LLrin_53 = llrin[i*128+53];
		top->io_LLrin_54 = llrin[i*128+54];
		top->io_LLrin_55 = llrin[i*128+55];
		top->io_LLrin_56 = llrin[i*128+56];
		top->io_LLrin_57 = llrin[i*128+57];
		top->io_LLrin_58 = llrin[i*128+58];
		top->io_LLrin_59 = llrin[i*128+59];
		top->io_LLrin_60 = llrin[i*128+60];
		top->io_LLrin_61 = llrin[i*128+61];
		top->io_LLrin_62 = llrin[i*128+62];
		top->io_LLrin_63 = llrin[i*128+63];
		top->io_LLrin_64 = llrin[i*128+64];
		top->io_LLrin_65 = llrin[i*128+65];
		top->io_LLrin_66 = llrin[i*128+66];
		top->io_LLrin_67 = llrin[i*128+67];
		top->io_LLrin_68 = llrin[i*128+68];
		top->io_LLrin_69 = llrin[i*128+69];
		top->io_LLrin_70 = llrin[i*128+70];
		top->io_LLrin_71 = llrin[i*128+71];
		top->io_LLrin_72 = llrin[i*128+72];
		top->io_LLrin_73 = llrin[i*128+73];
		top->io_LLrin_74 = llrin[i*128+74];
		top->io_LLrin_75 = llrin[i*128+75];
		top->io_LLrin_76 = llrin[i*128+76];
		top->io_LLrin_77 = llrin[i*128+77];
		top->io_LLrin_78 = llrin[i*128+78];
		top->io_LLrin_79 = llrin[i*128+79];
		top->io_LLrin_80 = llrin[i*128+80];
		top->io_LLrin_81 = llrin[i*128+81];
		top->io_LLrin_82 = llrin[i*128+82];
		top->io_LLrin_83 = llrin[i*128+83];
		top->io_LLrin_84 = llrin[i*128+84];
		top->io_LLrin_85 = llrin[i*128+85];
		top->io_LLrin_86 = llrin[i*128+86];
		top->io_LLrin_87 = llrin[i*128+87];
		top->io_LLrin_88 = llrin[i*128+88];
		top->io_LLrin_89 = llrin[i*128+89];
		top->io_LLrin_90 = llrin[i*128+90];
		top->io_LLrin_91 = llrin[i*128+91];
		top->io_LLrin_92 = llrin[i*128+92];
		top->io_LLrin_93 = llrin[i*128+93];
		top->io_LLrin_94 = llrin[i*128+94];
		top->io_LLrin_95 = llrin[i*128+95];
		top->io_LLrin_96 = llrin[i*128+96];
		top->io_LLrin_97 = llrin[i*128+97];
		top->io_LLrin_98 = llrin[i*128+98];
		top->io_LLrin_99 = llrin[i*128+99];
		top->io_LLrin_100 = llrin[i*128+100];
		top->io_LLrin_101 = llrin[i*128+101];
		top->io_LLrin_102 = llrin[i*128+102];
		top->io_LLrin_103 = llrin[i*128+103];
		top->io_LLrin_104 = llrin[i*128+104];
		top->io_LLrin_105 = llrin[i*128+105];
		top->io_LLrin_106 = llrin[i*128+106];
		top->io_LLrin_107 = llrin[i*128+107];
		top->io_LLrin_108 = llrin[i*128+108];
		top->io_LLrin_109 = llrin[i*128+109];
		top->io_LLrin_110 = llrin[i*128+110];
		top->io_LLrin_111 = llrin[i*128+111];
		top->io_LLrin_112 = llrin[i*128+112];
		top->io_LLrin_113 = llrin[i*128+113];
		top->io_LLrin_114 = llrin[i*128+114];
		top->io_LLrin_115 = llrin[i*128+115];
		top->io_LLrin_116 = llrin[i*128+116];
		top->io_LLrin_117 = llrin[i*128+117];
		top->io_LLrin_118 = llrin[i*128+118];
		top->io_LLrin_119 = llrin[i*128+119];
		top->io_LLrin_120 = llrin[i*128+120];
		top->io_LLrin_121 = llrin[i*128+121];
		top->io_LLrin_122 = llrin[i*128+122];
		top->io_LLrin_123 = llrin[i*128+123];
		top->io_LLrin_124 = llrin[i*128+124];
		top->io_LLrin_125 = llrin[i*128+125];
		top->io_LLrin_126 = llrin[i*128+126];
		top->io_LLrin_127 = llrin[i*128+127];
		//AppoutFetch(dutappout,i);
		//if(i == 15){
		//	bool diff = AppOutdiff(appout,dutappout,0);
		//	if(diff){
		//		Log("**********App Initial Diff Pass*********\n");
		//	}else {
		//		Log("**********App Initial Diff Fail*********\n");
		//		exit(0);
		//	}
		//}
		clockntimes(1);
		//printf("I:%d counter:%d\n",i,top->io_counter);
		AppoutFetch(dutappout,i);
	} 
	top->io_Start = 0;
	clockntimes(1);
	//Decoder退出了initial态 进入decode
	//printf("counter:%d\n",top->io_counter);
	bool diff = AppOutdiff(appout,dutappout,0);
	if(diff){
		Log("**********App Initial Diff Pass*********");
	}else {
		Log("**********App Initial Diff Fail*********");
		exit(0);
	}
	//printf("counter:%d\n",top->io_counter);
	//此时decoder 已经跳入decode态 初始化完成
	//更新各个信号的数值，并进行比较
	valuefetch(dutmin1,dutmin2,dutsubmin1,dutsubmin2,
	dutminaddr1,dutsubminaddr1,dutminaddr2,dutsubminaddr2,
	dutsign ,dutsign0,dutc2v,dutc2v0,dutv2c);
	diff = valuediff(dutmin1,dutmin2,dutsubmin1,dutsubmin2,
	dutminaddr1,dutsubminaddr1,dutminaddr2, dutsubminaddr2,
	dutsign ,dutsign0,dutv2c, v2c, 
	min1,submin1,min_addr1,submin_addr1, 
	min2,submin2,min_addr2,submin_addr2, 
	sign , sign0 );
	if(diff){
		Log("**********Initial Diff Pass*********");
	}else {
		Log("**********Initial Diff Fail*********");
		sim_exit();
		exit(0);
	}
	//Iteration 
	bool success = 1; 
	for(int iter = 0 ; iter < ITERMAX; iter++) {
		
		for (int iindex = 0; iindex < 16; iindex ++) {
			//CNU 
			for (int id = 0 ; id < 128 ; id++ ){
				int i = iindex*128+id;
				Message appval = llrin[i];
				for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
					int j = CofV[i][jindex];
					Message minval ;Message subminval;
					int minaddr;int subminaddr;

					choose_true_min(min1[j],min2[j],submin1[j],submin2[j],
					min_addr1 [j],min_addr2[j] ,submin_addr1[j] ,submin_addr2[j] ,
					minval ,subminval,
						minaddr,subminaddr );
					//printf("minval:%d minaddr:%d\n",minval,minaddr);
					Message c2v_val = ((i/BLKSIZE == minaddr) ? subminval : minval);

					c2v_val = c2v_val * alpha;

					if((i/BLKSIZE)%2==0){	
						if(sign[j] != v2c[i][j]){
							c2v_val = -c2v_val;	
						}	
						c2v[j] = c2v_val;
					}else {
						if(sign0[j] != v2c[i][j]){
							c2v_val = -c2v_val;	
						}	
						c2v0[j] = c2v_val;
					}

					appval += c2v_val;
				}
				appout[i] = appval;
			}
			//此时做完了两列 更新一下C2V diff一下  
			c2vfetch(dutc2v,dutc2v0);
			bool localdiff1 = 1;
			bool localdiff2 = 1;
			localdiff1 = c2vdiff(c2v,dutc2v);
			localdiff2 = c2vdiff(c2v0,dutc2v0);
			if((localdiff1)&&(localdiff2)){
				Log("**********C2V Diff Pass*********");
			}else {
				Log("**********C2V Diff Fail*********");
				exit(0);
			}
			//VNU   update v2c 
			for (int id = 0 ; id < 128 ; id++ ){
				int i = iindex*128+id;
				Message appval = appout[i];
				for( int jindex = 0 ; jindex < ROWNUMBER/BLKSIZE ; jindex ++) {
					
					int j = CofV[i][jindex];
					Message v2c_abs;
					if((i/BLKSIZE)%2==0){
						v2c_abs = appval - c2v[j];
					}else{
						v2c_abs = appval - c2v0[j];
					}
					int v2csign = 0 ; 
					if(v2c_abs < 0 ) {
						v2c_abs = -v2c_abs ;
						v2csign = 1; 
					}

					v2c_abs = v2c_abs <= C2VMAX ? v2c_abs : C2VMAX ;

					if((i/BLKSIZE)%2==0){	
						if(v2csign != v2c[i][j]) {
							sign[j] = sign[j] ^ 1; 
						}
					}else {
						if(v2csign != v2c[i][j]) {
							sign0[j] = sign0[j] ^ 1; 
						}	
					}
					v2c[i][j] = v2csign ;
					if((i/BLKSIZE)%2 == 0){
						update_min_vss(min1[j],submin1[j],min_addr1[j],submin_addr1[j],v2c_abs,i/BLKSIZE);
					}else {
						update_min_vss(min2[j],submin2[j],min_addr2[j],submin_addr2[j],v2c_abs,i/BLKSIZE);
					} 

				}
			}
			//此时min1 min2 更新 
			//Decoder进下一个周期  min1 min2的值也更新
			//保留本周期的appout输出
			AppoutFetch(dutappout,iindex);
			clockntimes(1);
			//diff一下
			valuefetch(dutmin1,dutmin2,dutsubmin1,dutsubmin2,
			dutminaddr1,dutsubminaddr1,dutminaddr2,dutsubminaddr2,
			dutsign ,dutsign0,dutc2v,dutc2v0,dutv2c);
			bool difflocal = valuediff(dutmin1,dutmin2,dutsubmin1,dutsubmin2,
			dutminaddr1,dutsubminaddr1,dutminaddr2, dutsubminaddr2,
			dutsign ,dutsign0,dutv2c, v2c, 
			min1,submin1,min_addr1,submin_addr1, 
			min2,submin2,min_addr2,submin_addr2, 
			sign , sign0 );
			if(difflocal){
				Log("**********Decode Diff Pass*********");
			}else {
				Log("**********Decode Diff Fail*********");
				exit(0);
			}
		}
		//检验appout
		bool difflocal = AppOutdiff(appout,dutappout,1);
		if(difflocal){
			Log("**********APP Decode Diff Pass*********");
		}else {
			Log("**********APP Decode Diff Fail*********");
			exit(0);
		}
		success = 1; 
		for (int i = 0 ; i < COLNUMBER ; i ++) {
			if(appout[i] < 0 ){
				success = 0 ;
				break;
			}
		}
		if(success != top->io_Success) {
			Log("success diff ref %d dut %d",success,top->io_Success);
			exit(0);
		}
		clockntimes(1);
		if(success) {
			return success ; 
		}
	}
	return success ;
}

void cppDiffDecoderTest(){

	//int H[ROWNUMBER][COLNUMBER];
	//这里直接定义这个大矩阵会超内存
	//导致C程序错误退出  
	//用vector动态分配
	vector<vector<int>> H;
	H.resize(ROWNUMBER);
	for (int i = 0 ; i < ROWNUMBER; i ++ ){
		H[i].resize(COLNUMBER);
	}
	ifstream inr(BASE_MATRIX_FILENAME);
	if(!inr) {
		Log("No file %s",BASE_MATRIX_FILENAME) ;
		return ; 
	}
	string r ;
	for(int i = 0 ; i < ROWNUMBER; i ++) {
		string number ; 
		getline(inr,r);
		istringstream stream(r);
		for (int j = 0 ; j < COLNUMBER ; j ++) {
			stream >> number ; 
			H[i][j] = stoi(number);
		}
	}

	vector<vector<int>> VofC ;
	VofC.resize(ROWNUMBER);
	for(int i = 0; i < ROWNUMBER; i++){
		VofC[i].resize(COLNUMBER/BLKSIZE);
		for(int j = 0 ; j < COLNUMBER; j ++) {
			if(H[i][j]==1) {
				VofC[i][j/BLKSIZE] = j ;
			}
		}

	}
	vector<vector<int>> CofV ;
	CofV.resize(COLNUMBER);
	for(int i = 0; i < COLNUMBER; i++){
		CofV[i].resize(ROWNUMBER/BLKSIZE);
		for(int j = 0 ; j < ROWNUMBER; j ++) {
			if(H[j][i]==1) {
				CofV[i][j/BLKSIZE] = j ;
			}
		}

	}
	//printf("aaa\n");
	for (double sigma = sigmastart; sigma >= sigmaend ; sigma = sigma-sigmastep ){
		int frame = 0 ;
		int errorframe = 0; 
		//Log("hhhh");
		while((frame < maxtime) || (errorframe < maxerrortime)) {
			Message llrin[COLNUMBER]; 
			Message appout[COLNUMBER];
			//bool v2c[COLNUMBER][6]; 

			bool v2c[2048][6];
			Message c2v[ROWNUMBER] ;

			Message min1 [ROWNUMBER];
			Message min2 [ROWNUMBER]; 
			bool sign[ROWNUMBER] ;
			Message submin1 [ROWNUMBER];
			Message submin2 [ROWNUMBER];
			int min_addr1[ROWNUMBER];
			int min_addr2[ROWNUMBER]; 
			int submin_addr1[ROWNUMBER];
			int submin_addr2[ROWNUMBER];
			bool sign0[ROWNUMBER] ;
			Message c2v0[ROWNUMBER] ;
			//Log("hhhh");
			for (int i = 0 ; i < COLNUMBER;i++ ){
				llrin[i] = LLrInitial( (sigma)); 
//#if TYPE
				llrin[i] = llrin[i] <= -APPMAX ? -APPMAX : llrin[i] >= APPMAX ? APPMAX : llrin[i];
//#endif
			}
			bool success = cppdecodekernelFast2coldiff(VofC, CofV,  llrin, appout ,v2c, 
		 min1, submin1,min_addr1, submin_addr1, 
		 min2, submin2,min_addr2, submin_addr2, 
				sign ,sign0, c2v,c2v0 
		);
			//printf("aaa\n");
			if(!success) {
				errorframe++;
				//Log("fail frame %d",errorframe);
			}
			//else Log("success frame%d",frame);
			frame++;
		}
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)COLNUMBER;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
	}

	//Log("if there is no other output, it means the module works right");
}

#endif  
#elif TESTMODULE == 8 
void toptest(){
	uint64_t p_table[11][5] = {
    {0x00F6DE59DA609D40, 0x03D8972667FFD320, 0x08D62EDC6F866400, 0x123A4AC0DA34DA00, 0x2AE15EECABCB0000},
    {0x00D4C6F967D95498, 0x0378FF6FC977A6E0, 0x0832B202D6961F80, 0x114CE6ADF916BC00, 0x29BE9DB8C1EDA000},
    {0x00B5EF2F3C2BA5D8, 0x031E5FFD9697D1E0, 0x07937AFFCE63FD00, 0x106088159C106300, 0x28963B58EF860200},
    {0x009A3B0E7A344CD8, 0x02C8D9C2F5A5AEE0, 0x06F8E2BFE25A4640, 0x0F75935D7D441100, 0x2768358ADCF0CC00},
    {0x00818A4B91FA10C8, 0x027888440213C440, 0x066342AD1E391000, 0x0E8C74A939941900, 0x26348F382605D600},
    {0x006BB86D7E965E34, 0x022D80CE2913B4C0, 0x05D2F3E0B2761640, 0x0DA5A00395E54C00, 0x24FB51500C437400},
    {0x00589D178E071C04, 0x01E7D1AFAB37FF30, 0x05484E31CBDD5180, 0x0CC191717C245180, 0x23BC8BBDB73F7400},
    {0x00480C6D08D793E0, 0x01A781736CABADE0, 0x04C3A71F8E22AA40, 0x0BE0CCE87A553580, 0x2278567E19FCAC00},
    {0x0039D78F447547B6, 0x016C8E27681DABA0, 0x04455094D8222CC0, 0x0B03DE21828A1900, 0x212ED2D8B793B400},
    {0x002DCD35A4BF3130, 0x0136ECB54E6E5210, 0x03CD9785821A3AE0, 0x0A2B583F8CB28D80, 0x1FE02CBE92C1AA00},
	{0x0023BA5DD079D4D0, 0x010688560D870250, 0x035CC266342E88C0, 0x0957D54093BBD180, 0x1E8C9C528874BA00},

};
	double  rbertable[11] = {
    0.034520,0.032020,0.029600,0.027240,0.024960,0.022760,0.020640,0.018620,0.016680,0.014860,
0.013140};
	double  sigmatable[11] = {
    0.55,0.54,0.53,0.52,0.51,0.50,0.49,0.48,0.47,0.46,0.45
  };
	for (int i = 0; i < 11 ; i ++) {
		double sigma = sigmatable[i];
		top->io_IterInput = ITERMAX;
		top->io_maxError  = maxerrortime; 
		top->io_p0_0 = p_table[i][4];
		top->io_p0_1 = p_table[i][3];
		top->io_p0_2 = p_table[i][2];
		top->io_p0_3 = p_table[i][1];
		top->io_p0_4 = p_table[i][0];
		top->io_start = 1;
		top->io_nextready = 0;
		clockntimes(1); 
		top->io_start = 0;
		while(!top->io_Framevalid) {
			clockntimes(1); 
		}
		int frame =(int) top->io_totalframe ;
		int errorframe = (int )top->io_errorframe ; 
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)2048;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
		
		clockntimes(10);
		top->io_nextready = 1; 
		clockntimes(10);
		top->io_nextready = 0;
	}
  
}
#elif TESTMODULE == 9 
void toptest(){
	uint64_t p_table[11][5] = {
    {0x00F6DE59DA609D40, 0x03D8972667FFD320, 0x08D62EDC6F866400, 0x123A4AC0DA34DA00, 0x2AE15EECABCB0000},
    {0x00D4C6F967D95498, 0x0378FF6FC977A6E0, 0x0832B202D6961F80, 0x114CE6ADF916BC00, 0x29BE9DB8C1EDA000},
    {0x00B5EF2F3C2BA5D8, 0x031E5FFD9697D1E0, 0x07937AFFCE63FD00, 0x106088159C106300, 0x28963B58EF860200},
    {0x009A3B0E7A344CD8, 0x02C8D9C2F5A5AEE0, 0x06F8E2BFE25A4640, 0x0F75935D7D441100, 0x2768358ADCF0CC00},
    {0x00818A4B91FA10C8, 0x027888440213C440, 0x066342AD1E391000, 0x0E8C74A939941900, 0x26348F382605D600},
    {0x006BB86D7E965E34, 0x022D80CE2913B4C0, 0x05D2F3E0B2761640, 0x0DA5A00395E54C00, 0x24FB51500C437400},
    {0x00589D178E071C04, 0x01E7D1AFAB37FF30, 0x05484E31CBDD5180, 0x0CC191717C245180, 0x23BC8BBDB73F7400},
    {0x00480C6D08D793E0, 0x01A781736CABADE0, 0x04C3A71F8E22AA40, 0x0BE0CCE87A553580, 0x2278567E19FCAC00},
    {0x0039D78F447547B6, 0x016C8E27681DABA0, 0x04455094D8222CC0, 0x0B03DE21828A1900, 0x212ED2D8B793B400},
    {0x002DCD35A4BF3130, 0x0136ECB54E6E5210, 0x03CD9785821A3AE0, 0x0A2B583F8CB28D80, 0x1FE02CBE92C1AA00},
	{0x0023BA5DD079D4D0, 0x010688560D870250, 0x035CC266342E88C0, 0x0957D54093BBD180, 0x1E8C9C528874BA00},

};
	double  rbertable[11] = {
    0.034520,0.032020,0.029600,0.027240,0.024960,0.022760,0.020640,0.018620,0.016680,0.014860,
0.013140};
	double  sigmatable[11] = {
    0.55,0.54,0.53,0.52,0.51,0.50,0.49,0.48,0.47,0.46,0.45
  };
	for (int i = 0; i < 11 ; i ++) {
		double sigma = sigmatable[i];
		top->io_IterInput = ITERMAX;
		top->io_maxError  = maxerrortime; 
		top->io_p0_0 = p_table[i][4];
		top->io_p0_1 = p_table[i][3];
		top->io_p0_2 = p_table[i][2];
		top->io_p0_3 = p_table[i][1];
		top->io_p0_4 = p_table[i][0];
		top->io_start = 1;
		top->io_nextready = 0;
		clockntimes(1); 
		top->io_start = 0;
		while(top->io_errorframe < maxerrortime) {
			clockntimes(1);
		//	printf("error:%d\n",top->io_errorframe);
		}
		int frame =(int) top->io_totalframe ;
		int errorframe = (int )top->io_errorframe ; 
		double fer = (double) errorframe / (double) frame;
		double rate = (double)1723/(double)2048;
		double snr  = 10*log10(1.0/(2.0*rate*sigma*sigma));
		Log("snr:%f sigma:%f errorframe:%d frame:%d Fer:%f",snr,sigma,errorframe,frame,fer);
		reset(10);
		top->io_start = 0;
		clockntimes(10);
		//top->io_nextready = 1; 
		//clockntimes(10);
		//top->io_nextready = 0;
	}
  
}
#elif TESTMODULE == 10 
void IOupdate(int* appin , int * errorblk,int* errorcol){
	top->io_appin_0= appin[0];
	top->io_appin_1= appin[1];
	top->io_appin_2= appin[2];
	top->io_appin_3= appin[3];
	top->io_appin_4= appin[4];
	top->io_appin_5= appin[5];
	top->io_appin_6= appin[6];
	top->io_appin_7= appin[7];
	top->io_appin_8= appin[8];
	top->io_appin_9= appin[9];
	top->io_appin_10= appin[10];
	top->io_appin_11= appin[11];
	top->io_appin_12= appin[12];
	top->io_appin_13= appin[13];
	top->io_appin_14= appin[14];
	top->io_appin_15= appin[15];
	top->io_appin_16= appin[16];
	top->io_appin_17= appin[17];
	top->io_appin_18= appin[18];
	top->io_appin_19= appin[19];
	top->io_appin_20= appin[20];
	top->io_appin_21= appin[21];
	top->io_appin_22= appin[22];
	top->io_appin_23= appin[23];
	top->io_appin_24= appin[24];
	top->io_appin_25= appin[25];
	top->io_appin_26= appin[26];
	top->io_appin_27= appin[27];
	top->io_appin_28= appin[28];
	top->io_appin_29= appin[29];
	top->io_appin_30= appin[30];
	top->io_appin_31= appin[31];
	top->io_appin_32= appin[32];
	top->io_appin_33= appin[33];
	top->io_appin_34= appin[34];
	top->io_appin_35= appin[35];
	top->io_appin_36= appin[36];
	top->io_appin_37= appin[37];
	top->io_appin_38= appin[38];
	top->io_appin_39= appin[39];
	top->io_appin_40= appin[40];
	top->io_appin_41= appin[41];
	top->io_appin_42= appin[42];
	top->io_appin_43= appin[43];
	top->io_appin_44= appin[44];
	top->io_appin_45= appin[45];
	top->io_appin_46= appin[46];
	top->io_appin_47= appin[47];
	top->io_appin_48= appin[48];
	top->io_appin_49= appin[49];
	top->io_appin_50= appin[50];
	top->io_appin_51= appin[51];
	top->io_appin_52= appin[52];
	top->io_appin_53= appin[53];
	top->io_appin_54= appin[54];
	top->io_appin_55= appin[55];
	top->io_appin_56= appin[56];
	top->io_appin_57= appin[57];
	top->io_appin_58= appin[58];
	top->io_appin_59= appin[59];
	top->io_appin_60= appin[60];
	top->io_appin_61= appin[61];
	top->io_appin_62= appin[62];
	top->io_appin_63= appin[63];
	errorcol[0] = top->io_Errorcol_0;
	errorblk[0] = top->io_Errorblk_0;
	errorcol[1] = top->io_Errorcol_1;
	errorblk[1] = top->io_Errorblk_1;
	errorcol[2] = top->io_Errorcol_2;
	errorblk[2] = top->io_Errorblk_2;
	errorcol[3] = top->io_Errorcol_3;
	errorblk[3] = top->io_Errorblk_3;
	errorcol[4] = top->io_Errorcol_4;
	errorblk[4] = top->io_Errorblk_4;
	errorcol[5] = top->io_Errorcol_5;
	errorblk[5] = top->io_Errorblk_5;
	errorcol[6] = top->io_Errorcol_6;
	errorblk[6] = top->io_Errorblk_6;
	errorcol[7] = top->io_Errorcol_7;
	errorblk[7] = top->io_Errorblk_7;
	errorcol[8] = top->io_Errorcol_8;
	errorblk[8] = top->io_Errorblk_8;
	errorcol[9] = top->io_Errorcol_9;
	errorblk[9] = top->io_Errorblk_9;
}
void toptest(){
	int appin[64];
	int errorblk[10];
	int errorcol[10];
	//printf("aaaaa\n");
	for(int i = 0 ; i < 64;i++) {
		appin[i] = 0;  
	}
	for(int i = 0 ; i < 10 ; i++){
		errorblk[i] =0;
		errorcol[i] = 0;
	}
	int time = 0 ;
	int testtime = 20; 
	//printf("jjjjj\n");
	while(time < testtime) {
		time ++ ;
		for(int i = 0; i < 32;i++) {
			top->io_coladdr = i ;
			top->io_appvalid =   1 ;
			if(i>=14)appin[i+10] = 1;
			IOupdate(appin,errorblk,errorcol);
			clockntimes(1);
			appin[i+10] = 0 ; 
		}
		while(!top->io_outvalid){
			clockntimes(1);
		}
		IOupdate(appin,errorblk,errorcol);
		printf("errorbitlocation\n");
		for(int i = 0; i < 10 ;i ++ ){
			printf("ind:%d errorcol:%d errorblk:%d\n",i,errorcol[i],errorblk[i]);
		}
		for(int i = 0;i < 64;i++) {
			appin[i] = 0 ;
		}
		top->io_appvalid = 0;
		top->io_updatevalid = 1;
		clockntimes(1);
		top->io_updatevalid = 0;
	}
}





#endif 

#endif
