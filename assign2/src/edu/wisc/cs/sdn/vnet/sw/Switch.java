package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	

	private MACTracker tracker;
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		tracker = new MACTracker();
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
		MACAddress src = etherPacket.getSourceMAC();
		MACAddress dst = etherPacket.getDestinationMAC();
		
		/********************************************************************/
	}

	/** @author AJ */
	private class MACTracker
	{
		private Timer timer;
		private ConcurrentHashMap<MACAddress, LiveLink> liveAddresses;

		public MACTracker()
		{
			timer = new Timer(true);
		}

		public Iface getCacheIface(MACAddress macAddr)
		{
			LiveLink link = liveAddresses.get(macAddr);
			if (link == null) return null;

			// reset killTask
			link.task.cancel();
			link.task = killEntryTask(macAddr);
			registerTask(link.task);

			return link.iface;
		}

		public void put(MACAddress macAddr, Iface iface)
		{
			TimerTask task = killEntryTask(macAddr);
			LiveLink link = new LiveLink(task, iface);
			liveAddresses.put(macAddr, link);
			registerTask(task);
		}

		private void registerTask(TimerTask task)
		{
			final int timeout = 15*1000;
			timer.schedule(task, timeout);
		}

		private TimerTask killEntryTask(MACAddress macAddr)
		{
			return new TimerTask()
			{
				public void run()
				{
					liveAddresses.remove(macAddr);
				}
			};
		}

		private class LiveLink
		{
			public TimerTask task;
			public final Iface iface;

			public LiveLink(TimerTask task, Iface iface)
			{
				this.task = task;
				this.iface = iface;
			}
		}

	}
}
