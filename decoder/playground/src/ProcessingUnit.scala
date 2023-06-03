package decoder 
import chisel3._
import chisel3.util._

class ProcessingUnit1 extends Module with COMMON {
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
   // Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(V2CWIDTH.W)))
   // Seq.fill(ROWNUM)(Mem(BLKSIZE,UInt(V2CWIDTH.W)))
  val C2VRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  //Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  //Seq.fill(ROWNUM)(Mem(BLKSIZE,UInt(C2VWIDTH.W)))
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
  // RAM占用了太多的IO资源，采用REG实现
  //两块RAM 分别存V2C C2V 消息
  val V2CRams = Seq.fill(ROWNUM)(Seq.fill(BLKSIZE)(RegInit(0.U(V2CWIDTH.W))))
  //val V2CRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(V2CWIDTH.W)))
   // Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(V2CWIDTH.W)))
   // Seq.fill(ROWNUM)(Mem(BLKSIZE,UInt(V2CWIDTH.W)))
  //val C2VRams = Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  val C2VRams = Seq.fill(ROWNUM)(Seq.fill(BLKSIZE)(RegInit(0.U(C2VWIDTH.W))))
  //Seq.fill(ROWNUM)(SyncReadMem(BLKSIZE,UInt(C2VWIDTH.W)))
  //Seq.fill(ROWNUM)(Mem(BLKSIZE,UInt(C2VWIDTH.W)))
  // V2C写   
  for ( i <- 0 until ROWNUM) {
    for ( j<- 0 until BLKSIZE) {
      when(io.V2CWriteEn && (io.V2CWriteAddr(i)===j.U)){
        V2CRams(i)(j) := io.V2CWriteData(i)
      }
    }
  }
  // C2V写 
  for(i <- 0 until ROWNUM){
    for ( j <- 0 until BLKSIZE) {
      when (io.C2VWriteEn && (io.RowCounter === i.U)&&(io.C2VWriteAddr === j.U)) {
        C2VRams(i)(j) := io.C2VWriteData 
      }
      when (io.C2VFlush &&(io.C2VWriteAddr === j.U)) {
        C2VRams(i)(j) := io.C2VWriteData 
      }
    }
  }
  // V2Cread
  val V2CReadData = Seq.fill(ROWNUM)(Wire(UInt(V2CWIDTH.W)))
  for (i <- 0 until ROWNUM) {
    V2CReadData(i) := 0.U
    for ( j <- 0 until BLKSIZE) {
      when(j.U === io.V2CReadAddr) {
        V2CReadData(i) := V2CRams(i)(j)
      }
    }
  } 
  val table = Seq(
    (0.U,V2CReadData(0)),
    (1.U,V2CReadData(1)),
    (2.U,V2CReadData(2)),
    (3.U,V2CReadData(3)),
    (4.U,V2CReadData(4)),
    (5.U,V2CReadData(5))
  )
  io.V2CReadData :=Mux(io.V2CReadEn, MuxLookup(io.RowCounter,0.U,table),0.U)
  // Varialble Node 
  
  val C2VReadData = Seq.fill(ROWNUM)(Wire(UInt(C2VWIDTH.W)))
  for( i <- 0 until ROWNUM) {
    C2VReadData(i) := 0.U
    for ( j <- 0 until BLKSIZE ) {
      when(io.C2VReadAddr(i) === j.U) {
        C2VReadData(i):= C2VRams(i)(j) 
      }
    }    
  }

  val Vnode = Module (new VariableNode) 
  Vnode.io.LLrin := io.LLrin 
  for (i <- 0 until ROWNUM){
    Vnode.io.Checkin(i) := C2VReadData(i)   //Mux(io.C2VReadEn,C2VRams(i).read(io.C2VReadAddr(i)),0.U(C2VWIDTH.W))
  }
  io.VariableOut <> Vnode.io.Checkout 
  io.Appout := Vnode.io.APPout 

}
