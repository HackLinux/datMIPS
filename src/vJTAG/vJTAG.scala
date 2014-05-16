import Chisel._
import Node._

class vJTAG extends Module {
	val io = new Bundle {
		val tdi = UInt( INPUT, 1 )
		val ir_in = UInt( INPUT, 1 )
		val v_sdr = UInt( INPUT, 1 )
		val udr = UInt( INPUT, 1 )
		val CoreWData = Bits( INPUT, 8 )
		val CoreWrite = Bool( INPUT )
		val CoreReq = Bool( INPUT )
		val tdo = UInt( OUTPUT, 1 )
		val CoreRData = Bits( OUTPUT, 9 )
	}
		


	val CoreSender = Module( new CrossSender() )
	val CoreReceiver = Module( new CrossReceiver() )
	val JSender = Module( new CrossSender() )
	val JReceiver = Module( new CrossReceiver() )
	val CoreBuf = Module( new JTAGBuffer() )
	val JBuf = Module( new JTAGBuffer() )
	val JSideBuf = Module( new JTAGBuffer() )
	val interface = Module( new vJTAG_interface() )

	CoreSender.io.data_in := JBuf.io.data_out( 7,0 )
	CoreSender.io.ack := JReceiver.io.ack
	CoreSender.io.ready := JBuf.io.data_out( 8 )
	
	CoreReceiver.io.data_in := JSender.io.data_out
	CoreReceiver.io.valid_in := JSender.io.valid
	CoreReceiver.io.taken := CoreBuf.io.taken

	JSender.io.data_in := interface.io.write_data
	JSender.io.ack := CoreReceiver.io.ack
	JSender.io.ready := interface.io.write_valid.toBool()

	JReceiver.io.data_in := CoreSender.io.data_out
	JReceiver.io.valid_in := CoreSender.io.valid
	JReceiver.io.taken := JSideBuf.io.taken

	CoreBuf.io.ready := CoreReceiver.io.ready
	CoreBuf.io.req := io.CoreReq
	CoreBuf.io.data_in := CoreReceiver.io.data_out
	
	JBuf.io.ready := io.CoreWrite
	JBuf.io.req := CoreSender.io.take  
	JBuf.io.data_in := io.CoreWData 

	JSideBuf.io.ready := JReceiver.io.ready 
	JSideBuf.io.data_in := JReceiver.io.data_out 
	JSideBuf.io.req := io.udr.toBool()

	io.tdo := interface.io.tdo
	io.CoreRData := CoreBuf.io.data_out

	interface.io.tdi := io.tdi
	interface.io.ir_in := io.ir_in
	interface.io.v_sdr := io.v_sdr
	interface.io.udr := io.udr
	interface.io.read_data := JSideBuf.io.data_out( 7, 0 )
	interface.io.read_valid := JSideBuf.io.data_out( 8 )
}

object vJTAG {
	def main( args: Array[String] ): Unit = {
		chiselMain( args, () => Module( new vJTAG() ) )
	}
}
