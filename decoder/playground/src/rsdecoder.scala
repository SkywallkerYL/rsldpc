package decoder 
import chisel3._
import chisel3.util._
import scala.util.Random
class rsdecoder extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput   = Input(UInt(ITERWITH.W))
    val maxError    = Input(UInt(FRAMEWITH.W))
    val start       = Input(Bool())
    val nextready   = Input(Bool())
    val p0          = Input(Vec(PNUM,UInt(64.W))) 
    val Framevalid  = Output(Bool())
    val totalframe  = Output(UInt(FRAMEWITH.W))
    val errorframe  = Output(UInt(FRAMEWITH.W)) 

    //错误比特记录 
    val localvalid  = Input(Bool())
    val updatevalid = Input(Bool())
    val Errorcol    = Output(Vec(MAXERRORNUM,UInt((COLADDR).W))) 
    val Errorblk    = Output(Vec(MAXERRORNUM,UInt((BLKADDR).W))) 
    val Errornum    = Output(UInt(COLADDR.W))                      
    val outvalid    = Output(Bool())

  })
  
  val errorframe = RegInit(0.U(FRAMEWITH.W))
  val maxerror   = RegInit(0.U(FRAMEWITH.W))
  val totalframe = RegInit(0.U(FRAMEWITH.W))  
  io.totalframe := totalframe 
  io.errorframe := errorframe 
  io.Framevalid := false.B

  // 模块例化
  val decoder   = Module(new DecoderCol)  

  val errorbit  = Module(new Errorbits)
  val Gaussgen  = Module(new GngWrapper(BLKSIZE)) 
  val dinvalid  = Wire(Bool())
  dinvalid := false.B
  // 模块连线  
  for (i <- 0 until BLKSIZE) {
    Gaussgen.io.din(i) := 0.U  
  }
  Gaussgen.io.dinvalid := dinvalid 
  //p0信号需要一直拉高  
  Gaussgen.io.p0       := io.p0    
  decoder.io.LLrin     := Gaussgen.io.dout 
  decoder.io.Start     := dinvalid  
  decoder.io.IterInput := io.IterInput 

  //错误比特记录相关模块 
  errorbit.io.appin := decoder.io.appout
  errorbit.io.appvalid := decoder.io.appvalid  
  errorbit.io.coladdr := decoder.io.counter 

  errorbit.io.localvalid := io.localvalid
  errorbit.io.updatevalid := io.updatevalid 

  io.Errorcol := errorbit.io.Errorcol 
  io.Errorblk := errorbit.io.Errorblk 
  io.Errornum := errorbit.io.Errornum 
  io.outvalid := errorbit.io.outvalid

  val idle :: framestart :: frameinitial :: decode :: framevalid :: frameend :: Nil = Enum(6)
  // idle         : 等待   
  // framestart   : 开始仿真 初始化 噪声sigma等信息p0 最大错误帧   
  // frameinitial : 给decoder进行LLr初始化   
  // decode       : 等待decoder译码  
  // framevalid   : decoder 译码完成，统计错误帧  
  // 如果到达最大错误 则进入frameend 进行信息的输出 
  // 否则跳回 framestart  
  // frameend     : valid 拉高，输出错误帧等信息，如果有下一个sigma的使能，则 
  // 跳回 framestart  
  // 否则，跳转到idle 
  val currentState = RegInit(idle) 
  switch (currentState ) {
    is(idle) {
      when(io.start){
        currentState := framestart 
        totalframe := 0.U 
        errorframe := 0.U  
        maxerror   := io.maxError 
      }
    }
    is(framestart) {
      //这个周期dinvalid 拉高   
      //噪声发生器下个周期输出噪声   
      //Decoder 下个周期进入 initial 态，开始接受噪声 
      dinvalid := true.B 
      currentState := frameinitial 
    }
    is(frameinitial) {
      dinvalid := true.B  
      when(decoder.io.counter === (COLNUM-1).U) {
        dinvalid := false.B 
        currentState := decode  
      }
    }
    is(decode) {
      when(decoder.io.OutValid){
        totalframe := totalframe + 1.U 
        when(!(decoder.io.Success)) {
          errorframe := errorframe + 1.U 
        }
        currentState := framevalid 
      }

    }
    is(framevalid) {
      when(errorframe === maxerror) {
        currentState := frameend 
      }.otherwise{
        currentState := framestart  
      }
    }
    is(frameend) {
      io.Framevalid := true.B 
      //currentState := idle
      when(io.nextready) {
        currentState := idle 
      }
    }

  }

}

