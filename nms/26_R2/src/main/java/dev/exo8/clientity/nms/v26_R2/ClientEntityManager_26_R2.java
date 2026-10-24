package dev.exo8.clientity.nms.v26_R2;

import dev.exo8.clientity.api.entity.ClientEntity;
import dev.exo8.clientity.api.entity.ClientEntityManager;
import net.minecraft.world.entity.EntitySpawnReason;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ClientEntityManager_26_R2 implements ClientEntityManager
{
    private final Map<Integer, ClientEntity<?>> entities = new HashMap<>();

    private static final Map<Class<? extends Entity>, EntityType> ENTITY_TYPES = Arrays.stream(EntityType.values())
            .filter(type -> type.getEntityClass() != null)
            .collect(Collectors.toUnmodifiableMap(EntityType::getEntityClass, Function.identity()));

    private final Plugin plugin;

    ClientEntityManager_26_R2(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> ClientEntity<T> create(Class<T> type, Location location)
    {
        var entity = createEntity(type, location);
        var clientEntity = (ClientEntity<T>) new ClientEntity_26_R2<>(plugin, this, entity);
        entities.put(clientEntity.id(), clientEntity);
        return clientEntity;
    }

    @Override
    public ClientEntity<?> get(int id) {
        return entities.get(id);
    }

    @Override
    public Collection<ClientEntity<?>> all() {
        return Collections.unmodifiableCollection(new HashSet<>(entities.values()));
    }

    public void remove(int id) {
        this.entities.remove(id);
    }

    private <T extends Entity> net.minecraft.world.entity.Entity createEntity(Class<T> type, Location location)
    {
        var world = location.getWorld();
        var level = ((CraftWorld) world).getHandle();
        var nmsType = CraftEntityType.bukkitToMinecraft(toBukkit(type));

        var entity = nmsType.create(level, EntitySpawnReason.EVENT);

        if (entity == null) {
            throw new IllegalStateException(
                    "Unable to create entity " + type.getName()
            );
        }

        entity.setPos(location.getX(), location.getY(), location.getZ());

        return entity;
    }

    private static EntityType toBukkit(Class<? extends Entity> type)
    {
        var entityType = ENTITY_TYPES.get(type);

        if (entityType == null) {
            throw new IllegalArgumentException("Unsupported entity class: " + type.getName());
        }

        return entityType;
    }
}