package decoder 
import chisel3._
import chisel3.util._
import scala.util.Random
// 这个模块  将输入码字添加噪声后，转化为LLRIN 
class GngWrapper(num : Int = COMMON.COLNUM) extends Module with COMMON {
  val io = IO(new Bundle{
    val din = Input(Vec(num,UInt(1.W))) 
    val dinvalid = Input (Bool()) 
    val p0  = Input(Vec(PNUM,UInt(PWITH.W)))
    val dout = Output(Vec(num,UInt(LLRWIDTH.W))) 
    val doutvalid = Output(Bool()) 
  })
  val upperBond = BigInt(2).pow(64) - 1 
  val random    = new Random() 
  val randomBigInts = Seq.fill(num * 3) (BigInt(upperBond.bitLength,random).min(upperBond))

  val paramsList = randomBigInts.grouped(3).map{
    case Seq(z1_SEED,z2_SEED,z3_SEED) => RandSeedParams(z1_SEED,z2_SEED,z3_SEED)  
  }.toList
  val Gng = paramsList.map(params => Module(new gng(params)))
  for (i <- 0 until num) {
    Gng(i).io.din := io.din(i) 
    Gng(i).io.dinvalid := io.dinvalid 
    Gng(i).io.p0 := io.p0   
    if (PNUM == 3) {
      io.dout(i) := table3(Gng(i).io.dout).asUInt
    }else if (PNUM == 5) {
      io.dout(i) := table5(Gng(i).io.dout).asUInt
    }else if (PNUM == 15){
      io.dout(i) := Gng(i).io.dout//table15(Gng(i).io.dout).asUInt
    }
    io.doutvalid := Gng(i).io.doutvalid 
  }
}

