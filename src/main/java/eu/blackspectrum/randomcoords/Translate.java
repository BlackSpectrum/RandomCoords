package eu.blackspectrum.randomcoords;

import java.util.List;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class Translate
{


	public static void incoming( final PacketContainer packet, final Player player ) {
		switch ( packet.getType().name() ) {
		case "POSITION":
			recvDouble( packet, player, 0 );
			break;
		case "POSITION_LOOK":
			if ( !isSpecialMove( packet ) )
			{
				recvDouble( packet, player, 0 );
				PrecisionFix.onClientChangePos( packet, player );
			}
			break;
		case "BLOCK_DIG":
			recvInt( packet, player, 0 );
			break;
		case "BLOCK_PLACE":
			if ( !isSpecialPlace( packet ) )
				recvInt( packet, player, 0 );
			break;
		case "UPDATE_SIGN":
			recvInt( packet, player, 0 );
		}
	}




	public static void outgoing( final PacketContainer packet, final Player player ) {
		switch ( packet.getType().name() ) {
		case "SPAWN_POSITION":
			sendInt( packet, player, 0 );
			break;
		case "RESPAWN":
			PlayerCoords.addNewPlayer( player );
			break;
		case "POSITION":
			PrecisionFix.onServerChangePos( packet, player );
			sendDouble( packet, player, 0 );
			break;
		case "BED":
			sendInt( packet, player, 1 );
			break;
		case "NAMED_ENTITY_SPAWN":
		case "SPAWN_ENTITY":
			sendFixedPointNumber( packet, player, 1 );
			break;
		case "SPAWN_ENTITY_LIVING":
			sendFixedPointNumber( packet, player, 2 );
			break;
		case "SPAWN_ENTITY_PAINTING":
			sendInt( packet, player, 1 );
			break;
		case "SPAWN_ENTITY_EXPERIENCE_ORB":
		case "ENTITY_TELEPORT":
			sendFixedPointNumber( packet, player, 1 );
			break;
		case "MAP_CHUNK":
			sendChunk( packet, player );
			break;
		case "MULTI_BLOCK_CHANGE":
			sendChunkUpdate( packet, player );
			break;
		case "BLOCK_CHANGE":
		case "BLOCK_ACTION":
			sendInt( packet, player, 0 );
			break;
		case "BLOCK_BREAK_ANIMATION":
			sendInt( packet, player, 1 );
			break;
		case "MAP_CHUNK_BULK":
			sendChunkBulk( packet, player );
			break;
		case "EXPLOSION":
			sendExplosion( packet, player );
			break;
		case "WORLD_EVENT":
			sendInt( packet, player, 2 );
			break;
		case "NAMED_SOUND_EFFECT":
			sendInt8( packet, player, 0 );
			break;
		case "WORLD_PARTICLES":
			sendFloat( packet, player, 1 );
			break;
		case "SPAWN_ENTITY_WEATHER":
			sendFixedPointNumber( packet, player, 1 );
			break;
		case "UPDATE_SIGN":
			sendInt( packet, player, 0 );
			break;
		case "TILE_ENTITY_DATA":
			sendTileEntityData( packet, player );
			break;
		case "OPEN_SIGN_ENTITY":
			sendInt( packet, player, 0 );
		}
	}




	private static boolean isSpecialMove( final PacketContainer packet ) {
		final double y = packet.getDoubles().read( 1 ).doubleValue();
		final double s = packet.getDoubles().read( 3 ).doubleValue();

		if ( y == -999.0D && s == -999.0D )
			return true;

		return false;
	}




	private static boolean isSpecialPlace( final PacketContainer packet ) {
		final int x = packet.getIntegers().read( 0 );
		final int y = packet.getIntegers().read( 1 );
		final int z = packet.getIntegers().read( 2 );
		final int d = packet.getIntegers().read( 3 );

		if ( x == -1 && y == 255 && z == -1 && d == 255 )
			return true;

		return false;
	}




	private static void recvDouble( final PacketContainer packet, final Player player, final int index ) {

		final double curr_x = packet.getDoubles().read( index + 0 ).doubleValue();
		final double curr_z = packet.getDoubles().read( index + 2 ).doubleValue();

		packet.getDoubles().write( index + 0, curr_x + PlayerCoords.getX( player ) );
		packet.getDoubles().write( index + 2, curr_z + PlayerCoords.getZ( player ) );
	}




	private static void recvInt( final PacketContainer packet, final Player player, final int index ) {

		final int curr_x = packet.getIntegers().read( index + 0 );
		final int curr_z = packet.getIntegers().read( index + 2 );

		packet.getIntegers().write( index + 0, curr_x + PlayerCoords.getX( player ) );
		packet.getIntegers().write( index + 2, curr_z + PlayerCoords.getZ( player ) );
	}




	private static void sendChunk( final PacketContainer packet, final Player player ) {

		final int curr_x = packet.getIntegers().read( 0 );
		final int curr_z = packet.getIntegers().read( 1 );

		packet.getIntegers().write( 0, curr_x - PlayerCoords.getChunkX( player ) );
		packet.getIntegers().write( 1, curr_z - PlayerCoords.getChunkZ( player ) );
	}




	private static void sendChunkBulk( final PacketContainer packet, final Player player ) {

		final int[] x = packet.getIntegerArrays().read( 0 ).clone();
		final int[] z = packet.getIntegerArrays().read( 1 ).clone();

		for ( int i = 0; i < x.length; i++ )
		{

			x[i] = x[i] - PlayerCoords.getChunkX( player );
			z[i] = z[i] - PlayerCoords.getChunkZ( player );
		}

		packet.getIntegerArrays().write( 0, x );
		packet.getIntegerArrays().write( 1, z );
	}




	private static void sendChunkUpdate( final PacketContainer packet, final Player player ) {

		final ChunkCoordIntPair newCoords = new ChunkCoordIntPair( packet.getChunkCoordIntPairs().read( 0 ).getChunkX()
				- PlayerCoords.getChunkX( player ), packet.getChunkCoordIntPairs().read( 0 ).getChunkZ() - PlayerCoords.getChunkZ( player ) );

		packet.getChunkCoordIntPairs().write( 0, newCoords );
	}




	private static void sendDouble( final PacketContainer packet, final Player player, final int index ) {

		final double curr_x = packet.getDoubles().read( index + 0 ).doubleValue();
		final double curr_z = packet.getDoubles().read( index + 2 ).doubleValue();

		packet.getDoubles().write( index + 0, curr_x - PlayerCoords.getX( player ) );
		packet.getDoubles().write( index + 2, curr_z - PlayerCoords.getZ( player ) );
	}




	private static void sendExplosion( final PacketContainer packet, final Player player ) {
		sendDouble( packet, player, 0 );

		final List<ChunkPosition> lst = packet.getPositionCollectionModifier().read( 0 );

		for ( int i = 0; i < lst.size(); i++ )
		{
			final ChunkPosition curr = lst.get( i );
			final ChunkPosition next = new ChunkPosition( curr.getX() - PlayerCoords.getX( player ), curr.getY(), curr.getZ()
					- PlayerCoords.getZ( player ) );
			lst.set( i, next );
		}

		packet.getPositionCollectionModifier().write( 0, lst );
	}




	private static void sendFixedPointNumber( final PacketContainer packet, final Player player, final int index ) {
		final int curr_x = packet.getIntegers().read( index + 0 );
		final int curr_z = packet.getIntegers().read( index + 2 );

		packet.getIntegers().write( index + 0, curr_x - ( PlayerCoords.getX( player ) << 5 ) );
		packet.getIntegers().write( index + 2, curr_z - ( PlayerCoords.getZ( player ) << 5 ) );
	}




	private static void sendFloat( final PacketContainer packet, final Player player, final int index ) {
		final float curr_x = packet.getFloat().read( index + 0 ).floatValue();
		final float curr_z = packet.getFloat().read( index + 2 ).floatValue();

		packet.getFloat().write( index + 0, curr_x - PlayerCoords.getX( player ) );
		packet.getFloat().write( index + 2, curr_z - PlayerCoords.getZ( player ) );
	}




	private static void sendInt( final PacketContainer packet, final Player player, final int index ) {
		final int curr_x = packet.getIntegers().read( index + 0 );
		final int curr_z = packet.getIntegers().read( index + 2 );

		packet.getIntegers().write( index + 0, curr_x - PlayerCoords.getX( player ) );
		packet.getIntegers().write( index + 2, curr_z - PlayerCoords.getZ( player ) );
	}




	private static void sendInt8( final PacketContainer packet, final Player player, final int index ) {

		final int curr_x = packet.getIntegers().read( index + 0 );
		final int curr_z = packet.getIntegers().read( index + 2 );

		packet.getIntegers().write( index + 0, curr_x - ( PlayerCoords.getX( player ) << 3 ) );
		packet.getIntegers().write( index + 2, curr_z - ( PlayerCoords.getZ( player ) << 3 ) );
	}




	private static void sendTileEntityData( final PacketContainer packet, final Player player ) {
		final int curr_x = packet.getIntegers().read( 0 );
		final int curr_z = packet.getIntegers().read( 2 );

		packet.getIntegers().write( 0, curr_x - PlayerCoords.getX( player ) );
		packet.getIntegers().write( 2, curr_z - PlayerCoords.getZ( player ) );

		final NbtCompound nbt = (NbtCompound) packet.getNbtModifier().read( 0 );
		nbt.put( "x", curr_x - PlayerCoords.getX( player ) );
		nbt.put( "z", curr_z - PlayerCoords.getZ( player ) );
	}
}
