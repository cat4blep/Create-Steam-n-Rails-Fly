package com.railwayteam.railways.content.conductor.vent.fabric;

import com.railwayteam.railways.content.conductor.vent.CopycatVentModel;
import net.minecraft.client.resources.model.BakedModel;

public class CopycatVentModelImpl extends CopycatVentModel {
    public CopycatVentModelImpl(BakedModel originalModel) {
        super(originalModel);
    }

    public static CopycatVentModel create(BakedModel bakedModel) {
        return new CopycatVentModelImpl(bakedModel);
    }
}