class rsdecoder2col extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput   = Input(UInt(ITERWITH.W))
    val maxError    = Input(UInt(FRAMEWITH.W))
    val start       = Input(Bool())
    val nextready   = Input(Bool())
    val p0          = Input(Vec(PNUM,UInt(PWITH.W))) 
    val Framevalid  = Output(Bool())
    val totalframe  = Output(UInt(FRAMEWITH.W))
    val errorframe  = Output(UInt(FRAMEWITH.W)) 
    val errorbit  = Output(UInt(FRAMEWITH.W))
    val checknum  = Output(UInt(FRAMEWITH.W))
    //val iter      = Output(UInt(FRAMEWITH.W))
    val postvalid = Input(Bool())
    val strongMessage  = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    val weakMessage    = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    //val strongMessage = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage   = Input(UInt(C2VWIDTHCOL.W))
    //val strongMessage0 = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage0   = Input(UInt(C2VWIDTHCOL.W))
    val postIterInput = Input(UInt(ITERWITH.W))
    //val errorbit  = Output(UInt(11.W))
    //val llrout      = Output(Vec(32,UInt(256.W)))
    //val llroutcheck = Output((Vec(32,UInt(256.W))))
    //val llrou      = Output(Vec(32,UInt(256.W)))
    val errorflush  = Input(Bool())
    val errorvalid  = Output(Bool())
    //错误比特记录 
    //val localvalid  = Input(Bool())
    //val updatevalid = Input(Bool())
    //val Errorcol    = Output(Vec(MAXERRORNUM,UInt((COLADDR).W))) 
    //val Errorblk    = Output(Vec(MAXERRORNUM,UInt((BLKADDR).W))) 
    //val Errornum    = Output(UInt(COLADDR.W))                      
    //val outvalid    = Output(Bool())

  })
  
  val errorframe = RegInit(0.U(FRAMEWITH.W))
  val maxerror   = RegInit(0.U(FRAMEWITH.W))
  val totalframe = RegInit(0.U(FRAMEWITH.W)) 
  val errorbit   = RegInit(0.U(FRAMEWITH.W))
  val itercount  = RegInit(0.U(FRAMEWITH.W))
  val checknum   = RegInit(0.U(FRAMEWITH.W))

  io.totalframe := totalframe 
  io.errorframe := errorframe 
  io.Framevalid := false.B
  io.errorbit := errorbit
  //io.iter     := itercount
  io.checknum := checknum
  // 模块例化
  val decoder   = Module(new Decoder2Col)  

  //val errorbit  = Module(new Errorbits)
  val Gaussgen  = Module(new GngWrapper(2*BLKSIZE)) 
  val dinvalid  = Wire(Bool())
  dinvalid := false.B
  // 模块连线  
  for (i <- 0 until 2*BLKSIZE) {
    Gaussgen.io.din(i) := 0.U  
  }
  Gaussgen.io.dinvalid := dinvalid 
  //p0信号需要一直拉高  
  Gaussgen.io.p0       := io.p0    
  decoder.io.LLrin     := Gaussgen.io.dout 
  decoder.io.Start     := dinvalid  
  decoder.io.IterInput := io.IterInput 
