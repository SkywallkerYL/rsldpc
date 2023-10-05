package decoder 
import chisel3._
import chisel3.util._

class CheckNode1 extends Module with COMMON {
  val io = IO(new Bundle {
    val input = Input(Vec(COLNUM, UInt(V2CWIDTH.W)))
    val minVal = Output(UInt(C2VWIDTH.W))
    val minIdx = Output(UInt(COLADDR.W))
    val subminVal = Output(UInt(C2VWIDTH.W))
    val subminIdx = Output(UInt(COLADDR.W))
    val xor_result = Output(Bool()) 
  })

  val minVal = Wire(UInt(C2VWIDTH.W))
  val minIdx = Wire(UInt(COLADDR.W))
  val subminVal = Wire(UInt(C2VWIDTH.W))
  val subminIdx = Wire(UInt(COLADDR.W))

  // Find the minimum value and its index
  val abs_data = VecInit(Seq.fill(COLNUM)(0.U(C2VWIDTH.W)))
  for (i <- 0 until COLNUM ){
    abs_data(i) := Mux(io.input(i).asSInt.abs.asUInt > MAXC2V.U,MAXC2V.U,io.input(i).asSInt.abs.asUInt) // Mux(io.input(i)(31)===1.U,~io.input(i)+1.U,io.input(i) ) 
  }
  //val abs_data = io.input.map(n => Mux(n(32)===1.U,~n+1.U ,n))
  minVal := abs_data.reduceLeft(_ min _)
  minIdx := abs_data.indexWhere(_ === minVal)

  // Remove the minimum value and find the subminimum value and its index
 // val subminInput = abs_data.filter(_ != minVal)
  //val subminInput = VecInit(Seq.fill(32)(0.U(C2VWIDTH.W)))//abs_data.zipWithIndex.filter(_._2.U != minIdx).map(_._1)
  //for (i <- 0 until 32){
    //subminInput(i) := Mux( i.U === minIdx,15.U,abs_data(i) )  
  //}
  val subminInput = VecInit(abs_data.zipWithIndex.map { case (data, i) =>
    Mux(i.U === minIdx, MAXC2V.U, data)
  })
  subminVal := subminInput.reduceLeft(_ min _)
  subminIdx := subminInput.indexWhere(_ === subminVal)
  
  val xor_result = io.input.map(_.asUInt()(V2CWIDTH-1)).reduce(_ ^ _)
  // Output the results
  io.minVal := minVal
  io.minIdx := minIdx
  io.subminVal := subminVal
  io.subminIdx := subminIdx
  io.xor_result := xor_result
  //GenerateIO.Gen()
}
class Muxminandsecmin extends Module with COMMON {
  val io = IO(new Bundle {
    val input      = Input(Vec(4, UInt(V2CWIDTH.W)))
    val inputindex = Input(Vec(4, UInt(COLADDR.W)))
    val minVal = Output(UInt(V2CWIDTH.W))
    val minIdx = Output(UInt(COLADDR.W))
    val subminVal = Output(UInt(V2CWIDTH.W))
    val subminIdx = Output(UInt(COLADDR.W))
    
  })
  val abs_data = io.input
  val minVal = Wire(UInt(V2CWIDTH.W))
  val minIdx = Wire(UInt(COLADDR.W))
  val subminVal = Wire(UInt(V2CWIDTH.W))
  val subminIdx = Wire(UInt(COLADDR.W))

