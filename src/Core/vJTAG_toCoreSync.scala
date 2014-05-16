import Chisel._
import Node._
import scala.collection.mutable.HashMap

//Synchronize signals going to the vJTAG write buffer
class vJTAG_toCoreSync extends Module {
	val io = new Bundle {
		val valid_in = UInt( INPUT, 1 )
		val write_in = UInt( INPUT, 1 )
		val write_out = UInt( OUTPUT, 1 )
		val valid_out = UInt( OUTPUT, 1 )
		val request_in = UInt( INPUT, 1 )
		val request_out = UInt( OUTPUT, 1 )
	}

	val r0_valid = Reg( next = io.valid_in )
	val r1_valid = Reg( next = r0_valid )
	io.valid_out := r1_valid

	val r0_write = Reg( next = io.write_in )
	val r1_write = Reg( next = r0_write )
	io.write_out := r1_write 

	val r0_request = Reg( next = io.request_in )
	val r1_request = Reg( next = r0_request )
	io.request_out := r1_request

}

object vJTAG_toCoreSyncMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new vJTAG_toCoreSync()))
	}
}
