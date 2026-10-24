package dev.exo8.clientity.api.nms;

import org.bukkit.entity.Player;

public interface PlayerPacketInjector
{
    void inject(Player player);
    void uninject(Player player);
}