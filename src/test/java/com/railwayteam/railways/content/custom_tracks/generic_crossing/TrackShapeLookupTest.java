package com.railwayteam.railways.content.custom_tracks.generic_crossing;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.track.TrackShape;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackShapeLookupTest {
    @Test
    void crossingDirectionsKeepTheirOriginalMaterialsInBothPlacementOrders() {
        TrackShape[][] crossings = {
            {TrackShape.XO, TrackShape.ZO, TrackShape.CR_O},
            {TrackShape.PD, TrackShape.ND, TrackShape.CR_D},
            {TrackShape.XO, TrackShape.PD, TrackShape.CR_PDX},
            {TrackShape.XO, TrackShape.ND, TrackShape.CR_NDX},
            {TrackShape.ZO, TrackShape.PD, TrackShape.CR_PDZ},
            {TrackShape.ZO, TrackShape.ND, TrackShape.CR_NDZ}
        };

        for (TrackShape[] crossing : crossings) {
            assertPlacementOrder(crossing[0], crossing[1], crossing[2]);
            assertPlacementOrder(crossing[1], crossing[0], crossing[2]);
        }
    }

    @Test
    void slopesPortalsAndExistingJunctionsCannotBecomeTwoPieceCrossings() {
        for (TrackShape shape : TrackShape.values()) {
            if (shape == TrackShape.XO || shape == TrackShape.ZO || shape == TrackShape.PD || shape == TrackShape.ND)
                continue;
            assertNull(TrackShapeLookup.getMerged(shape, TrackShape.XO), shape.name());
            assertNull(TrackShapeLookup.getMerged(TrackShape.XO, shape), shape.name());
        }
        assertNull(TrackShapeLookup.getMerged(TrackShape.XO, TrackShape.XO));
    }

    private static void assertPlacementOrder(TrackShape existing, TrackShape placed, TrackShape crossing) {
        Pair<TrackShape, Boolean> merged = TrackShapeLookup.getMerged(existing, placed);
        assertNotNull(merged);
        assertEquals(crossing, merged.getFirst());
        Couple<TrackShape> directions = TrackShapeLookup.getUnmerged(crossing);
        assertNotNull(directions);
        boolean flipped = merged.getSecond();
        assertEquals(existing, directions.get(!flipped), "The original material must stay on its original direction");
        assertEquals(placed, directions.get(flipped), "The added material must stay on the added direction");
    }
}
