package edu.wisc.cs.sdn.vnet.rt;

import java.nio.ByteBuffer;
import java.util.Map;
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
	// public void handlePacket(Ethernet etherPacket, Iface inIface)
	// {
	// 	System.out.println("*** -> Received packet: " +
	// 			etherPacket.toString().replace("\n", "\n\t"));
		
	// 	/********************************************************************/
	// 	/* TODO: Handle packets                                             */
	// 	if(etherPacket.getEtherType() != Ethernet.TYPE_IPv4){
	// 		System.out.println("Packet Type wasnt IPv4");
	// 		return;
	// 	}
	// 	IPv4 packet = (IPv4) etherPacket.getPayload();
		
	// 	short orig = packet.getChecksum();
	// 	packet.setChecksum((short) 0);
	// 	// byte headerLength = packet.getHeaderLength();
	// 	// byte[] data = new byte[packet.getTotalLength()];
	// 	// ByteBuffer bb = ByteBuffer.wrap(data);
	// 	packet.serialize();
		
	// 	short sum = packet.getChecksum();

	// 	// compute checksum if needed
	// 	// bb.rewind();
	// 	// int accumulation = 0;
	// 	// for (int i = 0; i < headerLength * 2; ++i) {
	// 	// 	accumulation += 0xffff & bb.getShort();
	// 	// }
	// 	// accumulation = ((accumulation >> 16) & 0xffff)
	// 	// 		+ (accumulation & 0xffff);
	// 	// sum = (short) (~accumulation & 0xffff);
	// 	// bb.putShort(10, sum);

	// 	if(orig != sum) {

	// 		System.out.println("CheckSum didn't match: " +orig+" : "+sum);
	// 		return;
	// 	}

	// 	packet.setTtl((byte) (packet.getTtl()-1));

	// 	if (packet.getTtl() <= (byte) 0) {
	// 		System.out.println("TTL was 0");
	// 		return;
	// 	}

	// 	packet.setChecksum(orig);

	// 	for(Iface iface : interfaces.values()) {
	// 		if(iface.getIpAddress() == packet.getDestinationAddress()){
	// 			System.out.println("Exact match in first Interface");
	// 			return;
	// 		}
	// 	}

	// 	RouteEntry entry = routeTable.lookup(packet.getDestinationAddress());

	// 	if (entry == null) {
	// 		System.out.println("Route Entry was Null");
	// 		return;
	// 	}
	// 	// System.out.printf("Dest: %d", args);
	// 	MACAddress addr = arpCache.lookup(entry.getDestinationAddress()).getMac();
	// 	etherPacket.setDestinationMACAddress(addr.toBytes());
	// 	etherPacket.setSourceMACAddress(entry.getInterface().getMacAddress().toBytes());

	// 	this.sendPacket(etherPacket, entry.getInterface());
	// 	System.out.println("Packet was sent to: " + entry.getInterface().getName());
		
	// 	/********************************************************************/
	// }
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		

		// Ethertype IPv4 check NEED TO FIND ETHERTYPES
		if(etherPacket.getEtherType() != Ethernet.TYPE_IPv4) {
		    System.out.println("Dropping Packet Due to EtherType\n");
		    return;
		}
		else {
		    net.floodlightcontroller.packet.IPv4 payload = (net.floodlightcontroller.packet.IPv4) etherPacket.getPayload();

		    // Serialize copypasta NEED TO TRIM
		    
		    byte[] buf = payload.serialize();

		    int length = buf.length;
		    int i = 0;
		    
		    long sum = 0;
		    long data;

		    while(length > 1) {
			data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
			sum += data;
			if((sum & 0xFFFF0000) > 0) {
			    sum = sum & 0xFFFF;
			    sum += 1;
			}

			i += 2;
			length -= 2;
		    }

		    if(length > 0) {
			sum += (buf[i] << 8 & 0xFF00);
			if((sum & 0xFFFF0000) > 0) {
			    sum = sum & 0xFFFF;
			    sum += 1;
			}
		    }

		    sum = ~sum;
		    sum = sum & 0xFFFF;

		    /*
		      byte[] payloadData = null; */
		    /*
		    if(payload != null) {
			payload.setParent(this);
			payloadData = payload.serialize();
			}*/
		    
		    /*
		    int optionsLength = 0;
		    if(payload.getOptions() != null)
			optionsLength = payload.getOptions().length / 4;
		    payload.getHeaderLength() = (byte) (5 + optionsLength);
		    payload.getTotalLength = (short) (payload.getHeaderLength() * 4 + ((payloadData == null) ? 0
									 : payloadData.length));
		    
		    byte[] data = new byte[payload.getHeaderLength()];
		    ByteBuffer bb = ByteBuffer.wrap(data);
		    
		    bb.put((byte) (((payload.getVersion() & 0xf) << 4) | (payload.getHeaderLength() & 0xf)));
		    bb.put(payload.getDiffServ());
		    bb.putShort(payload.getTotalLength());
		    bb.putInt(payload.getSourceAddress());
		    bb.putInt(payload.getDestinationAddress());
		    if(payload.getOptions() != null)
			bb.put(payload.getOptions());
		    if(payloadData != null)
			bb.put(payloadData);
		    
		    bb.rewind();
		    */
		    /*
		    long accumulation = 0;
		    for(int i = 0; i < 9; ++i) {
			accumulation += ~bb.getShort();
		    }
		    accumulation = accumulation;
		    */
		    if(payload.getChecksum() == 0) {
			System.out.println("CHECKSUM IS 0!!!");
		    }
		    System.out.println("Payload Checksum:\t" + payload.getChecksum());
		    System.out.println("Calculated Checksum:\t" + sum);
		    
		    if(payload.getChecksum() == sum) {
			System.out.println("Dropping Packet Due to Checksum\n");
			return;
		    }

		    // End copypasta

		    // Decrement TTL
		    if((payload.getTtl() - 1) == 0) {
			System.out.println("Dropping Packet Due to TTL\n");
			return;
		    }
		    else
			payload.setTtl((byte)((int)payload.getTtl() - 1));

		    // Check interfaces
		    for(Map.Entry<String, Iface> entry : getInterfaces().entrySet()) {
			if(entry.getValue().getIpAddress() == payload.getDestinationAddress()) {
			    System.out.println("Dropping Packet Due to Interface Address Conflict\n");
			    return;
			}
		    }

		    // FORWARD PACKET

		    // Make sure there is a matching RouteEntry
		    RouteEntry routeLook = routeTable.lookup(payload.getDestinationAddress());
		    if(routeLook == null) {
			System.out.println("Dropping Packet Due to the RouteEntry being null!\n");
			return;
		    }

		    // ArpCache lookup
		    ArpEntry arpLook = arpCache.lookup(routeLook.getDestinationAddress());
			if(arpLook == null){
				System.out.println("ARP LOOK is null");
				return;
			}

		    // Set new packet MACs MACAddress
		    etherPacket.setDestinationMACAddress(arpLook.getMac().toBytes());
		    etherPacket.setSourceMACAddress(routeLook.getInterface().getMacAddress().toBytes());

		    // Send packet
		    sendPacket(etherPacket, routeLook.getInterface());
		}
		
		/********************************************************************/
	}
}
