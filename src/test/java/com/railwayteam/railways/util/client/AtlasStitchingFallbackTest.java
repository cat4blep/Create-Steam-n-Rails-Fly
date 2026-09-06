/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.util.client;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AtlasStitchingFallbackTest {
    private record Sprite(int width, int height, Identifier name) implements Stitcher.Entry {}

    private static Sprite sprite(String namespace, int size, int index) {
        return new Sprite(size, size, Identifier.fromNamespaceAndPath(namespace, "block/test/" + index));
    }

    private static Stitcher<Sprite> pack(List<Sprite> sprites, int limit, int mip, int anisotropyBit) {
        Stitcher<Sprite> stitcher = new Stitcher<>(limit, limit, mip, anisotropyBit);
        sprites.forEach(stitcher::registerSprite);
        stitcher.stitch();
        return stitcher;
    }

    @Test
    void fitsThousandsOfConnectedTexturesWithAnisotropicPaddingWithoutDroppingSprites() {
        // 26.2 adds a 128-pixel border to each side at mip 4 / anisotropy 16x.
        // This reproduces an overflow using vanilla's real packing algorithm,
        // independently of the installed GPU and without allocating GPU images.
        List<Sprite> sprites = IntStream.range(0, 12_000).mapToObj(i -> sprite("railways", 16, i)).toList();
        assertThrows(StitcherException.class, () -> pack(sprites, 16_384, 4, 4));
        var result = AtlasStitchingFallback.stitch(TextureAtlas.LOCATION_BLOCKS, sprites, 4,
            mip -> pack(sprites, 16_384, mip, 4));
        assertTrue(result.mipLevel() < 4);
        assertTrue(result.value().getWidth() <= 16_384);
        assertTrue(result.value().getHeight() <= 16_384);
        HashSet<Identifier> packed = new HashSet<>();
        result.value().gatherSprites((entry, x, y, padding) -> {
            assertEquals(16, entry.width());
            assertEquals(16, entry.height());
            assertTrue(x >= 0 && y >= 0);
            assertTrue(x + entry.width() + 2 * padding <= result.value().getWidth());
            assertTrue(y + entry.height() + 2 * padding <= result.value().getHeight());
            assertTrue(packed.add(entry.name()), "sprite packed twice");
        });
        assertEquals(sprites.size(), packed.size());
    }

    @Test
    void leavesSuccessfulPackingAndRequestedMipLevelAlone() {
        AtomicInteger attempts = new AtomicInteger();
        Object expected = new Object();
        var result = AtlasStitchingFallback.stitch(TextureAtlas.LOCATION_BLOCKS,
            List.of(sprite("railways", 16, 0)), 4, mip -> {
                attempts.incrementAndGet();
                assertEquals(4, mip);
                return expected;
            });
        assertSame(expected, result.value());
        assertEquals(4, result.mipLevel());
        assertEquals(1, attempts.get());
    }

    @Test
    void retriesTheReportedExceptionProducedByVanillaSpriteLoader() {
        SharedConstants.tryDetectVersion();
        List<Sprite> sprites = List.of(sprite("railways", 16, 0));
        var result = AtlasStitchingFallback.stitch(TextureAtlas.LOCATION_BLOCKS, sprites, 4, mip -> {
            try {
                return pack(sprites, 128, mip, 4);
            } catch (StitcherException error) {
                throw new ReportedException(CrashReport.forThrowable(error, "Stitching"));
            }
        });
        assertEquals(2, result.mipLevel());
    }

    @Test
    void propagatesAnAtlasThatCannotFitEvenAtMipZero() {
        List<Sprite> sprites = List.of(sprite("railways", 256, 0));
        List<Integer> attempts = new ArrayList<>();
        assertThrows(StitcherException.class, () -> AtlasStitchingFallback.stitch(
            TextureAtlas.LOCATION_BLOCKS, sprites, 4, mip -> {
                attempts.add(mip);
                return pack(sprites, 128, mip, 4);
            }));
        assertEquals(List.of(4, 3, 2, 1, 0), attempts);
    }

    @Test
    void doesNotRetryOtherAtlasesOrAtlasesWithoutRailwaysSprites() {
        for (String namespace : List.of("minecraft", "railways")) {
            Identifier atlas = namespace.equals("minecraft") ? TextureAtlas.LOCATION_BLOCKS
                : Identifier.withDefaultNamespace("textures/atlas/items.png");
            List<Sprite> sprites = List.of(sprite(namespace, 256, 0));
            AtomicInteger attempts = new AtomicInteger();
            assertThrows(StitcherException.class, () -> AtlasStitchingFallback.stitch(atlas, sprites, 4, mip -> {
                attempts.incrementAndGet();
                return pack(sprites, 128, mip, 4);
            }));
            assertEquals(1, attempts.get());
        }
    }

    @Test
    void doesNotHideUnrelatedReloadFailures() {
        IllegalStateException expected = new IllegalStateException("broken model");
        AtomicInteger attempts = new AtomicInteger();
        assertSame(expected, assertThrows(IllegalStateException.class, () -> AtlasStitchingFallback.stitch(
            TextureAtlas.LOCATION_BLOCKS, List.of(sprite("railways", 16, 0)), 4, mip -> {
                attempts.incrementAndGet();
                throw expected;
            })));
        assertEquals(1, attempts.get());
    }
}
