package dev.exo8.clientity.api.raycast;

import dev.exo8.clientity.api.entity.ClientEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface ClientEntityRaycaster
{
    RaytraceResult raytrace(Player player);
    RaytraceResult raytrace(Entity entity, double distance);
    RaytraceResult raytrace(Location origin, Vector direction, double distance);

    record RaytraceResult(ClientEntity<?> entity, Vector hit, Vector uv) { }
}