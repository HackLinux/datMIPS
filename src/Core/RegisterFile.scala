import Chisel._
import Node._

class RegisterFile extends Module {
	val io = new Bundle {
		val jale = Bool( INPUT )
		val jaldata = Bits( INPUT, 32 )
		val rr1 = UInt( INPUT, 5 ) //read register 1
		val rr2 = UInt( INPUT, 5 ) //read register 2
		val wr = UInt( INPUT, 5 ) //write register
		val wdata = UInt( INPUT, 32 ) //write data
		val we = Bool( INPUT ) //write enable
		val rd1 = UInt( OUTPUT, 32 ) //read data 1
		val rd2 = UInt( OUTPUT, 32 ) //read data 2
		val sp = Bits( OUTPUT, 32 ) //stackpointer
	}

	val RegVec = Vec.fill( 32 ){ Reg( init = UInt( 0, width = 32 ) ) }

	for ( i <- 1 until 31 by 1 ) {
		RegVec( i ) := RegVec( i )
	}

	//Reg 0 is always 0
	RegVec( 0 ) := UInt( 0 )

	//read from registers
	io.rd1 := RegVec( io.rr1 )
	io.rd2 := RegVec( io.rr2 )
	io.sp := RegVec( 29 )


	//write to registers
	when ( io.we === Bool( true ) ) {
		when ( io.wr != UInt( 0 ) ) {
			RegVec( io.wr ) := io.wdata

			//forward write if it is being read
			when ( io.rr1 === io.wr ) {
				io.rd1 := io.wdata
			}
			when ( io.rr2 === io.wr ) {
				io.rd2 := io.wdata
			}	
		}
	} 

	when ( io.jale ) {
		RegVec( 31 ) := io.jaldata 
	}
}


object RegisterFileMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new RegisterFile()))
	}
}
