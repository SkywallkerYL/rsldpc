package decoder 
import chisel3._
import chisel3.util._
import scala.io.{BufferedSource,Source}
import java.io._
trait COMMON{
  val V2CWIDTH = 8
  val C2VWIDTH = 5
  val APPWIDTH = V2CWIDTH

  val MAXC2V = scala.math.pow(2,C2VWIDTH-1).toInt-1  
  val MAXV2C = scala.math.pow(2,V2CWIDTH-1).toInt-1

  val COLNUM = 32
  val ROWNUM = 6
  val BLKSIZE= 64

  val COLADDR = log2Ceil(COLNUM)
  val ROWADDR = log2Ceil(ROWNUM)
  val BLKADDR = log2Ceil(BLKSIZE)

  val IOTablePath     : String    = "./build/Table.h"
}
object COMMON extends COMMON {}

object GenerateIO extends COMMON{
    def Gen(module : Int = 0) : Unit  = {
        val writer = new PrintWriter(new File(IOTablePath))
        //writer.println("VLDPC *top;")
        
        //writer.println("for (size_t i = 0; i < "+VNum+"; i++) {")
       if(module == 0){
         for(i <- 0 until COLNUM){
            //writer.println("#define io_YnInit("+i+")"+" io_YnInit_ ## "+i)
           //for checknode  
            writer.println("top->io_input_"+i+" = array["+i+"];")
         }
       }
       else if (module == 1){
         for(i <- 0 until ROWNUM){
            writer.println("top->io_Checkin_"+i+" = checkin["+i+"];")
         }  
       }
        writer.close()
    }
}
