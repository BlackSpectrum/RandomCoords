package randomcoords;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

import java.util.List;

import org.bukkit.entity.Player;

public class Translate
{	
	public static void outgoing(PacketEvent event)
	{
		switch (event.getPacketType().name())
		{
			case "SPAWN_POSITION": 
				sendInt(event, 0);
				break;
			case "RESPAWN": 
				PlayerCoords.addNewPlayer(event.getPlayer());
				break;
			case "POSITION": 
				PrecisionFix.onServerChangePos(event);
				sendDouble(event, 0);
				break;
			case "BED": 
				sendInt(event, 1);
				break;
			case "NAMED_ENTITY_SPAWN": 
			case "SPAWN_ENTITY": 
				sendFixedPointNumber(event, 1);
				break;
			case "SPAWN_ENTITY_LIVING": 
				sendFixedPointNumber(event, 2);
				break;
			case "SPAWN_ENTITY_PAINTING": 
				sendInt(event, 1);
				break;
			case "SPAWN_ENTITY_EXPERIENCE_ORB": 
			case "ENTITY_TELEPORT": 
				sendFixedPointNumber(event, 1);
				break;
			case "MAP_CHUNK": 
				sendChunk(event);
				break;
			case "MULTI_BLOCK_CHANGE": 
				sendChunkUpdate(event);
				break;
			case "BLOCK_CHANGE": 
			case "BLOCK_ACTION": 
				sendInt(event, 0);
				break;
			case "BLOCK_BREAK_ANIMATION": 
				sendInt(event, 1);
				break;
			case "MAP_CHUNK_BULK": 
				sendChunkBulk(event);
				break;
			case "EXPLOSION": 
				sendExplosion(event);
				break;
			case "WORLD_EVENT": 
				sendInt(event, 2);
				break;
			case "NAMED_SOUND_EFFECT": 
				sendInt8(event, 0);
				break;
			case "WORLD_PARTICLES": 
				sendFloat(event, 1);
				break;
			case "SPAWN_ENTITY_WEATHER": 
				sendFixedPointNumber(event, 1);
				break;
			case "UPDATE_SIGN": 
				sendInt(event, 0);
				break;
			case "TILE_ENTITY_DATA": 
				sendTileEntityData(event);
				break;
			case "OPEN_SIGN_ENTITY": 
				sendInt(event, 0);	
		}
	}
	
	public static void incoming(PacketEvent event)
	{
		switch (event.getPacketType().name())
		{
			case "POSITION": 
				recvDouble(event, 0);
				break;
			case "POSITION_LOOK": 
				if (!isSpecialMove(event))
				{
					recvDouble(event, 0);
					PrecisionFix.onClientChangePos(event);
				}
				break;
			case "BLOCK_DIG": 
				recvInt(event, 0);
				break;
			case "BLOCK_PLACE": 
				if (!isSpecialPlace(event))
					recvInt(event, 0);
				break;
			case "UPDATE_SIGN": 
				recvInt(event, 0);
		}
	}
	
	private static void sendInt(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		int curr_x = event.getPacket().getIntegers().read(index + 0);
		int curr_z = event.getPacket().getIntegers().read(index + 2);
		
		event.getPacket().getIntegers().write(index + 0, curr_x - PlayerCoords.getX(p) );
		event.getPacket().getIntegers().write(index + 2, curr_z - PlayerCoords.getZ(p) );
	}
	
	private static void sendDouble(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		double curr_x = event.getPacket().getDoubles().read(index + 0).doubleValue();
		double curr_z = event.getPacket().getDoubles().read(index + 2).doubleValue();
		
		event.getPacket().getDoubles().write(index + 0, curr_x - PlayerCoords.getX(p) );
		event.getPacket().getDoubles().write(index + 2, curr_z - PlayerCoords.getZ(p) );
	}
	
	private static void sendFloat(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		float curr_x = event.getPacket().getFloat().read(index + 0).floatValue();
		float curr_z = event.getPacket().getFloat().read(index + 2).floatValue();
		
		event.getPacket().getFloat().write(index + 0, curr_x - (float)PlayerCoords.getX(p) );
		event.getPacket().getFloat().write(index + 2, curr_z - (float)PlayerCoords.getZ(p) );
	}
	
	private static void sendChunk(PacketEvent event)
	{
		Player p = event.getPlayer();
		
		int curr_x = event.getPacket().getIntegers().read(0);
		int curr_z = event.getPacket().getIntegers().read(1);
		
		event.getPacket().getIntegers().write(0, curr_x - PlayerCoords.getChunkX(p));
		event.getPacket().getIntegers().write(1, curr_z - PlayerCoords.getChunkZ(p));
	}
	
