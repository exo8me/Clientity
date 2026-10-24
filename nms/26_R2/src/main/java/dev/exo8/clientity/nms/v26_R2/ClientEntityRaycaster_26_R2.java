package dev.exo8.clientity.nms.v26_R2;

import dev.exo8.clientity.api.entity.ClientEntityManager;
import dev.exo8.clientity.api.raycast.ClientEntityRaycaster;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class ClientEntityRaycaster_26_R2 implements ClientEntityRaycaster {
    private final ClientEntityManager manager;

    ClientEntityRaycaster_26_R2(ClientEntityManager manager) {
        this.manager = manager;
    }

    @Override
    public RaytraceResult raytrace(Player player) {
        var distance = 4.0;
        var attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute != null) distance = attribute.getValue();
        return raytrace(player, distance);
    }

    @Override
    public RaytraceResult raytrace(Entity entity, double distance) {
        var eye = entity instanceof LivingEntity living ? living.getEyeLocation() : entity.getLocation();
        return raytrace(eye, eye.getDirection(), distance);
    }

    @Override
    public RaytraceResult raytrace(Location origin, Vector direction, double distance)
    {
        var start = new Vec3(
                origin.getX(),
                origin.getY(),
                origin.getZ()
        );

        var dir = direction.clone().normalize();

        var end = start.add(
                dir.getX() * distance,
                dir.getY() * distance,
                dir.getZ() * distance
        );

        RaytraceResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (var clientEntity : manager.all())
        {
            if (!(clientEntity.handle() instanceof net.minecraft.world.entity.Entity entity)) {
                continue;
            }

            var multiplier = 1.0;

            if (entity instanceof net.minecraft.world.entity.LivingEntity living)
            {
                var instance = living.getAttribute(Attributes.SCALE);
                multiplier = instance == null ? 1.0 : instance.getValue();
            }

            var box = entity.getBoundingBox();

            if (multiplier != 1.0)
            {
                var center = box.getCenter();

                var halfX = box.getXsize() * multiplier * 0.5;
                var halfY = box.getYsize() * multiplier * 0.5;
                var halfZ = box.getZsize() * multiplier * 0.5;

                box = new AABB(
                        center.x - halfX,
                        center.y - halfY,
                        center.z - halfZ,
                        center.x + halfX,
                        center.y + halfY,
                        center.z + halfZ
                );
            }

            var hit = box.clip(start, end);

            var hitPos = (Vec3) null;

            if (box.contains(start)) hitPos = start;
            else
            {
                if (hit.isEmpty()) continue;
                hitPos = hit.get();
            }

            var distanceSqr = start.distanceToSqr(hitPos);

            if (distanceSqr >= nearestDistance) {
                continue;
            }

            nearestDistance = distanceSqr;

            var width = box.getXsize();
            var height = box.getYsize();
            var depth = box.getZsize();

            var u = width == 0.0 ? 0.0 : Math.clamp((hitPos.x - box.minX) / width, 0.0, 1.0);
            var v = height == 0.0 ? 0.0 : Math.clamp((hitPos.y - box.minY) / height, 0.0, 1.0);
            var w = depth == 0.0 ? 0.0 : Math.clamp((hitPos.z - box.minZ) / depth, 0.0, 1.0);

            nearest = new RaytraceResult(
                    clientEntity, new Vector(hitPos.x, hitPos.y, hitPos.z), new Vector(u, v, w)
            );
        }

        return nearest;
    }
}