  subminVal := abs_data(3) 
  subminIdx := io.inputindex(3)

  
 // minVal := abs_data.reduceLeft(_ min _)
 // minIdx := 0.U
 // //minIdx := abs_data.indexWhere(_ === minVal)
 // for ( i <- 0 until 4) {
 //   when(minVal === abs_data(i)){
 //     minIdx := io.inputindex(i)
 //   }
 // }
  val minDecide = VecInit(Seq.fill(6)(false.B)) 
  minDecide(0) := abs_data(0) < abs_data (1) 
  minDecide(1) := abs_data(0) < abs_data (2) 
  minDecide(2) := abs_data(0) < abs_data (3) 
  minDecide(3) := abs_data(1) < abs_data (2) 
  minDecide(4) := abs_data(1) < abs_data (3) 
  minDecide(5) := abs_data(2) < abs_data (3) 
  when (minDecide(0) & minDecide(1) & minDecide(2)) {
    minVal := abs_data(0) 
    minIdx := io.inputindex(0) 
    when(minDecide(3) & minDecide(4)) {
      subminVal := abs_data(1)
      subminIdx := io.inputindex(1)
    }.elsewhen(minDecide(5)){
      subminVal := abs_data(2)
      subminIdx := io.inputindex(2)
    }
  }.elsewhen(minDecide(3) & minDecide(4)){
    minVal := abs_data(1)
    minIdx := io.inputindex(1)
    when(minDecide(1) & minDecide(2)) {
      subminVal := abs_data(0)
      subminIdx := io.inputindex(0)
    }.elsewhen(minDecide(5)){
      subminVal := abs_data(2)
      subminIdx := io.inputindex(2)
    }
  }.elsewhen(minDecide(5)) {
    minVal := abs_data(2)
    minIdx := io.inputindex(2) 
    when(minDecide(0) & minDecide(2)) {
      subminVal := abs_data(0)
      subminIdx := io.inputindex(0)
    }.elsewhen(minDecide(4)){
      subminVal := abs_data(1)
      subminIdx := io.inputindex(1)
    }    
  }.otherwise{
    minVal := abs_data(3)
    minIdx := io.inputindex(3)
    when(minDecide(0) & minDecide(1)) {
      subminVal := abs_data(0)
      subminIdx := io.inputindex(0)
    }.elsewhen(minDecide(3)){
      subminVal := abs_data(1)
      subminIdx := io.inputindex(1)
    }.otherwise{
      subminVal := abs_data(2)
      subminIdx := io.inputindex(2) 
    }
  } 
  



  io.minVal := minVal
  io.minIdx := minIdx 
  io.subminVal := subminVal
  io.subminIdx := subminIdx // io.inputindex(subminIdx)

} 
class CheckNode  extends Module with COMMON {
  val io = IO(new Bundle {
    val input = Input(Vec(COLNUM, UInt(V2CWIDTH.W)))
    val minVal = Output(UInt(C2VWIDTH.W))
    val minIdx = Output(UInt(COLADDR.W))
    val subminVal = Output(UInt(C2VWIDTH.W))
    val subminIdx = Output(UInt(COLADDR.W))
    val xor_result = Output(Bool()) 
  })

  val muxLayer1 = Seq.fill(8)(Module(new Muxminandsecmin)) 
  val muxLayer2 = Seq.fill(4)(Module(new Muxminandsecmin)) 
  val muxLayer3 = Seq.fill(2)(Module(new Muxminandsecmin)) 
  val muxLayer4 = Module(new Muxminandsecmin)
  val abs_data = VecInit(Seq.fill(COLNUM)(0.U(C2VWIDTH.W)))
  for (i <- 0 until COLNUM ){
    abs_data(i) := Mux(io.input(i).asSInt.abs.asUInt > MAXC2V.U,MAXC2V.U,io.input(i).asSInt.abs.asUInt) // Mux(io.input(i)(31)===1.U,~io.input(i)+1.U,io.input(i) ) 
  }
  for (i <- 0 until 8) {
    for (j <- 0 until 4) {
      muxLayer1(i).io.input(j) := abs_data(i*4+j) 
      muxLayer1(i).io.inputindex(j) := (i*4+j).U
    }
  }
  
