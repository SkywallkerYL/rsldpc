package decoder 
import chisel3._
import chisel3.util._


class Errorbits extends Module with COMMON {
  val io = IO(new Bundle {
    val appin       = Input(Vec(BLKSIZE,UInt(1.W)))
    val appvalid    = Input(Bool())
    val localvalid  = Input(Bool())
    val updatevalid = Input(Bool())
    val coladdr     = Input(UInt(COLADDR.W))
    val Errorcol    = Output(Vec(MAXERRORNUM,UInt((COLADDR).W))) 
    val Errorblk    = Output(Vec(MAXERRORNUM,UInt((BLKADDR).W))) 
    val Errornum    = Output(UInt(COLADDR.W))                      
    //val Modulebusy  = Output(Bool())
    val outvalid    = Output(Bool())
   // val Checkin  = Input(Vec(ROWNUM, UInt(C2VWIDTH.W)))
   // val Checkout = Output(Vec(ROWNUM,UInt(V2CWIDTH.W)))
   // val APPout   = Output(UInt(APPWIDTH.W)) 
  })
  io.outvalid := false.B  
  //GenerateIO.Gen(4)
 // 不要用ram ，改用Reg ，用RAM存在一个要提前一个周期送地址的问题 
 // 也可以用，只是要加一些设计
  val APPRAMS    = Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(1.W))) 
  val writeen = Wire(Bool())
  val writeaddr = Wire(UInt(COLADDR.W))
  val writedata =VecInit(Seq.fill(BLKSIZE)(0.U(1.W))) 
  val readen  = Wire(Bool())
  writeen := false.B 
  readen  := false.B  
  writeaddr := io.coladdr
  for(i <- 0 until BLKSIZE) {
    writedata(i) := io.appin(i) 
  }
  when(writeen) {
    for(i <- 0 until COLNUM) {
      when(i.U === writeaddr ) {
        for(j <- 0 until BLKSIZE) {
          APPRAMS(i).write(j.U,writedata(j))
        }
      }
    }
  }

  //记录错误时读取的地址
  val colcounter = RegInit(0.U(COLADDR.W)) 
  val blkcounter = RegInit(0.U(BLKADDR.W)) 
  val colnext    = RegNext(colcounter)
  val blknext    = RegNext(blkcounter)
  val wrongIndexcounter = RegInit(0.U(COLADDR.W))
  //val wrongIndex = (Seq.fill(MAXERRORNUM)(RegInit(0.U((COLADDR+BLKADDR).W))))
  val wrongIndex = RegInit(VecInit(Seq.fill(MAXERRORNUM)(0.U((COLADDR+BLKADDR).W))))
  for(i <-0 until MAXERRORNUM) {
    io.Errorblk(i) := wrongIndex(i)(BLKADDR-1,0) 
  }
  for(i <-0 until MAXERRORNUM) {
    io.Errorcol(i) := wrongIndex(i)(COLADDR+BLKADDR-1,BLKADDR) 
  }
  io.Errornum := wrongIndexcounter 
  val updateend = RegInit(0.U(1.W))
  //val readaddr = Wire(UInt(COLADDR.W)) 
  val readdata = Wire(UInt(1.W))
  val readdataGroup = VecInit(Seq.fill(COLNUM)(0.U(1.W)))

  when (readen) {
    for(i <- 0 until COLNUM) {
        readdataGroup(i) := APPRAMS(i).read(blkcounter)    
    }
  }
  readdata := readdataGroup(colcounter)

  val idle :: record :: write :: update:: Nil = Enum(4)  

  // idle 等待 
  // record 从decoder输入的app写入RAM   
  // write 遍历2048个APP 地址，将错误的比特
  // update 等待外部信号读当前的错误比特，外部读完之后  再给这个法信息 回到idle态 
  // 并且要把RAM里的消息给归0 其实这一部也不用做  
  // 因为不考虑两个错误帧基本同时出现的情况
  // 还是要做归0   
  val currentState = RegInit(idle) 

  switch(currentState){
    is(idle) {
      when(io.appvalid&&io.localvalid) {
        when(io.coladdr === (COLNUM-1).U) {
          currentState := write  
          colcounter := 0.U 
          blkcounter := 1.U 
          readen := true.B
         // wrongIndexcounter := 0.U 
         // for(i <- 0 until MAXERRORNUM) {
         //   wrongIndex(i) := 0.U
         // }
        }.otherwise{        
          colcounter := 0.U 
          blkcounter := 0.U 
          currentState := record
        }
        writeen := true.B  
      }
    }
    is(record) {
      when(io.coladdr === (COLNUM-1).U) {
        currentState := write
        colcounter := 0.U 
        blkcounter := 1.U 
        wrongIndexcounter := 0.U 
        for(i <- 0 until MAXERRORNUM) {
          wrongIndex(i) := 0.U
        }
      }
      writeen := true.B  
    }
    is(write) {
      when((colnext === (COLNUM-1).U) && (blknext ===(BLKSIZE-1).U)){
        currentState := update  
        colcounter := 0.U 
        updateend := 0.U 
       //blkcounter := 0.U 
      }.elsewhen(blkcounter === (BLKSIZE-1).U) {
        colcounter := colcounter + 1.U 
        blkcounter := 0.U 
      }.otherwise{
        blkcounter := blkcounter + 1.U 
      } 
      readen:= true.B
      when(readdata === 1.U && (wrongIndexcounter < MAXERRORNUM.U)) {
        wrongIndexcounter := wrongIndexcounter + 1.U 
        wrongIndex(wrongIndexcounter) := colnext ## blknext 
      }
    }
    is(update) {
      
      //readen :=true.B
      when((updateend===1.U) && io.updatevalid) {
        currentState := idle 
        colcounter := 0.U 
        blkcounter := 0.U 
        wrongIndexcounter := 0.U 
        for(i <- 0 until MAXERRORNUM) {
          wrongIndex(i) := 0.U 
        }
      }

      when(colcounter === (COLNUM-1).U) {
        updateend := 1.U  
      }.otherwise{
        colcounter := colcounter + 1.U 
      }
      writeaddr := colcounter
      for(i <- 0 until BLKSIZE) {
        writedata(i) := 0.U 
      }
      io.outvalid := updateend  
    }

  }

  //GenerateIO.Gen(1)
}
