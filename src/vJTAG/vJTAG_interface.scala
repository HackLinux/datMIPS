import Chisel._
import Node._
import scala.collection.mutable.HashMap

class vJTAG_interface extends Module {
	val io = new Bundle {
		val tdi = UInt( INPUT, 1 )
		val ir_in = UInt( INPUT, 1 )
		val v_sdr = UInt( INPUT, 1 )
		val udr = UInt( INPUT, 1 )
		val read_data = UInt( INPUT, 8 )
		val read_valid =  UInt( INPUT, 1 )
		val tdo = UInt( OUTPUT, 1 )
		val write_data = UInt( OUTPUT, 8)
		val write_valid = UInt( OUTPUT, 1 )
		val req = Bool( OUTPUT )
		
	}

	val DR = Vec.fill( 9 ){ Reg( init = UInt( 0, width = 1 ) ) }
	val write_reg = Reg( init = UInt( 0, width = 8 ) )
	val write_valid_reg = Reg( init = UInt( 0, width = 1 ) )
	val read_reg = Vec.fill( 8 ){ Reg( init = UInt( 0, width = 1 ) ) }
	val read_valid_reg = Reg( init = UInt( 0, width = 1 ) )

	// write valid reg should only be high for a single cycle
	when ( write_valid_reg === UInt( 1 ) ) {
		write_valid_reg := UInt( 0 )
	}

	when ( io.v_sdr === UInt( 1 ) && io.ir_in === UInt( 1 ) ) {
		// data register shifting
		DR( 8 ) := io.tdi
		for ( i <- 8 to 1 by -1 ) { 
			DR( i - 1 ) := DR( i )
		}
		//shift out the read value and it's validity
		read_reg( 7 ) := read_valid_reg
		read_valid_reg := UInt( 0 )
		for( i <- 7 to 1 by -1 ) {
			read_reg( i - 1 ) := read_reg( i )
		}
		io.tdo := read_reg( 0 )
	} .otherwise {
		//bypass
		io.tdo := io.tdi
	}

	//data transfer is complete
	when ( io.udr === UInt( 1 ) ) {
		//set write reg and write_valid registers
		write_valid_reg := DR( 8 )
		when( DR( 8 ) === UInt( 1 ) ) {
			write_reg := Cat( DR(7), DR(6), DR(5), DR(4), DR(3), DR(2), DR(1), DR(0) ).toBits().toUInt()
		}

		//pull new data into read reg
		io.req := Bool( true )
		read_reg := io.read_data.toBits()
		read_valid_reg := io.read_valid.toBits()

	} .otherwise {
		io.req := Bool( false )
	}

	io.write_data := write_reg
	io.write_valid := write_valid_reg

}

object vJTAG_interfaceMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new vJTAG_interface()))
	}
}

