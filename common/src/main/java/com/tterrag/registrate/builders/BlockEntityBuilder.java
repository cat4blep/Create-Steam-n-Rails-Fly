package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {
    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }

    private final BlockEntityFactory<T> factory;
    private Block[] validBlocks = new Block[0];

    public BlockEntityBuilder(Registrate owner, String name, BlockEntityFactory<T> factory) {
        super(owner, name, null);
        this.factory = factory;
    }

    public BlockEntityBuilder<T, P> validBlocks(com.tterrag.registrate.util.entry.BlockEntry<?>... entries) {
        validBlocks = java.util.Arrays.stream(entries).map(com.tterrag.registrate.util.entry.BlockEntry::get).toArray(Block[]::new);
        return this;
    }

    public BlockEntityBuilder<T, P> renderer(java.util.function.Supplier<?> renderer) {
        return this;
    }

    public BlockEntityEntry<T> register() {
        BlockEntityType<T> type = createType();
        owner.registerVanilla(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, type);
        return new BlockEntityEntry<>(owner.id(name), type);
    }

    @SuppressWarnings("unchecked")
    private BlockEntityType<T> createType() {
        try {
            BlockEntityType<?>[] typeRef = new BlockEntityType<?>[1];
            Class<?> supplierClass = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
            Object supplier = java.lang.reflect.Proxy.newProxyInstance(
                supplierClass.getClassLoader(),
                new Class<?>[]{supplierClass},
                (proxy, method, args) -> factory.create(typeRef[0], (BlockPos) args[0], (BlockState) args[1])
            );
            java.lang.reflect.Constructor<BlockEntityType> constructor = BlockEntityType.class.getDeclaredConstructor(supplierClass, java.util.Set.class);
            constructor.setAccessible(true);
            BlockEntityType<T> type = (BlockEntityType<T>) constructor.newInstance(supplier, java.util.Set.of(validBlocks));
            typeRef[0] = type;
            return type;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create block entity type " + owner.id(name), e);
        }
    }
}
