#
# Building a Hello World Chisel without too much sbt/scala/... stuff
#
# sbt looks for default into a folder ./project and . for build.sdt and Build.scala
# sbt creates per default a ./target folder

SBT = java -Xmx1024M -Xss8M -XX:MaxPermSize=128M -jar sbt/sbt-launch.jar

# This generates the Verilog and C++ files by invoking main from
# class HelloMain in package hello.
# The source directory is configured in build.sbt.
# The Scala/Java build directory is default ./target.

# The first two arguments are consumed by sbt, the rest is
# forwarded to the Scala/Chisel main().



# Generate Verilog code
hdl:
	$(SBT) "run-main hello.HelloMain --targetDir generated --backend v"

# C++ genertion
cpp:
	$(SBT) "run-main hello.HelloMain --backend c --compile --targetDir generated"

#vjag
vJTAG-interface:
	$(SBT) "run-main vJTAG_interfaceMain --targetDir generated --backend v"

#sync units
sync:
	$(SBT) "run-main CrossReceiverMain --targetDir generated --backend v"
	$(SBT) "run-main CrossSenderMain --targetDir generated --backend v"

sync-test:
	$(SBT) "run-main CrossSenderTester --backend c --compile --genHarness --test --vcd --targetDir generated"

#ALU
ALU:
	$(SBT) "run-main ALUMain --targetDir generated --backend v"

#register file
reg:
	$(SBT) "run-main RegisterFileMain --targetDir generated --backend v"

control:
	$(SBT) "run-main ControllerMain --targetDir generated --backend v"

forwarding:
	$(SBT) "run-main ForwardUnitMain --targetDir generated --backend v"

hazard:
	$(SBT) "run-main HazardUnitMain --targetDir generated --backend v"

MainMem:
	$(SBT) "run-main MemMain --targetDir generated --backend v"

MainMem-test:
	$(SBT) "run-main MainMemTester --backend c --compile --genHarness --test --targetDir generated --vcd"

fetch:
	$(SBT) "run-main FetchMain --targetDir generated --backend v"

fetch-test:
	$(SBT) "run-main FetchTester --backend c --compile --genHarness --test --targetDir generated --vcd"

decode:
	$(SBT) "run-main DecodeMain --targetDir generated --backend v"

execute:
	$(SBT) "run-main ExecuteMain --targetDir generated --backend v"

memory:
	$(SBT) "run-main MemoryMain --targetDir generated --backend v"

writeback:
	$(SBT) "run-main WritebackMain --targetDir generated --backend v"

core:
	$(SBT) "run-main CoreMain --targetDir generated --backend v"

connections:
	$(SBT) "run-main ConnectionsMain --targetDir generated --backend v"

connections-test:
	$(SBT) "run-main ConnectionsTester --backend c --compile --genHarness --test --targetDir generated --vcd"

test:
	$(SBT) "run-main testMain --targetDir generated --backend v"

JTAGBuffer:
	$(SBT) "run-main JTAGBufferMain --targetDir generated --backend v"

vJTAG:
	$(SBT) "run-main vJTAG --targetDir generated --backend v"

stackcache:
	$(SBT) "run-main StackCacheMain --targetDir generated --backend v"

memhierarchy:
	$(SBT) "run-main MemHierarchyMain --targetDir generated --backend v"
