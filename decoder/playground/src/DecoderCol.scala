package decoder 
import chisel3._
import chisel3.util._

class DecoderCol extends Module with COMMON {
  val io = IO(new Bundle {
    val LLrin     = Input(Vec(BLKSIZE,UInt(APPWIDTH.W)))
    val Start     = Input(Bool())
    val IterInput = Input(UInt(ITERWITH.W))
    //val Appout    = Output(Vec(COLNUM,UInt(APPWIDTH.W)))
    val OutValid  = Output(Bool())
    val Success   = Output(Bool())
    val IterOut   = Output(UInt(ITERWITH.W))
    val counter   = Output(UInt(BLKADDR.W))
    val appvalid  = Output(Bool())
    val appout    = Output(Vec(BLKSIZE,UInt(1.W)))

    // for difftest  
   // val appout    = Output(Vec(BLKSIZE,UInt(APPWIDTH.W))) 
   // val c2v       = Output(Vec(BLKSIZE*ROWNUM,UInt(C2VWIDTHCOL.W)))
  
   // val min       = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
   // val submin    = Output(Vec(BLKSIZE*ROWNUM,UInt((C2VWIDTHCOL-1).W)))
   // val minaddr   = Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
   // val subminaddr= Output(Vec(BLKSIZE*ROWNUM,UInt(COLADDR.W)))
   // val sign      = Output(Vec(BLKSIZE*ROWNUM,UInt(1.W)))
   // val v2csign   = Output(Vec(BLKSIZE*COLNUM,Vec(ROWNUM,UInt(1.W))))

  })
  // 按列作，一次作一列。 
  val VarGroup = Seq.fill(BLKSIZE)(Module(new VariableNode))   
  // 64*6个校验节点   
  val CheckGroup = Seq.fill(BLKSIZE)(Seq.fill(ROWNUM)(Module (new CheckNodeCOL))) 
  // 64 个ram 存LLR 消息 深度32  
  val LLrRams = Seq.fill(BLKSIZE)(SyncReadMem(COLNUM,UInt(APPWIDTH.W)))
  // 64 个ram 存v2c符号 深度32  数据位宽 rownum   
  val SignRams = Seq.fill(BLKSIZE)(SyncReadMem(COLNUM,UInt(ROWNUM.W)))
  //  Var 的out data 给CheckNode 的RAM
  val VartoCheckMux = Seq.fill(BLKSIZE)(Seq.fill(ROWNUM)(Module(new Mux32to1(V2CWIDTHCOL))))
  // Var 的符号给CheckNode的RAM  
  val SigntoCheckMux = Seq.fill(BLKSIZE)(Seq.fill(ROWNUM)(Module(new Mux32to1(1))))
  //  CheckNode 的output 送给这个mux ,mux从32个里选一个，送给Var的一个input  
  val ChecktoVarMux  = Seq.fill(BLKSIZE)(Seq.fill(ROWNUM)(Module(new Mux32to1(C2VWIDTHCOL))))
//  6选一 的 addr mux 
// EN wire 
  val V2CReadEn   = Wire(Bool())
  val C2VWriteEn  = Wire(Bool())
  val C2VReadEn   = Wire(Bool())
  val V2CWriteEn  = Wire(Bool())
  val C2VFlush    = Wire(Bool())
  val updateen    = Wire(Bool()) 
  val initialen   = Wire(Bool())
  V2CReadEn := false.B 
  C2VWriteEn := false.B
  C2VReadEn := false.B
  V2CWriteEn := false.B
  C2VFlush   := false.B  
  io.appvalid := false.B
  io.Success := false.B
  updateen := false.B 
  initialen := false.B
// LLR_RAM  
  val LLrReadEn = Wire(Bool())
  val LLrWriteEn = Wire(Bool())
  val LLrAddrWrite  = Wire(UInt(COLADDR.W))
  val LLrAddrRead   = Wire(UInt(COLADDR.W))
  LLrReadEn := false.B 
  LLrWriteEn := false.B
  //Seq.fill(COLNUM)(SyncReadMem(BLKSIZE,UInt(APPWIDTH.W)))
  //Seq.fill(COLNUM)(Mem(BLKSIZE,UInt(APPWIDTH.W)))

// Wire Connect 
  val counter     = RegInit(0.U((COLADDR+1).W))
  val counter1    = RegNext(counter)  
  val counter2    = RegNext(counter1) 
  io.counter := counter2 
  io.OutValid := false.B
  LLrAddrWrite := counter 
  LLrAddrRead  := counter1 
  val ChecktoVarSel = Wire(UInt(COLADDR.W)) 
  ChecktoVarSel := counter2  

  val VartoCheckSel = Wire(UInt(COLADDR.W)) 
  VartoCheckSel := counter2 

  val SignMuxSel = Wire(UInt(COLADDR.W)) 
  SignMuxSel := counter2 

  val CheckAddr = Wire(UInt(COLADDR.W))
  CheckAddr := counter2 

  val signReadAddr = Wire(UInt(COLADDR.W)) 
  signReadAddr := counter1
  val signWriteAddr = Wire(UInt(COLADDR.W)) 
  signWriteAddr := counter2 
  val signWriteEn = Wire(Bool()) 
  signWriteEn := false.B  

