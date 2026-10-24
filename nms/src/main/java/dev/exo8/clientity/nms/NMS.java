package dev.exo8.clientity.nms;

import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.nms.PlayerPacketInjector;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public interface NMS
{
    ClientEntityManager manager(Plugin plugin);
    ClientEntityRaycaster raycaster(ClientEntityManager manager);
    PlayerPacketInjector injector(Plugin plugin, ClientEntityRaycaster raycaster);

    Map<String, String> VERSION_TO_REVISION = new HashMap<>() {{
        put("1.21.9", "1_21_R6");
        put("1.21.10", "1_21_R6");
        put("1.21.11", "1_21_R7");

        put("26.1", "26_R1");
        put("26.1.1", "26_R1");
        put("26.2", "26_R2");
    }};

    String MINIMAL_VERSION = "1.21.9";
    String FALLBACK_VERSION = "26.2";

    static Result find()
    {
        var bukkitPackageName =
                Bukkit.getServer().getClass().getPackage().getName();

        var bukkitVersion =
                Bukkit.getBukkitVersion().split("-")[0];

        var nmsVersion = (String) null;

        if (!bukkitPackageName.contains(".v"))
        {
            nmsVersion = VERSION_TO_REVISION.getOrDefault(
                    bukkitVersion,
                    VERSION_TO_REVISION.get(FALLBACK_VERSION)
            );
        }
        else
        {
            nmsVersion = bukkitPackageName
                    .split("\\.")[3]
                    .substring(1);
        }

        var className = "dev.exo8.clientity.nms.v" + nmsVersion + ".NMS_" + nmsVersion;

        var nms = (NMS) null;

        try {
            nms = (NMS) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            try {
                nms = (NMS) Class.forName(
                        "dev.exo8.clientity.nms.v" + VERSION_TO_REVISION.get(FALLBACK_VERSION) + ".NMS_" + VERSION_TO_REVISION.get(FALLBACK_VERSION)
                ).getDeclaredConstructor().newInstance();
            } catch (Exception ignored) { }
        }

        return new Result(
                nms,
                new Version(bukkitVersion, nmsVersion),
                new Version(MINIMAL_VERSION, VERSION_TO_REVISION.get(MINIMAL_VERSION)),
                new Version(FALLBACK_VERSION, VERSION_TO_REVISION.get(FALLBACK_VERSION))
        );
    }

    record Version(
            String bukkit,
            String nms
    ) {
        public String info()
        {
            return "Bukkit Version = " + bukkit
                    + ", NMS Version = " + nms;
        }
    }

    record Result(
            NMS nms,
            Version current,
            Version min,
            Version max
    ) {}
}