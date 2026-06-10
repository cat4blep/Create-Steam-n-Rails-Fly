package com.railwayteam.railways.content.conductor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class ConductorRenderModel extends EntityModel<ConductorRenderState> implements HeadedModel {
    private final ModelPart head;

    public ConductorRenderModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

    @Override
    public void setupAnim(ConductorRenderState state) {
        super.setupAnim(state);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
    }

    @Override
    public ModelPart getHead() {
        return head;
    }
}
