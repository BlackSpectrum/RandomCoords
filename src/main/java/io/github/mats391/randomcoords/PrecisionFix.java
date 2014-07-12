package io.github.mats391.randomcoords;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketEvent;

public class PrecisionFix
{

	private static final ConcurrentHashMap<UUID, double[]>	playerPositions	= new ConcurrentHashMap<UUID, double[]>();

	public static void clean( final Player player ) {
		playerPositions.remove( player.getUniqueId() );
	}

	public static void clear() {
		playerPositions.clear();
	}

	public static void onClientChangePos( final PacketEvent event ) {
		if ( !playerPositions.containsKey( event.getPlayer().getUniqueId() ) )
			return;

		final double curr_x = event.getPacket().getDoubles().read( 0 );
		final double curr_z = event.getPacket().getDoubles().read( 2 );
		final double dx = playerPositions.get( event.getPlayer().getUniqueId() )[0] - curr_x;
		final double dz = playerPositions.get( event.getPlayer().getUniqueId() )[1] - curr_z;

		if ( Math.abs( dx ) + Math.abs( dz ) > 0.00390625D )
			return;

		event.getPacket().getDoubles().write( 0, playerPositions.get( event.getPlayer().getUniqueId() )[0] );
		event.getPacket().getDoubles().write( 2, playerPositions.get( event.getPlayer().getUniqueId() )[1] );
		playerPositions.remove( event.getPlayer().getUniqueId() );
	}

	public static void onServerChangePos( final PacketEvent event ) {
		final double curr_x = event.getPacket().getDoubles().read( 0 );
		final double curr_z = event.getPacket().getDoubles().read( 2 );
		playerPositions.put( event.getPlayer().getUniqueId(), new double[] { curr_x, curr_z } );
	}

}
