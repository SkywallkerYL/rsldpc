package decoder 
import chisel3._
import chisel3.util._

class Decoder2Col extends Module with COMMON {
  val io = IO(new Bundle {
    val LLrin     = Input(Vec(BLKSIZE*2,UInt(APPWIDTH.W)))
    val Start     = Input(Bool())
    val IterInput = Input(UInt(ITERWITH.W))
    //val Appout    = Output(Vec(COLNUM,UInt(APPWIDTH.W)))
    val OutValid  = Output(Bool())
    val Success   = Output(Bool())
    val IterOut   = Output(UInt(ITERWITH.W))
    val counter   = Output(UInt(BLKADDR.W))
    val errorbit  = Output(UInt(11.W))
    val check     = Output(UInt(8.W))
    val postvalid = Input(Bool())
    val strongMessage  = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    val weakMessage    = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    //val strongMessage  = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage    = Input(UInt(C2VWIDTHCOL.W))
    //val strongMessage0 = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage0   = Input(UInt(C2VWIDTHCOL.W))
    //val strongMessage1 = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage1   = Input(UInt(C2VWIDTHCOL.W))
    val postIterInput = Input(UInt(ITERWITH.W))
    //val appvalid  = Output(Bool())
    //val appout    = Output(Vec(BLKSIZE,UInt(1.W)))
    //val llrout    = Output(Vec(32,UInt(256.W)))
    val errorflush  = Input(Bool())
    val errorvalid  = Output(Bool())

    //for vio_check 
    //val llroutcheck = Output((Vec(32,UInt(256.W))))
    //val llrou       = Output((Vec(32,UInt(256.W))))
    // for difftest  
    //val appout     = Output(Vec(BLKSIZE*2,UInt(APPWIDTH.W))) 
    //val c2v        = Output(Vec(BLKSIZE*ROWNUM,UInt(C2VWIDTHCOL.W))) 
    //val min        = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
    //val submin     = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
    //val minaddr    = Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
    //val subminaddr = Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
    //val sign       = Output(Vec(BLKSIZE*ROWNUM,UInt(1.W)))
    //val c2v0       = Output(Vec(BLKSIZE*ROWNUM,UInt(C2VWIDTHCOL.W))) 
    //val min0       = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
    //val submin0    = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
    //val minaddr0   = Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
    //val subminaddr0= Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
    //val sign0      = Output(Vec(BLKSIZE*ROWNUM,UInt(1.W)))
    //val v2csign    = Output(Vec(BLKSIZE*COLNUM,Vec(ROWNUM,UInt(1.W))))

  })
  // 按列作，一次作一列。 
  // 一次作两列 一次进的噪声也要两个blk 
  val VarGroup = Seq.fill(BLKSIZE*2)(Module(new VariableNode))   
  // 64*6个校验节点   
  val CheckGroup = Seq.fill(BLKSIZE)(Seq.fill(ROWNUM)(Module (new CheckNode2Col))) 
  // 128 个ram 存LLR 消息 深度16  
  val LLrRams = Seq.fill(BLKSIZE*2)(SyncReadMem(COLNUM/2,UInt(LLRWIDTH.W)))
  // 128 个ram 存v2c符号 深度16  数据位宽 rownum   
  val SignRams = Seq.fill(BLKSIZE*2)(SyncReadMem(COLNUM/2,UInt(ROWNUM.W)))
  //  Var 的out data 给CheckNode 的RAM
  val VartoCheckMux = Seq.fill(BLKSIZE*2)(Seq.fill(ROWNUM)(Module(new Mux16to1(V2CWIDTHCOL))))
  val apptoCheckMux = Seq.fill(BLKSIZE*2)(Seq.fill(ROWNUM)(Module(new Mux16to1(1))))
  // Var 的符号给CheckNode的RAM  
  val SigntoCheckMux = Seq.fill(BLKSIZE*2)(Seq.fill(ROWNUM)(Module(new Mux16to1(1))))
  //  CheckNode 的output 送给这个mux ,mux从16个里选一个，送给Var的一个input  
  val ChecktoVarMux  = Seq.fill(BLKSIZE*2)(Seq.fill(ROWNUM)(Module(new Mux16to1(C2VWIDTHCOL))))
// EN wire 
  val V2CReadEn   = Wire(Bool())
  val C2VWriteEn  = Wire(Bool())
  val C2VReadEn   = Wire(Bool())
  val V2CWriteEn  = Wire(Bool())
  val C2VFlush    = Wire(Bool())
  val updateen    = Wire(Bool()) 
  val initialen   = Wire(Bool())
  val checkreset  = Wire(Bool())
  val updatecheck = Wire(Bool())
  V2CReadEn := false.B 
  C2VWriteEn := false.B
  C2VReadEn := false.B
  V2CWriteEn := false.B
  C2VFlush   := false.B  
  //io.appvalid := false.B
  io.Success := false.B
  updateen := false.B 
  initialen := false.B
  checkreset := false.B
  updatecheck := false.B
// LLR_RAM  
  val LLrReadEn = Wire(Bool())
  val LLrWriteEn = Wire(Bool())
  val LLrAddrWrite  = Wire(UInt(COLADDR.W))
  val LLrAddrRead   = Wire(UInt(COLADDR.W))
  LLrReadEn := false.B 
  LLrWriteEn := false.B
  //Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(APPWIDTH.W)))
  //Seq.fill(COLNUM)(Mem(BLKSIZE,UInt(APPWIDTH.W)))
//error record 
  val errorrecord = RegInit(0.U(1.W))
  when(io.errorflush){
    errorrecord := 0.U
  }
  io.errorvalid := errorrecord === 1.U
// Wire Connect 
  val counter     = RegInit(0.U((COLADDR-1).W))
  val counter1    = RegInit(0.U((COLADDR-1).W))  
  //val counter2    = RegNext(counter1) 
  io.counter := counter 
  io.OutValid := false.B
  LLrAddrWrite := counter 
  LLrAddrRead  := counter1 
  //为了解决counter的高扇出问题  将寄存器复制
  //
  val ChecktoVarSel = RegInit(VecInit(Seq.fill(ROWNUM)(0.U((COLADDR-1).W)))) 
  //Wire(UInt(COLADDR.W)) 
  //ChecktoVarSel := counter  

