import Chisel._
import Node._

class Core_tovJTAGSync extends Module {
	val io = new Bundle {
		val read_in = UInt( INPUT, 8 )
		val valid_in = UInt( INPUT, 1 )
		val read_out = UInt( OUTPUT, 8 )
		val valid_out = UInt( OUTPUT, 1 )
	}

	val r0_read = Reg( next = io.read_in )
	val r1_read = Reg( next = r0_read )
	io.read_out := r1_read

	val r0_valid = Reg( next = io.valid_in )
	val r1_valid = Reg( next = r0_valid )
	io.valid_out := r1_valid

}

object Core_tovJTAGSyncMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Core_tovJTAGSync()))
	}
}
