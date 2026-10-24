package dev.exo8.clientity.nms.v1_21_R7;

import dev.exo8.clientity.nms.PacketSender;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

final class PacketSender_1_21_R7 implements PacketSender<Packet<?>>
{
    @Override
    public void send(Player player, Packet<?> packet) {
        connection(player).send(packet);
    }

    @Override
    public void send(Player player, Collection<Packet<?>> packets)
    {
        var connection = connection(player);
        packets.forEach(connection::send);
    }

    private ServerGamePacketListenerImpl connection(Player player) {
        return ((CraftPlayer) player).getHandle().connection;
    }
}
