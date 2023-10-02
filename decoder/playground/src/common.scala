package decoder 
import chisel3._
import chisel3.util._
import scala.io.{BufferedSource,Source}
import java.io._
trait COMMON{
  val V2CWIDTH = 4
  val C2VWIDTH = 4
  val APPWIDTH = 7

  val V2CWIDTHCOL = 4
  val C2VWIDTHCOL = 4
  val APPWIDTHCOL = 7


  //有一个符号位
  val MAXC2V = scala.math.pow(2,C2VWIDTH-1).toInt-1  
  val MAXV2C = scala.math.pow(2,V2CWIDTH-1).toInt-1

  val MAXC2VCOL = scala.math.pow(2,C2VWIDTHCOL-1).toInt-1  
  val MAXV2CCOL = scala.math.pow(2,V2CWIDTHCOL-1).toInt-1

  val COLNUM = 32
  val ROWNUM = 6
  val BLKSIZE= 64

  val COLADDR = log2Ceil(COLNUM)
  val ROWADDR = log2Ceil(ROWNUM)
  val BLKADDR = log2Ceil(BLKSIZE)
  //  噪声发生器的p0  的个数 
  val PNUM    = 5 
  //  量化区间的个数   
  val QuantiNUM   = log2Ceil(PNUM+1)
  val MAXSPACEIND = scala.math.pow(2,QuantiNUM).toInt-1   
  //  量化表  
  val table3 = VecInit(Seq(
      4.S(APPWIDTH.W),
      1.S(APPWIDTH.W) ,
     -1.S(APPWIDTH.W)   ,
     -4.S(APPWIDTH.W)
    ))
  val table5 = VecInit(Seq(
      7.S(APPWIDTH.W),
      3.S(APPWIDTH.W),
      1.S(APPWIDTH.W),
      -1.S(APPWIDTH.W),
      -3.S(APPWIDTH.W),
      -7.S(APPWIDTH.W)
    ))
  //ITER 的宽度 
  val ITERWITH = 5  
  val FRAMEWITH = 64
//这个相对路径是根据makefile所在的文件夹决定的
  val IOTablePath     : String    = "./build/Table.h"
  val FilePath        : String    = "../matrix/2048_1723.txt"
//RAM的类型 ，是用syncreadmem 或者 mem  
//并行路数
  val PARRELNUM  = 1 
// 最多记录的错误比特的个数   
  val MAXERRORNUM = 10
}
object COMMON extends COMMON {}

object GenerateIO extends COMMON{
    def Gen(module : Int = 0) : Unit  = {
        val  writer = new PrintWriter(new File(IOTablePath))
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
       else if (module == 2) {
         for(i <- 0 until COLNUM){
            writer.println("top->io_LLrin_"+i+" = LLrInital(RandomGen(sigma));")
         } 
       }
       else if(module ==3) {
         //for(i <- 0 until COLNUM){
         //   writer.println("top->io_LLrin_"+i+" = LLrInital(RandomGen(sigma));")
         //}
         for (i <- 0 until BLKSIZE) {
           writer.println("appout[i*64+"+i+"] = top->io_appout_"+i+";")  
         }
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("c2v["+i+"] = top->io_c2v_"+i+";")  
         }
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("min["+i+"] = top->io_min_"+i+";")  
         }
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("submin["+i+"] = top->io_submin_"+i+";")  
         }
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("minaddr["+i+"] = top->io_minaddr_"+i+";")  
         } 
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("subminaddr["+i+"] = top->io_subminaddr_"+i+";")  
         }   
         for (i <- 0 until BLKSIZE*ROWNUM) {
           writer.println("sign["+i+"] = top->io_sign_"+i+";")  
         } 
         for (i <- 0 until BLKSIZE*COLNUM) {
           for (j <- 0  until ROWNUM){
             writer.println("v2c["+i+"]["+j+"] = top->io_v2csign_"+i+"_"+j+";")  
           }
         } 
       }
       else if (module == 4) {
         for (i <- 0 until BLKSIZE) {
           writer.println("top->io_appin_"+i+"= appin["+i+"];")
         }
         for (i <- 0 until MAXERRORNUM) {
           writer.println("errorcol["+i+"] = top->io_Errorcol_"+i+";")
           writer.println("errorblk["+i+"] = top->io_Errorblk_"+i+";")
         }
       }
        writer.close()
    }
}
object ReadMatrix extends COMMON {
  def ReadM() : Array[Array[Int]] = {
    val file = Source.fromFile(FilePath)
    val Matrix : Array[Array[Int]] = file.getLines().map(_.split("\t")).map(_.map(_.toInt)).toArray 
    //check if read right 
   /* 
    for (i <- 0 until ROWNUM*BLKSIZE ){
      for ( j <- 0 until COLNUM*BLKSIZE ) {
        print(Matrix(i)(j))
        print(" ")
      }
      print("\n")
    }
    */
   /* 
    val checkfile = new File("check.txt")
    val bw = new BufferedWriter(new FileWriter(checkfile))
    for(row <- Matrix){
      val line = row.mkString("\t")
      bw.write(line)
      bw.newLine
    }
    bw.close()
    */ 
    file.close()
    return Matrix 
  }

}
