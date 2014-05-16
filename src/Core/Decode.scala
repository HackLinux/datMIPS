import Chisel._
import Node._

class Decode extends Module {
	val io = new Bundle {
		val memStall = Bool( INPUT )
		val instr = Bits( INPUT, 32 )
		val CompA = Bool( INPUT )
		val CompB = Bool( INPUT )
		val wr = Bits( INPUT, 5 )
		val wdata = Bits( INPUT, 32 )
		val we = Bool( INPUT )
		val FwdData = Bits( INPUT, 32 )
		val PC = Bits( INPUT, 32 )
		val stall = Bool( OUTPUT )
		val PCSelect = Bool( OUTPUT )
		val PCData = Bits( OUTPUT, 32 )
		val imm = Bits( OUTPUT, 32 )
		val Rd = Bits( OUTPUT, 5 )
		val Rt = Bits( OUTPUT, 5 )
		val Rs = Bits( OUTPUT, 5 )
		val rsdata = Bits( OUTPUT, 32 )
		val rtdata = Bits( OUTPUT, 32 )
		val ALUOp = Bits( OUTPUT, 4 )
		val ALUSrc = Bits( OUTPUT, 2 )
		val shamt = Bits( OUTPUT, 5 )
		val MemRead = Bool( OUTPUT )
		val MemWrite = Bool( OUTPUT )
		val MemtoReg = Bool( OUTPUT )
		val RegWrite = Bool( OUTPUT )
		val outMemLength = Bits( OUTPUT, 2 )
		val sp = Bits( OUTPUT, 32 )
	}

	val rsdata = Bits( width = 32 )
	val rtdata = Bits( width = 32 )
	val regEq = Bool()
	val imm = Bits( width = 32 )
	val PCBranch = Bits( width = 32 )
	//pipe registers
	val PipeRd = Reg( init = Bits( 0, width = 5 ) )
	val PipeRs = Reg( init = Bits( 0, width = 5 ) )
	val PipeRt = Reg( init = Bits( 0, width = 5 ) )
	val PipeRsData = Reg( init = Bits( 0, width = 32 ) )
	val PipeRtData = Reg( init = Bits( 0, width = 32 ) ) 
	val PipeShamt = Reg( init = Bits( 0, width = 5 ) )
	val PipeImm = Reg( init = Bits( 0, width = 32 ) )
	val PipeALUOp = Reg( init = Bits( 0, width = 4 ) )
	val PipeALUSrc = Reg( init = Bits( 0, width = 2 ) )
	val PipeMemRead = Reg( Bool() )
	val PipeMemWrite = Reg( Bool() )
	val PipeMemtoReg = Reg( Bool() )
	val PipeRegWrite = Reg( Bool() )
	val PipeMemLength = Reg( Bits( 0, width = 2 ) )

	// hazard unit 
	val haz = Module( new HazardUnit() )

	// main control
	val ctl = Module( new Controller() )

	// register file connections
	val rf = Module( new RegisterFile() )

	
	// register file connections
	rf.io.rr1 := io.instr( 25, 21 ) //rs
	rf.io.rr2 := io.instr( 20, 16 ) //rt
	rf.io.wr := io.wr
	rf.io.wdata := io.wdata 
	rf.io.we := io.we 
	rf.io.jale := ctl.io.jal
	rf.io.jaldata := io.PC + UInt( 8 ) 
	rsdata := rf.io.rd1 
	rtdata := rf.io.rd2 

	// Comparator
	when ( io.CompA ) {
		regEq := io.FwdData === rtdata 
	} .elsewhen( io.CompB ) {
		regEq := rsdata === io.FwdData 
	} .otherwise {
		regEq := rsdata === rtdata 
	}

	ctl.io.op := io.instr( 31, 26 )
	ctl.io.funct := io.instr( 5, 0 )
	ctl.io.regEq := regEq 

