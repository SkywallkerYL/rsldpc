package decoder 
import chisel3._
import chisel3.util._

class CheckNode extends Module with COMMON {
/*
  val io = IO(new Bundle {
    val data_in = Input(Vec(32, SInt(8.W))) 
    val abs_min_value = Output(UInt(8.W)) 
    val second_min_value = Output(UInt(8.W)) 
    val min_value_index = Output(UInt(5.W)) 
    val xor_result = Output(Bool()) 
  })


  val abs_data = io.data_in.map(n => Mux(n >= 0.S, n, (-n).asSInt()))

  val min_value = abs_data.reduceOption((a, b) => Mux(a < b, a, b)).getOrElse(0.S)
 
  //val second_abs_data = abs_data.filterNot(_ == min_value) 
  //val second_min_value = second_abs_data.reduceOption((a, b) => Mux(a < b, a, b)).getOrElse(0.S)
  val second_min_value = abs_data.filter(_ != min_value).min(new Ordering [UInt]{
    def compare(x : UInt ,y:UInt ) = x < y 
  })

  val min_value_index = PriorityEncoder(io.data_in.map(x => x === min_value))


  val xor_result = io.data_in.map(_.asUInt()(7)).reduce(_ ^ _)


  io.abs_min_value := min_value.asUInt()
  io.second_min_value := second_min_value.asUInt()
  io.min_value_index := min_value_index.asUInt()
  io.xor_result := xor_result
  */
  val io = IO(new Bundle {
    val input = Input(Vec(32, UInt(C2VWIDTH.W)))
    val minVal = Output(UInt(C2VWIDTH.W))
    val minIdx = Output(UInt(5.W))
    val subminVal = Output(UInt(C2VWIDTH.W))
    val subminIdx = Output(UInt(5.W))
    val xor_result = Output(Bool()) 
  })

  val minVal = Wire(UInt(C2VWIDTH.W))
  val minIdx = Wire(UInt(5.W))
  val subminVal = Wire(UInt(C2VWIDTH.W))
  val subminIdx = Wire(UInt(5.W))

  // Find the minimum value and its index
  val abs_data = VecInit(Seq.fill(32)(0.U(C2VWIDTH.W)))
  for (i <- 0 until 32 ){
    abs_data(i) := io.input(i).asSInt.abs.asUInt // Mux(io.input(i)(31)===1.U,~io.input(i)+1.U,io.input(i) ) 
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
  Mux(i.U === minIdx, 15.U, data)
})
  subminVal := subminInput.reduceLeft(_ min _)
  subminIdx := subminInput.indexWhere(_ === subminVal)
  
  val xor_result = io.input.map(_.asUInt()(C2VWIDTH-1)).reduce(_ ^ _)
  // Output the results
  io.minVal := minVal
  io.minIdx := minIdx
  io.subminVal := subminVal
  io.subminIdx := subminIdx
  io.xor_result := xor_result
  GenerateIO.Gen()
}

