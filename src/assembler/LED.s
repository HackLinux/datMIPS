# include <iregdef.h>
.set noreorder
.text
.globl start
.ent start
start:	li t0, 0xFF		#light all LEDs
		li t1, 0x0		#counter variable
		lui t2, 0x005f	#delay cycles
		ori t2, t2, 0x5e10
		sw t0, 1023($0) #light the leds
loop1:	
		addi t1, t1, 0x1 #increment loop count
		bne t1, t2, loop1 #delay
		nop
		li t1, 0x0  
		li t0, 0x0 
		sw t0, 1023($0)
loop2:	addi t1, t1, 0x1 
		bne t1, t2, loop2
		nop
		beq zero, zero, start  
		nop
.end start
