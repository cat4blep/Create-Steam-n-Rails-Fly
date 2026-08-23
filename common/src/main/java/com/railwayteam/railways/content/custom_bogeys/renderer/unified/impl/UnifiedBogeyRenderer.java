/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.custom_bogeys.renderer.unified.impl;

import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplayHolder;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyRenderer;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class UnifiedBogeyRenderer implements BogeyRenderer, BogeyDisplayHolder {
    private final Couple<Renderer> renderers;
    private final @Nullable BogeyRenderer customRenderer;

    public UnifiedBogeyRenderer(BogeyDisplay.Factory factory) {
        this.renderers = Couple.createWithContext(inContraption -> Renderer.create(factory, inContraption));
        this.customRenderer = factory.createCustomRenderer();
    }
    public void runWithDisplay(Consumer<BogeyDisplay> consumer) {
        consumer.accept(renderers.getFirst().display);
        consumer.accept(renderers.getSecond().display);

        if (customRenderer instanceof BogeyDisplayHolder customDisplayHolder) {
            customDisplayHolder.runWithDisplay(consumer);
        }
    }
    @Override
    public BogeyRenderState getRenderData(@Nullable CompoundTag bogeyData, float wheelAngle, float partialTick,
                                          int packedLight, @Nullable CardinalLighting cardinalLighting,
                                          boolean inContraption) {
        final Renderer renderer = renderers.get(inContraption);
        if (bogeyData == null)
            bogeyData = new CompoundTag();
        renderer.reset();
        renderer.display.update(bogeyData, wheelAngle);
        BogeyRenderState customState = customRenderer == null ? null
            : customRenderer.getRenderData(bogeyData, wheelAngle, partialTick, packedLight, cardinalLighting,
                inContraption);

        List<SuperByteBufferRenderState> elementStates = new ArrayList<>(renderer.allElements.size());
        for (RenderedElement.Single element : renderer.singleElements) {
            elementStates.add(prepareElement(element.model(), element.element(), packedLight, cardinalLighting)
                .extractRenderState());
        }

        for (RenderedElement.Multiple elements : renderer.multipleElements) {
            for (RenderedElement element : elements.elements()) {
                elementStates.add(prepareElement(elements.model(), element, packedLight, cardinalLighting)
                    .extractRenderState());
            }
        }

        for (RenderedElement.Scrolling element : renderer.scrollingElements) {
            float spriteSize = element.entry.getTarget().getV1() - element.entry.getTarget().getV0();
            float scrollV = element.shiftV - Mth.floor(element.shiftV);
            scrollV = scrollV * spriteSize * 0.5f;
            elementStates.add(prepareElement(element.model, element.element, packedLight, cardinalLighting)
                .shiftUVScrolling(element.entry, scrollV)
                .extractRenderState());
        }

        return new RenderState(List.copyOf(elementStates), customState);
    }

    private static SuperByteBuffer prepareElement(PartialModel model, RenderedElement element, int packedLight,
                                                  @Nullable CardinalLighting cardinalLighting) {
        return CachedBuffers.partial(model, Blocks.AIR.defaultBlockState())
            .mulPose(element.pose)
            .mulNormal(new Matrix3f(element.pose))
            .cardinalLighting(cardinalLighting)
            .light(packedLight)
            .overlay(OverlayTexture.NO_OVERLAY);
    }

    private record RenderState(List<SuperByteBufferRenderState> elementStates,
                               @Nullable BogeyRenderState customState) implements BogeyRenderState {

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector queue) {
            poseStack.pushPose();
            poseStack.translate(0, -1.5 - 1 / 128f, 0);
            for (SuperByteBufferRenderState elementState : elementStates)
                elementState.submit(poseStack, queue);
            poseStack.popPose();

            if (customState != null)
                customState.submit(poseStack, queue);
        }
    }

    private record Renderer(
        BogeyDisplay display,
        List<RenderedElement.Single> singleElements,
        List<RenderedElement.Multiple> multipleElements,
        List<RenderedElement.Scrolling> scrollingElements,
        List<RenderedElement> allElements
    ) {
        private void reset() {
            allElements.forEach(element -> element.pose.identity());
            scrollingElements.forEach(element -> element.shiftV = 0);
        }

        private static Renderer create(BogeyDisplay.Factory factory, boolean inContraption) {
            ArrayList<RenderedElement.Single> singleElements = new ArrayList<>();
            ArrayList<RenderedElement.Multiple> multipleElements = new ArrayList<>();
            ArrayList<RenderedElement.Scrolling> scrollingElements = new ArrayList<>();

            var prov = new RenderedElementProvider(singleElements, multipleElements, scrollingElements);
            BogeyDisplay display = factory.create(prov, inContraption);
            prov.freeze();

            List<RenderedElement> allElements = new ArrayList<>();
            singleElements.forEach(s -> allElements.add(s.element()));
            multipleElements.forEach(m -> Collections.addAll(allElements, m.elements()));
            scrollingElements.forEach(s -> allElements.add(s.element));

            return new Renderer(display, singleElements, multipleElements, scrollingElements, allElements);
        }
    }
}
