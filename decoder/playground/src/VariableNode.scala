package decoder 
import chisel3._
import chisel3.util._

class VariableNode extends Module with COMMON {
  val io = IO(new Bundle {
    val LLrin    = Input(UInt(LLRWIDTH.W))
    val Checkin  = Input(Vec(ROWNUM, UInt(C2VWIDTH.W)))
    val Checkout = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val APPout   = Output(UInt(APPWIDTH.W)) 
  })
  val LLrinext = Fill(APPWIDTH-LLRWIDTH,io.LLrin(LLRWIDTH-1))##io.LLrin
  val Checklocal = VecInit(Seq.fill(ROWNUM)(0.U(APPWIDTH.W)))
  val checkoutAPP = VecInit(Seq.fill(ROWNUM)(0.U(APPWIDTH.W)))
  //val checkinsign = VecInit(Seq.fill(ROWNUM)(0.U(1.W)))
  for (i <- 0 until ROWNUM){
    //checkinsign(i) := io.Checkin(i)(C2VWIDTHCOL-1)
    Checklocal(i) := Fill(APPWIDTH-C2VWIDTH,io.Checkin(i)(C2VWIDTHCOL-1))##io.Checkin(i)
    //用fill 会生成一个判断的逻辑门，这里直接用拼接
    //但实际上这样子反而会造成更多的路径延迟，因此还是用fill
   // Checklocal(i) := checkinsign(i) ## checkinsign(i)##checkinsign(i)##io.Checkin(i)
  }
  val sum = Wire(UInt(APPWIDTH.W))
  sum := Checklocal.reduce(_+_)
  for(i <- 0 until ROWNUM){
    checkoutAPP(i) := LLrinext + sum - Checklocal(i)
  }
  val min = Wire(UInt(C2VWIDTHCOL.W))
  min := ~(MAXC2V.U(C2VWIDTHCOL.W))+1.U
  for(i <- 0 until ROWNUM) {
   io.Checkout(i) :=Mux( checkoutAPP(i).asSInt <=(-MAXV2C.S),min,Mux(checkoutAPP(i).asSInt>=MAXV2C.S,MAXV2C.U,checkoutAPP(i)(V2CWIDTH-1,0) ))  
  }
  io.APPout := sum + LLrinext
  //GenerateIO.Gen(1)
}