	private static void sendChunkUpdate(PacketEvent event)
	{
		Player p = event.getPlayer();
		
		
		ChunkCoordIntPair newCoords = new ChunkCoordIntPair(
				event.getPacket().getChunkCoordIntPairs().read(0).getChunkX() - PlayerCoords.getChunkX(p),
				event.getPacket().getChunkCoordIntPairs().read(0).getChunkZ() - PlayerCoords.getChunkZ(p)
				);
		
		
		event.getPacket().getChunkCoordIntPairs().write(0, newCoords);
	}
	
	private static void sendChunkBulk(PacketEvent event)
	{
		Player p = event.getPlayer();
		
		int[] x = event.getPacket().getIntegerArrays().read(0).clone();
		int[] z = event.getPacket().getIntegerArrays().read(1).clone();
		
		for(int i = 0; i < x.length; i++)
		{
			
			x[i] = x[i] - PlayerCoords.getChunkX(p);
			z[i] = z[i] - PlayerCoords.getChunkZ(p);
		}
		
		
		event.getPacket().getIntegerArrays().write(0, x);
		event.getPacket().getIntegerArrays().write(1, z);
	}
	
	private static void sendInt8(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		int curr_x = event.getPacket().getIntegers().read(index + 0);
		int curr_z = event.getPacket().getIntegers().read(index + 2);
		
		event.getPacket().getIntegers().write(index + 0, curr_x - 8 * PlayerCoords.getX(p) );
		event.getPacket().getIntegers().write(index + 2, curr_z - 8 * PlayerCoords.getZ(p) );
	}
	
	private static void sendFixedPointNumber(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		int curr_x = event.getPacket().getIntegers().read(index + 0);
		int curr_z = event.getPacket().getIntegers().read(index + 2);
		
		event.getPacket().getIntegers().write(index + 0, curr_x - 32 * PlayerCoords.getX(p) );
		event.getPacket().getIntegers().write(index + 2, curr_z - 32 * PlayerCoords.getZ(p) );
	}
	
	private static void sendExplosion(PacketEvent event)
	{
		sendDouble(event, 0);
		
		List<ChunkPosition> lst = event.getPacket().getPositionCollectionModifier().read(0);
		
		for (int i = 0; i < lst.size(); i++)
		{
			ChunkPosition curr = lst.get(i);
			ChunkPosition next = new ChunkPosition(
			curr.getX() - PlayerCoords.getX(event.getPlayer()), 
			curr.getY(), 
			curr.getZ() - PlayerCoords.getZ(event.getPlayer()));
			lst.set(i, next);
		}
		
		event.getPacket().getPositionCollectionModifier().write(0, lst);
	}
	
	private static void sendTileEntityData(PacketEvent event)
	{
		sendInt(event, 0);
		
		Player p = event.getPlayer();
		
		NbtCompound nbt = (NbtCompound)event.getPacket().getNbtModifier().read(0);
		nbt.put("x", nbt.getInteger("x") - PlayerCoords.getX(p));
		nbt.put("z", nbt.getInteger("z") - PlayerCoords.getZ(p));
	}
	
	private static void recvInt(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		int curr_x = event.getPacket().getIntegers().read(index + 0);
		int curr_z = event.getPacket().getIntegers().read(index + 2);
		
		event.getPacket().getIntegers().write(index + 0, curr_x + PlayerCoords.getX(p) );
		event.getPacket().getIntegers().write(index + 2, curr_z + PlayerCoords.getZ(p) );
	}
	
	private static void recvDouble(PacketEvent event, int index)
	{
		Player p = event.getPlayer();
		
		double curr_x = event.getPacket().getDoubles().read(index + 0).doubleValue();
		double curr_z = event.getPacket().getDoubles().read(index + 2).doubleValue();
		
		event.getPacket().getDoubles().write(index + 0, curr_x + PlayerCoords.getX(p) );
		event.getPacket().getDoubles().write(index + 2, curr_z + PlayerCoords.getZ(p) );
	}
	
	private static boolean isSpecialMove(PacketEvent event)
	{
		double y = event.getPacket().getDoubles().read(1).doubleValue();
		double s = event.getPacket().getDoubles().read(3).doubleValue();
		
		if ((y == -999.0D) && (s == -999.0D)) 
			return true;

		return false;
	}
	private static boolean isSpecialPlace(PacketEvent event)
	{
		int x = event.getPacket().getIntegers().read(0);
		int y = event.getPacket().getIntegers().read(1);
		int z = event.getPacket().getIntegers().read(2);
		int d = event.getPacket().getIntegers().read(3);
		
		if ((x == -1) && (y == 255) && (z == -1) && (d == 255)) 
			return true;
		
		return false;
	}
}
