package decoder 
import chisel3._
import chisel3.util._
import scala.io.{BufferedSource,Source}
import java.io._
trait COMMON{
  val V2CWIDTH = 5
  val C2VWIDTH = 5
  val APPWIDTH = 7
  
  val IOTablePath     : String    = "./build/Table.h"
}
object COMMON extends COMMON {}

object GenerateIO extends COMMON{
    def Gen() : Unit  = {
        val writer = new PrintWriter(new File(IOTablePath))
        //writer.println("VLDPC *top;")
        
        //writer.println("for (size_t i = 0; i < "+VNum+"; i++) {")
        for(i <- 0 until 32){
            //writer.println("#define io_YnInit("+i+")"+" io_YnInit_ ## "+i)
           //for checknode  
            writer.println("top->io_input_"+i+" = array["+i+"];")
            
        }
        //writer.println("}")
        writer.close()
    }
}
