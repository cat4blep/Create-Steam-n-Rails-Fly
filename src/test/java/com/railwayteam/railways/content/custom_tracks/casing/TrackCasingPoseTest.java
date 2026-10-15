/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_tracks.casing;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackCasingPoseTest {
    @Test
    void scaledCurveSegmentKeepsSubmittedNormalsNormalized() {
        PoseStack.Pose segment = new PoseStack.Pose();
        segment.scale(1, 1, 2.1f);
        PoseStack stack = new PoseStack();

        TrackCasingLayout.applySegmentTransform(stack, segment);

        Vector3f normal = stack.last().transformNormal(0, 0, 1, new Vector3f());
        assertEquals(1, normal.length(), 0.000001f,
            "Nonuniform curve scale must mark the normal matrix for normalization");
        assertEquals(1, normal.z(), 0.000001f);
    }

    @Test
    void segmentCompositionPreservesParentCoordinatesAndInverseTransposeNormals() {
        PoseStack stack = new PoseStack();
        stack.translate(13, -4, 27);
        stack.mulPose(new Quaternionf().rotateY(0.7f));
        stack.scale(1.4f, 0.8f, 1);
        PoseStack.Pose segment = new PoseStack.Pose();
        segment.translate(2, 3, 4);
        segment.rotate(new Quaternionf().rotateX(0.4f));
        segment.scale(1, 1, 2.1f);
        Matrix4f originalSegment = new Matrix4f(segment.pose());
        Matrix4f expectedPose = new Matrix4f(stack.last().pose()).mul(segment.pose());
        Vector3f inputNormal = new Vector3f(1, 1, 1).normalize();
        Vector3f expectedNormal = new Matrix3f(expectedPose).invert().transpose()
            .transform(new Vector3f(inputNormal)).normalize();

        TrackCasingLayout.applySegmentTransform(stack, segment);

        assertTrue(stack.last().pose().equals(expectedPose, 0.000001f));
        assertTrue(segment.pose().equals(originalSegment), "Shared baked segment pose must stay unchanged");
        Vector3f actualNormal = stack.last().transformNormal(inputNormal, new Vector3f());
        assertEquals(expectedNormal.x(), actualNormal.x(), 0.000001f);
        assertEquals(expectedNormal.y(), actualNormal.y(), 0.000001f);
        assertEquals(expectedNormal.z(), actualNormal.z(), 0.000001f);
        assertEquals(1, actualNormal.length(), 0.000001f);
    }
}
