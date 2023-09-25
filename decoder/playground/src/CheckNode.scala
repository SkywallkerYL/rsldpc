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
