package dev.exo8.clientity.nms.v26_R2;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;

final class EntityDataHelper
{
    static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    static EntityDataAccessor<Pose> DATA_POSE;

    static
    {
        try
        {
            var DATA_SHARED_FLAGS_ID_FIELD = Entity.class.getDeclaredField("DATA_SHARED_FLAGS_ID");
            DATA_SHARED_FLAGS_ID_FIELD.setAccessible(true);
            DATA_SHARED_FLAGS_ID = (EntityDataAccessor<Byte>) DATA_SHARED_FLAGS_ID_FIELD.get(null);

            var DATA_POSE_FIELD = Entity.class.getDeclaredField("DATA_POSE");
            DATA_POSE_FIELD.setAccessible(true);
            DATA_POSE = (EntityDataAccessor<Pose>) DATA_POSE_FIELD.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static byte getFlags(Entity entity) {
        return entity.getEntityData().get(DATA_SHARED_FLAGS_ID);
    }
}