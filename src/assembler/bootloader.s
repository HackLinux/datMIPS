# include <iregdef.h>
.set noreorder
.text
.globl start
.ent start
start:	li t9, 0x88				#address to place new instruction
		li t8, 0x88				#start address for loaded programs
		lui t0, 0xFFFF
		li s1, 0xFFFF
		or s1, t0, s1		#load magic stop word

ainstr:	jal LJTG				#load from JTAG
		li s0, 0				#initilize instruction to 0
		or s0, t0, 0		#move first byte to instr reg
		jal LJTG				#load next byte of instruction
		nop
		sll s0, s0, 8			#align instruction 
		or s0, s0, t0		#add to instruction word
		jal LJTG				#load next byte of instruction
		nop
		sll s0, s0, 8 		#align instruction 
		or s0, s0, t0		#add to instruction word
		jal LJTG				#load next byte of instruction
		nop
		sll s0, s0, 8		#align instruction 
		or s0, s0, t0		#add to instruction word
		beq s0, s1, GTOUT		#if end of programming jump to start of loaded program
		nop 
		sw s0, 0(t9)			#store loaded instruction
		j ainstr				#load a new instruction
		addi t9, t9, 4		#increase placement counter in delay slot

GTOUT:	jr t8					#jump to beginning of loaded program
		nop						#remember to fill branch delay slot

LJTG:	lw t0, 1024($0)		#load from JTAG
		andi t1, t0, 0x0100	#check data is valid
		beq t1, $0, LJTG		#if not valid load again
		nop						#dont utilize branch delay slot
		andi t0, t0, 0xFF		#extract data 
		jr ra					#return
		nop
.end start