  val SignReadData = VecInit(Seq.fill(BLKSIZE)(0.U(ROWNUM.W)))  
  for (i <- 0 until BLKSIZE) {
    SignReadData(i) := SignRams(i).read(signReadAddr)  
  } 
  val SignWriteData = VecInit(Seq.fill(BLKSIZE)(0.U(ROWNUM.W)))
  for (i <- 0 until BLKSIZE) {
    SignWriteData(i) := Cat(VarGroup(i).io.Checkout.reverse.map(_.asUInt()(V2CWIDTHCOL-1)))
  }
  when(signWriteEn){
    for( i <- 0 until BLKSIZE) {
      SignRams(i).write(signWriteAddr,SignWriteData(i))
    }
  }

  val RowCounter  = RegInit(0.U(ROWADDR.W))
  val rightreg    = RegInit(1.U(1.W)) 
  val appoutjudge = VecInit(Seq.fill(BLKSIZE)(0.U(1.W)))
  for(i <- 0 until BLKSIZE) {
    appoutjudge(i) := ~VarGroup(i).io.APPout(APPWIDTHCOL-1)
  }
  val rightdecide = appoutjudge.reduce(_ & _)
// read Matrix  
  val rsMatrix : Array[Array[Int]] = ReadMatrix.ReadM()
  //GenerateIO.Gen(2)
 //  注意LLR 的数据会延迟一个周期给Var   
 //  Var input   LLR  
  for(i <- 0 until BLKSIZE) {
    VarGroup(i).io.LLrin := LLrRams(i).read(LLrAddrRead)  
  }
  when(LLrWriteEn) {
    for (i <- 0 until BLKSIZE) {
      LLrRams(i).write(LLrAddrWrite,io.LLrin(i))
    }
  }
// Var input Checkin  
  for (i <- 0 until BLKSIZE) {
    for (j <- 0 until ROWNUM) {
      VarGroup(i).io.Checkin(j) := ChecktoVarMux(i)(j).io.output   
    
      ChecktoVarMux(i)(j).io.sel := ChecktoVarSel
    }
  }
// Check input addr and data  
  // 这个信号在顶层是根dinvalid一块的，   可能会一直拉高，要限制一个处于idle态
  val start = Wire(Bool()) 
  for (i <- 0 until BLKSIZE) {
    for (j <- 0 until ROWNUM) {
     CheckGroup(i)(j).io.input :=  VartoCheckMux(i)(j).io.output//VarGroup(i).io.Checkout(j)  
     CheckGroup(i)(j).io.inputaddr := CheckAddr 
     CheckGroup(i)(j).io.inputsign := SigntoCheckMux(i)(j).io.output  
     CheckGroup(i)(j).io.updatemin := updateen  
     CheckGroup(i)(j).io.initial   := initialen  
     CheckGroup(i)(j).io.signreset := start///io.Start
    }
  }
//   VartoCheckMux 连线  一个有32个输入   来自32个变量节点的对应行的输出  选一个送给校验节点。
  for( i <- 0 until ROWNUM) {
    for( j <- 0 until BLKSIZE) {
      for (m <- 0 until BLKSIZE) {
        for( k <- 0 until COLNUM) {
          val z : Int = rsMatrix(i*BLKSIZE+j)(k*BLKSIZE+m)
          if(z == 1) {
            //横向第j个,对应纵向第m个
            VartoCheckMux(j)(i).io.input(k) := VarGroup(m).io.Checkout(i) 
          }
        }
      }
      VartoCheckMux(j)(i).io.sel := VartoCheckSel   
    }
  }
// ChecktoVarMux  连线  一个有32个输入，来自32个校验节点的输出   ，选一个送给 对应的变量节点对应的input  
  for( i <- 0 until ROWNUM) {
    for( k <- 0 until COLNUM) {
      for( j <- 0 until BLKSIZE) {
        for (m <- 0 until BLKSIZE) {
          val z : Int = rsMatrix(i*BLKSIZE+m)(k*BLKSIZE+j)
          if(z == 1) {
            //纵向第j个var的输入是    横向第m个  
            ChecktoVarMux(j)(i).io.input(k) := CheckGroup(m)(i).io.minval 
          }
        }
        ChecktoVarMux(j)(i).io.sel := ChecktoVarSel  
      }
    }
  }
// sign Sram 的mux   
for( i <- 0 until ROWNUM) {
  for( k <- 0 until COLNUM) {
    for( j <- 0 until BLKSIZE) {
      for (m <- 0 until BLKSIZE) {
        val z : Int = rsMatrix(i*BLKSIZE+j)(k*BLKSIZE+m)
        if(z == 1) {
          //横向第j个Check的输入是    纵向第m个readdata的第 i 位    
          SigntoCheckMux(j)(i).io.input(k) := SignReadData(m)(i) 
        }
      }
      SigntoCheckMux(j)(i).io.sel := SignMuxSel   
    }
  }
} 


// modeule connect   

