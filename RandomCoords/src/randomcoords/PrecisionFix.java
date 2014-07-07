package randomcoords;

import com.comphenix.protocol.events.PacketEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PrecisionFix implements Listener
{
	private static final ConcurrentHashMap<UUID, double[]> playerPositions = new ConcurrentHashMap<UUID, double[]>();
	
	public static void onServerChangePos(PacketEvent event)
	{
		double curr_x = ((Double)event.getPacket().getDoubles().read(0));
		double curr_z = ((Double)event.getPacket().getDoubles().read(2));
		playerPositions.put(event.getPlayer().getUniqueId(), new double[]{curr_x, curr_z});
	}
	
	public static void onClientChangePos(PacketEvent event)
	{
		if (!playerPositions.containsKey(event.getPlayer().getUniqueId()))
			return;
			
		double curr_x = ((Double)event.getPacket().getDoubles().read(0));
		double curr_z = ((Double)event.getPacket().getDoubles().read(2));
		double dx = playerPositions.get(event.getPlayer().getUniqueId())[0] - curr_x;
		double dz = playerPositions.get(event.getPlayer().getUniqueId())[1] - curr_z;
		
	    if (Math.abs(dx) + Math.abs(dz) > 0.00390625D)
	      return;

		event.getPacket().getDoubles().write(0, playerPositions.get(event.getPlayer().getUniqueId())[0]);
		event.getPacket().getDoubles().write(2, playerPositions.get(event.getPlayer().getUniqueId())[1]);
		playerPositions.remove(event.getPlayer().getUniqueId());
	}
	
	public static void clean(Player player)
	{
		playerPositions.remove(player.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		clean(event.getPlayer());
	}
}
