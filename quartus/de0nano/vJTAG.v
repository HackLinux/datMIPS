module vJTAG(input clk_50, 
		 output LED_6,
		 output LED_5,
		 output LED_4,
		 output LED_3,
		 output LED_2,
		 output LED_1,
		 output LED_0,
		 output LED_clk);
		 
wire tck;
wire tdi;
wire ir_in;
wire sdr;
wire udr;
wire tdo;
wire [7:0] LEDs;
wire ir_out;
wire reset;
wire read_valid;
wire [7:0] read_data;


assign reset = 1'b0;
assign read_valid = 1'b1;
assign read_data = 8'b11111111;


vJTAG_altera vJTAG_altera1(.tdo( tdo ),
				 .ir_in( ir_in ),
				 .tck( tck ),
				 .tdi( tdi ),
				 .virtual_state_sdr( sdr ),
				 .virtual_state_udr( udr ));
vJTAG_interface vJTAG_interface1(
				 .clk( tck ),
				 .reset( reset ),
				 .io_tdi( tdi ),
				 .io_ir_in( ir_in ),
				 .io_v_sdr( sdr ),
				 .io_tdo( tdo ),
				 .io_udr( udr ),
				 .io_write_data( LEDs ),
				 .io_read_valid( read_valid ),
				 .io_read_data( read_data ) );
				 
clk_blinker clk_blinker1( 
				 .clk( clk_50 ),
				 .blink( LED_clk ));
				 
LED_Connector LED_connector1( 
				 .LEDs( LEDs[6:0] ),
				 .LED_6( LED_6 ),
				 .LED_5( LED_5 ),
				 .LED_4( LED_4 ),
				 .LED_3( LED_3 ),
				 .LED_2( LED_2 ),
				 .LED_1( LED_1 ),
				 .LED_0( LED_0 ));
endmodule

