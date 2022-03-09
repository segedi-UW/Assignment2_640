package edu.wisc.cs.sdn.vnet.rt;

import java.nio.ByteBuffer;
import java.util.jar.Attributes.Name;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.MACAddress;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
	}

    private void separate() {
        System.out.println("\n_\n");
    }

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		//System.out.println("Read From: " + IPv4.fromIPv4Address(inIface.getIpAddress()));
		// TODO remove
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4)
			return;

		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		if(etherPacket.getEtherType() != Ethernet.TYPE_IPv4){
			System.out.println("Packet Type wasn't IPv4: " + etherPacket.getEtherType());
            separate();
			return;
		}


		IPv4 packet = (IPv4) etherPacket.getPayload();
		
        // checksum
		short orig = packet.getChecksum();
		packet.setChecksum((short) 0);
		if (packet.getChecksum() != 0) {
			System.out.println("ERROR: checksum was not set correctly");
			return;
		}

		packet.serialize(); // recomputes checksum for us when 0
		if (packet.getChecksum() == 0) {
			System.out.println("ERROR: checksum was not set correctly by serialize");
			return;
		}
		short sum = packet.getChecksum();

		if(orig != sum) {
			System.out.println("CheckSum didn't match: " +orig+" : "+sum);
            separate();
			return;
		}

        // ttl check
		byte ttl = packet.getTtl();
		packet.setTtl((byte) (packet.getTtl()-1));
		if (packet.getTtl() == ttl) {
			System.out.println("ERROR: ttl was not set correctly");
		}

		if (packet.getTtl() <= (byte) 0) {
			System.out.println("TTL was 0");
            separate();
			return;
		}

        // immediate interface check
		for(Iface iface : interfaces.values()) {
			if(iface.getIpAddress() == packet.getDestinationAddress()){
				System.out.println("Exact match in first Interface");
                separate();
				return;
			}
		}
		//System.out.println("Packet Dest: "+ IPv4.fromIPv4Address(packet.getDestinationAddress()));
		// NEED TO RECOMPUTE THE CHECKSUM AFTER CHANGING TTL
		packet.resetChecksum();
		packet.serialize();
		if (packet.getChecksum() == 0) {
			System.out.println("ERROR: checksum not set properly after reset");
		}

		Ethernet eth = (Ethernet)etherPacket.setPayload(packet);
		RouteEntry entry = routeTable.lookup(packet.getDestinationAddress());

		if (entry == null) {
			System.out.println("Route Entry was Null");
            separate();
			return;
		}
        int gateAddr = entry.getGatewayAddress();
		int ip = gateAddr == 0 ? packet.getDestinationAddress() : gateAddr;
		System.out.println("Ip to send to: " + IPv4.fromIPv4Address(ip));

		ArpEntry arpEntry = arpCache.lookup(ip);
		if (arpEntry == null) return;
			
		MACAddress addr = arpEntry.getMac();
		eth = eth.setDestinationMACAddress(addr.toBytes());
		eth = eth.setSourceMACAddress(entry.getInterface().getMacAddress().toBytes());

		// checks for standard packet
		if (!sendPacket(eth, entry.getInterface()))
			System.err.println("Failed to send packet. Check headers");

		System.out.println("Packet was sent to: " + entry.getInterface().getName());
		separate();

		/********************************************************************/
	}
}
