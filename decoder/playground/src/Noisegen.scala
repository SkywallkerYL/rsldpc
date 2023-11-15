package decoder 
import chisel3._
import chisel3.util._
case class RandSeedParams(
   z1_SEED : BigInt = BigInt("5030521883283424767" ),
   z2_SEED : BigInt = BigInt("18445829279364155008"),
   z3_SEED : BigInt = BigInt("18436106298727503359"))
class Noisegen ( params : RandSeedParams 
  ) extends Module with COMMON {
  val io = IO(new Bundle {
    val ce        = Input(Bool())
    val ValidOut  = Output(Bool()) 
    val DataOut   = Output(UInt(PWITH.W))  
  })
  val z1 = RegInit(params.z1_SEED.U(64.W))
  val z2 = RegInit(params.z2_SEED.U(64.W)) 
  val z3 = RegInit(params.z3_SEED.U(64.W)) 
  val z1next = z1(39,1) ## (z1(58,34) ^ z1(63,39)) 
  val z2next = z2(50,6) ## (z2(44,26) ^ z2(63,45))
  val z3next = z3(56,9) ## (z3(39,24) ^ z3(63,48)) 
  when ( io.ce) {
    z1 := z1next 
    z2 := z2next 
    z3 := z3next  
  }
  io.ValidOut := RegNext(io.ce) 
  //val out = z1next^z2next^z3next
  val out = RegNext(z1next^z2next^z3next)
  io.DataOut  := out((PWITH-1),0) 
  //64位会造成拥塞，将输出换成32位的，用32位的噪声。
}
class gng(  params : RandSeedParams
  ) extends Module with COMMON {
  val io = IO(new Bundle {
    val din        = Input(UInt(1.W))      // 输入码字
    val dinvalid   = Input(Bool())
    val p0         = Input(Vec(PNUM,UInt(PWITH.W)))
    val dout       = Output(UInt(LLRWIDTH.W))  
    val doutvalid  = Output(Bool()) 
  })
  val gngctg = Module(new Noisegen(params))
  gngctg.io.ce := io.dinvalid  
  io.doutvalid := gngctg.io.ValidOut  
  val negp0 = io.p0.map(x => (~x+1.U).asUInt) 
  val noise = gngctg.io.DataOut  
  //val noise = VecInit(Seq.fill(PNUM)(RegNext( gngctg.io.DataOut)))
  //val douttable = VecInit(Seq.tabulate(PNUM+1)(i => (i).U))
  //io.dout := douttable(PNUM)    
  //switch(io.din) {
    //is(0.U) {
      // 0 对应LLR 正方向的最大值  
      io.dout := table15(0).asUInt
      for (i <- 0 until PNUM) {
        //顺序开始遍历，保证大端的p有更高的优先级   
        //负的与正的是对称的 
        when(noise <= io.p0(i)) {
          io.dout := table15(i+1).asUInt//douttable(i+1)
        }
      }
    //}
    //PNUM 对应LLR 负方向的最大值 
    //is(1.U){
      //io.dout := douttable(PNUM)
      //for (i <- 0 until PNUM) {
        //when(noise > negp0(i)){
        //  io.dout := douttable(PNUM-1-i)
        //}
      //}
    //}

 // }

}
