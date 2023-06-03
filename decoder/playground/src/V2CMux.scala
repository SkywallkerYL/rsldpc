package decoder 
import chisel3._
import chisel3.util._

class V2CMux extends Module with COMMON {
  val io = IO(new Bundle {
    val SramAddr    =  Input(Vec(BLKSIZE,UInt(BLKADDR.W)))
    val sel         =  Input(UInt(BLKADDR.W))
    val ChooseAddr  =  Output(UInt(BLKADDR.W))
  })
  io.ChooseAddr := MuxLookup(io.sel,0.U,( 0 until 64).map(i => (i.U -> io.SramAddr(i))))
//  io.ChooseAddr := io.SramAddr(io.sel)
 // ReadMatrix.ReadM()
  
}
class RowMux extends Module with COMMON {
  val io = IO(new Bundle {
    val V2Caddr     =  Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val sel         =  Input(UInt(ROWADDR.W))
    val ChooseAddr  =  Output(UInt(BLKADDR.W))
  })
  //这样写 与简便的写法是一样的。
    //io.ChooseAddr := 0.U 
    //switch(io.sel) {
    //is(0.U) {
    //  io.ChooseAddr := io.V2Caddr(0)
    //}
    //is(1.U) {
    //  io.ChooseAddr := io.V2Caddr(1)
    //}
    //is(2.U) {
    //  io.ChooseAddr := io.V2Caddr(2)
    //}
    //is(3.U) {
    //  io.ChooseAddr := io.V2Caddr(3)
    //}
    //is(4.U) {
    //  io.ChooseAddr := io.V2Caddr(4)
    //}
    //is(5.U) {
    //  io.ChooseAddr := io.V2Caddr(5)
    //}

  //}
  io.ChooseAddr := io.V2Caddr(io.sel)
 // ReadMatrix.ReadM()
  
}

