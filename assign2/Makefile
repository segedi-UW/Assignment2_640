# 1 start pox 
run_pox: run_pox.sh
	./run_pox.sh
# 2 connect mininet w/ pox
mini_emu: run_mininet.py topos/single_sw.topo
	sudo ./run_mininet.py topos/single_sw.topo -a
# 3 start virtualNetwork.jar
run_virt_jar: build_virt_jar VirtualNetwork.jar
	java -jar VirtualNetwork.jar -v s1
build_virt_jar: VirtualNetwork.jar
	ant
