package  decoder 
import circt.stage._
import decoder._

object Elaborate extends App {
  // CheckNode VariableNode ProcessingUnit
  // V2CMux  Decoder Noisegen gng(RandSeedParams())    
  // GngWrapper Topdecoder   
  def top = new Topdecoder

  val useMFC = false // use MLIR-based firrtl compiler
  val generator = Seq(
    chisel3.stage.ChiselGeneratorAnnotation(() => top),
    //firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
    //ModulePrefixAnnotation("ysyx_22050550_")
  )
  if (useMFC) {
    (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
  } else {
    (new chisel3.stage.ChiselStage).execute(args, generator)
  }
}
