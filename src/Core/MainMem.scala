import Chisel._
import Node._

class MainMem extends Module {
	val io = new Bundle {
		val JTAGRData = Bits( INPUT, 9 )
		val wen = Bool( INPUT )
		val WAddr = Bits( INPUT, 32 )
		val WData = Bits( INPUT, 32 )
		val WLength = Bits( INPUT, 2 )
		val RAddr1 = Bits( INPUT, 32 )
		val RAddr2 = Bits( INPUT, 32 )
		val ren = Bool( INPUT )
		val RLength = Bits( INPUT, 2 )
		val ROut1 = Bits( OUTPUT, 32 )
		val ROut2 = Bits( OUTPUT, 32 )
		val JTAGWData = Bits( OUTPUT, 8 )
		val JTAGWrite = Bool( OUTPUT )
		val JTAGReq = Bool( OUTPUT )
		val ready = Bool( OUTPUT )
		val IO = Bits( OUTPUT, 8 )
	}

	// readBin reads a binary file and creates a rom from it. Stolen from patmos.
	def readBin(fileName: String, width: Int): Vec[Bits] = {

		val bytesPerWord = (width+7) / 8

		println("Reading " + fileName)
		// an encoding to read a binary file? Strange new world.
		val source = scala.io.Source.fromFile(fileName)(scala.io.Codec.ISO8859)
		val byteArray = source.map(_.toByte).toArray
		source.close()

		// use an array to convert input
		val arr = new Array[Bits](math.max(1, byteArray.length / bytesPerWord))

		if (byteArray.length == 0) {
		  arr(0) = Bits(0, width = width)
		}

		for (i <- 0 until byteArray.length / bytesPerWord) {
		  var word = 0
		  for (j <- 0 until bytesPerWord) {
			word <<= 8
			word += byteArray(i * bytesPerWord + j).toInt & 0xff
		  }
		  // printf("%08x\n", Bits(word))
		  arr(i) = Bits(word, width = width)
		}

		// use vector to represent ROM
		Vec[Bits](arr)
	  }


	val rom = readBin( "generated/bootloader.o", 32 )
	val bank0 = Mem( Bits( width = 8 ), 2048 )
	val bank1 = Mem( Bits( width = 8 ), 2048 )
	val bank2 = Mem( Bits( width = 8 ), 2048 )
	val bank3 = Mem( Bits( width = 8 ), 2048 )
	val BankEnables = Bits( width = 4 )
	val BankWData = Bits( width = 32 )
	val IOReg = Reg( init = Bits( 0, width = 8 ) )

	io.ready := Bool( true )
	

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

	io.JTAGWrite := Bool( false )
	io.JTAGWData := Bits( 0 )
	
	when ( io.WAddr === Bits( 1024 ) && io.wen ) {
		io.JTAGWData := io.WData( 7, 0 )
		io.JTAGWrite := Bool( true )
	} .elsewhen ( io.WAddr === Bits( 1023 ) && io.wen ) {
		IOReg := io.WData( 7, 0 )
	} .otherwise {
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
	}


	when ( io.RAddr1 < UInt( rom.length * 4 ) ) {
		io.ROut1 := rom( io.RAddr1( 31, 2 ) )
	} .otherwise {
		io.ROut1 := Cat( bank3( io.RAddr1(31,2 ) ), bank2( io.RAddr1(31,2 ) ), bank1( io.RAddr1(31,2 ) ), bank0( io.RAddr1(31,2 ) ) )
	}

	io.JTAGReq := Bool( false )
	when( io.RAddr2 === Bits( 1024 ) ) {
		io.ROut2 := Cat( Bits( "b00000000000000000000000" ), io.JTAGRData )
		io.JTAGReq := Bool( true )
	} .elsewhen ( io.RLength === Bits( 0 ) ) {
		switch ( io.RAddr2( 1, 0 ) ) {
			is ( Bits( "b00" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank3( io.RAddr2( 31, 2 ) ) )
			}
			is ( Bits( "b01" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank2( io.RAddr2( 31, 2 ) ) )
			}
			is ( Bits( "b10" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank1( io.RAddr2( 31, 2 ) ) )
			}
			is ( Bits( "b11" ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), Bits( "h00" ), bank0( io.RAddr2( 31, 2 ) ) )
			}
		}
	} .elsewhen ( io.RLength === Bits( 1 ) ) {
		switch ( io.RAddr2( 1 ) ) {
			is ( Bits( 0 ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), bank3( io.RAddr2( 31, 2 ) ), bank2( io.RAddr2( 31, 2 ) ) )
			}
			is ( Bits( 1 ) ) {
				io.ROut2 := Cat( Bits( "h00" ), Bits( "h00" ), bank1( io.RAddr2( 31, 2 ) ), bank0( io.RAddr2( 31, 2 ) ) )
			}
		}
	} .otherwise {
		io.ROut2 := Cat( bank3( io.RAddr2(31,2 ) ), bank2( io.RAddr2(31,2 ) ), bank1( io.RAddr2(31,2 ) ), bank0( io.RAddr2(31,2 ) ) )
	}
	

	io.IO := IOReg

}

object MemMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new MainMem()))
	}
}


class MainMemTester( dut: MainMem ) extends Tester( dut ) {
	for ( i <- 0 to 32 by 1 ) {
		poke( dut.io.RAddr1, i*4 )
		step( 1 )
		peek( dut.io.ROut1 )
	}
	step( 1 )
	peek( dut.io.ROut1 )

}

object MainMemTester {
	def main( args: Array[String] ): Unit = {
		chiselMainTest( args, () => Module( new MainMem ) ) {
			f => new MainMemTester( f )
		}
	}
}
