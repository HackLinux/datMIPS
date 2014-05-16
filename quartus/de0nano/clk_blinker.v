module clk_blinker (
	input clk,
	output blink
);

reg [25:0] count;

always @(posedge clk)
	count <= count + 1'b1;
	
assign blink = count[25]; 


endmodule
