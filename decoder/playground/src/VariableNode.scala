package decoder 
import chisel3._
import chisel3.util._

class VariableNode extends Module with COMMON {
  val io = IO(new Bundle {
    val LLrin    = Input(UInt(APPWIDTH.W))
    val Checkin  = Input(Vec(ROWNUM, UInt(C2VWIDTH.W)))
    val Checkout = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val APPout   = Output(UInt(APPWIDTH.W)) 
  })
  val Checklocal = VecInit(Seq.fill(ROWNUM)(0.U(APPWIDTH.W)))
  for (i <- 0 until ROWNUM){
    Checklocal(i) := Fill(V2CWIDTH-C2VWIDTH,io.Checkin(i)(C2VWIDTH-1))##io.Checkin(i)
  }
  val sum = Wire(UInt(APPWIDTH.W))
  sum := Checklocal.reduce(_+_)
  for(i <- 0 until ROWNUM){
    io.Checkout(i) := sum - Checklocal(i)
  }
  io.APPout := sum + io.LLrin
  //GenerateIO.Gen(1)
}
