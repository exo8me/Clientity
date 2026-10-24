package dev.exo8.clientity.core;

import dev.exo8.clientity.api.entity.ClientEntity;
import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

final class ClientEntityListener implements Listener
{
    private final JavaPlugin plugin;
    private final ClientEntityManager manager;
    private final ClientEntityRaycaster raycaster;
    private final PlayerPacketInjector injector;

    ClientEntityListener(JavaPlugin plugin, ClientEntityManager manager, ClientEntityRaycaster raycaster, PlayerPacketInjector injector)
    {
        this.plugin = plugin;
        this.manager = manager;
        this.raycaster = raycaster;
        this.injector = injector;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        if (injector == null) {
            return;
        }

        injector.inject(e.getPlayer());

        var id = e.getPlayer().getUniqueId();

        for (var clientEntity : manager.all()) {
            if (clientEntity.viewers().stream().map(Player::getUniqueId).anyMatch(id::equals) || clientEntity.mode() == ClientEntity.VisibleMode.ALL) {
                clientEntity.show();
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        injector.uninject(e.getPlayer());
    }
//
//    @EventHandler
//    public void onRespawn(PlayerRespawnEvent e) {
//        resync(e.getPlayer());
//    }
//
//    @EventHandler
//    public void onChangeWorld(PlayerChangedWorldEvent e) {
//        resync(e.getPlayer());
//    }
//
//    @EventHandler
//    public void onChangeGameMode(PlayerGameModeChangeEvent e) {
//        Bukkit.getScheduler().runTask(plugin, () -> resync(e.getPlayer()));
//    }
//
//    private void resync(Player player)
//    {
//        for (var clientEntity : manager.all())
//        {
//            clientEntity.removeViewer(player);
//            clientEntity.hide();
//
//            clientEntity.addViewer(player);
//            clientEntity.show();
//        }
//    }
}