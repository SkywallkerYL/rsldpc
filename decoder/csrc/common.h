#ifndef COMMON
#define COMMON


#include <stdint.h>
#include <stdbool.h>
#include <assert.h>
#include <string.h>
#include <time.h>
#include <random>
#include <math.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include "verilated_vcd_c.h"

#define LLR_INIT_TABLE 5


#define TESTMODULE 11
#if TESTMODULE == 1 
// CheckNode Test 
#include "VCheckNode.h"
#include "VCheckNode___024root.h"
//#include "VCheckNode__Dpi.h"
VCheckNode* top;
#elif TESTMODULE == 2
#include "VVariableNode.h"
#include "VVariableNode___024root.h"
//#include "VCheckNode__Dpi.h"
VVariableNode* top;
#elif TESTMODULE == 3
#include "VProcessingUnit.h"
#include "VProcessingUnit___024root.h"
VProcessingUnit* top;
#elif TESTMODULE == 4
#include "VDecoder.h"
#include "VDecoder___024root.h"
VDecoder* top;
double sigmastart = 0.55;
double sigmaend   = 0.35;
double sigmastep  = 0.01; 
int maxtime			= 1000;
int maxerrortime	= 50 ;
int ITERMAX		  = 20 ;
#elif TESTMODULE == 5 
#include "VGngWrapper.h"
#include "VGngWrapper___024root.h"
VGngWrapper* top;
#elif TESTMODULE == 6
#include "VTopdecoder.h"
#include "VTopdecoder___024root.h"
VTopdecoder* top;
int maxerrortime = 50 ;
int ITERMAX      = 6 ;
#elif TESTMODULE == 7
#include "VDecoder2Col.h"
#include "VDecoder2Col___024root.h"
VDecoder2Col* top;
double sigmastart = 0.43;
double sigmaend   = 0.35;
double sigmastep  = 0.01; 
int maxtime			= 10000;
int maxerrortime	= 50 ;
int ITERMAX		  = 5;
int APPWIDTH = 7 ;
int V2CWIDTH = 4; 
int C2VWIDTH = 4 ; 
int APPMAX = pow(2,APPWIDTH-1)-1;
int V2CMAX = pow(2,V2CWIDTH-1)-1;
int C2VMAX = pow(2,C2VWIDTH-1)-1;
double alpha = 0.75;
int COLNUMBER = 2048;
int ROWNUMBER = 384;
int BLKSIZE   = 64;
#define Message int
const char BASE_MATRIX_FILENAME[] = "/home/yangli/rsldpc/matrix/2048_1723.txt";
#elif TESTMODULE == 8
#include "Vrsdecoder.h"
#include "Vrsdecoder___024root.h"
Vrsdecoder* top;
int maxerrortime = 50 ;
int ITERMAX      = 20 ;
#elif TESTMODULE == 9
#include "Vrsdecodertop.h"
#include "Vrsdecodertop___024root.h"
Vrsdecodertop* top;
int maxerrortime = 50 ;
int ITERMAX      = 6 ;
#elif TESTMODULE == 10
#include "VErrorbits.h"
#include "VErrorbits___024root.h"
VErrorbits* top;
int maxerrortime = 50 ;
int ITERMAX      = 20 ;
#elif TESTMODULE == 11
#include "VrsdecodertopGauss.h"
#include "VrsdecodertopGauss___024root.h"
VrsdecodertopGauss* top;
int maxerrortime = 50 ;
int ITERMAX      = 5 ;
#elif TESTMODULE == 12
#include "Vrsdecoder2colGauss.h"
#include "Vrsdecoder2colGauss___024root.h"
Vrsdecoder2colGauss* top;
int maxerrortime = 50;
int ITERMAX      = 5 ;
#endif
//#define DIFFTEST 


#define POSTPROCESS 1
int strongMessage_0 = 5;
int weakMessage_0 = 1;
int strongMessage_1 = 5;
int weakMessage_1 = 1;
int strongMessage_2 = 5;
int weakMessage_2 = 1;
int postInter = 12;
const char WRONG_FILENAME[] = "/home/yangli/rsldpc/decoder/csrc/error_llr_base.txt";
#define READLLR 1
//#define WAVE 
#define WAVE_BEGIN 0 
#define WAVE_END   10000

