import Chisel._
import Node._

class Core extends Module {
	val io = new Bundle {
		val memReady = Bool( INPUT )
		val MemoryData1 = Bits( INPUT, 32 )
		val MemoryData2 = Bits( INPUT, 32 )
		val MemoryWen = Bool( OUTPUT )
		val MemoryWData = Bits( OUTPUT, 32 )
		val MemoryWAddr = Bits( OUTPUT, 32 )
		val MemoryWLength = Bits( OUTPUT, 2 )
		val MemoryRLength = Bits( OUTPUT, 2 )
		val MemoryRAddr1 = Bits( OUTPUT, 32 )
		val MemoryRAddr2 = Bits( OUTPUT, 32 )
		val MemoryRen = Bool( OUTPUT )
		val sp = Bits( OUTPUT, 32 )

	}

	val fetch = Module( new Fetch() )
	val decode = Module( new Decode() )
	val execute = Module( new Execute() )
	val memory = Module( new Memory() )
	val writeback = Module( new Writeback() )

	fetch.io.PCWrite := ~decode.io.stall
	fetch.io.PipeWrite := ~decode.io.stall
	fetch.io.PCData := decode.io.PCData 
	fetch.io.PCSelect := decode.io.PCSelect
	fetch.io.instrin := io.MemoryData1
	fetch.io.memStall := memory.io.memStall

	decode.io.instr := fetch.io.instrout 
	decode.io.CompA := execute.io.CompA
	decode.io.CompB := execute.io.CompB
	decode.io.wr := writeback.io.outRd 
	decode.io.wdata := writeback.io.outWData 
	decode.io.we := writeback.io.outRegWrite 
	decode.io.FwdData := execute.io.ALUVal 
	decode.io.PC := fetch.io.PipePC
	decode.io.memStall := memory.io.memStall
	
	execute.io.MemLength := decode.io.outMemLength
	execute.io.imm := decode.io.imm
	execute.io.Rd := decode.io.Rd 
	execute.io.Rt := decode.io.Rt 
	execute.io.Rs := decode.io.Rs 
	execute.io.rsdata := decode.io.rsdata
	execute.io.rtdata := decode.io.rtdata
	execute.io.ALUOp := decode.io.ALUOp
	execute.io.ALUSrc := decode.io.ALUSrc 
	execute.io.shamt := decode.io.shamt 
	execute.io.MemRead := decode.io.MemRead 
	execute.io.MemWrite := decode.io.MemWrite
	execute.io.MemtoReg := decode.io.MemtoReg 
	execute.io.RegWrite := decode.io.RegWrite
	execute.io.IFIDRs := fetch.io.instrout( 25, 21 )
	execute.io.IFIDRt := fetch.io.instrout( 20, 16 )
	execute.io.EXMEMRegWrite := execute.io.outRegWrite 
	execute.io.EXMEMRd := execute.io.outRd 
	execute.io.EXMEMRt := execute.io.outRt
	execute.io.EXMEMALUVal := execute.io.ALUVal
	execute.io.MEMWBRd := memory.io.outRd 
	execute.io.MEMWBVal := writeback.io.outWData 
	execute.io.MEMWBMemWrite := memory.io.outMemWrite 
	execute.io.MEMWBRegWrite := memory.io.outRegWrite 
	execute.io.IDEXRs := decode.io.Rs 
	execute.io.IDEXRt := decode.io.Rt
	execute.io.memStall := memory.io.memStall

	memory.io.memReady := io.memReady 
	memory.io.rtdata := execute.io.outrtdata 
	memory.io.ForwardMem := execute.io.ForwardMem
	memory.io.FwdData := writeback.io.outWData 
	memory.io.Rd := execute.io.outRd 
	memory.io.ALUVal := execute.io.ALUVal
	memory.io.MemtoReg := execute.io.outMemtoReg 
	memory.io.MemWrite := execute.io.outMemWrite
	memory.io.RegWrite := execute.io.outRegWrite
	memory.io.MemRead := execute.io.outMemRead 
	memory.io.MemLength := execute.io.outMemLength
	memory.io.MemData := io.MemoryData2

	writeback.io.MemtoReg := memory.io.outMemtoReg 
	writeback.io.RegWrite := memory.io.outRegWrite
	writeback.io.MemVal := memory.io.outMemVal 
	writeback.io.ALUVal := memory.io.outALUVal
	writeback.io.Rd := memory.io.outRd 

	io.MemoryWen := memory.io.wen
	io.MemoryWData := memory.io.WData 
	io.MemoryRAddr1 := fetch.io.PC 
	io.MemoryRAddr2 := memory.io.RAddr 
	io.MemoryRLength := memory.io.RLength 
	io.MemoryWLength := memory.io.WLength 
	io.MemoryWAddr := memory.io.WAddr 
	io.sp := decode.io.sp
	io.MemoryRen := memory.io.ren

}


object CoreMain {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new Core()))
	}
}