  muxLayer2(0).io.input(0)      := muxLayer1(0).io.minVal 
  muxLayer2(0).io.inputindex(0) := muxLayer1(0).io.minIdx 
  muxLayer2(0).io.input(1)      := muxLayer1(0).io.subminVal 
  muxLayer2(0).io.inputindex(1) := muxLayer1(0).io.subminIdx 
  muxLayer2(0).io.input(2)      := muxLayer1(1).io.minVal 
  muxLayer2(0).io.inputindex(2) := muxLayer1(1).io.minIdx 
  muxLayer2(0).io.input(3)      := muxLayer1(1).io.subminVal 
  muxLayer2(0).io.inputindex(3) := muxLayer1(1).io.subminIdx
  muxLayer2(1).io.input(0)      := muxLayer1(2).io.minVal 
  muxLayer2(1).io.inputindex(0) := muxLayer1(2).io.minIdx 
  muxLayer2(1).io.input(1)      := muxLayer1(2).io.subminVal 
  muxLayer2(1).io.inputindex(1) := muxLayer1(2).io.subminIdx 
  muxLayer2(1).io.input(2)      := muxLayer1(3).io.minVal 
  muxLayer2(1).io.inputindex(2) := muxLayer1(3).io.minIdx 
  muxLayer2(1).io.input(3)      := muxLayer1(3).io.subminVal 
  muxLayer2(1).io.inputindex(3) := muxLayer1(3).io.subminIdx
  muxLayer2(2).io.input(0)      := muxLayer1(4).io.minVal 
  muxLayer2(2).io.inputindex(0) := muxLayer1(4).io.minIdx 
  muxLayer2(2).io.input(1)      := muxLayer1(4).io.subminVal 
  muxLayer2(2).io.inputindex(1) := muxLayer1(4).io.subminIdx 
  muxLayer2(2).io.input(2)      := muxLayer1(5).io.minVal 
  muxLayer2(2).io.inputindex(2) := muxLayer1(5).io.minIdx 
  muxLayer2(2).io.input(3)      := muxLayer1(5).io.subminVal 
  muxLayer2(2).io.inputindex(3) := muxLayer1(5).io.subminIdx
  muxLayer2(3).io.input(0)      := muxLayer1(6).io.minVal 
  muxLayer2(3).io.inputindex(0) := muxLayer1(6).io.minIdx 
  muxLayer2(3).io.input(1)      := muxLayer1(6).io.subminVal 
  muxLayer2(3).io.inputindex(1) := muxLayer1(6).io.subminIdx 
  muxLayer2(3).io.input(2)      := muxLayer1(7).io.minVal 
  muxLayer2(3).io.inputindex(2) := muxLayer1(7).io.minIdx 
  muxLayer2(3).io.input(3)      := muxLayer1(7).io.subminVal 
  muxLayer2(3).io.inputindex(3) := muxLayer1(7).io.subminIdx
//Layer3  
  muxLayer3(0).io.input(0)      := muxLayer2(0).io.minVal 
  muxLayer3(0).io.inputindex(0) := muxLayer2(0).io.minIdx 
  muxLayer3(0).io.input(1)      := muxLayer2(0).io.subminVal 
  muxLayer3(0).io.inputindex(1) := muxLayer2(0).io.subminIdx 
  muxLayer3(0).io.input(2)      := muxLayer2(1).io.minVal 
  muxLayer3(0).io.inputindex(2) := muxLayer2(1).io.minIdx 
  muxLayer3(0).io.input(3)      := muxLayer2(1).io.subminVal 
  muxLayer3(0).io.inputindex(3) := muxLayer2(1).io.subminIdx
  muxLayer3(1).io.input(0)      := muxLayer2(2).io.minVal 
  muxLayer3(1).io.inputindex(0) := muxLayer2(2).io.minIdx 
  muxLayer3(1).io.input(1)      := muxLayer2(2).io.subminVal 
  muxLayer3(1).io.inputindex(1) := muxLayer2(2).io.subminIdx 
  muxLayer3(1).io.input(2)      := muxLayer2(3).io.minVal 
  muxLayer3(1).io.inputindex(2) := muxLayer2(3).io.minIdx 
  muxLayer3(1).io.input(3)      := muxLayer2(3).io.subminVal 
  muxLayer3(1).io.inputindex(3) := muxLayer2(3).io.subminIdx
 //Layer4 
  muxLayer4.io.input(0)      := muxLayer3(0).io.minVal 
  muxLayer4.io.inputindex(0) := muxLayer3(0).io.minIdx 
  muxLayer4.io.input(1)      := muxLayer3(0).io.subminVal 
  muxLayer4.io.inputindex(1) := muxLayer3(0).io.subminIdx 
  muxLayer4.io.input(2)      := muxLayer3(1).io.minVal 
  muxLayer4.io.inputindex(2) := muxLayer3(1).io.minIdx 
  muxLayer4.io.input(3)      := muxLayer3(1).io.subminVal 
  muxLayer4.io.inputindex(3) := muxLayer3(1).io.subminIdx
 