/*
  dinvalid  连接到io.start,进而关系所有check的符号复位
  这个是一个高扇出信号
  
  需要修改一下，降低扇出，首先改成寄存器的形式

*/



  decoder.io.postvalid     := RegNext(io.postvalid)
  //decoder.io.strongMessage     := io.strongMessage 
  decoder.io.strongMessage     := RegNext(io.strongMessage)
  //decoder.io.weakMessage     := io.weakMessage 
  decoder.io.weakMessage     := RegNext(io.weakMessage)
  //decoder.io.strongMessage0     := io.strongMessage0
  //decoder.io.weakMessage0     := io.weakMessage0 
  decoder.io.postIterInput   := RegNext(io.postIterInput) 
  //io.llrou          := decoder.io.llrou   
  //io.llrout          := decoder.io.llrout     
  //io.llroutcheck     := decoder.io.llroutcheck
  decoder.io.errorflush      := io.errorflush 
  io.errorvalid      := decoder.io.errorvalid 
  //错误比特记录相关模块 
  //errorbit.io.appin := decoder.io.appout
  //errorbit.io.appvalid := decoder.io.appvalid  
  //errorbit.io.coladdr := decoder.io.counter 

  //errorbit.io.localvalid := io.localvalid
  //errorbit.io.updatevalid := io.updatevalid 

  //io.Errorcol := errorbit.io.Errorcol 
  //io.Errorblk := errorbit.io.Errorblk 
  //io.Errornum := errorbit.io.Errornum 
  //io.outvalid := errorbit.io.outvalid

  val idle :: framestart :: frameinitial :: decode :: framevalid :: frameend :: Nil = Enum(6)
  // idle         : 等待   
  // framestart   : 开始仿真 初始化 噪声sigma等信息p0 最大错误帧   
  // frameinitial : 给decoder进行LLr初始化   
  // decode       : 等待decoder译码  
  // framevalid   : decoder 译码完成，统计错误帧  
  // 如果到达最大错误 则进入frameend 进行信息的输出 
  // 否则跳回 framestart  
  // frameend     : valid 拉高，输出错误帧等信息，如果有下一个sigma的使能，则 
  // 跳回 framestart  
  // 否则，跳转到idle 
  val currentState = RegInit(idle) 
  switch (currentState ) {
    is(idle) {
      when(io.start){
        currentState := framestart 
        totalframe := 0.U 
        errorframe := 0.U  
        errorbit   := 0.U
        itercount  := 0.U
        checknum   := 0.U
        maxerror   := io.maxError 
      }
    }
    is(framestart) {
      //这个周期dinvalid 拉高   
      //噪声发生器下个周期输出噪声   
      //Decoder 下个周期进入 initial 态，开始接受噪声 
      dinvalid := true.B 
      currentState := frameinitial 
    }
    is(frameinitial) {
      dinvalid := true.B  
      when(decoder.io.counter === (COLNUM/2-1).U) {
        dinvalid := false.B 
        currentState := decode  
      }
    }
    is(decode) {
      when(decoder.io.OutValid){
        totalframe := totalframe + 1.U 
        itercount  := itercount + decoder.io.IterOut
        when(!(decoder.io.Success)) {
          errorframe := errorframe + 1.U 
          errorbit   := errorbit + decoder.io.errorbit
          checknum   := checknum + decoder.io.check
        }
        currentState := framevalid 
      }

    }
    is(framevalid) {
      when(errorframe === maxerror) {
        currentState := frameend 
      }.otherwise{
        currentState := framestart  
      }
    }
    is(frameend) {
      io.Framevalid := true.B 
      //currentState := idle
      when(io.nextready) {
        currentState := idle 
      }
    }

  }

}


