import Chisel._
import Node._

class Connections extends Module {
	val io = new Bundle {
		val tdi = UInt( INPUT, 1 )
		val ir_in = UInt( INPUT, 1 )
		val v_sdr = UInt( INPUT, 1 )
		val udr = UInt( INPUT, 1 )
		val tdo = UInt( OUTPUT, 1 ) 
		val IO = Bits( OUTPUT, 8 )

	}

	val core = Module( new Core() )
	val mem = Module( new MemHierarchy() )
	val vJTAG = Module( new vJTAG() )

	core.io.MemoryData1 := mem.io.ROut1
	core.io.MemoryData2 := mem.io.ROut2
	core.io.memReady := mem.io.memReady

	mem.io.wen := core.io.MemoryWen
	mem.io.WAddr := core.io.MemoryWAddr
	mem.io.WData := core.io.MemoryWData
	mem.io.WLength := core.io.MemoryWLength
	mem.io.RAddr1 := core.io.MemoryRAddr1
	mem.io.RAddr2 := core.io.MemoryRAddr2
	mem.io.RLength := core.io.MemoryRLength
	mem.io.JTAGRData := vJTAG.io.CoreRData
	mem.io.ren := core.io.MemoryRen
	mem.io.sp := core.io.sp
	
	vJTAG.io.tdi := io.tdi
	vJTAG.io.ir_in := io.ir_in
	vJTAG.io.v_sdr := io.v_sdr
	vJTAG.io.udr := io.udr
	vJTAG.io.CoreWData := mem.io.JTAGWData
	vJTAG.io.CoreWrite := mem.io.JTAGWrite
	vJTAG.io.CoreReq := mem.io.JTAGReq 
	
	io.tdo := vJTAG.io.tdo
	io.IO := mem.io.IO 
}

object ConnectionsMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Connections()))
	}
}

