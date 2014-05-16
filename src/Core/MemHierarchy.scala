import Chisel._
import Node._

class MemHierarchy extends Module {
	val io = new Bundle {
		val sp = Bits( INPUT, 32 )
		val JTAGRData = Bits( INPUT, 9 )
		val wen = Bool( INPUT )
		val ren = Bool( INPUT )
		val WAddr = Bits( INPUT, 32 )
		val WData = Bits( INPUT, 32 )
		val WLength = Bits( INPUT, 2 )
		val RAddr1 = Bits( INPUT, 32 )
		val RAddr2 = Bits( INPUT, 32 )
		val RLength = Bits( INPUT, 2 )
		val ROut1 = Bits( OUTPUT, 32 )
		val ROut2 = Bits( OUTPUT, 32 )
		val JTAGWData = Bits( OUTPUT, 8 )
		val JTAGWrite = Bool( OUTPUT )
		val JTAGReq = Bool( OUTPUT )
		val IO = Bits( OUTPUT, 8 )
		val memReady = Bool( OUTPUT )
	}

	val stackcache = Module( new StackCache() )
	val mainmem = Module( new MainMem() )

	val stackrange = Bool()
	
	//top quarter of memory is stack range
	stackrange := io.WAddr > Bits(1536) || io.RAddr2 > Bits(1536) 

	mainmem.io.RAddr1 := io.RAddr1 
	mainmem.io.JTAGRData := io.JTAGRData
	stackcache.io.sp := io.sp

	io.ROut1 := mainmem.io.ROut1
	io.JTAGWData := mainmem.io.JTAGWData
	io.JTAGWrite := mainmem.io.JTAGWrite
	io.JTAGReq := mainmem.io.JTAGReq
	io.IO := mainmem.io.IO

	when ( ( (io.RAddr2 < stackcache.io.bos && io.RAddr2 > stackcache.io.tos ) ||
		(io.WAddr < stackcache.io.bos && io.WAddr > stackcache.io.tos ) ) && stackrange ) {
		stackcache.io.wen := io.wen
		stackcache.io.addr := io.RAddr2
		stackcache.io.WData := io.WData 
		stackcache.io.waddr := io.WAddr 
		stackcache.io.ren := io.ren 
		stackcache.io.length := io.RLength 
		stackcache.io.wlength := io.WLength
		stackcache.io.memData := mainmem.io.ROut2
		stackcache.io.memReady := mainmem.io.ready

		mainmem.io.wen := stackcache.io.memWrite
		mainmem.io.ren := stackcache.io.memRead
		mainmem.io.WAddr := stackcache.io.memWAddr 
		mainmem.io.WLength := stackcache.io.memWLength
		mainmem.io.RAddr2 := stackcache.io.memAddr 
		mainmem.io.WData := stackcache.io.memWData
		mainmem.io.RLength := stackcache.io.memLength

		io.ROut2 := stackcache.io.ROut
		io.memReady := stackcache.io.ready
	} .otherwise {
		stackcache.io.wen := Bool( false )
		stackcache.io.ren := Bool( false )
		stackcache.io.addr := Bits( 0 )
		stackcache.io.waddr := Bits( 0 )
		stackcache.io.WData := Bits( 0 )
		stackcache.io.length := Bits( 0 )
		stackcache.io.wlength := Bits( 0 )
		stackcache.io.memData := Bits( 0 )
		stackcache.io.memReady := Bool( false )
		
		mainmem.io.wen := io.wen
		mainmem.io.ren := io.ren
		mainmem.io.RAddr2 := io.RAddr2 
		mainmem.io.WLength := io.WLength
		mainmem.io.RLength := io.RLength
		mainmem.io.WAddr := io.WAddr 
		mainmem.io.WData := io.WData 

		io.ROut2 := mainmem.io.ROut2
		io.memReady := mainmem.io.ready
	}
}

object MemHierarchyMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new MemHierarchy()))
	}
}
