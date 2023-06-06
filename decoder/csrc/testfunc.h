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
		top->io_LLrin_0 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_1 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_2 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_3 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_4 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_5 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_6 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_7 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_8 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_9 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_10 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_11 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_12 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_13 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_14 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_15 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_16 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_17 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_18 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_19 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_20 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_21 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_22 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_23 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_24 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_25 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_26 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_27 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_28 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_29 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_30 = LLrInitial(RandomGen(sigma));
		top->io_LLrin_31 = LLrInitial(RandomGen(sigma));
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
	//Log("if there is no other output, it means the module works right");
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
			llrin[i*32 + j ] = LLrInitial(RandomGen(sigma)); 
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
#endif 

#endif
