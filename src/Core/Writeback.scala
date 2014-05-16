import Chisel._
import Node._

class Writeback extends Module {
	val io = new Bundle {
		val MemtoReg = Bool( INPUT )
		val RegWrite = Bool( INPUT )
		val MemVal = Bits( INPUT, 32 )
		val ALUVal = Bits( INPUT, 32 )
		val Rd = Bits( INPUT, 5 )
		val outWData = Bits( OUTPUT, 32 )
		val outRd = Bits( OUTPUT, 5 )
		val outRegWrite = Bool( OUTPUT )
	}

	io.outRd := io.Rd 
	io.outRegWrite := io.RegWrite

	when ( io.MemtoReg === Bool( true ) ) {
		io.outWData := io.MemVal 
	} .otherwise {
		io.outWData := io.ALUVal 
	}

}

object WritebackMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Writeback()))
	}
}

