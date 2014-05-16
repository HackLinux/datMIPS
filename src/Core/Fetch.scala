import Chisel._
import Node._

class Fetch extends Module {
	val io = new Bundle {
		val instrin = Bits( INPUT, 32 )
		val memStall = Bool( INPUT )
		val PCWrite = Bool( INPUT )
		val PCSelect = Bool( INPUT )
		val PCData = Bits( INPUT, 32 )
		val PipeWrite = Bool( INPUT )
		val PC = Bits( OUTPUT, 32 )
		val instrout = Bits( OUTPUT, 32 )
		val PipePC = Bits( OUTPUT, 32 )
	}

	val PCReg = Reg( init = Bits( 0, width = 32 ) )
	val PipeInstr = Reg( init = Bits( 0, width = 32 ) )
	val PipePC = Reg( init = Bits( 0, width = 32 ) ) 


	io.PC := PCReg
	io.PipePC := PipePC

	when ( io.PCWrite && ~io.memStall ) {
		switch( io.PCSelect ) {
			is ( Bool( false ) ) { PCReg := PCReg + Bits( 4 ) }
			is ( Bool( true ) ) { PCReg := io.PCData }
		}
	}

	when ( io.PipeWrite && ~io.memStall ) {
		PipeInstr := io.instrin  
		PipePC := PCReg
	}

	io.instrout := PipeInstr 
}


object FetchMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Fetch()))
	}
}


class FetchTester( dut: Fetch ) extends Tester( dut ) {
	poke( dut.io.instrin, 1024 )
	poke( dut.io.PCWrite, 0 )
	poke( dut.io.PCSelect, 0 )
	poke( dut.io.PCData, 666 )
	poke( dut.io.PipeWrite, 0 )
	step( 1 )
	poke( dut.io.PCWrite, 1 )
	poke( dut.io.PipeWrite, 1 )
	step( 1 )
	step( 2 )
}

object FetchTester {
	def main( args: Array[String] ): Unit = {
		chiselMainTest( args, () => Module( new Fetch ) ) {
			f => new FetchTester( f )
		}
	}
}
