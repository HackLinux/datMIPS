import socket
import struct

host = 'localhost' 
port = 2540
size = 1024 

def Open(host, port):
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect(( host,port))
	return s

def Write(conn,intValue, valid):
    if intValue:
        intValue = struct.unpack("B", intValue)[0] 
    else:
        intValue = 0
    size = 8
    bStr_LEDValue = bin( valid ).split( '0b' )[ 1 ] + bin(intValue).split('0b')[1].zfill(size) #Convert from int to binary string
    print( bStr_LEDValue + '\n' )
    conn.send(bytes(bStr_LEDValue + '\n', "ascii")) #Newline is required to flush the buffer on the Tcl server


conn = Open(host, port)
with open( "../../generated/fib.o", "rb" ) as f:
    byte = f.read(1)
    while byte:
        Write( conn, byte, 1 ) 
        byte = f.read( 1 )

Write( conn,  bytes([0xff] ), 1 )
Write( conn,  bytes([0xff] ), 1 )
Write( conn,  bytes([0xff] ), 1 )
Write( conn,  bytes([0xff] ), 1 )

for i in range( 50 ):
    Write( conn, bytes( [0x00] ), 0 )

conn.close()



