import Chisel._
import Node._

class Execute extends Module {
	val io = new Bundle {
		val memStall = Bool( INPUT )
		val MemLength = Bits( INPUT, 2 )
		val imm = Bits( INPUT, 32 )
		val Rd = Bits( INPUT, 5 )
		val Rt = Bits( INPUT, 5 )
		val Rs = Bits( INPUT, 5 )
		val rsdata = Bits( INPUT, 32 )
		val rtdata = Bits( INPUT, 32 )
		val ALUOp = Bits( INPUT, 4 )
		val ALUSrc = Bits( INPUT, 2 )
		val shamt = Bits( INPUT, 5 )
		val MemRead = Bool( INPUT )
		val MemWrite = Bool( INPUT )
		val MemtoReg = Bool( INPUT )
		val RegWrite = Bool( INPUT )
		val IFIDRs = Bits( INPUT, 5 )
		val IFIDRt = Bits( INPUT, 5 )
		val EXMEMRegWrite = Bool( INPUT )
		val EXMEMRd = Bits( INPUT, 5 )
		val EXMEMRt = Bits( INPUT, 5 )
		val EXMEMALUVal = Bits( INPUT, 32 )
		val MEMWBRegWrite = Bool( INPUT )
		val MEMWBMemWrite = Bool( INPUT )
		val MEMWBRd = Bits( INPUT, 5 )
		val MEMWBVal = Bits( INPUT, 32 )
		val IDEXRs = Bits( INPUT, 5 )
		val IDEXRt = Bits( INPUT, 5 )
		val ForwardMem = Bool( OUTPUT )
		val CompA = Bool( OUTPUT )
		val CompB = Bool( OUTPUT )
		val outMemWrite = Bool( OUTPUT )
		val outMemRead = Bool( OUTPUT )
		val outMemtoReg = Bool( OUTPUT )
		val outRegWrite = Bool( OUTPUT )
		val outRd = Bits( OUTPUT, 5 )
		val ALUVal = Bits( OUTPUT, 32 )
		val outMemLength = Bits( OUTPUT, 2 )
		val outrtdata = Bits( OUTPUT, 32 )
		val outRt = Bits( OUTPUT, 5 )
	}

	// Pipeline registers for next stage
	val PipeRd = Reg( Bits( 0, width = 5 ) )
	val PipeALUVal = Reg( Bits( width = 32 ) )
	val PipeMemRead = Reg( Bool() )
	val PipeMemWrite = Reg( Bool() )
	val PipeRegWrite = Reg( Bool() )
	val PipeMemtoReg = Reg( Bool() )
	val PipeMemLength = Reg( Bits( 0, width = 2 ) )
	val PipeRtData = Reg( Bits( 0, width = 32 ) )
	val PipeRt = Reg( Bits( width = 5 ) )

	val ALU = Module( new ALU() )
	val fwd = Module( new ForwardUnit() )

	//internal signals
	val opA = Bits( width = 32 )
	val opB = Bits( width = 32 )
	val result = Bits( width = 32 )
	val zero = Bool()

	// forward unit connections
	fwd.io.IFIDRs := io.IFIDRs 
	fwd.io.IFIDRt := io.IFIDRt 
	fwd.io.EXMEMRd := io.EXMEMRd 
	fwd.io.EXMEMRt := io.EXMEMRt
	fwd.io.EXMEMRegWrite := io.EXMEMRegWrite 
	fwd.io.MEMWBRegWrite := io.MEMWBRegWrite 
	fwd.io.MEMWBMemWrite := io.MEMWBMemWrite 
	fwd.io.MEMWBRd := io.MEMWBRd 
	fwd.io.IDEXRs := io.Rs 
	fwd.io.IDEXRt := io.Rt 
	io.CompA := fwd.io.CompA
	io.CompB := fwd.io.CompB
	io.ForwardMem := fwd.io.ForwardMem

	opA := Bits( 0 )
	switch ( fwd.io.ForwardA ) {
		is ( Bits( "b00" ) ) { opA := io.rsdata } 
		is ( Bits( "b01" ) ) { opA := io.MEMWBVal } 
		is ( Bits( "b10" ) ) { opA := io.EXMEMALUVal }
	}

	when ( io.ALUSrc === Bits( 0 ) ) {
		switch ( fwd.io.ForwardB ) {
			is ( Bits( "b00" ) ) { opB := io.rtdata } 
			is ( Bits( "b01" ) ) { opB := io.MEMWBVal } 
			is ( Bits( "b10" ) ) { opB := io.EXMEMALUVal }
		}
	} .otherwise {
		opB := io.imm 
	}

	ALU.io.shamt := io.shamt
	ALU.io.a := opA 
	ALU.io.b := opB
	ALU.io.op := io.ALUOp 
	result := ALU.io.out 
	zero := ALU.io.zero 

	when ( ~io.memStall ) {
		PipeRd := io.Rd 
		PipeALUVal := result 
		PipeMemRead := io.MemRead 
		PipeRegWrite := io.RegWrite 
		PipeMemWrite := io.MemWrite 
		PipeMemtoReg := io.MemtoReg 
		PipeMemLength := io.MemLength 
		PipeRtData := io.rtdata 
		PipeRt := io.Rt
	}

	io.outMemRead := PipeMemRead 
	io.outMemWrite := PipeMemWrite 
	io.outMemtoReg := PipeMemtoReg 
	io.outRegWrite := PipeRegWrite 
	io.outRd := PipeRd 
	io.outRt := PipeRt
	io.ALUVal := PipeALUVal 
	io.outMemLength := PipeMemLength
	io.outrtdata := PipeRtData 

	
}

object ExecuteMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Execute()))
	}
}
