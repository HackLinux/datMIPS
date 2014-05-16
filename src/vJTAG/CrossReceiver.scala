import Chisel._
import Node._

class CrossReceiver extends Module {
	val io = new Bundle {
		val data_in = UInt( INPUT, 8 )
		val valid_in = Bool( INPUT )
		val taken = Bool( INPUT )
		val ack = Bool( OUTPUT )
		val data_out = UInt( OUTPUT, 8 )
		val ready = Bool( OUTPUT )
	}

	//synchronize the valid signal
	val v0 = Reg( next = io.valid_in )
	val valid = Reg( next = v0 )
	val valid_old = Reg( next = valid )

	//Synchronize data signal
	val d0 = Reg( next = io.data_in )
	val data = Reg( next = d0 )

	val stabledata = Reg( init = UInt( 0, width = 8 ) )

	//state register
	val state = Reg( init = UInt( 0, width = 2 ) )

	when ( state === UInt( 0 ) ) {
		when ( valid === !valid_old ) { 
		state := UInt( 1 )
		}
		stabledata := data 
	}
	when ( state === UInt( 1 ) ) {
		when ( io.taken === Bool( true ) ) { state := UInt( 2 ) }
	}
	when ( state === UInt( 2 ) ) {
		when ( valid === !valid_old ) { state := UInt( 0 ) }
	}

	io.ready := (state === UInt( 1 ) )
	io.ack := (state === UInt( 2 ) )
	io.data_out := stabledata

}

object CrossReceiverMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new CrossReceiver()))
	}
}



