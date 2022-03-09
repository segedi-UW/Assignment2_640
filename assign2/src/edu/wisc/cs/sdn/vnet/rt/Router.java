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

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
				etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		if(etherPacket.getEtherType() != Ethernet.TYPE_IPv4){
			System.out.println("Packet Type wasnt IPv4");
			return;
		}
		IPv4 packet = (IPv4) etherPacket.getPayload();
		
		short orig = packet.getChecksum();
		packet.setChecksum((short) 0);
		// byte headerLength = packet.getHeaderLength();
		// byte[] data = new byte[packet.getTotalLength()];
		// ByteBuffer bb = ByteBuffer.wrap(data);
		packet.serialize();
		
		short sum = packet.getChecksum();

		// compute checksum if needed
		// bb.rewind();
		// int accumulation = 0;
		// for (int i = 0; i < headerLength * 2; ++i) {
		// 	accumulation += 0xffff & bb.getShort();
		// }
		// accumulation = ((accumulation >> 16) & 0xffff)
		// 		+ (accumulation & 0xffff);
		// sum = (short) (~accumulation & 0xffff);
		// bb.putShort(10, sum);

		if(orig != sum) {

			System.out.println("CheckSum didn't match: " +orig+" : "+sum);
			return;
		}

		packet.setTtl((byte) (packet.getTtl()-1));

		if (packet.getTtl() <= (byte) 0) {
			System.out.println("TTL was 0");
			return;
		}

		packet.setChecksum(orig);

		for(Iface iface : interfaces.values()) {
			if(iface.getIpAddress() == packet.getDestinationAddress()){
				System.out.println("Exact match in first Interface");
				return;
			}
		}
		System.out.println("Packet Dest: "+ IPv4.fromIPv4Address(packet.getDestinationAddress()));
		RouteEntry entry = routeTable.lookup(packet.getDestinationAddress());

		if (entry == null) {
			System.out.println("Route Entry was Null");
			return;
		}
		int ip = entry.getGatewayAddress() == 0 ? entry.getDestinationAddress() : entry.getGatewayAddress();
		System.out.println(IPv4.fromIPv4Address(ip));
		System.out.println(arpCache);

		MACAddress addr = arpCache.lookup(ip).getMac();
		etherPacket.setDestinationMACAddress(addr.toBytes());
		etherPacket.setSourceMACAddress(entry.getInterface().getMacAddress().toBytes());

		this.sendPacket(etherPacket, entry.getInterface());
		System.out.println("Packet was sent to: " + entry.getInterface().getName());
		
		/********************************************************************/
	}
}