  val VartoCheckSel = RegInit(VecInit(Seq.fill(ROWNUM)(0.U((COLADDR-1).W)))) 
  //Wire(UInt(COLADDR.W)) 

  //VartoCheckSel := counter 
  val apptoCheckSel = RegInit(VecInit(Seq.fill(ROWNUM)(0.U((COLADDR-1).W)))) 
  
  val SignMuxSel = RegInit(VecInit(Seq.fill(ROWNUM)(0.U((COLADDR-1).W)))) 
  //Wire(UInt(COLADDR.W)) 
  //SignMuxSel := counter

  val CheckAddr = RegInit(VecInit(Seq.fill(ROWNUM)(0.U((COLADDR-1).W)))) 
  //Wire(UInt(COLADDR.W))
  //CheckAddr := counter 

  val signReadAddr = Wire(UInt(COLADDR.W)) 
  signReadAddr := counter1
  val signWriteAddr = Wire(UInt(COLADDR.W)) 
  signWriteAddr := counter 
  val signWriteEn = Wire(Bool()) 
  signWriteEn := false.B  

  val SignReadData = VecInit(Seq.fill(BLKSIZE*2)(0.U(ROWNUM.W)))  
  for (i <- 0 until BLKSIZE*2) {
    SignReadData(i) := SignRams(i).read(signReadAddr)  
  } 
  val SignWriteData = VecInit(Seq.fill(BLKSIZE*2)(0.U(ROWNUM.W)))
  for (i <- 0 until BLKSIZE*2) {
    SignWriteData(i) := Cat(VarGroup(i).io.Checkout.reverse.map(_.asUInt()(V2CWIDTHCOL-1)))
  }
  when(signWriteEn){
    for( i <- 0 until BLKSIZE*2) {
      SignRams(i).write(signWriteAddr,SignWriteData(i))
    }
  }

