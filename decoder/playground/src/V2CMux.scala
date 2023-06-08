package decoder 
import chisel3._
import chisel3.util._
class Mux4to1 extends Module with COMMON {
  val io = IO(new Bundle {
    val addr = Input(Vec(4,UInt(BLKADDR.W)))
    val sel  = Input(UInt(2.W))
    val chooseaddr = Output(UInt(BLKADDR.W)) 
  })
  io.chooseaddr := io.addr(io.sel)
}
class V2CMux extends Module with COMMON {
  val io = IO(new Bundle {
    val SramAddr    =  Input(Vec(BLKSIZE,UInt(BLKADDR.W)))
    val sel         =  Input(UInt(BLKADDR.W))
    val ChooseAddr  =  Output(UInt(BLKADDR.W))
  })
 // io.ChooseAddr := MuxLookup(io.sel,0.U,( 0 until 64).map(i => (i.U -> io.SramAddr(i))))
//  io.ChooseAddr := io.SramAddr(io.sel)
 // ReadMatrix.ReadM()
  //原来直接按sel的写法延迟太高，会有64个逻辑门的延迟   
  //将64个输入 分成4组 每组16个    再分成4组 ，每组4个  ，这样子就3个4选1  一个12个逻辑门延迟
  //val Group1 =Seq.fill(4)(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(BLKADDR.W)))))
  //for (i <- 0 until 4 ) {
  //  for (j <- 0 until 4) {
  //    for( k <- 0 until 4) {
  //      Group1(i)(j)(k) := io.SramAddr(i.U*16.U+j.U*4.U+k.U)
  //    }
  //    ///Group1(i)(j) := io.SramAddr(i.U*16.U+j.U)
  //  }
  //}
  
  val MuxLayer1 = Seq.fill(16)(Module (new Mux4to1)) 
  val sel1 = io.sel(1,0)
  for (i <- 0 until 16) {
    for ( j <- 0 until 4) {
      MuxLayer1(i).io.addr(j) := io.SramAddr(i*4+j)  
    }
    MuxLayer1(i).io.sel := sel1 
  }
  val MuxLayer2 = Seq.fill(4)(Module (new Mux4to1)) 
  val sel2 = io.sel(3,2) 
  for (i <- 0 until 4) {
    for (j <- 0 until 4) {
      MuxLayer2(i).io.addr(j) := MuxLayer1(i*4+j).io.chooseaddr  
    }
    MuxLayer2(i).io.sel := sel2 
  } 
  val MuxLayer3 = Module (new Mux4to1) 
  for (i <- 0 until 4) {
    MuxLayer3.io.addr(i) := MuxLayer2(i).io.chooseaddr 
  }
  MuxLayer3.io.sel := io.sel(5,4)
  io.ChooseAddr := MuxLayer3.io.chooseaddr
}
class RowMux extends Module with COMMON {
  val io = IO(new Bundle {
    val V2Caddr     =  Input(Vec(ROWNUM,UInt(BLKADDR.W)))
    val sel         =  Input(UInt(ROWADDR.W))
    val ChooseAddr  =  Output(UInt(BLKADDR.W))
  })
  //这样写 与简便的写法是一样的。
    //io.ChooseAddr := 0.U 
    //switch(io.sel) {
    //is(0.U) {
    //  io.ChooseAddr := io.V2Caddr(0)
    //}
    //is(1.U) {
    //  io.ChooseAddr := io.V2Caddr(1)
    //}
    //is(2.U) {
    //  io.ChooseAddr := io.V2Caddr(2)
    //}
    //is(3.U) {
    //  io.ChooseAddr := io.V2Caddr(3)
    //}
    //is(4.U) {
    //  io.ChooseAddr := io.V2Caddr(4)
    //}
    //is(5.U) {
    //  io.ChooseAddr := io.V2Caddr(5)
    //}

  //}
  io.ChooseAddr := io.V2Caddr(io.sel)
 // ReadMatrix.ReadM()
  
}
class Mux32to1(width : Int = 7) extends Module with COMMON {
  val io = IO(new Bundle {
    val input       =  Input(Vec(COLNUM,UInt(width.W)))
    val sel         =  Input(UInt(COLADDR.W))
    val output      =  Output(UInt(width.W))
  })
 // io.ChooseAddr := MuxLookup(io.sel,0.U,( 0 until 64).map(i => (i.U -> io.SramAddr(i))))
//  io.ChooseAddr := io.SramAddr(io.sel)
 // ReadMatrix.ReadM()
  //原来直接按sel的写法延迟太高，会有64个逻辑门的延迟   
  //将64个输入 分成4组 每组16个    再分成4组 ，每组4个  ，这样子就3个4选1  一个12个逻辑门延迟
  //val Group1 =Seq.fill(4)(Seq.fill(4)(VecInit(Seq.fill(4)(0.U(BLKADDR.W)))))
  //for (i <- 0 until 4 ) {
  //  for (j <- 0 until 4) {
  //    for( k <- 0 until 4) {
  //      Group1(i)(j)(k) := io.SramAddr(i.U*16.U+j.U*4.U+k.U)
  //    }
  //    ///Group1(i)(j) := io.SramAddr(i.U*16.U+j.U)
  //  }
  //}
  
  val MuxLayer1 = Seq.fill(8)(Module (new Mux4to1)) 
  val sel1 = io.sel(1,0)
  for (i <- 0 until 8) {
    for ( j <- 0 until 4) {
      MuxLayer1(i).io.addr(j) := io.input(i*4+j)  
    }
    MuxLayer1(i).io.sel := sel1 
  }
  val MuxLayer2 = Seq.fill(2)(Module (new Mux4to1)) 
  val sel2 = io.sel(3,2) 
  for (i <- 0 until 2) {
    for (j <- 0 until 4) {
      MuxLayer2(i).io.addr(j) := MuxLayer1(i*4+j).io.chooseaddr  
    }
    MuxLayer2(i).io.sel := sel2 
  } 
  val sel3 = io.sel(4)
  io.output := Mux(sel3 === 0.U,MuxLayer2(0).io.chooseaddr, MuxLayer2(1).io.chooseaddr)
}
