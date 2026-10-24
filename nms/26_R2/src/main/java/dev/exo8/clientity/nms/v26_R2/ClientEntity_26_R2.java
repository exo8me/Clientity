package dev.exo8.clientity.nms.v26_R2;

import com.mojang.datafixers.util.Pair;
import dev.exo8.clientity.nms.AbstractClientEntity;
import net.kyori.adventure.util.TriState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static net.minecraft.world.entity.MoverType.SELF;
import static net.minecraft.world.phys.Vec3.ZERO;
import static net.minecraft.world.scores.Team.CollisionRule.ALWAYS;
import static net.minecraft.world.scores.Team.CollisionRule.NEVER;

final class ClientEntity_26_R2<T extends Entity> extends AbstractClientEntity<Packet<?>, T>
{
    private final net.minecraft.world.entity.Entity entity;

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    private List<Integer> passengers = List.of();

    ClientEntity_26_R2(Plugin plugin, ClientEntityManager_26_R2 clientEntityManager, net.minecraft.world.entity.Entity entity)
    {
        super(plugin, clientEntityManager, new PacketSender_26_R2());

        this.entity = entity;

        entity.valid = true;
    }

    @Override
    protected void tickInternal()
    {
        if (entity.isRemoved()) {
            return;
        }

        if (entity instanceof LivingEntity living && living.isAlive())
        {
            restorePosition();
            return;
        }

        var velocity = entity.getDeltaMovement();

        if (velocity.lengthSqr() == 0.0) return;

        entity.move(SELF, velocity);

        entity.setDeltaMovement(ZERO);

        savePosition();
    }

    private void restorePosition()
    {
        entity.setPos(x, y, z);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYHeadRot(yaw);
    }

    private void savePosition()
    {
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
        yaw = entity.getYRot();
        pitch = entity.getXRot();
    }

    @Override
    protected List<Packet<?>> showPackets()
    {
        return packets(
                entityPacket(),
                metadataPacket(),
                attributePacket(),
                equipmentPacket(),
                effectPackets(),
                passengerPacket(),
                collisionPackets(),
                waypointPackets()
        );
    }

    @Override
    protected List<Packet<?>> destroyPackets() {
        return packets(
                removeEntityPacket(),
                removeWaypointPacket()
        );
    }

    @Override
    protected List<Packet<?>> updatePackets()
    {
        return packets(
                positionPacket(),
                rotationPacket(),
                motionPacket(),
                metadataPacket(),
                attributePacket(),
                equipmentPacket(),
                effectPackets(),
                passengerPacket(),
                collisionPackets(),
                waypointPackets()
        );
    }

    @Override
    public int id() {
        return entity.getId();
    }

    @Override
    public UUID uuid() {
        return entity.getUUID();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T bukkit() {
        return (T) entity.getBukkitEntity();
    }

    @Override
    public Object handle() {
        return entity;
    }

    @Override
    public void remove()
    {
        for (var uuid : new HashSet<>(showedSet))
        {
            var player = plugin.getServer().getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                continue;
            }

            packetSender.send(player, destroyPackets());
        }

        showedSet.clear();
        viewerSet.clear();

        entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);

