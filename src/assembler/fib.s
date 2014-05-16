# include <iregdef.h>
.set noreorder
.text
.globl start
.ent start
start:	li a0, 0x09			#load the fib seed
		li sp, 2044			#initialise stack pointer
		li fp, 2044			#initialise frame pointer
		jal fib				#compute fib(n)
		nop
		sw v0, 1024($0)		#print result to JTAG

loop:	beq zero, zero, loop
		nop
		
fib:	bne a0, 0, not0		#check for n=0
		nop 
		li v0, 0x00			#fib(0)=0
		jr ra				#return
		nop

not0:	bne a0, 1, not1		#check for n=1
		nop
		li v0, 0x01			#fib(1) = 1
		jr ra				#return
		nop

not1:	add t0, a0, -1		#n-1
		add t1, a0, -2		#n-2
		addi sp, sp, -4		#decrement stack pointer
		nop
		sw t1, 0(sp)		#save n-2 to stack
		addi sp, sp, -4		#dec stack pointer
		sw ra, 0(sp)		#save return address to stack
		add a0, zero, t0	#pass n-1 as argument
		jal fib				#call fib(n-1)
		nop
		addi sp, sp, -4		#dec stack pointer
		sw v0, 0(sp)		#save fib(n-1) to stack
		lw a0, 8(sp)		#load n-2 as argument
		jal fib				#call fib(n-2)
		nop
		lw t1, 0(sp)		#load fib(n-1)
		nop
		add v0, v0, t1		#compute fib(n-1)+fib(n-2)
		lw ra, 4(sp)		#load return address
		addi sp, sp, 12		#return stack pointe to before call
		nop
		jr ra
		nop
.end start