 val idle :: initial:: decode :: judge :: Nil = Enum(4)
 // idle 等待  
 // initial: 初始化   couter 0 写LLRram  
 // couter1 下一个周期给0申请从0读  
 // couter2 再下一个周期给0，表示当前处理的是第0列，此时才能拿到写入的第0个LLR  
 // counter2===63的时候， counter1 和counter此时是0  跳decode  并且要保证counter这一个周期是1 
 // 用多一位，让counter可以继续往上，不然读写使能不好控制
 // 这样子下一个周期counter1才是1。
 // 这个要求只要counter一直+1就可以
 // decode : 上一个周期counter1已经是0，这一个周期可以拿到LLR0 中的数据，以及各个variableout  
 // 这一个周期counter2也是0，处理第0列  
 // 当counter2 === 63的时候，跳judge 并且judge时，counter要是1,couter1要是0  
 // 保证judge不过时，能在跳回decode时，直接拿到0的数据  
 // 这个要求counter在counter2===62的时候跳回0一个周期，后面一直+  
 // judge : 通过，就跳回idle .... 
 //
 // couter 是LLRRAMS 的写   
 // counter1是LLRRAMS的读 SignRams的读   
 // counter2 是SignRams的写 
 // counter  


  val currentState = RegInit(idle)


  //decode 状态下两个周期写回一个数据
  //第一个周期V2C读，然后第二个周期
  //拿到数据，向V2C写回  
  val Iter    = RegInit(0.U(ITERWITH.W))
  start := currentState === idle && io.Start
  switch(currentState){
    is(idle){
      when(io.Start) {
        currentState := initial 
        counter := 0.U
        RowCounter := 0.U 
        counter1 := 33.U
        counter2 := 33.U 
        Iter := io.IterInput 
      }
    }
    is(initial){
      when(counter2 === (COLNUM-1).U){
        currentState := decode       
        counter := 2.U 
        counter1 := 1.U  
        counter2 := 0.U 
        LLrAddrRead := 0.U 
        signReadAddr := 0.U
      }.otherwise{
        counter := counter + 1.U
      }
      when(counter>= 0.U && counter <=31.U) {
        LLrWriteEn := true.B  
      }
      when(counter2 >= 0.U && counter2 <= 31.U) {
        initialen := true.B  
        updateen  := true.B  
        signWriteEn := true.B 
      }
    }
 
    is(decode) {
      when(counter2 === (COLNUM-1).U){
        currentState := judge         
        // 保证下一个周期即使失败， 跳回decode仍能拿到0中的数据  
        counter := 1.U   
        counter1 := 0.U  
        counter2 := 0.U 
      }.otherwise{
        counter := counter + 1.U
      }
      when(counter2 >= 0.U && counter2 <= 31.U) {
        updateen  := true.B  
        signWriteEn := true.B 
        rightreg := rightreg & rightdecide  
        when(!rightdecide) {
          io.appvalid := true.B
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
          currentState := decode    
          counter := counter + 1.U
        }
      }
    }
  }
  io.IterOut := Iter 
  for(i <- 0 until BLKSIZE) {
    io.appout(i) := VarGroup(i).io.APPout(APPWIDTHCOL-1)
  }
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
//  for(i <- 0 until BLKSIZE) {
//    io.appout(i) := VarGroup(i).io.APPout   
//  }
// //io.c2v       =
//  for(i <- 0 until BLKSIZE) {
//    for ( j <- 0 until ROWNUM) {
//      io.c2v(i+j*BLKSIZE) := CheckGroup(i)(j).io.minval    
//    } 
//  }
// //io.min       =
// for(i <- 0 until BLKSIZE) {
//    for ( j <- 0 until ROWNUM) {
//      io.min(i+j*BLKSIZE) := CheckGroup(i)(j).io.min   
//    } 
//  }
// //io.submin    =
//  for(i <- 0 until BLKSIZE) {
//    for ( j <- 0 until ROWNUM) {
//      io.submin(i+j*BLKSIZE) := CheckGroup(i)(j).io.submin    
//    } 
//  }
//  //io.minaddr   =
// for(i <- 0 until BLKSIZE) {
//    for ( j <- 0 until ROWNUM) {
//      io.minaddr(i+j*BLKSIZE) := CheckGroup(i)(j).io.minaddr    
//    } 
//  }
//  //io.subminaddr=
//  for(i <- 0 until BLKSIZE) {
//   for ( j <- 0 until ROWNUM) {
//     io.subminaddr(i+j*BLKSIZE) := CheckGroup(i)(j).io.subminaddr    
//   } 
//  } 
//  //io.sign      =
//  for(i <- 0 until BLKSIZE) {
//    for ( j <- 0 until ROWNUM) {
//      io.sign(i+j*BLKSIZE) := CheckGroup(i)(j).io.sign   
//    } 
//  }
// //io.v2csign   =
// for(i <- 0 until BLKSIZE*COLNUM) {
//   for (j <- 0 until ROWNUM) {
//
//    io.v2csign(i)(j):= SignRams(i%BLKSIZE).read((i/BLKSIZE).U)(j)
// 
//   }
//
// }
 //GenerateIO.Gen(3)
}
