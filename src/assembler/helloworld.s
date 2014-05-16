# include <iregdef.h>
.set noreorder
.text
.globl start
.ent start
start:	li t1, 0x48
		sw t1, 1024($0)
		li t2, 0x45
		sw t2, 1024($0)
		li t3, 0x4c
		sw t3, 1024($0)
		li t4, 0x4c
		sw t4, 1024($0)
		li t5, 0x4f
		sw t5, 1024($0)
		li t6, 0x20
		sw t6, 1024($0)
		li t7, 0x57
		sw t7, 1024($0)
		li t8, 0x4f
		sw t8, 1024($0)
		li t1, 0x52
		sw t1, 1024($0)
		li t2, 0x4c
		sw t2, 1024($0)
		li t3, 0x44
		sw t3, 1024($0)
		li t4, 0x21
		sw t4, 1024($0)
frevr:  nop
		beq zero, zero, frevr
		nop
.end start
