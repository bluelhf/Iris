package net.coderbot.iris.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface AccessorWorldRenderer {
	@Invoker(value = "setupTerrain")
	void doSetupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator);

	@Accessor("capturedFrustum")
	Frustum getCapturedFrustum();
}
