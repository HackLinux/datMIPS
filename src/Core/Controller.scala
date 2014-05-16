import Chisel._
import Node._

class Controller extends Module {
	val io = new Bundle {
		val op = Bits( INPUT, 6 )
		val funct = Bits( INPUT, 6 )
		val regEq = Bool( INPUT )
		val ALUOp = Bits( OUTPUT, 4 )
		val RegDst = Bool( OUTPUT )
		val RegWrite = Bool( OUTPUT )
		val ALUSrc = Bits( OUTPUT, 2 ) //0: op = second reg 1: op = imm 2: shamt
		val PCSrc = Bits( OUTPUT, 3 ) //0: PC+4 1: badder calc 2: register 3: jtype
		val jal = Bool( OUTPUT )
		val sext = Bool( OUTPUT )
		val MemRead = Bool( OUTPUT )
		val MemWrite = Bool( OUTPUT )
		val MemtoReg = Bool( OUTPUT )
		val MemLength = Bits( OUTPUT, 2 )
		val IDCompare = Bool( OUTPUT )
	}

	//defaults
	io.RegDst := Bool( true )
	io.MemtoReg := Bool( false )
	io.MemRead := Bool( false )
	io.MemWrite := Bool( false )
	io.PCSrc := Bits( 0 )
	io.ALUOp := Bits( 0 )
	io.RegWrite := Bool( false )
	io.ALUSrc := Bits( 0 )
	io.jal := Bool( false )
	io.sext := Bool( false )
	io.MemLength := Bits( 0 )
	io.IDCompare := Bool( false )

	when ( io.op === Bits( 0 ) ) {
		when ( io.funct === Bits( 0 ) ) { //sll
			io.ALUOp := Bits( 6 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 2 ) ) { //srl
			io.ALUOp := Bits( 7 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen( io.funct === Bits( 8 ) ) { //jr
			io.ALUOp := Bits( 0 )
			io.RegWrite := Bool( false )
			io.ALUSrc := Bits( 0 )
			io.PCSrc := Bits( 2 )
		} .elsewhen ( io.funct === Bits( 32 ) ) { //add
			io.ALUOp := Bits( 4 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 33 ) ) { // addu
			io.ALUOp := Bits( 4 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 34 ) ) { // sub
			io.ALUOp := Bits( 5 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 35 ) ) { //subu
			io.ALUOp := Bits( 5 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 36 ) ) { //and
			io.ALUOp := Bits( 0 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 37 ) ) { // or
			io.ALUOp := Bits( 1 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 38 ) ) { // xor
			io.ALUOp := Bits( 3 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 39 ) ) { // nor
			io.ALUOp := Bits( 2 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 42 ) ) { //slt
			io.ALUOp := Bits( 8 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		} .elsewhen ( io.funct === Bits( 43 ) ) { //sltu
			io.ALUOp := Bits( 8 )
			io.RegWrite := Bool( true )
			io.ALUSrc := Bits( 0 )
		}
	} .elsewhen ( io.op === Bits( 2 ) ) { // jump
		io.PCSrc := Bits( 3 )
	} .elsewhen ( io.op === Bits( 3 ) ) { // jal
		io.PCSrc := Bits( 3 ) 
		io.jal := Bool( true )
	} .elsewhen ( io.op === Bits( 4 ) ) { // beq
		io.sext := Bool( true )
		io.IDCompare := Bool( true )
		when ( io.regEq === Bool( true ) ) {
			io.PCSrc := Bits( 1 )
		} .otherwise {
			io.PCSrc := Bits( 0 )
		}
	} .elsewhen ( io.op === Bits( 5 ) ) { // bne
		io.sext := Bool( true )
		io.IDCompare := Bool( true )
		when ( io.regEq === Bool( false ) ) {
			io.PCSrc := Bits( 1 )
		} .otherwise {
			io.PCSrc := Bits( 0 )
		}
	} .elsewhen ( io.op === Bits( 8 ) ) { // addi
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
	} .elsewhen ( io.op === Bits( 9 ) ) { // addiu
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 10 ) ) { // slti
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 8 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
	} .elsewhen ( io.op === Bits( 11 ) ) { // sltiu
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 8 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 12 ) ) { // andi
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 0 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 13 ) ) { // ori
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 1 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 14 ) ) { // xori
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 3 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 15 ) ) { // lui
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 9 )
		io.RegWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
	} .elsewhen ( io.op === Bits( 35 ) ) { // lw
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.RegWrite := Bool( true )
		io.MemtoReg := Bool( true )
		io.MemRead := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 3 )
	} .elsewhen ( io.op === Bits( 36 ) ) { // lbu
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.RegWrite := Bool( true )
		io.MemtoReg := Bool( true )
		io.MemRead := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 0 )
	} .elsewhen ( io.op === Bits( 37 ) ) { // lhu
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.RegWrite := Bool( true )
		io.MemtoReg := Bool( true )
		io.MemRead := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 2 )
	} .elsewhen ( io.op === Bits( 40 ) ) { // sb
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.MemWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 0 )
	} .elsewhen ( io.op === Bits( 41 ) ) { // sh
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.MemWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 2 )
	} .elsewhen( io.op === Bits( 43 ) ) { // sw
		io.RegDst := Bool( false )
		io.ALUOp := Bits( 4 )
		io.MemWrite := Bool( true )
		io.ALUSrc := Bits( 1 )
		io.sext := Bool( true )
		io.MemLength := Bits( 3 )
	}

}

object ControllerMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Controller()))
	}
}
