package eu.blackspectrum.randomcoords;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.nbt.NbtBase;

public class RandomCoords extends JavaPlugin implements Listener
{


	@Override
	public void onDisable() {
		PrecisionFix.clear();
		PlayerCoords.clear();
	}




	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( this, this );
		final ProtocolManager pm = ProtocolLibrary.getProtocolManager();

		final HashSet<PacketType> packets = new HashSet<PacketType>();

		// /Server side packets
		{
			this.getServer().getLogger()
					.info( PacketType.fromCurrent( PacketType.Protocol.PLAY, PacketType.Sender.SERVER, 53, 132 ).name() );

			final PacketAdapter.AdapterParameteters paramsServer = PacketAdapter.params();
			paramsServer.plugin( this );
			paramsServer.connectionSide( ConnectionSide.SERVER_SIDE );
			paramsServer.listenerPriority( ListenerPriority.HIGHEST );
			paramsServer.gamePhase( GamePhase.BOTH );

			packets.add( PacketType.Play.Server.BED );
			packets.add( PacketType.Play.Server.BLOCK_ACTION );
			packets.add( PacketType.Play.Server.BLOCK_BREAK_ANIMATION );
			packets.add( PacketType.Play.Server.BLOCK_CHANGE );
			packets.add( PacketType.Play.Server.MULTI_BLOCK_CHANGE );
			packets.add( PacketType.Play.Server.MAP_CHUNK );
			packets.add( PacketType.Play.Server.MAP_CHUNK_BULK );
			packets.add( PacketType.Play.Server.EXPLOSION );
			packets.add( PacketType.Play.Server.SPAWN_POSITION );

			packets.add( PacketType.Play.Server.RESPAWN );
			packets.add( PacketType.Play.Server.POSITION );

			packets.add( PacketType.Play.Server.WORLD_PARTICLES );
			packets.add( PacketType.Play.Server.WORLD_EVENT );

			packets.add( PacketType.Play.Server.NAMED_SOUND_EFFECT );

			packets.add( PacketType.Play.Server.NAMED_ENTITY_SPAWN );
			packets.add( PacketType.Play.Server.SPAWN_ENTITY_WEATHER );
			packets.add( PacketType.Play.Server.SPAWN_ENTITY );
			packets.add( PacketType.Play.Server.SPAWN_ENTITY_LIVING );
			packets.add( PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB );
			packets.add( PacketType.Play.Server.SPAWN_ENTITY_PAINTING );
			packets.add( PacketType.Play.Server.ENTITY_TELEPORT );

			packets.add( PacketType.Play.Server.UPDATE_SIGN );

			packets.add( PacketType.Play.Server.OPEN_SIGN_ENTITY );
			packets.add( PacketType.Play.Server.TILE_ENTITY_DATA );

			paramsServer.types( packets );

			pm.addPacketListener( new PacketAdapter( paramsServer ) {


				@Override
				public void onPacketSending( final PacketEvent event ) {

					PacketContainer packet = event.getPacket();

					if ( packet.getType().equals( PacketType.Play.Server.TILE_ENTITY_DATA ) )
						packet = RandomCoords.this.clonePacket( event.getPacket() );
					else
						packet = event.getPacket().shallowClone();

					event.setPacket( packet );

					Translate.outgoing( packet, event.getPlayer() );

				}

			} );
		}// End Server Packets

		// /Client side Packets
		{
			final PacketAdapter.AdapterParameteters paramsClient = PacketAdapter.params();
			paramsClient.plugin( this );
			paramsClient.connectionSide( ConnectionSide.CLIENT_SIDE );
			paramsClient.listenerPriority( ListenerPriority.LOWEST );
			paramsClient.gamePhase( GamePhase.BOTH );

			packets.clear();

			packets.add( PacketType.Play.Client.POSITION );
			packets.add( PacketType.Play.Client.POSITION_LOOK );
			packets.add( PacketType.Play.Client.BLOCK_DIG );
			packets.add( PacketType.Play.Client.BLOCK_PLACE );
			packets.add( PacketType.Play.Client.UPDATE_SIGN );

			paramsClient.types( packets );

			pm.addPacketListener( new PacketAdapter( paramsClient ) {


				@Override
				public void onPacketReceiving( final PacketEvent event ) {
					try
					{
						Translate.incoming( event.getPacket(), event.getPlayer() );
					}
					catch ( final UnsupportedOperationException e )
					{
						event.setCancelled( true );
						Bukkit.getServer().broadcastMessage( "Failed: " + event.getPacket().getType().name() );
					}
				}
			} );
		}// End client packets
	}




	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit( final PlayerQuitEvent event ) {
		PrecisionFix.clean( event.getPlayer() );
		PlayerCoords.clean( event.getPlayer() );
	}




	private PacketContainer clonePacket( final PacketContainer packet ) {
		final PacketContainer newPacket = new PacketContainer( packet.getType() );

		int i = 0;
		for ( final Object obj : packet.getModifier().getValues() )
		{
			newPacket.getModifier().write( i, obj );
			i++;
		}

		i = 0;
		for ( final NbtBase<?> obj : packet.getNbtModifier().getValues() )
		{
			newPacket.getNbtModifier().write( i, obj.deepClone() );
			i++;
		}

		return newPacket;
	}
}
