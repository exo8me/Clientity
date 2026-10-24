package dev.exo8.clientity.nms.v26_R2;

import dev.exo8.clientity.api.event.ClientEntityInteractEvent;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static dev.exo8.clientity.api.event.ClientEntityInteractEvent.ActionType.INTERACT;

final class PlayerPacketInjector_26_R2 implements PlayerPacketInjector
{
    private final Plugin plugin;
    private final ClientEntityRaycaster raycaster;

    PlayerPacketInjector_26_R2(Plugin plugin, ClientEntityRaycaster raycaster)
    {
        this.plugin = plugin;
        this.raycaster = raycaster;
    }


    @Override
    public void inject(Player player)
    {
        var handlerName = getHandlerName();

        var channel = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();

        if (channel.get(handlerName) == null) {
            channel.addBefore("packet_handler", handlerName, new PlayerConnection(player));
        }
    }

    @Override
    public void uninject(Player player)
    {
        var handlerName = getHandlerName();

        var channel = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();

        if (channel.get(handlerName) != null) {
            channel.remove(handlerName);
        }
    }

    private String getHandlerName() {
        return plugin.getName() + ":ClientEntityLib";
    }

    private class PlayerConnection extends ChannelDuplexHandler
    {
        private final Player player;

        private PlayerConnection(Player player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
        {
            if (!(msg instanceof ServerboundInteractPacket packet))
            {
                super.channelRead(ctx, msg);
                return;
            }

            var result = raycaster.raytrace(player);

            if (result == null || result.entity() == null)
            {
                super.channelRead(ctx, msg);
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getServer().getPluginManager().callEvent(
                            new ClientEntityInteractEvent(player, INTERACT, result)
                    )
            );

            super.channelRead(ctx, msg);
        }
    }
}