#define TRACE_CONDITION(a,begin,end) ((a>=begin)&&(a<end))
int wavecount = 0 ;


const double boundy1 = 0.126591786710688;
const double boundy2 = 0.235418474661616;
const double boundy3 = 0.346304112688928;
const double boundy4 = 0.452660297000054;
const double boundy5 = 0.562194230398888;
const double boundy6 = 0.696028500882122;
const double boundy7 = 0.872153396888965;
const int LLR_INIT4bit1 = 1;//1;
const int LLR_INIT4bit2 = 3;//2;
const int LLR_INIT4bit3 = 5;//3;//4 5
const int LLR_INIT4bit4 = 6;//4;//8 9
const int LLR_INIT4bit5 = 8;//5;
const int LLR_INIT4bit55= 10;//5;  
const int LLR_INIT4bit6 = 12;//7;
const int LLR_INIT4bit7 = 15;//9;//4 5


# define __PRI64_PREFIX	"l"
# define PRIx8		"x"
# define PRIx16		"x"
# define PRIx32		"x"
# define PRIx64		__PRI64_PREFIX "x"
#define FMT_WORD    "0x%016lx"

#define ANSI_FG_BLACK   "\33[1;30m"
#define ANSI_FG_RED     "\33[1;31m"
#define ANSI_FG_GREEN   "\33[1;32m"
#define ANSI_FG_YELLOW  "\33[1;33m"
#define ANSI_FG_BLUE    "\33[1;34m"
#define ANSI_FG_MAGENTA "\33[1;35m"
#define ANSI_FG_CYAN    "\33[1;36m"
#define ANSI_FG_WHITE   "\33[1;37m"
#define ANSI_BG_BLACK   "\33[1;40m"
#define ANSI_BG_RED     "\33[1;41m"
#define ANSI_BG_GREEN   "\33[1;42m"
#define ANSI_BG_YELLOW  "\33[1;43m"
#define ANSI_BG_BLUE    "\33[1;44m"
#define ANSI_BG_MAGENTA "\33[1;35m"
#define ANSI_BG_CYAN    "\33[1;46m"
#define ANSI_BG_WHITE   "\33[1;47m"
#define ANSI_NONE       "\33[0m"

#define ANSI_FMT(str, fmt) fmt str ANSI_NONE

#include <string.h>

//// macro stringizing
//#define str_temp(x) #x
//#define str(x) str_temp(x)

// strlen() for string constant
#define STRLEN(CONST_STR) (sizeof(CONST_STR) - 1)

// calculate the length of an array
#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))

// macro concatenation
#define concat_temp(x, y) x ## y
#define concat(x, y) concat_temp(x, y)
#define concat3(x, y, z) concat(concat(x, y), z)
#define concat4(x, y, z, w) concat3(concat(x, y), z, w)
#define concat5(x, y, z, v, w) concat4(concat(x, y), z, v, w)

// macro testing
// See https://stackoverflow.com/questions/26099745/test-if-preprocessor-symbol-is-defined-inside-macro
#define CHOOSE2nd(a, b, ...) b
#define MUX_WITH_COMMA(contain_comma, a, b) CHOOSE2nd(contain_comma a, b)
#define MUX_MACRO_PROPERTY(p, macro, a, b) MUX_WITH_COMMA(concat(p, macro), a, b)
// define placeholders for some property
#define __P_DEF_0  X,
#define __P_DEF_1  X,
#define __P_ONE_1  X,
#define __P_ZERO_0 X,
// define some selection functions based on the properties of BOOLEAN macro
#define MUXDEF(macro, X, Y)  MUX_MACRO_PROPERTY(__P_DEF_, macro, X, Y)
#define MUXNDEF(macro, X, Y) MUX_MACRO_PROPERTY(__P_DEF_, macro, Y, X)
#define MUXONE(macro, X, Y)  MUX_MACRO_PROPERTY(__P_ONE_, macro, X, Y)
#define MUXZERO(macro, X, Y) MUX_MACRO_PROPERTY(__P_ZERO_,macro, X, Y)

