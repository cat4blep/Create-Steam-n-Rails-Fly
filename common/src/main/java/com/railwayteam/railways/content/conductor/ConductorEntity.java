package com.railwayteam.railways.content.conductor;

import com.mojang.authlib.GameProfile;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.util.EntityUtils;
import com.railwayteam.railways.util.packet.SetCameraViewPacket;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.catnip.data.WorldAttached;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private final List<ItemStack> heldSchedules = new ArrayList<>();
    private WeakReference<ServerPlayer> currentlyViewing = new WeakReference<>(null);
    private int initialChunkLoadingDistance;
    private boolean hasSentChunks;
    public SectionPos oldSectionPos;

    public static final Set<UUID> RECENTLY_DISMOUNTED_PLAYERS = new HashSet<>();

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
        if (stack.getItem() instanceof ConductorCapItem cap)
            entity.setColor(cap.color);
        entity.setItemSlot(EquipmentSlot.HEAD, stack.copyWithCount(1));
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(entity);
        return entity;
    }

    public static DyeColor defaultColor() {
        return DyeColor.BLUE;
    }

    public static byte idFrom(DyeColor color) {
        return (byte) color.getId();
    }

    public static DyeColor colorFrom(byte id) {
        return DyeColor.byId(id);
    }

    public static boolean isPlayerDisguised(Player player) {
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (headStack.isEmpty() || !CRTags.AllItemTags.CONDUCTOR_CAPS.matches(headStack))
            return false;

        String hoverName = headStack.getHoverName().getString();
        return hoverName.startsWith("[sus]") || hoverName.equals("sus");
    }

    public static boolean canSpyInteract(BlockState state) {
        return state.is(BlockTags.BUTTONS)
            || state.is(BlockTags.TRAPDOORS)
            || state.getBlock() instanceof LeverBlock
            || state.getBlock() instanceof VentBlock
            || CRTags.AllBlockTags.CONDUCTOR_SPY_USABLE.matches(state);
    }

    public static boolean hasRecentlyDismounted(ServerPlayer player) {
        return RECENTLY_DISMOUNTED_PLAYERS.remove(player.getUUID());
    }

    public Job getJob() {
        return Job.values()[Math.max(0, Math.min(Job.values().length - 1, getEntityData().get(JOB)))];
    }

    public DyeColor getColor() {
        return colorFrom(getEntityData().get(COLOR));
    }

    public void setColor(DyeColor color) {
        getEntityData().set(COLOR, idFrom(color));
    }

    public void setJob(Job job) {
        getEntityData().set(JOB, job.ordinal());
    }

    public boolean isPossessed() {
        ServerPlayer player = currentlyViewing.get();
        return player != null && player.isAlive() && player.getCamera() == this;
    }

    public ItemStack getSecondaryHeadStack() {
        return ItemStack.EMPTY;
    }

    public boolean isCorrectEngineerCap(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof ConductorCapItem cap && cap.color == getColor();
    }

    public List<ItemStack> getHeldSchedules() {
        return heldSchedules;
    }

    public boolean isHoldingSchedules() {
        return heldSchedules.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public boolean isHoldingSchedulesClient() {
        return getEntityData().get(HOLDING_SCHEDULES);
    }

    public boolean isCarryingToolbox() {
        return toolbox != null;
    }

    public @Nullable MountedToolbox getToolbox() {
        return toolbox;
    }

    public MountedToolbox getOrCreateToolboxHolder() {
        if (toolbox == null)
            setToolbox(new MountedToolbox(this, DyeColor.BROWN));
        return toolbox;
    }

    public void setToolbox(@Nullable MountedToolbox toolbox) {
        if (this.toolbox != null)
            WITH_TOOLBOXES.get(level()).remove(this);
        this.toolbox = toolbox;
        if (toolbox != null)
            WITH_TOOLBOXES.get(level()).add(this);
    }

    public ItemStack getToolboxDisplayStack() {
        return toolbox == null ? ItemStack.EMPTY : toolbox.getDisplayStack();
    }

    public boolean equipToolbox(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(AllTags.AllItemTags.TOOLBOXES.tag))
            return false;
        DyeColor color = DyeColor.BROWN;
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ToolboxBlock toolboxBlock)
            color = toolboxBlock.getColor();
        MountedToolbox mounted = new MountedToolbox(this, color);
        mounted.readFromItem(stack);
        setToolbox(mounted);
        return true;
    }

    public ItemStack unequipToolbox() {
        if (toolbox == null)
            return ItemStack.EMPTY;
        ItemStack stack = toolbox.getCloneItemStack();
        toolbox.unequipTracked();
        setToolbox(null);
        return stack;
    }

    @Override
    public void tick() {
        SectionPos sectionPos = SectionPos.of(this);
        if (!sectionPos.equals(oldSectionPos))
            setHasSentChunks(false);

        super.tick();
        if (ventCooldown > 0)
            ventCooldown--;
        if (level().isClientSide())
            ConductorPossessionController.tryUpdatePossession(this);
        else if (level() instanceof ServerLevel serverLevel && isPossessed()) {
            ServerPlayer player = currentlyViewing.get();
            int viewDistance = player.level().getServer().getPlayerList().getViewDistance();
            ChunkPos chunkPos = new ChunkPos(blockPosition());
            for (int x = chunkPos.x - viewDistance; x <= chunkPos.x + viewDistance; x++) {
                for (int z = chunkPos.z - viewDistance; z <= chunkPos.z + viewDistance; z++) {
                    ChunkPos ticketPos = new ChunkPos(x, z);
                    serverLevel.getChunkSource().addTicketWithRadius(TicketType.FORCED, ticketPos, 3);
                }
            }
        }
        if (toolbox != null)
            toolbox.tick();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isCarryingToolbox()) {
            if (player.isShiftKeyDown()) {
                if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    ItemStack toolboxStack = unequipToolbox();
                    if (!toolboxStack.isEmpty() && !serverPlayer.getInventory().add(toolboxStack))
                        spawnAtLocation((ServerLevel) level(), toolboxStack);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer)
                MountedToolbox.openMenu(serverPlayer, toolbox);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(AllTags.AllItemTags.TOOLBOXES.tag)) {
            if (!level().isClientSide()) {
                equipToolbox(stack);
                if (!player.isCreative())
                    stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Color", getEntityData().get(COLOR));
        output.putString("Job", getJob().name());
        if (toolbox != null)
            output.store("Toolbox", CompoundTag.CODEC, toolbox.write(false));
        if (isHoldingSchedules()) {
            ValueOutput.TypedOutputList<ItemStack> schedules = output.list("HeldSchedules", ItemStack.OPTIONAL_CODEC);
            for (ItemStack stack : heldSchedules) {
                if (!stack.isEmpty())
                    schedules.add(stack);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setColor(colorFrom(input.getByteOr("Color", idFrom(defaultColor()))));
        input.getString("Job").ifPresent(jobName -> {
            try {
                setJob(Job.valueOf(jobName));
            } catch (IllegalArgumentException ignored) {
                setJob(Job.DEFAULT);
            }
        });
        input.read("Toolbox", CompoundTag.CODEC)
            .ifPresent(tag -> setToolbox(MountedToolbox.read(this, tag)));
        heldSchedules.clear();
        for (ItemStack stack : input.listOrEmpty("HeldSchedules", ItemStack.OPTIONAL_CODEC)) {
            if (!stack.isEmpty())
                heldSchedules.add(stack);
        }
        getEntityData().set(HOLDING_SCHEDULES, isHoldingSchedules());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        ItemStack stack = unequipToolbox();
        if (!stack.isEmpty())
            spawnAtLocation(level, stack);
        for (ItemStack schedule : heldSchedules) {
            if (!schedule.isEmpty())
                spawnAtLocation(level, schedule);
        }
        heldSchedules.clear();
        getEntityData().set(HOLDING_SCHEDULES, false);
    }

    public boolean startViewing(ServerPlayer player) {
        ServerPlayer current = currentlyViewing.get();
        if (current != null && current.getCamera() == this && current.isAlive() && current != player)
            return false;
        if (player.level() != level())
            return false;

        if (player.getCamera() instanceof ConductorEntity conductor)
            conductor.stopViewing(player);

        currentlyViewing = new WeakReference<>(player);
        oldSectionPos = null;
        setChunkLoadingDistance(player.level().getServer().getPlayerList().getViewDistance());
        setHasSentChunks(false);
        player.camera = this;
        CRPackets.PACKETS.sendTo(player, new SetCameraViewPacket(this));
        return true;
    }

    public void stopViewing(ServerPlayer player) {
        if (currentlyViewing.get() == player)
            currentlyViewing = new WeakReference<>(null);
        if (player.getCamera() == this) {
            player.camera = player;
            CRPackets.PACKETS.sendTo(player, new SetCameraViewPacket(player));
            RECENTLY_DISMOUNTED_PLAYERS.add(player.getUUID());
        }
    }

    public void onSpyInteract(BlockPos pos) {
        if (!(level() instanceof ServerLevel))
            return;
        ServerPlayer player = currentlyViewing.get();
        if (player == null || player.getCamera() != this)
            return;
        BlockState state = level().getBlockState(pos);
        if (!canSpyInteract(state))
            return;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        if (EntityUtils.handleUseEvent(player, InteractionHand.MAIN_HAND, hit))
            state.useWithoutItem(level(), player, hit);
    }

    public void teleportToForce(double x, double y, double z) {
        teleportTo(x, y, z);
    }

    public void turnView(double yaw, double pitch) {
        setYRot((float) (getYRot() + yaw * 0.15));
        setXRot(Mth.clamp((float) (getXRot() + pitch * 0.15), -90.0f, 90.0f));
    }

    public void setChunkLoadingDistance(int chunkLoadingDistance) {
        initialChunkLoadingDistance = chunkLoadingDistance;
    }

    public int getChunkLoadingDistance() {
        return initialChunkLoadingDistance;
    }

    public boolean hasSentChunks() {
        return hasSentChunks;
    }

    public void setHasSentChunks(boolean hasSentChunks) {
        this.hasSentChunks = hasSentChunks;
    }
}
