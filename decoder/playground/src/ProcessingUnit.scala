package decoder 
import chisel3._
import chisel3.util._
/*
class ProcessingUnit extends Module with COMMON {
  val io = IO(new Bundle {
    val V2CWriteAddr = Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val V2CWriteData = Input(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val V2CWriteEn   = Input(Bool())
    
    val V2CReadEn    = Input(Bool())
    val V2CReadAddr  = Input(Vec(ROWNUM,UInt(BLKADDR.W)))

    val C2VWriteEn   = Input(Bool())
    val C2VWriteAddr = Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val C2VWriteData = Input(Vec(ROWNUM,UInt(C2VWIDTH.W)))

    val C2VReadAddr  = Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val C2VReadEn    = Input(Bool())
    
    val LLrin        = Input(UInt(APPWIDTH.W))

    val V2CReadData  = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    
    val VariableOut  = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val Appout       = Output(UInt(APPWIDTH.W))  
  })
  //两块RAM 分别存V2C C2V 消息
  val V2CRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(V2CWIDTH.W)))
  val C2VRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  // V2C写   
  when(io.V2CWriteEn){
    for (i <- 0 until ROWNUM){
      V2CRams(i).write(io.V2CWriteAddr(i),io.V2CWriteData(i))
    }
  }
  // C2V写
  when(io.C2VWriteEn){
    for(i <- 0 until ROWNUM){
      C2VRams(i).write(io.C2VWriteAddr(i),io.C2VWriteData(i))
    }
  }
  // V2Cread   
  for( i <- 0 until ROWNUM) {
    io.V2CReadData(i) := Mux(io.V2CReadEn,V2CRams(i).read(io.V2CReadAddr(i)),0.U(V2CWIDTH.W))
  }
  // Varialble Node 

  val Vnode = Module (new VariableNode) 
  Vnode.io.LLrin := io.LLrin 
  for (i <- 0 until ROWNUM){
    Vnode.io.Checkin(i) := Mux(io.C2VReadEn,C2VRams(i).read(io.C2VReadAddr(i)),0.U(C2VWIDTH.W))
  }
  io.VariableOut <> Vnode.io.Checkout 
  io.Appout := Vnode.io.APPout 

}
*/
class ProcessingUnit extends Module with COMMON {
  val io = IO(new Bundle {
    val V2CWriteAddr = Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val V2CWriteData = Input(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val V2CWriteEn   = Input(Bool())
    
    val V2CReadEn    = Input(Bool())
    val V2CReadAddr  = Input(UInt(BLKADDR.W))

    val C2VWriteEn   = Input(Bool())
    val C2VFlush     = Input(Bool())  
    val RowCounter   = Input(UInt(ROWADDR.W))
    val C2VWriteAddr = Input(UInt(BLKADDR.W))
    val C2VWriteData = Input(UInt(C2VWIDTH.W))

    val C2VReadAddr  = Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val C2VReadEn    = Input(Bool())
    
    val LLrin        = Input(UInt(APPWIDTH.W))

    val V2CReadData  = Output(UInt(V2CWIDTH.W))
    
    val VariableOut  = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
    val Appout       = Output(UInt(APPWIDTH.W))  
  })
  //两块RAM 分别存V2C C2V 消息
  val V2CRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(V2CWIDTH.W)))
  val C2VRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  // V2C写   
  for ( i <- 0 until ROWNUM) {
    when(io.V2CWriteEn){
      V2CRams(i).write(io.V2CWriteAddr(i),io.V2CWriteData(i))
    }
  }
  // C2V写
  for(i <- 0 until ROWNUM){
    when(io.C2VWriteEn && (io.RowCounter === i.U)){
      C2VRams(i).write(io.C2VWriteAddr,io.C2VWriteData)
    }
    when(io.C2VFlush) {
      C2VRams(i).write(io.C2VWriteAddr,io.C2VWriteData)
    }
  }
  // V2Cread
  val table = Seq(
    (0.U,V2CRams(0).read(io.V2CReadAddr)),
    (1.U,V2CRams(1).read(io.V2CReadAddr)),
    (2.U,V2CRams(2).read(io.V2CReadAddr)),
    (3.U,V2CRams(3).read(io.V2CReadAddr)),
    (4.U,V2CRams(4).read(io.V2CReadAddr)),
    (5.U,V2CRams(5).read(io.V2CReadAddr))
  )
  io.V2CReadData :=Mux(io.V2CReadEn, MuxLookup(io.RowCounter,0.U,table),0.U)
  // Varialble Node 

  val Vnode = Module (new VariableNode) 
  Vnode.io.LLrin := io.LLrin 
  for (i <- 0 until ROWNUM){
    Vnode.io.Checkin(i) := Mux(io.C2VReadEn,C2VRams(i).read(io.C2VReadAddr(i)),0.U(C2VWIDTH.W))
  }
  io.VariableOut <> Vnode.io.Checkout 
  io.Appout := Vnode.io.APPout 

}