	//sign extension
	when ( ctl.io.sext ) {
		switch( io.instr( 15 ) ) {
			is ( Bits( 1 ) ) { imm := Cat( Bits( "hffff" ), io.instr( 15, 0 ) )  }
			is ( Bits( 0 ) ) { imm := Cat( Bits( "h0000" ), io.instr( 15, 0 ) )  }
		}
	} .otherwise {
		imm := Cat( Bits( "h0000" ), io.instr( 15, 0 ) ) 
	}


	// branch adder
	PCBranch := io.PC + Bits( 4 ) + ( imm << Bits( 2 ) ) 

	when ( ctl.io.PCSrc === Bits( 0 ) ) {
		io.PCSelect := Bool( false )
		io.PCData := Bits( 0 )
	} .elsewhen ( ctl.io.PCSrc === Bits( 1 ) ) {
		io.PCSelect := Bool( true ) 
		io.PCData := PCBranch
	} .elsewhen ( ctl.io.PCSrc === Bits( 2 ) ) {
		io.PCSelect := Bool( true )
		io.PCData := rsdata 
	} .elsewhen ( ctl.io.PCSrc === Bits( 3 ) ) {
		io.PCSelect := Bool( true )
		io.PCData := Cat( io.PC( 31, 28 ), io.instr( 25, 0 ) << Bits( 2 ) )
	} .otherwise {
		io.PCSelect := Bool( false )
		io.PCData := Bits( 0 )
	}

	haz.io.IDEXMemRead := PipeMemRead 
	haz.io.IDEXRd := PipeRd 
	haz.io.IDEXRegWrite := PipeRegWrite
	haz.io.IFIDRs := io.instr( 26, 21 )
	haz.io.IFIDRt := io.instr( 20, 16 )
	haz.io.IDCompare := ctl.io.IDCompare
	io.stall := haz.io.stall 

	when ( haz.io.stall ) {
		PipeRs := Bits( 0 )
		PipeRt := Bits( 0 )
		PipeRd := Bits( 0 )
		PipeRegWrite := Bool( false )
		PipeRsData := Bits( 0 )
		PipeRtData := Bits( 0 )
		PipeShamt := Bits( 0 )
		PipeMemRead := Bool( false )
		PipeMemWrite := Bool( false )
		PipeMemtoReg := Bool( false )
		PipeALUOp := Bits( 0 )
		PipeALUSrc := Bits( 0 )
		PipeMemLength := Bits( 0 )
		PipeImm := Bits( 0 ) 
	} .elsewhen ( ~io.memStall ) {
		//set Rd for set of the pipeline
		when ( ctl.io.RegDst ) {
			PipeRd := io.instr( 15, 11 )
		} .otherwise {
			PipeRd := io.instr( 20, 16 )
		}
		PipeRs := io.instr( 25,21 )
		PipeRt := io.instr( 20,16 )
		PipeRegWrite := ctl.io.RegWrite 
		PipeRsData := rsdata 
		PipeRtData := rtdata 
		PipeShamt := io.instr( 10, 6 )
		PipeMemRead := ctl.io.MemRead
		PipeMemWrite := ctl.io.MemWrite 
		PipeMemtoReg := ctl.io.MemtoReg 
		PipeALUOp := ctl.io.ALUOp
		PipeALUSrc := ctl.io.ALUSrc
		PipeMemLength := ctl.io.MemLength
		PipeImm := imm 
	}

	io.Rd := PipeRd 
	io.Rs := PipeRs 
	io.Rt := PipeRt
	io.rsdata := PipeRsData 
	io.rtdata := PipeRtData 
	io.shamt := PipeShamt
	io.imm := PipeImm 
	io.ALUOp := PipeALUOp 
	io.ALUSrc := PipeALUSrc 
	io.MemRead := PipeMemRead
	io.MemWrite := PipeMemWrite
	io.MemtoReg := PipeMemtoReg 
	io.RegWrite := PipeRegWrite 
	io.outMemLength := PipeMemLength
	io.sp := rf.io.sp
}

object DecodeMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Decode()))
	}
}
