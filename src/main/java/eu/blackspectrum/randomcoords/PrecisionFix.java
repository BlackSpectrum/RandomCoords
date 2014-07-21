package eu.blackspectrum.randomcoords;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;

public class PrecisionFix
{


	private static final ConcurrentHashMap<UUID, double[]>	playerPositions	= new ConcurrentHashMap<UUID, double[]>();




	public static void clean( final Player player ) {
		playerPositions.remove( player.getUniqueId() );
	}




	public static void clear() {
		playerPositions.clear();
	}




	public static void onClientChangePos( final PacketContainer packet, final Player player ) {
		if ( !playerPositions.containsKey( player.getUniqueId() ) )
			return;

		final double curr_x = packet.getDoubles().read( 0 );
		final double curr_z = packet.getDoubles().read( 2 );
		final double dx = playerPositions.get( player.getUniqueId() )[0] - curr_x;
		final double dz = playerPositions.get( player.getUniqueId() )[1] - curr_z;

		if ( Math.abs( dx ) + Math.abs( dz ) > 0.00390625D )
			return;

		packet.getDoubles().write( 0, playerPositions.get( player.getUniqueId() )[0] );
		packet.getDoubles().write( 2, playerPositions.get( player.getUniqueId() )[1] );
		playerPositions.remove( player.getUniqueId() );
	}




	public static void onServerChangePos( final PacketContainer packet, final Player player ) {
		final double curr_x = packet.getDoubles().read( 0 );
		final double curr_z = packet.getDoubles().read( 2 );
		playerPositions.put( player.getUniqueId(), new double[] { curr_x, curr_z } );
	}

}