  val minVal = Wire(UInt(C2VWIDTH.W))
  val minIdx = Wire(UInt(COLADDR.W))
  val subminVal = Wire(UInt(C2VWIDTH.W))
  val subminIdx = Wire(UInt(COLADDR.W))
  
  minVal := muxLayer4.io.minVal(C2VWIDTH-1,0)
  minIdx := muxLayer4.io.minIdx  
  subminVal := muxLayer4.io.subminVal(C2VWIDTH-1,0) 
  subminIdx := muxLayer4.io.subminIdx

  val xor_result = io.input.map(_.asUInt()(V2CWIDTH-1)).reduce(_ ^ _)
  // Output the results
  io.minVal := minVal
  io.minIdx := minIdx
  io.subminVal := subminVal
  io.subminIdx := subminIdx
  io.xor_result := xor_result
  //GenerateIO.Gen()
}
class CheckNodeCOL  extends Module with COMMON {
  val io = IO(new Bundle {
    val input = Input( UInt(V2CWIDTHCOL.W) )
    val updatemin = Input(Bool())
    val signreset  = Input(Bool())
    val initial   = Input(Bool())
    val inputaddr = Input(UInt(COLADDR.W))
    val inputsign = Input(UInt(1.W))//来自Ram里存的信号 
    val minval = Output(UInt(C2VWIDTHCOL.W))
    
    // for difftest 
    val min         = Output(UInt((C2VWIDTHCOL-1).W)) 
    val submin      = Output(UInt((C2VWIDTHCOL-1).W)) 
    val minaddr     = Output(UInt(COLADDR.W)) 
    val subminaddr  = Output(UInt(COLADDR.W))  
    val sign        = Output(UInt(1.W)) 
  })
  val min = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val minaddr = RegInit(32.U((COLADDR+1).W))
  val submin = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val subminaddr = RegInit(32.U((COLADDR+1).W))
  val sign  = RegInit(0.U(1.W))
  val inputsign = io.input(V2CWIDTHCOL-1)
  val absdata = Mux(inputsign === 1.U,~io.input+1.U ,io.input)
  //val absdata = Mux(absdata <= MAXC2VCOL.U,absdata,MAXC2VCOL)
  when(io.updatemin) {
    //when(((inputsign === 1.U)&&(io.initial) )||(io.inputsign =/= inputsign) ) {
    //  sign := sign ^ 1.U 
    //}
    when(io.initial) {
      when(inputsign === 1.U) {
        sign := sign ^ 1.U 
      }
    }.elsewhen(io.inputsign =/= inputsign){
      sign := sign ^ 1.U
    }
    // updata min 
  
   // when(absdata <= MAXC2VCOL.U) {
      when(io.inputaddr === minaddr) {
        when(absdata <= submin ) {
          min := absdata  
        }.otherwise{
          min := submin 
          minaddr := subminaddr 
          submin := absdata //Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
          subminaddr := io.inputaddr  
        }
      }.elsewhen(io.inputaddr === subminaddr){
        when(absdata <= min  ){
          submin := min 
          subminaddr := minaddr  
          min := absdata 
          minaddr := io.inputaddr 
        }.otherwise {
          submin :=  absdata//Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
        }
      }.otherwise{
        when(absdata <= min) {
          submin := min 
          subminaddr := minaddr 
          min := absdata 
          minaddr := io.inputaddr 
        }.elsewhen(absdata <= submin){
          submin := absdata 
          subminaddr := io.inputaddr
        }
      }
      /*
      when (absdata < min) {
        min := absdata 
        minaddr := io.inputaddr 
      }.elsewhen(absdata < submin) {
        submin := absdata 
      }*/
    //}
  }
  when (io.signreset) {
    sign := 0.U 
    min := MAXC2VCOL.U
    submin := MAXC2VCOL.U
    minaddr := 32.U  
    subminaddr := 32.U
  }
  val c2v = Wire(UInt(C2VWIDTHCOL.W)) 
  c2v := Mux(io.inputaddr === minaddr , submin ,min) 

  val c2vext = Wire(UInt((C2VWIDTHCOL+2).W))
  c2vext := Fill(2,0.U)##c2v 

