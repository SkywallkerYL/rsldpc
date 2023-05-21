package decoder 
import chisel3._
import chisel3.util._

class CheckNode extends Module with COMMON {
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
  GenerateIO.Gen()
}

