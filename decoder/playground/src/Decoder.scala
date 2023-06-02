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
    val IterOut   = Output(UInt(9.W))
    val counter   = Output(UInt(BLKADDR.W))
  })
//32 processing unit 
val Process = Seq.fill(COLNUM)(Module(new ProcessingUnit))
// 6 check node 
// 按行做， 一次只做一行，  
// 读V2C给校验节点，将计算结果写如C2V，
// 下一个周期读C2V ， 计算写回V2C   。
// 再下一个周期进入下一行  ，两个周期做一行 
val Check   = (Module(new CheckNode))
// 32 * 6 MUX
val AddrMux = Seq.fill(ROWNUM)(Seq.fill(COLNUM)(Module(new V2CMux)))
//  6选一 的 addr mux 
val Rowmux  = Seq.fill(COLNUM)(Module(new RowMux))
// EN wire 
  val V2CReadEn   = Wire(Bool())
  val C2VWriteEn  = Wire(Bool())
  val C2VReadEn   = Wire(Bool())
  val V2CWriteEn  = Wire(Bool())
  val C2VFlush    = Wire(Bool())
  V2CReadEn := false.B 
  C2VWriteEn := false.B
  C2VReadEn := false.B
  V2CWriteEn := false.B
  C2VFlush   := false.B  
  // to pocess unit 
  for (i <- 0 until COLNUM) {
      Process(i).io.C2VWriteEn := C2VWriteEn 
      Process(i).io.C2VReadEn  := C2VReadEn  
      Process(i).io.V2CReadEn  := V2CReadEn  
      Process(i).io.V2CWriteEn := V2CWriteEn 
      Process(i).io.C2VFlush   := C2VFlush  
  }


  io.OutValid := false.B
  io.Success := false.B
// LLR_RAM  
  val LLrReadEn = Wire(Bool())
  val LLrWriteEn = Wire(Bool())
  val LLrAddr   = Wire(UInt(BLKADDR.W))
  LLrReadEn := false.B 
  LLrWriteEn := false.B
  val LLRRams =Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(APPWIDTH.W))) 
  //Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(APPWIDTH.W)))
  //Seq.fill(COLNUM)(Mem(BLKSIZE,UInt(APPWIDTH.W)))
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
  LLrAddr := counter 
  io.counter := counter 
  // conter1 相比counter延后一个周期，这样子
  // counter 是一个addr V2C读   
  // 下一个周期拿到V2C的数据  
  // 用counter1 作为addr 写回C2V
  // 感觉还是不行 ，因为只有一个MUX   counter并不是真正的地址，所以还是要两个周期
  // 但是向读C2V，写回V2C的时候，应该是可以这样子的， 因为此时counter就是地址 
  val counter1    = RegInit(0.U((BLKADDR+1).W)) 
  val counter2    = RegNext(counter)
  val RowCounter  = RegInit(0.U(ROWADDR.W))
  val rightreg    = RegInit(1.U(1.W)) 
  val appoutjudge = VecInit(Seq.fill(COLNUM)(0.U(1.W)))
  for(i <- 0 until COLNUM) {
    appoutjudge(i) := ~Process(i).io.Appout(APPWIDTH-1)
    Process(i).io.RowCounter := RowCounter 
  }
  val rightdecide = appoutjudge.reduce(_ & _)
// read Matrix  
  val rsMatrix : Array[Array[Int]] = ReadMatrix.ReadM()
  //GenerateIO.Gen(2)

// modeule connect   
  //V2C write addr 默认counter   
  // 在从Vari写回的时候，要改成从C2V读的ADDR  
  // 写数据默认LLrin  
  // 从VAR写回的时候 要改成 Variabl 的checkout
  for( i <- 0 until COLNUM) {
    for (j <- 0 until ROWNUM) {
      Process(i).io.V2CWriteAddr(j) := counter 
      Process(i).io.V2CWriteData(j) := io.LLrin(i)
    }
  }
