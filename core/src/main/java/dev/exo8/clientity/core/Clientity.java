package dev.exo8.clientity.core;

import dev.exo8.clientity.api.entity.ClientEntity;
import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import dev.exo8.clientity.nms.NMS;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class Clientity
{
    private static final String NAMESPACE = "clientity";
    private static final String PREFIX = "<#833eb5>[Clientity] ";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static ConsoleCommandSender sender;

    private static ClientEntityManager manager;
    private static PlayerPacketInjector injector;
    private static ClientEntityRaycaster raycaster;

    private static BukkitTask ticker;

    private static boolean setup = false;

    public static void setup(JavaPlugin plugin)
    {
        if (setup) return;

        Clientity.sender = plugin.getServer().getConsoleSender();

        var result = NMS.find();

        info("Minimal supported version " + result.min().info());
        info("Detected version " + result.current().info());
        info("Fallback version " + result.max().info());

        if (result.nms() == null)
        {
            error("Unsupported Minecraft version!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        var nms = result.nms();

        Clientity.manager = nms.manager(plugin);
        Clientity.raycaster = nms.raycaster(manager);
        Clientity.injector = nms.injector(plugin, raycaster);

        plugin.getServer().getPluginManager().registerEvents(new ClientEntityListener(
                plugin, manager, raycaster, injector
        ), plugin);

        plugin.getServer().getOnlinePlayers().forEach(injector::inject);

        ticker = plugin.getServer().getScheduler().runTaskTimer(
                plugin, () -> manager.all().forEach(ClientEntity::tick), 0, 1
        );

        setup = true;
    }

    public static void shutdown()
    {
        if (!setup) return;

        Bukkit.getOnlinePlayers().forEach(injector::uninject);

        manager.all().forEach(ClientEntity::remove);

        if (ticker != null && !ticker.isCancelled()) {
            ticker.cancel();
        }

        ticker = null;
        manager = null;
        setup = false;
    }

    public static ClientEntityManager manager() {
        return manager;
    }

    public static ClientEntityRaycaster raycaster() {
        return raycaster;
    }

    static void info(String str) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + "<#ffdb0d>" + str));
    }

    static void error(String str) {
        sender.sendMessage(MINI_MESSAGE.deserialize(PREFIX + "<#b5291f>" + str));
    }

    private Clientity() {}
}