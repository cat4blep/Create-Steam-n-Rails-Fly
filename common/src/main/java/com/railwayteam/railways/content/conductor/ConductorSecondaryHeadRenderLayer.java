package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class ConductorSecondaryHeadRenderLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {
    public ConductorSecondaryHeadRenderLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
        if (state.secondaryHeadRenderState.isEmpty())
            return;

        poseStack.pushPose();
        getParentModel().getHead().translateAndRotate(poseStack);
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
        state.secondaryHeadRenderState.submit(poseStack, submitter, packedLight,
            LivingEntityRenderer.getOverlayCoords(state, 0), -1);
        poseStack.popPose();
    }
}
