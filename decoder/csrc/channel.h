#ifndef CHANNEL_H
#define CHANNEL_H  
/*************************************************************************
	> File Name: channel.h
	> Author: yangli
	> Mail: 577647772@qq.com 
	> Created Time: 2023年05月21日 星期日 14时26分51秒
 ************************************************************************/
#include "common.h"
#include<iostream>
using namespace std;

static default_random_engine channel(static_cast<unsigned>(time(NULL)));
double RandomGen(double sigma)
{
  normal_distribution<double> nd(0, sigma);
  return nd(channel);
}
/************Yn Initial Table*************/
int YnInitial(double Noise)
{
  //all zero code
  double llr_init = 1.0 + Noise;
#if LLR_INIT_TABLE == 1
  if (llr_init < 0)
    return -1;
  else
    return 1;
#elif LLR_INIT_TABLE == 2
  if(llr_init < -0.3144384671){
    return -4;
  }else if (llr_init < 0){
    return -1;
  }else if (llr_init < 0.3144384671){
    return 1;
  }else{
    return 4;
  }
#endif
}


#endif 