class rsdecoder2colGauss extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput   = Input(UInt(ITERWITH.W))
    val maxError    = Input(UInt(FRAMEWITH.W))
    val start       = Input(Bool())
    val nextready   = Input(Bool())
    //val p0          = Input(Vec(PNUM,UInt(PWITH.W)))
    val sigma       = Input(UInt(16.W)) 
    val Framevalid  = Output(Bool())
    val totalframe  = Output(UInt(FRAMEWITH.W))
    val errorframe  = Output(UInt(FRAMEWITH.W)) 
    val errorbit  = Output(UInt(FRAMEWITH.W))
    val checknum  = Output(UInt(FRAMEWITH.W))
    //val iter      = Output(UInt(FRAMEWITH.W))
    val postvalid = Input(Bool())
    val strongMessage  = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    val weakMessage    = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))

    val postIterInput = Input(UInt(ITERWITH.W))
    val errorflush  = Input(Bool())
    val errorvalid  = Output(Bool())

  })
  
  val errorframe = RegInit(0.U(FRAMEWITH.W))
  val maxerror   = RegInit(0.U(FRAMEWITH.W))
  val totalframe = RegInit(0.U(FRAMEWITH.W)) 
  val errorbit   = RegInit(0.U(FRAMEWITH.W))
  val itercount  = RegInit(0.U(FRAMEWITH.W))
  val checknum   = RegInit(0.U(FRAMEWITH.W))

  io.totalframe := totalframe 
  io.errorframe := errorframe 
  io.Framevalid := false.B
  io.errorbit := errorbit
  //io.iter     := itercount
  io.checknum := checknum
  // 模块例化
  val decoder   = Module(new Decoder2Colnew)  

  //val errorbit  = Module(new Errorbits)
  
  val Gaussgen  =  Seq.fill(32)(Module(new boxmullerout))
  
  val mapper = Module(new dsqmapper)

  val demapper = Module(new dsqdemapper)

  /*
    首先读  coded 和 uncoded
    进行一个mapper 
    两列两列的做，分别送进去
      输出的数值是对应的映射值，为32 * 2
    mapper的输出和Gaussgen的输出相+ 

    数值送给demapper

    demapper的输出送给decoder的llrin
    coded 的码字也送给decoder的codein

  */
  val DecoderCouter = Wire(UInt(4.W))
  val codedread   : Array[Int] = ReadCoded.ReadC()  
  val uncodedread : Array[Int] = ReadunCoded.ReadU()

  val coded   = VecInit(Seq.fill(2048)(0.U(1.W)))
  val uncoded = VecInit(Seq.fill(1536)(0.U(1.W)))

  val dincoded = VecInit(Seq.fill(128)(0.U(1.W)))
  val dinuncoded = VecInit(Seq.fill(96)(0.U(1.W)))
  
  for (i <- 0 until 2048) {
    coded(i) := codedread(i).U
  }
  for (i <- 0 until 1536) {
    uncoded(i) := uncodedread(i).U
  }
  val codedmux = Seq.fill(128)(Module(new Mux16to1(1)))

  val uncodemux = Seq.fill(96)(Module(new Mux16to1(1)))
  for(i <- 0 until 128){
    for(j <- 0 until 16){
      codedmux(i).io.input(j) := coded(j*128+i)
    }
    codedmux(i).io.sel := DecoderCouter
    dincoded(i) := codedmux(i).io.output
  }
  for(i <- 0 until 96){
    for(j <- 0 until 16){
      uncodemux(i).io.input(j) := coded(j*96+i)
    }
    uncodemux(i).io.sel := DecoderCouter
    dinuncoded(i) := uncodemux(i).io.output
  }
  //mapper
  mapper.io.coded := dincoded
  mapper.io.uncoded := dinuncoded

  //addnoise 

  for(i <- 0 until 32) {
    demapper.io.s1(i) := mapper.io.a1(i) + Gaussgen(i).io.x0
    demapper.io.s2(i) := mapper.io.a2(i) + Gaussgen(i).io.x1
  }
  //Noise gen parameter
  val random1 = new Random()
  val random2 = new Random()
  val upperBond = BigInt(2).pow(32) - 1 
  for(i <- 0 until 32) {
    Gaussgen(i).io.init := false.B 
    Gaussgen(i).io.ce   := true.B
    Gaussgen(i).io.seed0 :=BigInt(upperBond.bitLength,random1).min(upperBond).U
    Gaussgen(i).io.seed1 := BigInt(upperBond.bitLength,random2).min(upperBond).U
    Gaussgen(i).io.sigma := io.sigma

  }

  //Module(new GngWrapper(2*BLKSIZE)) 
  val dinvalid  = Wire(Bool())
  dinvalid := false.B
  // 模块连线  
  //for (i <- 0 until 2*BLKSIZE) {
  //  Gaussgen.io.din(i) := 0.U  
  //}
  //Gaussgen.io.dinvalid := dinvalid 
  //p0信号需要一直拉高  
  //Gaussgen.io.p0       := io.p0    
  decoder.io.LLrin     := demapper.io.llr
  decoder.io.codein    := dincoded
  decoder.io.Start     := dinvalid  
  decoder.io.IterInput := io.IterInput 
  DecoderCouter        := decoder.io.counter
