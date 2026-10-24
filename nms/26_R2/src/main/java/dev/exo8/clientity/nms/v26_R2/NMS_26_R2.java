package dev.exo8.clientity.nms.v26_R2;

import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import dev.exo8.clientity.nms.NMS;
import org.bukkit.plugin.Plugin;

public final class NMS_26_R2 implements NMS
{
    @Override
    public ClientEntityManager manager(Plugin plugin) {
        return new ClientEntityManager_26_R2(plugin);
    }

    @Override
    public ClientEntityRaycaster raycaster(ClientEntityManager manager) {
        return new ClientEntityRaycaster_26_R2(manager);
    }

    @Override
    public PlayerPacketInjector injector(Plugin plugin, ClientEntityRaycaster raycaster) {
        return new PlayerPacketInjector_26_R2(plugin, raycaster);
    }
}