        ((ClientEntityManager_26_R2) clientEntityManager).remove(id());
    }

    private Packet<?> entityPacket()
    {
        return new ClientboundAddEntityPacket(
                entity.getId(),
                entity.getUUID(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                entity.getXRot(),
                entity.getYRot(),
                entity.getType(),
                0,
                entity.getDeltaMovement(),
                entity.getYHeadRot()
        );
    }

    private Packet<?> metadataPacket()
    {
        var data = entity.getEntityData();
        data.set(EntityDataHelper.DATA_SHARED_FLAGS_ID, getClientFlags(), true);
        data.set(EntityDataHelper.DATA_POSE, getClientPose(), true);
        return new ClientboundSetEntityDataPacket(entity.getId(), data.packAll());
    }

    private Packet<?> attributePacket()
    {
        if (!(entity instanceof LivingEntity living)) return null;
        var attributes = living.getAttributes().getSyncableAttributes();
        return attributes.isEmpty() ? null :
                new ClientboundUpdateAttributesPacket(entity.getId(), attributes);
    }

    private Packet<?> equipmentPacket()
    {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }

        var entries = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

        for (var slot : EquipmentSlot.values())
        {
            var current = living.getItemBySlot(slot);
            if (ItemStack.matches(current, ItemStack.EMPTY)) continue;
            entries.add(Pair.of(slot, current.copy()));
        }

        return entries.isEmpty() ? null : new ClientboundSetEquipmentPacket(entity.getId(), entries);
    }

    private List<Packet<?>> effectPackets()
    {
        if (!(entity instanceof LivingEntity living)) {
            return List.of();
        }

        var effects = living.getActiveEffects();

        if (effects.isEmpty()) {
            return List.of();
        }

        var packets = new ArrayList<Packet<?>>(effects.size());

        for (var effect : effects)
        {
            packets.add(
                    new ClientboundUpdateMobEffectPacket(
                            entity.getId(),
                            effect,
                            true
                    )
            );
        }

        return packets;
    }

    private Packet<?> passengerPacket()
    {
        var passengers = entity.getPassengers()
                .stream()
                .map(net.minecraft.world.entity.Entity::getId)
                .toList();

        if (this.passengers.equals(passengers)) {
            return null;
        }

        this.passengers = passengers;

        return null;
    }


    private List<Packet<?>> collisionPackets()
    {
        if (!(entity instanceof LivingEntity living)) {
            return List.of();
        }

        var rule = living.collides ? ALWAYS : NEVER;

        var team = new PlayerTeam(new Scoreboard(), "clientity_" + entity.getId());

        team.setCollisionRule(rule);
        team.getPlayers().add(entity.getStringUUID());

        return List.of(
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        );
    }

    private List<Packet<?>> waypointPackets()
    {
        if (!(entity instanceof LivingEntity living)) {
            return List.of();
        }

        var icon = living.waypointIcon().cloneAndAssignStyle(living);

        if (!icon.hasData()) {
            return List.of(removeWaypointPacket());
        }

        return List.of(
                ClientboundTrackedWaypointPacket.addWaypointPosition(
                        entity.getUUID(),
                        icon,
                        entity.blockPosition()
                ),
                ClientboundTrackedWaypointPacket.updateWaypointPosition(
                        entity.getUUID(), icon, entity.blockPosition()
                )
        );
    }

    private Packet<?> removeEntityPacket() {
        return new ClientboundRemoveEntitiesPacket(entity.getId());
    }

    private Packet<?> positionPacket()
    {
        var position = new PositionMoveRotation(
                new Vec3(entity.getX(), entity.getY(), entity.getZ()),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot()
        );

        return new ClientboundEntityPositionSyncPacket(entity.getId(), position, entity.onGround());
    }

    private Packet<?> rotationPacket() {
        return new ClientboundRotateHeadPacket(entity, (byte) (entity.getYHeadRot() * 256.0F / 360.0F));
    }

    private Packet<?> motionPacket() {
        return new ClientboundSetEntityMotionPacket(entity.getId(), entity.getDeltaMovement());
    }

    private Packet<?> removeWaypointPacket() {
        return ClientboundTrackedWaypointPacket.removeWaypoint(entity.getUUID());
    }

    private byte getClientFlags()
    {
        var flags = EntityDataHelper.getFlags(entity);

        flags = setFlag(flags, 0, entity.visualFire == TriState.TRUE);
        flags = setFlag(flags, 1, entity instanceof LivingEntity living && living.getSharedFlag(1));
        flags = setFlag(flags, 3, entity instanceof LivingEntity living && living.getSharedFlag(3));
        flags = setFlag(flags, 4, entity instanceof LivingEntity living && living.getSharedFlag(4));
        flags = setFlag(flags, 5, entity.isInvisible());
        flags = setFlag(flags, 6, entity.hasGlowingTag());
        flags = setFlag(flags, 7, entity instanceof LivingEntity living && living.getSharedFlag(7));

        return flags;
    }

    private Pose getClientPose()
    {
        if (entity.isSwimming()) return Pose.SWIMMING;
        if (entity.isCrouching()) return Pose.CROUCHING;

        if (!(entity instanceof LivingEntity living)) return Pose.STANDING;

        if (living.isFallFlying()) return Pose.FALL_FLYING;
        if (living.isSleeping()) return Pose.SLEEPING;
        if (living.isAutoSpinAttack()) return Pose.SPIN_ATTACK;
        if (living.isDeadOrDying()) return Pose.DYING;

        return Pose.STANDING;
    }

    private static byte setFlag(byte flags, int bit, boolean value) {
        return value ? (byte) (flags | (1 << bit)) : (byte) (flags & ~(1 << bit));
    }

    private static List<Packet<?>> packets(Object... packets)
    {
        var result = new ArrayList<Packet<?>>(packets.length);

        for (var packet : packets)
        {
            if (packet instanceof Packet<?> p)
            {
                result.add(p);
                continue;
            }

            if (packet instanceof Collection<?> collection) {
                for (var element : collection) {
                    if (element instanceof Packet<?> p) {
                        result.add(p);
                    }
                }
            }
        }

        return result;
    }
}