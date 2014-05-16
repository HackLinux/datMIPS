import Chisel._
import Node._

class HazardUnit extends Module {
	val io = new Bundle {
		val IDEXMemRead = Bool( INPUT )
		val IDEXRegWrite = Bool( INPUT )
		val IDEXRd = Bits( INPUT, 5 )
		val IFIDRt = Bits( INPUT, 5 )
		val IFIDRs = Bits( INPUT, 5 )
		val IDCompare = Bool( INPUT )
		val stall = Bool( OUTPUT )
	}

	when ( io.IDEXMemRead && ( ( io.IDEXRd === io.IFIDRs ) || ( io.IDEXRd === io.IFIDRt )  ) ) {
		// load use hazard
		io.stall := Bool( true )
	} .elsewhen( io.IDCompare && io.IDEXRegWrite && ( ( io.IDEXRd === io.IFIDRs ) || ( io.IDEXRd === io.IFIDRt ) ) ) {
		// trying to compare something currently being calculated
		io.stall := Bool( true )
	} .otherwise {
		io.stall := Bool( false )
	}
}

object HazardUnitMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new HazardUnit()))
	}
}
