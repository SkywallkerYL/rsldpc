package decoder 
import chisel3._
import chisel3.util._

class Decoder extends Module with COMMON {
  val io = IO(new Bundle {
    val LLrin     = Input(Vec(COLNUM,UInt(APPWIDTH.W)))
    val Start     = Input(Bool())
    val IterInput = Input(UInt(9.W))
    //val Appout    = Output(Vec(COLNUM,UInt(APPWIDTH.W)))
    val OutValid  = Output(Bool())
    val Success   = Output(Bool())
  })
//32 processing unit 
  val Process = Seq.fill(COLNUM)(Module(new ProcessingUnit))
// 6 check node 
  val Check   = Seq.fill(ROWNUM)(Module(new CheckNode))
// 32 * 6 MUX
  val AddrMux = Seq.fill(ROWNUM)(Seq.fill(COLNUM)(Module(new V2CMux)))
// EN wire 
  val V2CReadEn   = Wire(Bool())
  val C2VWriteEn  = Wire(Bool())
  val C2VReadEn   = Wire(Bool())
  val V2CWriteEn  = Wire(Bool())
  V2CReadEn := false.B 
  C2VWriteEn := false.B
  C2VReadEn := false.B
  V2CWriteEn := false.B
  io.OutValid := false.B
  io.Success := false.B
// LLR_RAM  
  val LLrReadEn = Wire(Bool())
  val LLrWriteEn = Wire(Bool())
  val LLrAddr   = Wire(UInt(BLKADDR.W))
  LLrReadEn := false.B 
  LLrWriteEn := false.B
  val LLRRams = Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(APPWIDTH.W)))
  when (LLrWriteEn){
    for(i <- 0 until COLNUM) {
        LLRRams(i).write(LLrAddr,io.LLrin(i))
    }
  }
  for(i <-0 until COLNUM ) {
    Process(i).io.LLrin := Mux(LLrReadEn,LLRRams(i).read(LLrAddr),0.U(APPWIDTH.W))
  }

// Wire Connect 
  val counter     = RegInit(0.U(BLKADDR.W))
  val counter1    = RegInit(0.U((BLKADDR+1).W)) 
  val rightreg    = RegInit(1.U(1.W)) 
  val appoutjudge = VecInit(Seq.fill(COLNUM)(0.U(1.W)))
  for(i <- 0 until COLNUM) {
    appoutjudge(i) := Process(i).io.Appout(APPWIDTH-1)
  }
  val rightdecide = appoutjudge.reduce(_ & _)

  val idle :: initial:: check2var :: var2check :: judge :: Nil = Enum(5)

  val currentState = RegInit(idle)


  //decode 状态下两个周期写回一个数据
  //第一个周期V2C读，然后第二个周期
  //拿到数据，向V2C写回  
  val Iter    = RegInit(0.U(9.W))

  switch(currentState){
    is(idle){
      when(io.Start) {
        currentState := initial 
        counter := 0.U
        counter1 := 0.U
        Iter := io.IterInput 
      }
    }
    is(initial){
      when(counter === (BLKSIZE-1).U){
        currentState := check2var 
        counter := 0.U 
      }.otherwise{
        counter := counter + 1.U
      }
      V2CWriteEn := true.B 
      LLrWriteEn := true.B
    }
    is(check2var){
      when(counter1 === (2*BLKSIZE-1).U){
        currentState := var2check  
        counter := 0.U 
        counter1 := 0.U
        rightreg := 1.U
      }.otherwise{
        counter1 := counter1 + 1.U
      }

      //V2C读  
      when(counter1(0) === 0.U){
        V2CReadEn := true.B
      }.otherwise{
        //C2V写 
        C2VWriteEn := true.B 
        counter := counter + 1.U
      }
    }
    is(var2check) {
      when(counter1 === (2*BLKSIZE-1).U){
        currentState := judge 
        counter := 0.U
        counter1 := 0.U
      }.otherwise {
        counter1 := counter1 + 1.U
      }
      when(counter1(0) === 0.U){
        //C2V读  
        C2VReadEn := true.B 
        LLrReadEn := true.B
      }.otherwise{
        //V2C写
        V2CWriteEn := true.B 
        counter := counter + 1.U
        rightreg := rightreg & rightdecide
      }
    }
    is(judge) {
      when(Iter === 1.U){
        io.OutValid := true.B
        when(rightreg === 1.U ){
          io.Success := true.B
        }.otherwise {
          io.Success := false.B
        }
        currentState := idle 
      }.otherwise{
        when(rightreg === 1.U){
          io.OutValid := true.B
          io.Success  := true.B
          currentState := idle 
        }.otherwise{
          Iter := Iter - 1.U 
          currentState := check2var 
        }
      }
    }
  }

}
