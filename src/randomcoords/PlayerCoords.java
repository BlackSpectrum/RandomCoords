package randomcoords;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerCoords implements Listener{
	
	private static ConcurrentHashMap<UUID, int[]> playerCoords = new ConcurrentHashMap<UUID, int[]>();
	private final static Random rand = new Random();
	
	private static final int MINIMUM_OFFSET = -7500;
	
	public static void addNewPlayer(Player player)
	{
		int offsetX = (MINIMUM_OFFSET + rand.nextInt(-2 * MINIMUM_OFFSET));
		int offsetZ = (MINIMUM_OFFSET + rand.nextInt(-2 * MINIMUM_OFFSET));
		
		playerCoords.put(player.getUniqueId(), new int[] {	roundToClosest16(player.getLocation().getBlockX() + offsetX), 
															roundToClosest16(player.getLocation().getBlockZ() + offsetZ) });
	}
	
	public static int getX(Player player)
	{
		if (!playerCoords.containsKey(player.getUniqueId()))
			addNewPlayer(player);
		
		return playerCoords.get(player.getUniqueId())[0];
	}
	
	public static int getZ(Player player)
	{
		if (!playerCoords.containsKey(player.getUniqueId()))
			addNewPlayer(player);
		
		return playerCoords.get(player.getUniqueId())[1];
	}
	
	public static int getChunkX(Player player)
	{
		if (!playerCoords.containsKey(player.getUniqueId()))
			addNewPlayer(player);
		
		return  playerCoords.get(player.getUniqueId())[0]  / 16;
	}
	
	public static int getChunkZ(Player player)
	{
		if (!playerCoords.containsKey(player.getUniqueId()))
			addNewPlayer(player);
		
		return  playerCoords.get(player.getUniqueId())[1]  / 16;
	}
	
	public static void clean(Player player)
	{
		playerCoords.remove(player.getUniqueId());
	}
	

	
	private static int roundToClosest16(double d)
	{
		return (int)(d / 16) * 16;
	}

}
