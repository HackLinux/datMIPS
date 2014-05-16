import Chisel._
import Node._
import scala.collection.mutable.HashMap

class CrossSender extends Module {
	val io = new Bundle {
		val data_in = UInt( INPUT, 8 )
		val ack = Bool( INPUT )
		val ready = Bool( INPUT )
		val data_out = UInt( OUTPUT, 8 )
		val valid = Bool( OUTPUT )
		val take = Bool( OUTPUT )
	}

	//Synchronize acknowledge signal
	val ack0 = Reg( next = io.ack )
	val ack = Reg( next = ack0 )
	val ack_old = Reg( next = ack )

	val stabledata = Reg( init = Bits( 0, width = 8 ) )

	//states and state register
	val waiting = UInt( 0 )
	val update_data = UInt( 1 )
	val validate = UInt( 2 )
	val complete = UInt( 3 )
	val state = Reg( init = UInt( 0, width = 2) )

	when ( state === waiting ) {
		stabledata := io.data_in
		when ( io.ready ) { state := update_data }
	} .elsewhen ( state === update_data ) {
		state := validate 
	} .elsewhen ( state === validate ) {
		when ( ack === !ack_old ) { state := complete }
	} .elsewhen ( state === complete ) {
		when ( ack === !ack_old ) { state := waiting }
	}

	io.take := ( state === waiting )
	io.valid := ( state === validate )
	io.data_out := stabledata 

}

object CrossSenderMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new CrossSender()))
	}
}

class CrossSenderTester( dut: CrossSender ) extends Tester( dut ) {
	poke( dut.io.data_in,  111 )
	poke( dut.io.ready, 0 )
	poke( dut.io.ack, 0 )
	step( 1 )
	poke( dut.io.ready, 1 )
	step( 1 )
	poke( dut.io.ready, 0 )
	step( 1 )
	poke( dut.io.ack, 1 )
	step( 1 )
	step( 1 )
	step( 1 )
	step( 1 )
	step( 1 )
}

object CrossSenderTester {
	def main( args: Array[String]): Unit = {
		chiselMainTest(args, () => Module( new CrossSender ) ) {
			f => new CrossSenderTester(f)
		}
	}
}
