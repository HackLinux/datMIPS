import Chisel._
import Node._

class JTAGBuffer extends Module {
	val io = new Bundle {
		val ready = Bool( INPUT )
		val req = Bool( INPUT )
		val data_in = Bits( INPUT, 8 )
		val taken = Bool( OUTPUT )
		val data_out = Bits( OUTPUT, 9 )
	}

	val buffer = Vec.fill( 32 ){ Reg( init = Bits( 0, width = 8 )  ) }
	val index = Reg( init = UInt( 0, width = 5 ) )
	val valid = Bool()
	//val state = Reg( init = UInt( 0, width = 2 ) )

	valid := ~(index === UInt( 0 ) )

	buffer( 0 ) := Bits( "hFF" )
	io.data_out := Cat( valid.toBits(), buffer( index ) )
	io.taken := Bool( false )
	/*
	when ( io.req ) {
		when ( index != Bits( 0 ) ) {
			index := index - Bits( 1 )
		}
	} .elsewhen ( io.ready ) {
		io.taken := Bool( true )
		buffer( index + Bits( 1 ) ) := io.data_in 
		index := index + Bits( 1 )
	} */

	
    when ( io.ready ) {
		io.taken := Bool( true )
		for ( i <- 1 to 30 by 1 ) { 
			buffer( i + 1 ) := buffer( i )
		}
		buffer( 1 ) := io.data_in
		index := index + Bits( 1 )
	} .elsewhen ( io.req ) {
		when ( index != Bits( 0 ) ) {
			index := index - Bits( 1 )
		}
	} 

   /*
    when ( state === Bits( 0 ) ) {
		when ( io.ready ) {
			state := Bits( 1 ) 
		} .elsewhen ( io.req ) {
			state := Bits( 2 ) 
		}
	} .elsewhen ( state === Bits( 1 ) ) {
		state := Bits( 0 )
		io.taken := Bool( true )
		for ( i <- 1 to 30 by 1 ) { 
			buffer( i + 1 ) := buffer( i )
		}
		buffer( 1 ) := io.data_in
		index := index + Bits( 1 )
	} .elsewhen ( state === Bits( 2 ) ) {
		state := Bits( 0 )
		index := index - Bits( 1 )
	}
	*/


}

object JTAGBufferMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new JTAGBuffer ) )
	}
}