  val RowCounter  = RegInit(0.U(ROWADDR.W))
  //val rightreg    = RegInit(1.U(1.W)) 
  val appoutjudge = VecInit(Seq.fill(BLKSIZE*2)(0.U(11.W)))
  for(i <- 0 until BLKSIZE*2) {
    appoutjudge(i) := VarGroup(i).io.APPout(APPWIDTHCOL-1)
  }
  val errorcount = RegInit(0.U(11.W))
  val counten = Wire(Bool())
  counten := false.B
  val localerror = appoutjudge.reduce(_+_)
  when(counten){
    errorcount := errorcount + localerror
  }
  io.errorbit := errorcount + localerror
  val postcount = RegInit(0.U(4.W))
  val GroupCheck  = Seq.fill(ROWNUM)(Seq.fill(BLKSIZE)(Wire(UInt(8.W))))
  for(i <- 0 until ROWNUM){
    for(j <- 0 until BLKSIZE){
      GroupCheck(i)(j) := CheckGroup(j)(i).io.Check
    }
  }
  val Checkjudge = VecInit(Seq.fill(ROWNUM)(0.U(8.W)))
  for(i <- 0 until ROWNUM){
    Checkjudge(i):= GroupCheck(i).reduce(_ + _)
  }
  val allcheck = Checkjudge.reduce(_+_)
  val allcheck1 = allcheck
  //io.errorbit
  //Checkjudge.reduce(_+_)
  io.check := allcheck
  //val rightdecide = appoutjudge.reduce(_ & _)
// read Matrix  
  val rsMatrix : Array[Array[Int]] = ReadMatrix.ReadM()
  //GenerateIO.Gen(2)
 //  注意LLR 的数据会延迟一个周期给Var   
 //  Var input   LLR  
  for(i <- 0 until BLKSIZE*2) {
    VarGroup(i).io.LLrin := LLrRams(i).read(LLrAddrRead)  
  }
  when(LLrWriteEn) {
    for (i <- 0 until BLKSIZE*2) {
      LLrRams(i).write(LLrAddrWrite,io.LLrin(i))
    }
  }
// Var input Checkin  
  for (i <- 0 until BLKSIZE*2) {
    for (j <- 0 until ROWNUM) {
      VarGroup(i).io.Checkin(j) := ChecktoVarMux(i)(j).io.output   
    
      ChecktoVarMux(i)(j).io.sel := ChecktoVarSel(j)
    }
  }
// Check input addr and data  
  // 这个信号在顶层是根dinvalid一块的，   可能会一直拉高，要限制一个处于idle态
  val start = Wire(Bool()) 
  val postvalid = Wire(Bool()) 
  postvalid := false.B
  for (i <- 0 until BLKSIZE) {
    for (j <- 0 until ROWNUM) {
     CheckGroup(i)(j).io.input(0) :=  VartoCheckMux(i)(j).io.output  
     CheckGroup(i)(j).io.input(1) :=  VartoCheckMux(i+BLKSIZE)(j).io.output  
     CheckGroup(i)(j).io.appsign(0) :=  apptoCheckMux(i)(j).io.output  
     CheckGroup(i)(j).io.appsign(1) :=  apptoCheckMux(i+BLKSIZE)(j).io.output  
     CheckGroup(i)(j).io.inputaddr := CheckAddr(j)
     CheckGroup(i)(j).io.inputsign(0) := SigntoCheckMux(i)(j).io.output  
     CheckGroup(i)(j).io.inputsign(1) := SigntoCheckMux(i+BLKSIZE)(j).io.output  
     CheckGroup(i)(j).io.updatemin := updateen  
     CheckGroup(i)(j).io.initial   := initialen  
     CheckGroup(i)(j).io.signreset := start///io.Start
     CheckGroup(i)(j).io.postvalid := postvalid
     CheckGroup(i)(j).io.checkreset := checkreset
     CheckGroup(i)(j).io.updatecheck := updatecheck
     //CheckGroup(i)(j).io.strongMessage := Mux(postcount===0.U,io.strongMessage,io.strongMessage0)
     //CheckGroup(i)(j).io.weakMessage := Mux(postcount===0.U,io.weakMessage,io.weakMessage0)
     CheckGroup(i)(j).io.strongMessage := io.strongMessage(postcount)
     CheckGroup(i)(j).io.weakMessage := io.weakMessage(postcount)
     //CheckGroup(i)(j).io.strongMessage  := Mux(postcount===0.U,strongMessage.U ,strongMessage0.U)
     //CheckGroup(i)(j).io.weakMessage    := Mux(postcount===0.U,weakMessage.U   ,weakMessage0.U)
    }
  }
//   VartoCheckMux 连线  一个有16个输入   来自16个变量节点的对应行的输出  选一个送给校验节点。
//   一个校验节点对应两个VartocheckMux 为  j 和 j+BLKSIZE
  for( i <- 0 until ROWNUM) {
    for( j <- 0 until BLKSIZE) {
      for( k <- 0 until COLNUM/2) {
        for (m <- 0 until BLKSIZE*2) {
          val z : Int = rsMatrix(i*BLKSIZE+j)(k*2*BLKSIZE+m)
          if(z == 1) {
            //横向第j个,对应纵向第m个 
            if(m < BLKSIZE) {
              VartoCheckMux(j)(i).io.input(k) := VarGroup(m).io.Checkout(i) 
              apptoCheckMux(j)(i).io.input(k) := VarGroup(m).io.APPout(APPWIDTHCOL-1)
            }else {
              VartoCheckMux(j+BLKSIZE)(i).io.input(k) := VarGroup(m).io.Checkout(i) 
              apptoCheckMux(j+BLKSIZE)(i).io.input(k) := VarGroup(m).io.APPout(APPWIDTHCOL-1)
            }
          }
        }
      }
      VartoCheckMux(j)(i).io.sel := VartoCheckSel(i)   
      VartoCheckMux(j+BLKSIZE)(i).io.sel := VartoCheckSel(i)   
      apptoCheckMux(j)(i).io.sel := apptoCheckSel(i)  
      apptoCheckMux(j+BLKSIZE)(i).io.sel := apptoCheckSel(i)
    }
  }
// ChecktoVarMux  连线  一个有16个输入，来自16个校验节点的输出   ，选一个送给 对应的变量节点对应的input  
// 输入是校验节点的输出   
// 分别表示该校验节点对应的第一列 和第二列变量节点
  for( i <- 0 until ROWNUM) {
    for( k <- 0 until COLNUM/2) {
      for( j <- 0 until BLKSIZE*2) {
        for (m <- 0 until BLKSIZE) {
          val z : Int = rsMatrix(i*BLKSIZE+m)(k*2*BLKSIZE+j)
          if(z == 1) {
            //纵向第j个var的输入是    横向第m个  
            if(j < BLKSIZE){
              ChecktoVarMux(j)(i).io.input(k) := CheckGroup(m)(i).io.minval(0) 
            }  
            else {
              ChecktoVarMux(j)(i).io.input(k) := CheckGroup(m)(i).io.minval(1)
            } 
          }
        }
        ChecktoVarMux(j)(i).io.sel := ChecktoVarSel(i) 
      }
    }
  }
// sign Sram 的mux   
for( i <- 0 until ROWNUM) {
  for( k <- 0 until COLNUM/2) {
    for( j <- 0 until BLKSIZE) {
      for (m <- 0 until BLKSIZE*2) {
        val z : Int = rsMatrix(i*BLKSIZE+j)(k*2*BLKSIZE+m)
        if(z == 1) {
          //横向第j个Check的输入是    纵向第m个readdata的第 i 位
          if(m < BLKSIZE) {
            SigntoCheckMux(j)(i).io.input(k) := SignReadData(m)(i) 
          }else {
            SigntoCheckMux(j+BLKSIZE)(i).io.input(k) := SignReadData(m)(i) 
          }
        }
      }
      SigntoCheckMux(j)(i).io.sel := SignMuxSel(i)   
      SigntoCheckMux(j+BLKSIZE)(i).io.sel := SignMuxSel(i)  
    }
  }
} 


// modeule connect   

