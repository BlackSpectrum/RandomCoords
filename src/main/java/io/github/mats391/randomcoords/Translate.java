package io.github.mats391.randomcoords;

import java.util.List;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class Translate
{

	public static void incoming( final PacketEvent event ) {
		switch ( event.getPacketType().name() ) {
		case "POSITION":
			recvDouble( event, 0 );
			break;
		case "POSITION_LOOK":
			if ( !isSpecialMove( event ) )
			{
				recvDouble( event, 0 );
				PrecisionFix.onClientChangePos( event );
			}
			break;
		case "BLOCK_DIG":
			recvInt( event, 0 );
			break;
		case "BLOCK_PLACE":
			if ( !isSpecialPlace( event ) )
				recvInt( event, 0 );
			break;
		case "UPDATE_SIGN":
			recvInt( event, 0 );
		}
	}

	public static void outgoing( final PacketEvent event ) {
		switch ( event.getPacketType().name() ) {
		case "SPAWN_POSITION":
			sendInt( event, 0 );
			break;
		case "RESPAWN":
			PlayerCoords.addNewPlayer( event.getPlayer() );
			break;
		case "POSITION":
			PrecisionFix.onServerChangePos( event );
			sendDouble( event, 0 );
			break;
		case "BED":
			sendInt( event, 1 );
			break;
		case "NAMED_ENTITY_SPAWN":
		case "SPAWN_ENTITY":
			sendFixedPointNumber( event, 1 );
			break;
		case "SPAWN_ENTITY_LIVING":
			sendFixedPointNumber( event, 2 );
			break;
		case "SPAWN_ENTITY_PAINTING":
			sendInt( event, 1 );
			break;
		case "SPAWN_ENTITY_EXPERIENCE_ORB":
		case "ENTITY_TELEPORT":
			sendFixedPointNumber( event, 1 );
			break;
		case "MAP_CHUNK":
			sendChunk( event );
			break;
		case "MULTI_BLOCK_CHANGE":
			sendChunkUpdate( event );
			break;
		case "BLOCK_CHANGE":
		case "BLOCK_ACTION":
			sendInt( event, 0 );
			break;
		case "BLOCK_BREAK_ANIMATION":
			sendInt( event, 1 );
			break;
		case "MAP_CHUNK_BULK":
			sendChunkBulk( event );
			break;
		case "EXPLOSION":
			sendExplosion( event );
			break;
		case "WORLD_EVENT":
			sendInt( event, 2 );
			break;
		case "NAMED_SOUND_EFFECT":
			sendInt8( event, 0 );
			break;
		case "WORLD_PARTICLES":
			sendFloat( event, 1 );
			break;
		case "SPAWN_ENTITY_WEATHER":
			sendFixedPointNumber( event, 1 );
			break;
		case "UPDATE_SIGN":
			sendInt( event, 0 );
			break;
		case "TILE_ENTITY_DATA":
			sendTileEntityData( event );
			break;
		case "OPEN_SIGN_ENTITY":
			sendInt( event, 0 );
		}
	}

	private static boolean isSpecialMove( final PacketEvent event ) {
		final double y = event.getPacket().getDoubles().read( 1 ).doubleValue();
		final double s = event.getPacket().getDoubles().read( 3 ).doubleValue();

		if ( y == -999.0D && s == -999.0D )
			return true;

		return false;
	}

	private static boolean isSpecialPlace( final PacketEvent event ) {
		final int x = event.getPacket().getIntegers().read( 0 );
		final int y = event.getPacket().getIntegers().read( 1 );
		final int z = event.getPacket().getIntegers().read( 2 );
		final int d = event.getPacket().getIntegers().read( 3 );

		if ( x == -1 && y == 255 && z == -1 && d == 255 )
			return true;

		return false;
	}

	private static void recvDouble( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final double curr_x = event.getPacket().getDoubles().read( index + 0 ).doubleValue();
		final double curr_z = event.getPacket().getDoubles().read( index + 2 ).doubleValue();

		event.getPacket().getDoubles().write( index + 0, curr_x + PlayerCoords.getX( p ) );
		event.getPacket().getDoubles().write( index + 2, curr_z + PlayerCoords.getZ( p ) );
	}

	private static void recvInt( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final int curr_x = event.getPacket().getIntegers().read( index + 0 );
		final int curr_z = event.getPacket().getIntegers().read( index + 2 );

		event.getPacket().getIntegers().write( index + 0, curr_x + PlayerCoords.getX( p ) );
		event.getPacket().getIntegers().write( index + 2, curr_z + PlayerCoords.getZ( p ) );
	}

	private static void sendChunk( final PacketEvent event ) {
		final Player p = event.getPlayer();

		final int curr_x = event.getPacket().getIntegers().read( 0 );
		final int curr_z = event.getPacket().getIntegers().read( 1 );

		event.getPacket().getIntegers().write( 0, curr_x - PlayerCoords.getChunkX( p ) );
		event.getPacket().getIntegers().write( 1, curr_z - PlayerCoords.getChunkZ( p ) );
	}

	private static void sendChunkBulk( final PacketEvent event ) {
		final Player p = event.getPlayer();

		final int[] x = event.getPacket().getIntegerArrays().read( 0 ).clone();
		final int[] z = event.getPacket().getIntegerArrays().read( 1 ).clone();

		for ( int i = 0; i < x.length; i++ )
		{

			x[i] = x[i] - PlayerCoords.getChunkX( p );
			z[i] = z[i] - PlayerCoords.getChunkZ( p );
		}

		event.getPacket().getIntegerArrays().write( 0, x );
		event.getPacket().getIntegerArrays().write( 1, z );
	}

	private static void sendChunkUpdate( final PacketEvent event ) {
		final Player p = event.getPlayer();

		final ChunkCoordIntPair newCoords = new ChunkCoordIntPair( event.getPacket().getChunkCoordIntPairs().read( 0 ).getChunkX()
				- PlayerCoords.getChunkX( p ), event.getPacket().getChunkCoordIntPairs().read( 0 ).getChunkZ() - PlayerCoords.getChunkZ( p ) );

		event.getPacket().getChunkCoordIntPairs().write( 0, newCoords );
	}

	private static void sendDouble( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final double curr_x = event.getPacket().getDoubles().read( index + 0 ).doubleValue();
		final double curr_z = event.getPacket().getDoubles().read( index + 2 ).doubleValue();

		event.getPacket().getDoubles().write( index + 0, curr_x - PlayerCoords.getX( p ) );
		event.getPacket().getDoubles().write( index + 2, curr_z - PlayerCoords.getZ( p ) );
	}

	private static void sendExplosion( final PacketEvent event ) {
		sendDouble( event, 0 );

		final List<ChunkPosition> lst = event.getPacket().getPositionCollectionModifier().read( 0 );

		for ( int i = 0; i < lst.size(); i++ )
		{
			final ChunkPosition curr = lst.get( i );
			final ChunkPosition next = new ChunkPosition( curr.getX() - PlayerCoords.getX( event.getPlayer() ), curr.getY(), curr.getZ()
					- PlayerCoords.getZ( event.getPlayer() ) );
			lst.set( i, next );
		}

		event.getPacket().getPositionCollectionModifier().write( 0, lst );
	}

	private static void sendFixedPointNumber( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final int curr_x = event.getPacket().getIntegers().read( index + 0 );
		final int curr_z = event.getPacket().getIntegers().read( index + 2 );

		event.getPacket().getIntegers().write( index + 0, curr_x - 32 * PlayerCoords.getX( p ) );
		event.getPacket().getIntegers().write( index + 2, curr_z - 32 * PlayerCoords.getZ( p ) );
	}

	private static void sendFloat( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final float curr_x = event.getPacket().getFloat().read( index + 0 ).floatValue();
		final float curr_z = event.getPacket().getFloat().read( index + 2 ).floatValue();

		event.getPacket().getFloat().write( index + 0, curr_x - PlayerCoords.getX( p ) );
		event.getPacket().getFloat().write( index + 2, curr_z - PlayerCoords.getZ( p ) );
	}

	private static void sendInt( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final int curr_x = event.getPacket().getIntegers().read( index + 0 );
		final int curr_z = event.getPacket().getIntegers().read( index + 2 );

		event.getPacket().getIntegers().write( index + 0, curr_x - PlayerCoords.getX( p ) );
		event.getPacket().getIntegers().write( index + 2, curr_z - PlayerCoords.getZ( p ) );
	}

	private static void sendInt8( final PacketEvent event, final int index ) {
		final Player p = event.getPlayer();

		final int curr_x = event.getPacket().getIntegers().read( index + 0 );
		final int curr_z = event.getPacket().getIntegers().read( index + 2 );

		event.getPacket().getIntegers().write( index + 0, curr_x - 8 * PlayerCoords.getX( p ) );
		event.getPacket().getIntegers().write( index + 2, curr_z - 8 * PlayerCoords.getZ( p ) );
	}

	private static void sendTileEntityData( final PacketEvent event ) {
		sendInt( event, 0 );

		final Player p = event.getPlayer();

		final NbtCompound nbt = (NbtCompound) event.getPacket().getNbtModifier().read( 0 );
		nbt.put( "x", nbt.getInteger( "x" ) - PlayerCoords.getX( p ) );
		nbt.put( "z", nbt.getInteger( "z" ) - PlayerCoords.getZ( p ) );
	}
}
