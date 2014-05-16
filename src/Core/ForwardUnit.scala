import Chisel._
import Node._

class ForwardUnit extends Module {
	val io = new Bundle {
		val IFIDRs = Bits( INPUT, 5 )
		val IFIDRt = Bits( INPUT, 5 )
		val EXMEMRegWrite = Bool( INPUT )
		val EXMEMRd = Bits( INPUT, 5 )
		val EXMEMRt = Bits( INPUT, 5 )
		val MEMWBRegWrite = Bool( INPUT )
		val MEMWBMemWrite = Bool( INPUT )
		val MEMWBRd = Bits( INPUT, 5 )
		val IDEXRs = Bits( INPUT, 5 )
		val IDEXRt = Bits( INPUT, 5 )
		val ForwardA = Bits( OUTPUT, 2 )
		val ForwardB = Bits( OUTPUT, 2 )
		val ForwardMem = Bool( OUTPUT )
		val CompA = Bool( OUTPUT )
		val CompB = Bool( OUTPUT )
	}

	//Ex and Mem forwarding to execution stage
	when ( io.EXMEMRegWrite && ( io.EXMEMRd != Bits( 0 ) ) && ( io.EXMEMRd === io.IDEXRs ) ) {
		io.ForwardA := Bits( "b10" )
	} .elsewhen ( io.MEMWBRegWrite && (io.MEMWBRd != Bits( 0 ) ) && ( io.MEMWBRd === io.IDEXRs ) ) {
		io.ForwardA := Bits( "b01" )
	} .otherwise {
		io.ForwardA := Bits( "b00" )
	}
	when ( io.EXMEMRegWrite && ( io.EXMEMRd != Bits( 0 ) ) && ( io.EXMEMRd === io.IDEXRt ) ) { 
		io.ForwardB := Bits( "b10" )
	} .elsewhen ( io.MEMWBRegWrite && (io.MEMWBRd != Bits( 0 ) ) && ( io.MEMWBRd === io.IDEXRt ) ) {
		io.ForwardB := Bits( "b01" )
	} .otherwise {
		io.ForwardB := Bits( "b00" )
	}

	// Forward EXMEM to comparator in ID stage
	when ( io.EXMEMRegWrite && ( io.EXMEMRd != Bits( 0 ) ) && ( io.EXMEMRd === io.IFIDRs ) ) {
		io.CompA := Bool( true )
	} .otherwise {
		io.CompA := Bool( false )
	}

	when ( io.EXMEMRegWrite && ( io.EXMEMRd != Bits( 0 ) ) && ( io.EXMEMRd === io.IFIDRt ) ) {
		io.CompB := Bool( true )
	} .otherwise {
		io.CompB := Bool( false )
	}


	// Load store forwarding
	when ( io.MEMWBRegWrite && ( io.EXMEMRt != Bits( 0 ) ) && ( io.MEMWBRd === io.EXMEMRt ) ) {
		io.ForwardMem := Bool( true )
	} .otherwise {
		io.ForwardMem := Bool( false )
	}
}

object ForwardUnitMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new ForwardUnit()))
	}
}
