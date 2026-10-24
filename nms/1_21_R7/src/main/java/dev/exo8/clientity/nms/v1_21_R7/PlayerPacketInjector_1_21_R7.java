package dev.exo8.clientity.nms.v1_21_R7;

import dev.exo8.clientity.api.event.ClientEntityInteractEvent;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import static dev.exo8.clientity.api.event.ClientEntityInteractEvent.ActionType.ATTACK;
import static dev.exo8.clientity.api.event.ClientEntityInteractEvent.ActionType.INTERACT;

final class PlayerPacketInjector_1_21_R7 implements PlayerPacketInjector
{
    private final Plugin plugin;
    private final ClientEntityRaycaster raycaster;

    PlayerPacketInjector_1_21_R7(Plugin plugin, ClientEntityRaycaster raycaster)
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
            if (msg instanceof final ServerboundInteractPacket packet)
            {
                final boolean[] clicked = {false};

                packet.dispatch(new ServerboundInteractPacket.Handler() {
                    @Override
                    public void onAttack() {
                        clicked[0] = true;
                    }

                    @Override
                    public void onInteraction(@NonNull InteractionHand hand) {
                        clicked[0] = true;
                    }

                    @Override
                    public void onInteraction(@NonNull InteractionHand hand, @NonNull Vec3 pos) {
                        clicked[0] = false;
                    }
                });

                if (!clicked[0])
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

                var action = packet.isAttack() ? ATTACK : INTERACT;

                plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getServer().getPluginManager().callEvent(
                                new ClientEntityInteractEvent(player, action, result)
                        )
                );

                super.channelRead(ctx, msg);
                return;
            }
            super.channelRead(ctx, msg);
        }
    }
}