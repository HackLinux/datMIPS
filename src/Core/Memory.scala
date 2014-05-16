import Chisel._
import Node._

class Memory extends Module {
	val io = new Bundle {
		val memReady = Bool( INPUT )
		val rtdata = Bits( INPUT, 32 )
		val ForwardMem = Bool( INPUT )
		val FwdData = Bits( INPUT, 32 )
		val Rd = Bits( INPUT, 5 )
		val ALUVal = Bits( INPUT, 32 )
		val MemtoReg = Bool( INPUT )
		val MemWrite = Bool( INPUT )
		val MemRead = Bool( INPUT )
		val RegWrite = Bool( INPUT )
		val MemData = Bits( INPUT, 32 )
		val MemLength = Bits( INPUT, 2 )
		val outRd = Bits( OUTPUT, 5 )
		val outALUVal = Bits( OUTPUT, 32 )
		val outMemVal = Bits( OUTPUT, 32 )
		val outMemWrite = Bool( OUTPUT )
		val outRegWrite = Bool( OUTPUT )
		val outMemtoReg = Bool( OUTPUT )
		val wen = Bool( OUTPUT )
		val WData = Bits( OUTPUT, 32 )
		val WAddr = Bits( OUTPUT, 32 )
		val WLength = Bits( OUTPUT, 2 )
		val RLength = Bits( OUTPUT, 2 )
		val RAddr = Bits( OUTPUT, 32 )
		val memStall = Bool( OUTPUT )
		val ren = Bool( OUTPUT )
	}

	val PipeMemtoReg = Reg( Bool() )
	val PipeRegWrite = Reg( Bool() )
	val PipeMemWrite = Reg( Bool() )
	val PipeMemVal = Reg( Bits( 0, width = 32 ) )
	val PipeALUVal = Reg( Bits( 0, width = 32 ) )
	val PipeRd = Reg( Bits( 0, width = 5 ) )

	val memStall = Bool()

	memStall := ~io.memReady && ( io.MemWrite || io.MemRead ) 

	when ( ~memStall ) {
		PipeALUVal := io.ALUVal 
		PipeRegWrite := io.RegWrite 
		PipeMemtoReg := io.MemtoReg 
		PipeRd := io.Rd 
		PipeMemVal := io.MemData 
		PipeMemWrite := io.MemWrite
	}

	//defaults
	io.RLength := io.MemLength 
	io.RAddr := io.ALUVal 
	io.WLength := io.MemLength 
	io.WAddr := io.ALUVal 
	io.wen := Bool( false )
	io.WData := io.rtdata 
	io.ren := Bool( false )

	//memory logic
	when ( io.MemRead ) {
		io.RLength := io.MemLength 
		io.RAddr := io.ALUVal 
		io.ren := Bool( true )
	} .elsewhen ( io.MemWrite ) {
		io.WLength := io.MemLength 
		io.WAddr := io.ALUVal 
		io.wen := Bool( true )
		when ( io.ForwardMem === Bool( false ) ) {
			io.WData := io.rtdata 
		} .otherwise {
			io.WData := io.FwdData 
		}
	 
	}

	io.outMemtoReg := PipeMemtoReg 
	io.outRegWrite := PipeRegWrite 
	io.outMemVal := PipeMemVal 
	io.outALUVal := PipeALUVal 
	io.outRd := PipeRd 
	io.outMemWrite := PipeMemWrite
	io.memStall := memStall
}

object MemoryMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Memory()))
	}
}


