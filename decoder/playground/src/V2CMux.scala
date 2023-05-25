package decoder 
import chisel3._
import chisel3.util._

class V2CMux extends Module with COMMON {
  val io = IO(new Bundle {
    val SramAddr    =  Input(Vec(BLKSIZE,UInt(BLKADDR.W)))
    val sel         =  Input(UInt(BLKADDR.W))
    val ChooseAddr  =  Output(UInt(BLKADDR.W))
  })

  io.ChooseAddr := io.SramAddr(io.sel)
 // ReadMatrix.ReadM()
  
}
