import Chisel._
import Node._

class StackCache extends Module {
	val io = new Bundle {
		val sp = Bits( INPUT, 32 )
		val wen = Bool( INPUT )
		val waddr = Bits( INPUT, 32 )
		val addr = Bits( INPUT, 32 )
		val WData = Bits( INPUT, 32 )
		val ren = Bool( INPUT )
		val length = Bits( INPUT, 2 )
		val wlength = Bits( INPUT, 2 )
		val memData = Bits( INPUT, 32 )
		val memReady = Bool( INPUT )
		val memWData = Bits( OUTPUT, 32 )
		val memAddr = Bits( OUTPUT, 32 )
		val memWAddr = Bits( OUTPUT, 32 )
		val memLength = Bits( OUTPUT, 2 )
		val memWLength = Bits( OUTPUT, 2 )
		val memRead = Bool( OUTPUT )
		val memWrite = Bool( OUTPUT )
		val ready = Bool( OUTPUT )
		val ROut = Bits( OUTPUT, 32 )
		val bos = Bits( OUTPUT, 32 )
		val tos = Bits( OUTPUT, 32 )
	}

	val size = 128 //stack size in words
	val tag = Mem( Bits( width = 23 ), size )
	val bank0 = Mem( Bits( width = 8 ), size )
	val bank1 = Mem( Bits( width = 8 ), size )
	val bank2 = Mem( Bits( width = 8 ), size )
	val bank3 = Mem( Bits( width = 8 ), size )
	val BankEnables = Bits( width = 4 )
	val tagEnable = Bool()
	val bos = Reg( init = Bits( 2044, width = 32 ) )
	val tos = Reg( init = Bits( 2044-size*4, width = 32 ) )
	val valids = Vec.fill( size ){ Reg( init = Bool( false ) ) }
	val idle :: read_mem :: update_entry :: write :: Nil = Enum( Bits(), 4 )

	val state = Reg( init = Bits( 0, width = 3 ) )

	val in_cache = Bool()
	val currentTag = Bits( width = 23 )

	currentTag := tag( io.addr( 8, 2 ) )

	in_cache := ( currentTag  === io.addr( 31, 9 )  && valids( io.addr( 8, 2 )  ) && ~io.wen ) 

	// move the stack window
	when ( io.sp > bos ) {
		bos := io.sp
		tos := io.sp - Bits( size*4 ) 
	} .elsewhen ( io.sp < tos ) {
		tos := io.sp 
		bos := io.sp + Bits( size*4 ) 
	}
	

	io.memRead := Bool( false )
	io.memWrite := Bool( false )
	io.memAddr := io.addr
	io.memWAddr := io.waddr
	io.memWData := io.WData
	io.memLength := Bits( 0 )
	io.memWLength := Bits( 0 )
	io.ready := Bool( false )
	io.tos := tos
	io.bos := bos 
	
	BankEnables := Bits( "b0000" )
	tagEnable := Bool( false )

	when ( state === idle ) {
		io.ready := in_cache 
		when ( io.wen ) {
			state := write 
		} .elsewhen ( io.ren && ~in_cache ) {
			state := read_mem 
		}
	} .elsewhen ( state === read_mem ) {
		io.ready := Bool( false )
		io.memRead := Bool( true )
		io.memLength := Bits( 3 )
		io.memAddr := io.addr
		BankEnables := Bits( "b1111" )
		tagEnable := Bool( true )
		valids( io.addr( 8, 2 ) ) := Bool( true )
		when ( io.memReady ) {
			state := idle
		}
	} .elsewhen ( state === write ) {
		io.memWrite := Bool( true ) 
		io.ready := Bool( false )
		io.memWLength := io.wlength
		valids( io.waddr( 8, 2 ) ) := Bool( true )
		tagEnable := Bool( true )
		BankEnables := Bits( "b1111" )
		when ( io.memReady ) {
			state := idle 
			io.ready := Bool( true )
		}
	} 
	when ( tagEnable ) {
		tag( io.addr( 8, 2 ) ) := io.addr( 31, 9 )
	}
	when ( io.ren ) {
		when ( BankEnables( 0 ) === Bits( 1 ) ) {
			bank0( io.addr( 8, 2 ) ) := io.memData( 7, 0 )
		}
		when ( BankEnables( 1 ) === Bits( 1 ) ) {
			bank1( io.addr( 8, 2 ) ) := io.memData( 15, 8 )
		}
		when ( BankEnables( 2 ) === Bits( 1 ) ) {
			bank2( io.addr( 8, 2 ) ) := io.memData( 23, 16 )
		}
		when ( BankEnables( 3 ) === Bits( 1 ) ) {
			bank3( io.addr( 8, 2 ) ) := io.memData( 31, 24 )
		}
	} .elsewhen ( io.wen ) {
		when ( BankEnables( 0 ) === Bits( 1 ) ) {
			bank0( io.addr( 8, 2 ) ) := io.WData( 7, 0 )
		}
		when ( BankEnables( 1 ) === Bits( 1 ) ) {
			bank1( io.addr( 8, 2 ) ) := io.WData( 15, 8 )
		}
		when ( BankEnables( 2 ) === Bits( 1 ) ) {
			bank2( io.addr( 8, 2 ) ) := io.WData( 23, 16 )
		}
		when ( BankEnables( 3 ) === Bits( 1 ) ) {
			bank3( io.addr( 8, 2 ) ) := io.WData( 31, 24 )
		}
	}

	when ( io.length === Bits( 0 ) ) {
		switch ( io.addr( 1, 0 ) ) {
			is ( Bits( "b00" ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank3( io.addr( 31, 2 ) ) )
			}
			is ( Bits( "b01" ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank2( io.addr( 31, 2 ) ) )
			}
			is ( Bits( "b10" ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank1( io.addr( 31, 2 ) ) )
			}
			is ( Bits( "b11" ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank0( io.addr( 31, 2 ) ) )
			}
		}
	} .elsewhen ( io.length === Bits( 1 ) ) {
		switch ( io.addr( 1 ) ) {
			is ( Bits( 0 ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), bank3( io.addr( 31, 2 ) ), bank2( io.addr( 31, 2 ) ) )
			}
			is ( Bits( 1 ) ) {
				io.ROut := Cat( Bits( "h00" ), Bits( "h00" ), bank1( io.addr( 31, 2 ) ), bank0( io.addr( 31, 2 ) ) )
			}
		}
	} .otherwise {
		io.ROut := Cat( bank3( io.addr(31,2 ) ), bank2( io.addr(31,2 ) ), bank1( io.addr(31,2 ) ), bank0( io.addr(31,2 ) ) )
	}

	
}

object StackCacheMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new StackCache()))
	}
}
