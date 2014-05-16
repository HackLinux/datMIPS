module LED_Connector(
	input [6:0]LEDs,
	output wire LED_6,
	output wire LED_5,
	output wire LED_4,
	output wire LED_3,
	output wire LED_2,
	output wire LED_1,
	output wire LED_0

);

assign LED_6 = LEDs[6];
assign LED_5 = LEDs[5];
assign LED_4 = LEDs[4];
assign LED_3 = LEDs[3];
assign LED_2 = LEDs[2];
assign LED_1 = LEDs[1];
assign LED_0 = LEDs[0];



endmodule