package dev.exo8.clientity.nms;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface PacketSender<P>
{
    void send(Player player, P packet);
    void send(Player player, Collection<P> packets);
}