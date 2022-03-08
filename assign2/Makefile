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
mini_emu_multi: run_mininet.py topos/linear5_sw.topo
	sudo ./run_mininet.py topos/linear5_sw.topo
run_virt_jar_multi: build_virt_jar VirtualNetwork.jar
	java -jar VirtualNetwork.jar -v s1 &
	java -jar VirtualNetwork.jar -v s2 &
	java -jar VirtualNetwork.jar -v s3 &
	java -jar VirtualNetwork.jar -v s4 &
	java -jar VirtualNetwork.jar -v s5 &
run_virt_jar_route: build_virt_jar VirtualNetwork.jar
	java -jar VirtualNetwork.jar -v r1 -r rtable.r1 -a arp_cache
mini_emu_route: run_mininet.py topos/single_rt.topo
	sudo ./run_mininet.py topos/single_rt.topo
