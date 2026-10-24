package dev.exo8.clientity.nms.v1_21_R7;

import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import dev.exo8.clientity.nms.NMS;
import org.bukkit.plugin.Plugin;

public final class NMS_1_21_R7 implements NMS
{
    @Override
    public ClientEntityManager manager(Plugin plugin) {
        return new ClientEntityManager_1_21_R7(plugin);
    }

    @Override
    public ClientEntityRaycaster raycaster(ClientEntityManager manager) {
        return new ClientEntityRaycaster_1_21_R7(manager);
    }

    @Override
    public PlayerPacketInjector injector(Plugin plugin, ClientEntityRaycaster raycaster) {
        return new PlayerPacketInjector_1_21_R7(plugin, raycaster);
    }
}