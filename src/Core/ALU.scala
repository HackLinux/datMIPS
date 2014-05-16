import Chisel._
import Node._

class ALU extends Module {
	val io = new Bundle {
		val a = UInt( INPUT, 32 )
		val b = UInt( INPUT, 32 )
		val op = Bits( INPUT, 4 )
		val shamt = Bits( INPUT, 5 )
		val out = UInt( OUTPUT, 32 )
		val zero = Bool( OUTPUT )
	}

	when ( io.op === Bits( 0 ) ) {        // AND
		io.out := io.a & io.b 
	} .elsewhen ( io.op === Bits( 1 ) ) { // OR
		io.out := io.a | io.b 
	} .elsewhen ( io.op === Bits( 2 ) ) { // NOR
		io.out := ~( io.a | io.b ) 
	} .elsewhen ( io.op === Bits( 3 ) ) { // XOR
		io.out := io.a ^ io.b
	} .elsewhen ( io.op === Bits( 4 ) ) { // ADD
		io.out := io.a + io.b
	} .elsewhen ( io.op === Bits( 5 ) ) { // SUB
		io.out := io.a - io.b
	} .elsewhen ( io.op === Bits( 6 ) ) { // sll
		io.out := io.b << io.shamt
	} .elsewhen ( io.op === Bits( 7 ) ) { // srl
		io.out := io.b >> io.shamt
	} .elsewhen ( io.op === Bits( 8 ) ) { // slt
		io.out := io.a < io.b
	} .elsewhen ( io.op === Bits( 9 ) ) { // shift immediate by 16
		io.out := io.b << UInt( 16 )
	} .otherwise {
		io.out := UInt( 0 )
	}

	io.zero := ( io.a - io.b ) === UInt( 0 )
}

object ALUMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new ALU()))
	}
}

