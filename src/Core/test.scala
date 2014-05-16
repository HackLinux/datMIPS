import Chisel._
import Node._

class test extends Module {
	val io = new Bundle {
		val wen = Bool( INPUT )
		val WAddr = Bits( INPUT, 32 )
		val WData = Bits( INPUT, 32 )
		val WLength = Bits( INPUT, 2 )
		val RAddr1 = Bits( INPUT, 32 )
		val RAddr2 = Bits( INPUT, 32 )
		val RLength = Bits( INPUT, 2 )
		val ROut1 = Bits( OUTPUT, 32 )
		val ROut2 = Bits( OUTPUT, 32 )
	}

	val bank0 = Mem( Bits( width = 8 ), 2048 )
	val bank1 = Mem( Bits( width = 8 ), 2048 )
	val bank2 = Mem( Bits( width = 8 ), 2048 )
	val bank3 = Mem( Bits( width = 8 ), 2048 )
	val BankEnables = Bits( width = 4 )
	val BankWData = Bits( width = 32 )
	val RAddr1Reg = Reg( next = io.RAddr1 )
	val RAddr2Reg = Reg( next = io.RAddr2 )

	when ( io.wen ) {
		when ( io.WLength === Bits( 0 ) ) {
			switch ( io.WAddr( 1, 0 ) ) {
				is ( Bits( "b11" ) ) {
					BankWData := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), io.WData( 7, 0 ) )
					BankEnables := Bits( "b0001" )
				}
				is ( Bits( "b10" ) ) {
					BankWData := Cat( Bits( "h00" ), Bits( "h00" ), io.WData( 7, 0 ), Bits( "h00" ) )
					BankEnables := Bits( "b0010" )
				}
				is ( Bits( "b01" ) ) {
					BankWData := Cat( Bits( "h00" ), io.WData( 7, 0 ), Bits( "h00" ), Bits( "h00" ) )
					BankEnables := Bits( "b0100" )
				}
				is ( Bits( "b00" ) ) {
					BankWData := Cat( io.WData( 7, 0 ), Bits( "h00" ), Bits( "h00" ), Bits( "h00" ) )
					BankEnables := Bits( "b1000" )
				}
			}
		} .elsewhen ( io.WLength === Bits( 1 ) ) {
			switch ( io.WAddr( 1 ) ) {
				is ( Bits( 1 ) ) {
					BankWData := Cat( Bits( "h00" ), Bits( "h00" ), io.WData( 15, 0 ) )
					BankEnables := Bits( "b0011" )
				}
				is ( Bits( 0 ) ) {
					BankWData := Cat( io.WData( 15, 0 ), Bits( "h00" ), Bits( "h00" ) ) 
					BankEnables := Bits( "b1100" )
				}
			}
		} .otherwise {
				BankWData := io.WData 
				BankEnables := Bits( "b1111" )
		}
	} .otherwise {
		BankWData := io.WData 
		BankEnables := Bits( "b0000" )
	}

	when ( BankEnables( 0 ) === Bits( 1 ) ) {
		bank0( io.WAddr( 31, 2 ) ) := BankWData( 7,0 )
	}
	when ( BankEnables( 1 ) === Bits( 1 ) ) {
		bank1( io.WAddr( 31, 2 ) ) := BankWData( 15, 8 )
	}
	when ( BankEnables( 2 ) === Bits( 1 ) ) {
		bank2( io.WAddr( 31, 2 ) ) := BankWData( 23,16 )
	}
	when ( BankEnables( 3 ) === Bits( 1 ) ) {
		bank3( io.WAddr( 31, 2 ) ) := BankWData( 31,24 )
	}

		io.ROut1 := Cat( bank0( RAddr1Reg(31,2 ) ), bank1( RAddr1Reg(31,2 ) ), bank2( RAddr1Reg(31,2 ) ), bank3( RAddr1Reg(31,2 ) ) )

	when ( io.RLength === Bits( 0 ) ) {
		switch ( RAddr1Reg( 1, 0 ) ) {
			is ( Bits( "b00" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank0( RAddr2Reg( 31, 2 ) ) )
			}
			is ( Bits( "b01" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank1( RAddr2Reg( 31, 2 ) ) )
			}
			is ( Bits( "b10" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank2( RAddr2Reg( 31, 2 ) ) )
			}
			is ( Bits( "b11" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank3( RAddr2Reg( 31, 2 ) ) )
			}
		}
	} .elsewhen ( io.RLength === Bits( 1 ) ) {
		switch ( RAddr1Reg( 1 ) ) {
			is ( Bits( 0 ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), bank1( RAddr2Reg( 31, 2 ) ), bank1( RAddr2Reg( 31, 2 ) ) )
			}
			is ( Bits( 1 ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), bank2( RAddr2Reg( 31, 2 ) ), bank3( RAddr2Reg( 31, 2 ) ) )
			}
		}
	} .otherwise {
		io.ROut2 := Cat( bank0( RAddr2Reg(31,2 ) ), bank1( RAddr2Reg(31,2 ) ), bank2( RAddr2Reg(31,2 ) ), bank3( RAddr2Reg(31,2 ) ) )
	}

}

object testMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new test()))
	}
}

