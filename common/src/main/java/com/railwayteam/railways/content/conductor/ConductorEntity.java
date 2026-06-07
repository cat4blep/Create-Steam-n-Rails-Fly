package com.railwayteam.railways.content.conductor;

import com.mojang.authlib.GameProfile;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.registry.CREntities;
import com.zurrtum.create.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ConductorEntity extends AbstractGolem {
    public static final GameProfile FAKE_PLAYER_PROFILE =
        new GameProfile(UUID.fromString("00000000-0000-0000-0000-000000000001"), "[Railways Conductor]");
    public static final WorldAttached<Set<ConductorEntity>> WITH_TOOLBOXES = new WorldAttached<>(w -> new HashSet<>());
    public static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<BlockPos> BLOCK = SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BLOCK_POS);
    public static final EntityDataAccessor<Integer> JOB = SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> HOLDING_SCHEDULES = SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BOOLEAN);

    public int ventCooldown;
    private MountedToolbox toolbox;

    public enum Job {
        DEFAULT,
        REMOTE_CONTROL,
        SPY
    }

    public ConductorEntity(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes();
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, idFrom(defaultColor()));
        builder.define(BLOCK, BlockPos.ZERO);
        builder.define(JOB, Job.DEFAULT.ordinal());
        builder.define(HOLDING_SCHEDULES, false);
    }

    public static ConductorEntity spawn(Level level, BlockPos pos, ItemStack stack) {
        ConductorEntity entity = new ConductorEntity(CREntities.CONDUCTOR.get(), level);
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(entity);
        return entity;
    }

    public static DyeColor defaultColor() {
        return DyeColor.BROWN;
    }

    public static byte idFrom(DyeColor color) {
        return (byte) color.getId();
    }

    public static DyeColor colorFrom(byte id) {
        return DyeColor.byId(id);
    }

    public static boolean isPlayerDisguised(Player player) {
        return false;
    }

    public static boolean canSpyInteract(BlockState state) {
        return false;
    }

    public static boolean hasRecentlyDismounted(ServerPlayer player) {
        return false;
    }

    public Job getJob() {
        return Job.values()[Math.max(0, Math.min(Job.values().length - 1, getEntityData().get(JOB)))];
    }

    public boolean isPossessed() {
        return false;
    }

    public boolean isCarryingToolbox() {
        return toolbox != null;
    }

    public @Nullable MountedToolbox getToolbox() {
        return toolbox;
    }

    public MountedToolbox getOrCreateToolboxHolder() {
        if (toolbox == null)
            toolbox = new MountedToolbox(this, DyeColor.BROWN);
        return toolbox;
    }

    public boolean startViewing(ServerPlayer player) {
        return false;
    }

    public void stopViewing(ServerPlayer player) {
    }

    public void onSpyInteract(BlockPos pos) {
    }

    public void teleportToForce(double x, double y, double z) {
        teleportTo(x, y, z);
    }
}