  val scaledc2v =( c2vext+c2vext+c2vext) >> 2.U 

  val c2vval = Mux(sign === io.inputsign , scaledc2v , ~scaledc2v+1.U )

  io.minval := Mux(io.initial,0.U,c2vval) 
  

  io.min := min 
  io.submin := submin 
  io.minaddr := minaddr 
  io.subminaddr := subminaddr 
  io.sign := sign 


}
class CheckNode2Col  extends Module with COMMON {
  val io = IO(new Bundle {
    val input = Input(Vec(2, UInt(V2CWIDTHCOL.W)) )
    val updatemin = Input(Bool())
    val signreset  = Input(Bool())
    val initial   = Input(Bool())
    val inputaddr = Input(UInt((COLADDR-1).W))
    val inputsign = Input(Vec(2,UInt(1.W)))//来自Ram里存的信号 
    val minval = Output(Vec(2,UInt(C2VWIDTHCOL.W)))
    
    // for difftest 
    val min0         = Output(UInt((C2VWIDTHCOL-1).W)) 
    val submin0      = Output(UInt((C2VWIDTHCOL-1).W)) 
    val minaddr0     = Output(UInt(COLADDR.W)) 
    val subminaddr0  = Output(UInt(COLADDR.W))  
    val sign0        = Output(UInt(1.W)) 
    val min1         = Output(UInt((C2VWIDTHCOL-1).W)) 
    val submin1      = Output(UInt((C2VWIDTHCOL-1).W)) 
    val minaddr1     = Output(UInt(COLADDR.W)) 
    val subminaddr1  = Output(UInt(COLADDR.W))  
    val sign1        = Output(UInt(1.W)) 
  })
  val min0 = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val minaddr0 = RegInit(0.U((COLADDR).W))
  val submin0 = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val subminaddr0 = RegInit(0.U((COLADDR).W))
  val sign0  = RegInit(0.U(1.W))
  val inputsign0 = io.input(0)(V2CWIDTHCOL-1)
  val absdataext0 = Mux(inputsign0 === 1.U,~io.input(0)+1.U ,io.input(0))
  val absdata0 = absdataext0(C2VWIDTHCOL-2,0)
  val inputaddr0 = Wire(UInt(COLADDR.W))
  inputaddr0 := io.inputaddr##0.U  
  val min1 = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val minaddr1 = RegInit(0.U((COLADDR).W))
  val submin1 = RegInit(MAXC2VCOL.U((C2VWIDTHCOL-1).W))
  val subminaddr1 = RegInit(0.U((COLADDR).W))
  val sign1  = RegInit(0.U(1.W))
  val inputsign1 = io.input(1)(V2CWIDTHCOL-1)
  val absdataext1 = Mux(inputsign1 === 1.U,~io.input(1)+1.U ,io.input(1))
  val absdata1 = absdataext1(C2VWIDTHCOL-2,0)
  val inputaddr1 = Wire(UInt(COLADDR.W))
  inputaddr1 := io.inputaddr##1.U  
 
