package decoder 
import chisel3._
import chisel3.util._

class Controller extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput = Input(UInt(9.W))
    val IterOut   = Output(UInt(9.W))
  })
  
  val idle :: framestart :: frameinitial :: decode :: framevalid :: frameend :: Nil = Enum(5)
  // idle         : 等待   
  // framestart   : 开始仿真 初始化 噪声sigma等信息p0 最大错误帧   
  // frameinitial : 给decoder进行LLr初始化   
  // decode       : 等待decoder译码  
  // framevalid   : decoder 译码完成，统计错误帧  
  // 如果到达最大错误 则进入frameend 进行信息的输出 
  // 否则跳回 frameinitial  
  // frameend     : valid 拉高，输出错误帧等信息，如果有下一个sigma的使能，则 
  // 跳回 framestart  
  // 否则，跳转到idle 

  val currentState = RegInit(idle) 

}