/*
  dinvalid  连接到io.start,进而关系所有check的符号复位
  这个是一个高扇出信号
  
  需要修改一下，降低扇出，首先改成寄存器的形式

*/



  decoder.io.postvalid     := RegNext(io.postvalid)
  //decoder.io.strongMessage     := io.strongMessage 
  decoder.io.strongMessage     := RegNext(io.strongMessage)
  //decoder.io.weakMessage     := io.weakMessage 
  decoder.io.weakMessage     := RegNext(io.weakMessage)
  //decoder.io.strongMessage0     := io.strongMessage0
  //decoder.io.weakMessage0     := io.weakMessage0 
  decoder.io.postIterInput   := RegNext(io.postIterInput) 
  //io.llrou          := decoder.io.llrou   
  //io.llrout          := decoder.io.llrout     
  //io.llroutcheck     := decoder.io.llroutcheck
  decoder.io.errorflush      := io.errorflush 
  io.errorvalid      := decoder.io.errorvalid 
  //错误比特记录相关模块 

  val idle :: framestart :: frameinitial :: decode :: framevalid :: frameend :: Nil = Enum(6)
  // idle         : 等待   
  // framestart   : 开始仿真 初始化 噪声sigma等信息p0 最大错误帧   
  // frameinitial : 给decoder进行LLr初始化   
  // decode       : 等待decoder译码  
  // framevalid   : decoder 译码完成，统计错误帧  
  // 如果到达最大错误 则进入frameend 进行信息的输出 
  // 否则跳回 framestart  
  // frameend     : valid 拉高，输出错误帧等信息，如果有下一个sigma的使能，则 
  // 跳回 framestart  
  // 否则，跳转到idle 
  val currentState = RegInit(idle) 
  switch (currentState ) {
    is(idle) {
      when(io.start){
        currentState := framestart 
        totalframe := 0.U 
        errorframe := 0.U  
        errorbit   := 0.U
        itercount  := 0.U
        checknum   := 0.U
        maxerror   := io.maxError 
      }
    }
    is(framestart) {
      //这个周期dinvalid 拉高   
      //噪声发生器下个周期输出噪声   
      //Decoder 下个周期进入 initial 态，开始接受噪声 
      //要等待噪声发生器的输出有效之后再进入Decoder
      when(Gaussgen(0).io.x_en){
        dinvalid := true.B 
        currentState := frameinitial
      }
    }
    is(frameinitial) {
      dinvalid := true.B  
      when(decoder.io.counter === (COLNUM/2-1).U) {
        dinvalid := false.B 
        currentState := decode  
      }
    }
    is(decode) {
      when(decoder.io.OutValid){
        totalframe := totalframe + 1.U 
        itercount  := itercount + decoder.io.IterOut
        when(!(decoder.io.Success)) {
          errorframe := errorframe + 1.U 
          errorbit   := errorbit + decoder.io.errorbit
          checknum   := checknum + decoder.io.check
        }
        currentState := framevalid 
      }

    }
    is(framevalid) {
      when(errorframe === maxerror) {
        currentState := frameend 
      }.otherwise{
        currentState := framestart  
      }
    }
    is(frameend) {
      io.Framevalid := true.B 
      //currentState := idle
      when(io.nextready) {
        currentState := idle 
      }
    }

  }

}