// Mux ADDR 
  for (i <- 0 until ROWNUM) {
    for(j <- 0 until COLNUM) {
      AddrMux(i)(j).io.sel := counter 
      for( k <- 0 until BLKSIZE) {
        AddrMux(i)(j).io.SramAddr(k) := 0.U
        for (l <- 0 until BLKSIZE ) {
          val z : Int = rsMatrix(i*BLKSIZE+k)(j*BLKSIZE+l)  
          if(z == 1) {
              AddrMux(i)(j).io.SramAddr(k) := l.U
          }
        }
      }
    }
  }
  for ( i <- 0 until COLNUM) {
    for (j <- 0 until ROWNUM) {
      Rowmux(i).io.V2Caddr(j) := AddrMux(j)(i).io.ChooseAddr    
    }
    Rowmux(i).io.sel := RowCounter 
  }
  // V2C Sram ADDR  

  for (i <- 0 until COLNUM ){
    Process(i).io.V2CReadAddr  := Rowmux(i).io.ChooseAddr  
    Process(i).io.C2VWriteAddr := Rowmux(i).io.ChooseAddr  
  }
  // Sram  dataout -> Check Node   
  for(j <- 0 until COLNUM) {
      Check.io.input(j) := Process(j).io.V2CReadData
  }
  // check node out -> Sram 
  for (i <- 0 until COLNUM) {
      val minval = Mux(Check.io.minIdx === i.U,Check.io.subminVal,Check.io.minVal) 
      val sign   = Check.io.xor_result === Check.io.input(i)(V2CWIDTH-1)
      val scaledmin = (minval.asUInt * 3.U) >> 2.U
      Process(i).io.C2VWriteData := Mux(sign,scaledmin,~scaledmin + 1.U)
  }
  // c2v sram read addr 
  for (i <- 0 until COLNUM) {
    for (j <- 0 until ROWNUM) {
      Process(i).io.C2VReadAddr(j) := Rowmux(i).io.ChooseAddr  
    }
  }


  // c2v sram read data -> variable   
 // 这个已经在 process unit 内部连接了
 //  


 val idle :: initial:: v2c :: c2v :: judge :: Nil = Enum(5)


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
        RowCounter := 0.U 
        counter1 := 0.U
        Iter := io.IterInput 
      }
    }
    is(initial){
      when(counter === (BLKSIZE-1).U){
        currentState := v2c    
        counter := 0.U 
        RowCounter := 0.U
      }.otherwise{
        counter := counter + 1.U
      }
      V2CWriteEn := true.B 
      LLrWriteEn := true.B
      C2VFlush   := true.B
      for(i <- 0 until COLNUM) {
        Process(i).io.C2VWriteAddr := counter  
        Process(i).io.C2VWriteData := 0.U
      }
    }
    
    is(v2c){
      // 这是假定下一个周期拿到读数据的代码 
      // 但实际上chisel生成的SyncReadMem 当前周期就能拿到数据 
     // 当周期就拿到读数据的代码   
      V2CReadEn := true.B 
      C2VWriteEn := true.B  
      currentState := c2v 
    }
    
    // decode  
    // 完成对V2C的
    is(c2v ) {
      /*
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
      }.otherwise {
        V2CWriteEn := true.B
        counter := counter + 1.U
        rightreg := rightreg & rightdecide
      }
      */
       //V2C延后一个周期写 
       //同前 这是延迟一个周期拿到数据的写法  
       /*
       when ( counter2 =/= (BLKSIZE-1).U) {
        counter := counter + 1.U
        C2VReadEn := true.B 
        LLrReadEn := true.B
      }.otherwise{
        currentState := judge 
        counter := 0.U
      } 
      V2CWriteEn := RegNext(C2VReadEn) 
      when(V2CWriteEn) {
           rightreg := rightreg & rightdecide 
      }
      for( i <- 0 until COLNUM) {
        for (j <- 0 until ROWNUM) {
          Process(i).io.V2CWriteAddr(j) := counter2 
          Process(i).io.V2CWriteData(j) := Process(i).io.VariableOut(j)
        }
      }
      */  
      when ( (counter === (BLKSIZE-1).U)&&(RowCounter === (ROWNUM-1).U)) {
        currentState := judge 
        counter := 0.U
        RowCounter := 0.U 
      }.elsewhen((counter === (BLKSIZE-1).U) && RowCounter =/= (ROWNUM-1).U){
        RowCounter := RowCounter + 1.U 
        currentState := v2c   
        counter := 0.U
      }.otherwise {
        counter := counter + 1.U
        currentState := v2c  
      } 
      V2CWriteEn := true.B 
      LLrReadEn  := true.B
      C2VReadEn  := true.B  
      when(V2CWriteEn&& (RowCounter === (ROWNUM-1).U)) {
           rightreg := rightreg & rightdecide 
      }
      for( i <- 0 until COLNUM) {
        for (j <- 0 until ROWNUM) {
          Process(i).io.V2CWriteAddr(j) := Rowmux(i).io.ChooseAddr
          Process(i).io.V2CWriteData(j) := Process(i).io.VariableOut(j)
        }
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
        rightreg := 1.U
      }.otherwise{
        when(rightreg === 1.U){
          io.OutValid := true.B
          io.Success  := true.B
          currentState := idle 
        }.otherwise{
          Iter := Iter - 1.U 
          rightreg := 1.U
          currentState := v2c    
        }
      }
    }
  }
  io.IterOut := Iter

}
