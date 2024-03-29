package  decoder 
import circt.stage._
import decoder._

object Elaborate extends App {
  // CheckNode VariableNode ProcessingUnit
  // V2CMux  Decoder Noisegen gng(RandSeedParams())    
  // GngWrapper(1) Topdecoder  
  //Muxminandsecmin 
  //CheckNodeCOL  DecoderCol
  //rsdecoder  rsdecodertop 
  //CheckNode2Col
  //Errorbits   
  //Decoder2Col rsdecoder2colGauss   rsdecodertopGauss
  def top = new rsdecodertopGauss
  val useMFC = false // use MLIR-based firrtl compiler
  val generator = Seq(
    chisel3.stage.ChiselGeneratorAnnotation(() => top),
    //firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
    //ModulePrefixAnnotation("ysyx_22050550_")
  )
  if (useMFC) {
    (new ChiselStage).execute(args, generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
  } else{
    (new chisel3.stage.ChiselStage).execute(args, generator)
  }
}