class rsdecodertop extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput   = Input(UInt(ITERWITH.W))
    val maxError    = Input(UInt(FRAMEWITH.W))
    val start       = Input(Bool())
    val nextready   = Input(Bool())
    val p0          = Input(Vec(PNUM,UInt(PWITH.W))) 
    val Framevalid  = Output(Bool())
    val totalframe  = Output(UInt(FRAMEWITH.W))
    val errorframe  = Output(UInt(FRAMEWITH.W)) 
    val errorbit    = Output(UInt(FRAMEWITH.W))
    val checknum  = Output(UInt(FRAMEWITH.W))
    //val iter        = Output(UInt(FRAMEWITH.W))
    val postvalid = Input(Bool())
    val strongMessage  = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    val weakMessage    = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    //val strongMessage = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage   = Input(UInt(C2VWIDTHCOL.W))
    //val strongMessage0 = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage0   = Input(UInt(C2VWIDTHCOL.W))
    val postIterInput = Input(UInt(ITERWITH.W))

    //val llrout      = Output(Vec(32,UInt(256.W)))
    //val llroutcheck = Output((Vec(32,UInt(256.W))))
    //val llrou      = Output(Vec(32,UInt(256.W)))
    val errorflush  = Input(Bool())
    val errorvalid  = Output(Bool())
    //val localvalid  = Input(Bool())
    //val updatevalid = Input(Bool())
    //val Errorcol    = Output(Vec(MAXERRORNUM,UInt((COLADDR).W))) 
    //val Errorblk    = Output(Vec(MAXERRORNUM,UInt((BLKADDR).W))) 
    //val Errornum    = Output(UInt(COLADDR.W))                      
    //val outvalid    = Output(Bool())



  })
  val DecoderGroup = Seq.fill(PARRELNUM)(Module(new rsdecoder2col)) 
  for ( i <- 0 until PARRELNUM) {
    DecoderGroup(i).io.start := io.start 
    DecoderGroup(i).io.IterInput := io.IterInput
    DecoderGroup(i).io.nextready := io.nextready 
    DecoderGroup(i).io.maxError  := io.maxError  
    DecoderGroup(i).io.p0        := io.p0   
    DecoderGroup(i).io.postvalid     := io.postvalid
    DecoderGroup(i).io.strongMessage := io.strongMessage 
    DecoderGroup(i).io.weakMessage   := io.weakMessage 
    //DecoderGroup(i).io.strongMessage0 := io.strongMessage0 
    //DecoderGroup(i).io.weakMessage0   := io.weakMessage0 
    DecoderGroup(i).io.postIterInput := io.postIterInput 
    //DecoderGroup(i).io.postIterInput := io.postIterInput
    //io.llrout        := DecoderGroup(i).io.llrout  
    //io.llrou        := DecoderGroup(i).io.llrou  
    //io.llroutcheck   := DecoderGroup(i).io.llroutcheck      
    DecoderGroup(i).io.errorflush    := io.errorflush    
    io.errorvalid    := DecoderGroup(i).io.errorvalid    
  }
  io.Framevalid := DecoderGroup(0).io.Framevalid 
  val totalframenum = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val errorframenum = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val errorbitnum   = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val iternum       = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val checkall      = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val outvalidnum   = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val outvalidsign  = Wire(UInt(PARRELNUM.W))
  for( i <- 0 until PARRELNUM) {
    totalframenum(i) := DecoderGroup(i).io.totalframe 
    errorframenum(i) := DecoderGroup(i).io.errorframe 
    errorbitnum(i)   := DecoderGroup(i).io.errorbit
    //iternum(i)       := DecoderGroup(i).io.iter
    checkall(i)      := DecoderGroup(i).io.checknum
    //outvalidnum(i)   := DecoderGroup(i).io.outvalid
    //DecoderGroup(i).io.updatevalid := io.updatevalid  
    //DecoderGroup(i).io.localvalid := io.localvalid 
    //outvalidsign(i)  := DecoderGroup(i).io.outvalid 
  }
  io.totalframe := totalframenum.reduce(_+_)
  io.errorframe := errorframenum.reduce(_+_)
  io.errorbit   := errorbitnum.reduce(_+_)
  //io.iter       := iternum.reduce(_+_)
  io.checknum   := checkall.reduce(_+_)
  //io.outvalid   := outvalidnum.reduce(_|_) 
  //io.Errorcol := MuxCase(VecInit(Seq.fill(MAXERRORNUM)(0.U(COLADDR.W))),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errorcol 

  //})
  //io.Errorblk := MuxCase(VecInit(Seq.fill(MAXERRORNUM)(0.U(BLKADDR.W))),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errorblk 
  //})
  // io.Errornum := MuxCase(0.U(COLADDR.W),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errornum 
  //})

} 