  //val absdata = Mux(absdata <= MAXC2VCOL.U,absdata,MAXC2VCOL)
  when(io.updatemin) {
    when(io.initial) {
      when(inputsign1 === 1.U) {
        sign1 := sign1 ^ 1.U 
      }
    }.elsewhen(io.inputsign(1) =/= inputsign1){
      sign1 := sign1 ^ 1.U
    }
    when(io.initial) {
      when(inputsign0 === 1.U) {
        sign0 := sign0 ^ 1.U 
      }
    }.elsewhen(io.inputsign(0) =/= inputsign0){
      sign0 := sign0 ^ 1.U
    }
    // updata min 
  
    when(inputaddr0 === minaddr0) {
      when(absdata0 <= submin0 ) {
        min0 := absdata0  
      }.otherwise{
        min0 := submin0 
        minaddr0 := subminaddr0 
        submin0 := absdata0 //Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
        subminaddr0 := inputaddr0  
      }
    }.elsewhen(inputaddr0 === subminaddr0){
      when(absdata0 <= min0  ){
        submin0 := min0 
        subminaddr0 := minaddr0  
        min0 := absdata0 
        minaddr0 := inputaddr0 
      }.otherwise {
        submin0 :=  absdata0//Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
      }
    }.otherwise{
      when(absdata0 <= min0) {
        submin0 := min0 
        subminaddr0 := minaddr0 
        min0 := absdata0 
        minaddr0 := inputaddr0 
      }.elsewhen(absdata0 <= submin0){
        submin0 := absdata0 
        subminaddr0 := inputaddr0
      }
    }
    when(inputaddr1 === minaddr1) {
      when(absdata1 <= submin1 ) {
        min1 := absdata1  
      }.otherwise{
        min1 := submin1 
        minaddr1 := subminaddr1 
        submin1 := absdata1 //Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
        subminaddr1 := inputaddr1  
      }
    }.elsewhen(inputaddr1 === subminaddr1){
      when(absdata1 <= min1  ){
        submin1 := min1 
        subminaddr1 := minaddr1  
        min1 := absdata1 
        minaddr1 := inputaddr1 
      }.otherwise {
        submin1 :=  absdata1//Mux( absdata <= MAXC2VCOL.U , absdata, MAXC2VCOL.U)  
      }
    }.otherwise{
      when(absdata1 <= min1) {
        submin1 := min1 
        subminaddr1 := minaddr1 
        min1 := absdata1 
        minaddr1 := inputaddr1 
      }.elsewhen(absdata1 <= submin1){
        submin1 := absdata1 
        subminaddr1 := inputaddr1
      }
    }     
  }
  when (io.signreset) {
    sign0 := 0.U 
    min0 := MAXC2VCOL.U
    submin0 := MAXC2VCOL.U
    minaddr0 := 0.U  
    subminaddr0 := 0.U
    sign1 := 0.U 
    min1 := MAXC2VCOL.U
    submin1 := MAXC2VCOL.U
    minaddr1 := 0.U  
    subminaddr1 := 0.U
  }
  // choose mux  
  //val minmux = 
  val minmux = (Module(new Muxminandsecmin)) 
  minmux.io.input(0) := min0 
  minmux.io.input(1) := min1 
  minmux.io.input(2) := submin0 
  minmux.io.input(3) := submin1 
  minmux.io.inputindex(0) := minaddr0 
  minmux.io.inputindex(1) := minaddr1 
  minmux.io.inputindex(2) := subminaddr0 
  minmux.io.inputindex(3) := subminaddr1 

  val min = minmux.io.minVal 
  val minaddr = minmux.io.minIdx 
  val submin = minmux.io.subminVal 
  val subminaddr = minmux.io.subminIdx
// scale  
  val c2v0 = Wire(UInt(C2VWIDTHCOL.W)) 
  c2v0 := Mux(inputaddr0 === minaddr , submin ,min) 

  val c2vext0 = Wire(UInt((C2VWIDTHCOL+2).W))
  c2vext0 := Fill(2,0.U)##c2v0 

  val scaledc2v0 =( c2vext0+c2vext0+c2vext0) >> 2.U 

  val c2vval0 = Mux(sign0 === io.inputsign(0) , scaledc2v0 , ~scaledc2v0+1.U )

  io.minval(0) := Mux(io.initial,0.U,c2vval0) 
  
  val c2v1 = Wire(UInt(C2VWIDTHCOL.W)) 
  c2v1 := Mux(inputaddr1 === minaddr , submin ,min) 

  val c2vext1 = Wire(UInt((C2VWIDTHCOL+2).W))
  c2vext1 := Fill(2,0.U)##c2v1

  val scaledc2v1 =( c2vext1+c2vext1+c2vext1) >> 2.U 

  val c2vval1 = Mux(sign1 === io.inputsign(1) , scaledc2v1 , ~scaledc2v1+1.U )

  io.minval(1) := Mux(io.initial,0.U,c2vval1) 
 

  io.min0 := min0 
  io.submin0 := submin0 
  io.minaddr0 := minaddr0 
  io.subminaddr0 := subminaddr0 
  io.sign0 := sign0 
  io.min1 := min1 
  io.submin1 := submin1 
  io.minaddr1 := minaddr1 
  io.subminaddr1 := subminaddr1 
  io.sign1 := sign1 


}