 val idle :: initial:: decode :: postcheck::postinitial::postprocess :: Nil = Enum(6)
 // idle 等待  
 /****************************************/
 // initial: 初始化   
 /*
  * LLrin直接送给VarGroup同时写入LLrrams
  * Checknode的 updateen 打开 initial 打开 
  * counter 从0到15  
  * 下一个周期进入decode  
  * 新增counter1  在counter = 15时，counter1 要为0
  * 这样子进入decode的时候，才能立刻拿到LLrrams中的数据
  *
  *
  *
  */ 
 //decode : 译码 
  /*从initial过来的时候counter1已经+1变成了1 
   *LLR写  sign写 ,用counter地址 
   读都用counter1  
   *
   * 遍历完成时counter=15  counter1 = 0
   *这个时候就是用rightreg 和rightdecide判断解码是否正确 
   如果有提前结束的花就这样做 ，
   没有的话就Iter===1的时候结束 ，并且输出当前的判据   
   如果此时 顶层有译码的信号，表示要继续进数据  
   那么就跳回initial态 ，并且把 check的reset拉高  
   */ 
   /*
   修改之后用io.check判断解码是否正确

   */


  val currentState = RegInit(idle)


  //decode 状态下两个周期写回一个数据
  //第一个周期V2C读，然后第二个周期
  //拿到数据，向V2C写回  
  val Iter    = RegInit(0.U(ITERWITH.W))
  val postIter    = RegInit(0.U(ITERWITH.W))
  start := false.B //currentState === idle && io.Start
  switch(currentState){
    is(idle){
      when(io.Start) {
        currentState := initial 
        counter := 0.U
        for (i <- 0 until ROWNUM) {
          ChecktoVarSel(i) := 0.U 
          VartoCheckSel(i) := 0.U 
          apptoCheckSel(i) := 0.U 
          SignMuxSel(i)    := 0.U 
          CheckAddr(i)     := 0.U
        }
        counter1 := 0.U
        start := true.B
        //Iter := io.IterInput 
      }
    }
    is(initial){
      for(i <- 0 until BLKSIZE*2) {
        VarGroup(i).io.LLrin := io.LLrin(i)  
      }
      when(counter === 15.U) {
        Iter := io.IterInput
        checkreset := true.B
        currentState := decode 
        counter1 := counter1 + 1.U 
      }
      counter := counter + 1.U 
      for (i <- 0 until ROWNUM) {
        ChecktoVarSel(i) :=ChecktoVarSel(i) + 1.U 
        VartoCheckSel(i) :=VartoCheckSel(i) + 1.U 
        apptoCheckSel(i) :=apptoCheckSel(i) + 1.U 
        SignMuxSel(i)    :=SignMuxSel(i)    + 1.U 
        CheckAddr(i)     :=CheckAddr(i)     + 1.U
      }
      LLrWriteEn := true.B  
      initialen := true.B  
      updateen  := true.B  
      updatecheck := true.B
      signWriteEn := true.B 
    } 
    is(decode) {
      updateen  := true.B  
      updatecheck := true.B
      signWriteEn := true.B
      counter := counter + 1.U 
      for (i <- 0 until ROWNUM) {
        ChecktoVarSel(i) :=ChecktoVarSel(i) + 1.U 
        VartoCheckSel(i) :=VartoCheckSel(i) + 1.U 
        apptoCheckSel(i) :=apptoCheckSel(i) + 1.U 
        SignMuxSel(i)    :=SignMuxSel(i)    + 1.U 
        CheckAddr(i)     :=CheckAddr(i)     + 1.U
      }
      counter1 := counter1 + 1.U 
      //rightreg := rightreg & rightdecide  
      counten := true.B
      when(counter === 15.U){
        //提前终止 allcheck
        counten := false.B
        errorcount := 0.U
        checkreset := true.B
        when(((allcheck1)===0.U)||Iter===1.U) {
        //when(((rightreg & rightdecide)===1.U)||Iter===1.U) {
          io.OutValid := Mux(io.postvalid,(allcheck1) ===0.U,true.B) 
          io.Success  := (io.errorbit) ===0.U
          when((Iter===1.U)&&(io.errorbit =/= 0.U)&&(!io.postvalid)){
            errorrecord := 1.U
          }
          //rightreg := 1.U
          when(io.postvalid&&(Iter===1.U)&&(allcheck1 =/= 0.U)){
            currentState := postcheck
            checkreset := false.B
            counter := 0.U 
            for (i <- 0 until ROWNUM) {
              ChecktoVarSel(i) := 0.U 
              VartoCheckSel(i) := 0.U 
              apptoCheckSel(i) := 0.U 
              SignMuxSel(i)    := 0.U 
              CheckAddr(i)     := 0.U
            }
            counter1 := 1.U
          }.elsewhen(io.Start){
            currentState := initial 
            start := true.B 
          }.otherwise{
            currentState := idle  
          }
          //counter1 := 0.U
        }.otherwise{
            //rightreg := 1.U
            Iter := Iter - 1.U 
            currentState := decode 
        }
      } 
    }
    is (postcheck) {
      postvalid := true.B
      //checkreset := true.B
      counter := counter + 1.U
      for (i <- 0 until ROWNUM) {
        ChecktoVarSel(i) :=ChecktoVarSel(i) + 1.U 
        VartoCheckSel(i) :=VartoCheckSel(i) + 1.U 
        apptoCheckSel(i) :=apptoCheckSel(i) + 1.U 
        SignMuxSel(i)    :=SignMuxSel(i)    + 1.U 
        CheckAddr(i)     :=CheckAddr(i)     + 1.U
      }
      counter1 := counter1 + 1.U
      when(counter === 15.U) {
        postIter := io.postIterInput
        checkreset := true.B
        currentState := postprocess 
      }
      updateen  := true.B  
      signWriteEn := true.B 
    }
    //校验节点的check维持不变，postvalid
    is(postprocess){
      updateen  := true.B  
      signWriteEn := true.B
      updatecheck := true.B
      counter := counter + 1.U 
      for (i <- 0 until ROWNUM) {
        ChecktoVarSel(i) :=ChecktoVarSel(i) + 1.U 
        VartoCheckSel(i) :=VartoCheckSel(i) + 1.U 
        apptoCheckSel(i) :=apptoCheckSel(i) + 1.U 
        SignMuxSel(i)    :=SignMuxSel(i)    + 1.U 
        CheckAddr(i)     :=CheckAddr(i)     + 1.U
      }
      counter1 := counter1 + 1.U 
      //when(postIter === io.postIterInput){
      //  postvalid := true.B
      //}
      counten := true.B
      when(counter === 15.U){
        //提前终止 allcheck
        counten := false.B
        checkreset := true.B
        errorcount := 0.U
        when(((allcheck1)===0.U)||postIter===1.U) {
        //when(((rightreg & rightdecide)===1.U)||Iter===1.U) {
          io.OutValid :=  Mux(postcount=/=maxpostnum.U,(allcheck1) ===0.U,true.B)
          io.Success  := (io.errorbit) ===0.U
          //rightreg := 1.U
          when((postIter===1.U)&&(io.errorbit =/= 0.U)&&(postcount === maxpostnum.U)){
            errorrecord := 1.U
          }
          when((postcount =/= maxpostnum.U)&&(allcheck1 =/= 0.U)){
            currentState := postcheck
            checkreset := false.B
            //postIter := io.postIterInput
            counter := 0.U 
            for (i <- 0 until ROWNUM) {
              ChecktoVarSel(i) := 0.U 
              VartoCheckSel(i) := 0.U 
              apptoCheckSel(i) := 0.U 
              SignMuxSel(i)    := 0.U 
              CheckAddr(i)     := 0.U
            }
            counter1 := 1.U
          }.elsewhen(io.Start){
            currentState := initial 
            start := true.B 
          }.otherwise{
            currentState := idle  
          }
          //counter1 := 0.U
          postcount := postcount + 1.U
        }.otherwise{
            //rightreg := 1.U
            postIter := postIter - 1.U 
            currentState := postprocess 
        }
      } 
    }
    
  }
  io.IterOut := Iter 
 // for(i <- 0 until BLKSIZE) {
 //   io.appout(i) := VarGroup(i).io.APPout(APPWIDTHCOL-1)
 // }
  //new Module 
  //记录每次译码出错的错误比特    
  //记录时译码器应该处于 decode 态，并且Iter = 1 并且counter2 <=31 >=0
  // 
 // val appouterrornum = VecInit(Seq.fill(BLKSIZE)(0.U(BLKADDR.W)))
 // for(i <- 0 until BLKSIZE) {
 //   appouterrornum(i) := VarGroup(i).io.APPout(APPWIDTHCOL-1)
 // } 
 // val wrongnum = appouterrornum.reduce(_+_)
// //io.appout    =
  //for(i <- 0 until 2*BLKSIZE) {
  //  io.appout(i) := VarGroup(i).io.APPout   
  //}
  ////io.c2v       =
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.c2v(i+j*BLKSIZE) := CheckGroup(i)(j).io.minval(0)    
  //  } 
  //}
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.c2v0(i+j*BLKSIZE) := CheckGroup(i)(j).io.minval(1)    
  //  } 
  //}
  ////io.min       =
  //for(i <- 0 until BLKSIZE) {
  //   for ( j <- 0 until ROWNUM) {
  //     io.min(i+j*BLKSIZE) := CheckGroup(i)(j).io.min0   
  //   } 
  //}
  //for(i <- 0 until BLKSIZE) {
  //   for ( j <- 0 until ROWNUM) {
  //     io.min0(i+j*BLKSIZE) := CheckGroup(i)(j).io.min1   
  //   } 
  //}
  ////io.submin    =
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.submin(i+j*BLKSIZE) := CheckGroup(i)(j).io.submin0    
  //  } 
  //}
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.submin0(i+j*BLKSIZE) := CheckGroup(i)(j).io.submin1    
  //  } 
  //}
  ////io.minaddr   =
  //for(i <- 0 until BLKSIZE) {
  //   for ( j <- 0 until ROWNUM) {
  //     io.minaddr(i+j*BLKSIZE) := CheckGroup(i)(j).io.minaddr0    
  //   } 
  //}
  //for(i <- 0 until BLKSIZE) {
  //   for ( j <- 0 until ROWNUM) {
  //     io.minaddr0(i+j*BLKSIZE) := CheckGroup(i)(j).io.minaddr1    
  //   } 
  //}
  ////io.subminaddr=
  //for(i <- 0 until BLKSIZE) {
  // for ( j <- 0 until ROWNUM) {
  //   io.subminaddr(i+j*BLKSIZE) := CheckGroup(i)(j).io.subminaddr0    
  // } 
  //} 
  //for(i <- 0 until BLKSIZE) {
  // for ( j <- 0 until ROWNUM) {
  //   io.subminaddr0(i+j*BLKSIZE) := CheckGroup(i)(j).io.subminaddr1    
  // } 
  //}
  ////io.sign      =
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.sign(i+j*BLKSIZE) := CheckGroup(i)(j).io.sign0   
  //  } 
  //}
  //for(i <- 0 until BLKSIZE) {
  //  for ( j <- 0 until ROWNUM) {
  //    io.sign0(i+j*BLKSIZE) := CheckGroup(i)(j).io.sign1   
  //  } 
  //}
  ////io.v2csign   =
  //for(i <- 0 until BLKSIZE*COLNUM) {
  //  for (j <- 0 until ROWNUM) {
  //   io.v2csign(i)(j):= SignRams(i%(BLKSIZE*2)).read((i/(BLKSIZE*2)).U)(j)
  //  }
  //}
  //GenerateIO.Gen(3)
  //记录错误帧的LLR的寄存器
  //val recorden = Wire(Bool())
  //recorden := (errorrecord === 0.U)&&(currentState === initial)
  //val registersLLR = Seq.fill(32)(RegInit(VecInit(Seq.fill(64)(0.U(4.W)))))
  //when(recorden){
  //  for(i<-0 until 32){
  //    when (i.U === counter##(0.U(1.W))){
  //      for(j <- 0 until 64){
  //        registersLLR(i)(j) := io.LLrin(j)
  //      }
  //    }
  //    when (i.U === counter##(1.U(1.W))){
  //      for(j <- 0 until 64){
  //        registersLLR(i)(j) := io.LLrin(j+64)
  //      }
  //    }
  //  }
  //}
  //for(i <-0 until 32) {
  //  io.llrout(i) := Cat(registersLLR(i).map(_.asUInt()))
  //}

  //for(i <-0 until 32) {
  //  io.llroutcheck(i) := RegNext(io.llrout(i))//Cat(registersLLR(i).map(_.asUInt()))
  //}
  //for(i <-0 until 32) {
  //  io.llrou(i) := RegNext(io.llroutcheck(i))//Cat(registersLLR(i).map(_.asUInt()))
  //}
}