// test if a boolean macro is defined
#define ISDEF(macro) MUXDEF(macro, 1, 0)
// test if a boolean macro is undefined
#define ISNDEF(macro) MUXNDEF(macro, 1, 0)
// test if a boolean macro is defined to 1
#define ISONE(macro) MUXONE(macro, 1, 0)
// test if a boolean macro is defined to 0
#define ISZERO(macro) MUXZERO(macro, 1, 0)
// test if a macro of ANY type is defined
// NOTE1: it ONLY works inside a function, since it calls `strcmp()`
// NOTE2: macros defined to themselves (#define A A) will get wrong results
#define isdef(macro) (strcmp("" #macro, "" str(macro)) != 0)

// simplification for conditional compilation
#define __IGNORE(...)
#define __KEEP(...) __VA_ARGS__
// keep the code if a boolean macro is defined
#define IFDEF(macro, ...) MUXDEF(macro, __KEEP, __IGNORE)(__VA_ARGS__)
// keep the code if a boolean macro is undefined
#define IFNDEF(macro, ...) MUXNDEF(macro, __KEEP, __IGNORE)(__VA_ARGS__)
// keep the code if a boolean macro is defined to 1
#define IFONE(macro, ...) MUXONE(macro, __KEEP, __IGNORE)(__VA_ARGS__)
// keep the code if a boolean macro is defined to 0
#define IFZERO(macro, ...) MUXZERO(macro, __KEEP, __IGNORE)(__VA_ARGS__)

// functional-programming-like macro (X-macro)
// apply the function `f` to each element in the container `c`
// NOTE1: `c` should be defined as a list like:
//   f(a0) f(a1) f(a2) ...
// NOTE2: each element in the container can be a tuple
#define MAP(c, f) c(f)

#define BITMASK(bits) ((1ull << (bits)) - 1)
#define BITS(x, hi, lo) (((x) >> (lo)) & BITMASK((hi) - (lo) + 1)) // similar to x[hi:lo] in verilog
#define SEXT(x, len) ({ struct { int64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })
#define SEXTU(x, len) ({ struct { uint64_t n : len; } __x = { .n = x }; (uint64_t)__x.n; })

#define ROUNDUP(a, sz)   ((((uintptr_t)a) + (sz) - 1) & ~((sz) - 1))
#define ROUNDDOWN(a, sz) ((((uintptr_t)a)) & ~((sz) - 1))

#define PG_ALIGN __attribute((aligned(4096)))

#if !defined(likely)
#define likely(cond)   __builtin_expect(cond, 1)
#define unlikely(cond) __builtin_expect(cond, 0)
#endif

// for AM IOE
#define io_read(reg) \
  ({ reg##_T __io_param; \
    ioe_read(reg, &__io_param); \
    __io_param; })

#define io_write(reg, ...) \
  ({ reg##_T __io_param = (reg##_T) { __VA_ARGS__ }; \
    ioe_write(reg, &__io_param); })



#define Assert(cond, format, ...) \
  do { \
    if (!(cond)) { \
        (fflush(stdout), fprintf(stderr, ANSI_FMT(format, ANSI_FG_RED) "\n", ##  __VA_ARGS__)); \
           extern FILE* log_fp; fflush(log_fp); \
      extern void assert_fail_msg(); \
      assert_fail_msg(); \
      assert(cond); \
    } \
  } while (0)

#define panic(format, ...) Assert(0, format, ## __VA_ARGS__)

#define panic_on(cond, s) \
  ({ if (cond) { \
      putstr("AM Panic: "); putstr(s); \
      putstr(" @ " __FILE__ ":" TOSTRING(__LINE__) "  \n"); \
      halt(1); \
    } })
//#define panic(s) panic_on(1, s)
#define log_write(...) IFDEF(CONFIG_TARGET_NATIVE_ELF, \
  do { \
    extern FILE* log_fp; \
    extern bool log_enable(); \
    if (log_enable()) { \
      fprintf(log_fp, __VA_ARGS__); \
      fflush(log_fp); \
    } \
  } while (0) \
)

#define _Log(...) \
  do { \
    printf(__VA_ARGS__); \
    log_write(__VA_ARGS__); \
  } while (0)


#define Log(format, ...) \
    _Log(ANSI_FMT("[%s:%d %s] " format, ANSI_FG_BLUE) "\n", \
        __FILE__, __LINE__, __func__, ## __VA_ARGS__)
#define LogG(format, ...) \
    _Log(ANSI_FMT("[%s:%d %s] " format, ANSI_FG_GREEN) "\n", \
        __FILE__, __LINE__, __func__, ## __VA_ARGS__)



#endif
