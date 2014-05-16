import struct
def Write( dest,intValue, valid):
    if intValue:
        intValue = struct.unpack("B", intValue)[0] 
    else:
        intValue = 0
    size = 8
    bStr_LEDValue =  bin(intValue).split('0b')[1].zfill(size)[::-1] + bin( valid ).split( '0b' )[ 1 ] #Convert from int to binary string
    print( bStr_LEDValue )
    for i in range( 0, 9 ):
        dest.write( "force io_tdi " + bStr_LEDValue[i] + '\n' )
        dest.write( "run 10 \n" )
    dest.write( "force io_udr 1 \n" )
    dest.write( "run 10 \n" )
    dest.write( "force io_udr 0 \n" )
    dest.write( "run 10 \n" )

with open( "../../generated/fib.o", "rb" ) as f:
    gen = open( "gentest.do", "w+" )
    byte = f.read(1)
    while byte:
        Write( gen, byte, 1 ) 
        byte = f.read( 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
    Write( gen, bytes( [0xFF] ), 1 )