class rsdecodertopGauss extends Module with COMMON {
  val io = IO(new Bundle {
    val IterInput   = Input(UInt(ITERWITH.W))
    val maxError    = Input(UInt(FRAMEWITH.W))
    val start       = Input(Bool())
    val nextready   = Input(Bool())
    val sigma       = Input(UInt(18.W))
    val Framevalid  = Output(Bool())
    val totalframe  = Output(UInt(FRAMEWITH.W))
    val errorframe  = Output(UInt(FRAMEWITH.W)) 
    val errorbit    = Output(UInt(FRAMEWITH.W))
    val checknum  = Output(UInt(FRAMEWITH.W))
    //val iter        = Output(UInt(FRAMEWITH.W))
    val postvalid = Input(Bool())
    val strongMessage  = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    val weakMessage    = Input(Vec(maxpostnum+1, UInt(C2VWIDTHCOL.W)))
    //val strongMessage = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage   = Input(UInt(C2VWIDTHCOL.W))
    //val strongMessage0 = Input(UInt(C2VWIDTHCOL.W))
    //val weakMessage0   = Input(UInt(C2VWIDTHCOL.W))
    val postIterInput = Input(UInt(ITERWITH.W))

    //val llrout      = Output(Vec(32,UInt(256.W)))
    //val llroutcheck = Output((Vec(32,UInt(256.W))))
    //val llrou      = Output(Vec(32,UInt(256.W)))
    val errorflush  = Input(Bool())
    val errorvalid  = Output(Bool())
    //val localvalid  = Input(Bool())
    //val updatevalid = Input(Bool())
    //val Errorcol    = Output(Vec(MAXERRORNUM,UInt((COLADDR).W))) 
    //val Errorblk    = Output(Vec(MAXERRORNUM,UInt((BLKADDR).W))) 
    //val Errornum    = Output(UInt(COLADDR.W))                      
    //val outvalid    = Output(Bool())



  })
  val DecoderGroup = Seq.fill(PARRELNUM)(Module(new rsdecoder2colGauss)) 
  for ( i <- 0 until PARRELNUM) {
    DecoderGroup(i).io.start := io.start 
    DecoderGroup(i).io.IterInput := io.IterInput
    DecoderGroup(i).io.nextready := io.nextready 
    DecoderGroup(i).io.maxError  := io.maxError  
    DecoderGroup(i).io.sigma       := io.sigma   
    DecoderGroup(i).io.postvalid     := io.postvalid
    DecoderGroup(i).io.strongMessage := io.strongMessage 
    DecoderGroup(i).io.weakMessage   := io.weakMessage 
    //DecoderGroup(i).io.strongMessage0 := io.strongMessage0 
    //DecoderGroup(i).io.weakMessage0   := io.weakMessage0 
    DecoderGroup(i).io.postIterInput := io.postIterInput 
    //DecoderGroup(i).io.postIterInput := io.postIterInput
    //io.llrout        := DecoderGroup(i).io.llrout  
    //io.llrou        := DecoderGroup(i).io.llrou  
    //io.llroutcheck   := DecoderGroup(i).io.llroutcheck      
    DecoderGroup(i).io.errorflush    := io.errorflush    
    io.errorvalid    := DecoderGroup(i).io.errorvalid    
  }
  io.Framevalid := DecoderGroup(0).io.Framevalid 
  val totalframenum = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val errorframenum = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val errorbitnum   = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val iternum       = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  val checkall      = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val outvalidnum   = VecInit(Seq.fill(PARRELNUM)(0.U(FRAMEWITH.W)))
  //val outvalidsign  = Wire(UInt(PARRELNUM.W))
  for( i <- 0 until PARRELNUM) {
    totalframenum(i) := DecoderGroup(i).io.totalframe 
    errorframenum(i) := DecoderGroup(i).io.errorframe 
    errorbitnum(i)   := DecoderGroup(i).io.errorbit
    //iternum(i)       := DecoderGroup(i).io.iter
    checkall(i)      := DecoderGroup(i).io.checknum
    //outvalidnum(i)   := DecoderGroup(i).io.outvalid
    //DecoderGroup(i).io.updatevalid := io.updatevalid  
    //DecoderGroup(i).io.localvalid := io.localvalid 
    //outvalidsign(i)  := DecoderGroup(i).io.outvalid 
  }
  io.totalframe := totalframenum.reduce(_+_)
  io.errorframe := errorframenum.reduce(_+_)
  io.errorbit   := errorbitnum.reduce(_+_)
  //io.iter       := iternum.reduce(_+_)
  io.checknum   := checkall.reduce(_+_)
  //io.outvalid   := outvalidnum.reduce(_|_) 
  //io.Errorcol := MuxCase(VecInit(Seq.fill(MAXERRORNUM)(0.U(COLADDR.W))),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errorcol 

  //})
  //io.Errorblk := MuxCase(VecInit(Seq.fill(MAXERRORNUM)(0.U(BLKADDR.W))),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errorblk 
  //})
  // io.Errornum := MuxCase(0.U(COLADDR.W),Seq.tabulate(PARRELNUM){
  //  i => (outvalidnum(i) === 1.U) ->DecoderGroup(i).io.Errornum 
  //})

} 
