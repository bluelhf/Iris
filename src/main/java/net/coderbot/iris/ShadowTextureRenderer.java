package net.coderbot.iris;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.math.Matrices;
import net.coderbot.iris.mixin.AccessorWorldRenderer;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.*;

public class ShadowTextureRenderer {
	private static boolean RENDERING_SHADOW_TEX = false;
	private static final SystemTimeUniforms.FrameCounter COUNTER = new SystemTimeUniforms.FrameCounter();
	private static final Frustum EMPTY_FRUSTUM = new Frustum(new Matrix4f(), new Matrix4f()) {
		@Override
		public boolean isVisible(Box box) {
			return true;
		}
	};

	public static int SHADOW_TEX_WIDTH = 4096;

	public static void render(Camera camera, float tickDelta) {
		if(RENDERING_SHADOW_TEX) return;
		RENDERING_SHADOW_TEX = true;

		MatrixStack matrices = new MatrixStack();
		GlFramebuffer shadows = new GlFramebuffer();
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;
		WorldRenderer worldRenderer = client.worldRenderer;
		GameRenderer gameRenderer = client.gameRenderer;
		RenderTargets renderTargets = Iris.getPipeline().getRenderTargets();
		int shadowTex = renderTargets.getShadowTexture().getTextureId();
		int shadowNoTranslucents = renderTargets.getShadowTextureNoTranslucents().getTextureId();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		shadows.addDepthAttachment(shadowTex);
		//shadows.addDepthAttachment(shadowNoTranslucents);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		shadows.bind();
		ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_FRAMEBUFFER, ARBFramebufferObject.GL_DEPTH_ATTACHMENT, ARBInternalformatQuery2.GL_TEXTURE_2D, shadowTex, 0);
		GL32.glReadBuffer(GL11.GL_NONE);
		GL32.glDrawBuffer(GL11.GL_NONE);
		GbufferPrograms.push(GbufferProgram.SHADOW);
		GL32.glDepthMask(true);


		//gameRenderer.loadProjectionMatrix(projection);
		//CapturedRenderingState.INSTANCE.setShadowProjection(projection);
		//GL11.glPushMatrix();
		//RenderSystem.pushMatrix();
		GL32.glMatrixMode(GL11.GL_PROJECTION);
		Matrix4f projection = Matrices.ortho(-160, 160, -160, 160, 0.05f, 256);
		//float[] prev = new float[16];
		//GL32.glGetFloatv(GL11.GL_PROJECTION_MATRIX, prev);
		GL32.glLoadIdentity();
		//GL32.glMultMatrixf(Matrices.of(projection));
		GL32.glOrtho(-160, 160, -160, 160, 0.05f, 256);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, shadowNoTranslucents, 0);
		//matrices.push();
		//worldRenderer.render(matrices, client.getTickDelta(), world.getTime(), false, gameRenderer.getCamera(), gameRenderer, gameRenderer.getLightmapTextureManager(), matrices.peek().getModel());
		((AccessorWorldRenderer)worldRenderer).doSetupTerrain(camera, ((AccessorWorldRenderer)worldRenderer).getCapturedFrustum(), true, 0, false);
		GlStateManager.bindTexture(shadowTex);
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, ShadowTextureRenderer.SHADOW_TEX_WIDTH, ShadowTextureRenderer.SHADOW_TEX_WIDTH, 0);
		//GL11.glPopMatrix();
		//GlStateManager.bindTexture(shadowNoTranslucents);
		//GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, ShadowTextureRenderer.SHADOW_TEX_WIDTH, ShadowTextureRenderer.SHADOW_TEX_WIDTH, 0);
		CapturedRenderingState.INSTANCE.setShadowProjection(projection);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GbufferPrograms.pop(GbufferProgram.SHADOW);
		shadows.destroy();

		//GL32.glLoadMatrixf(prev);

		RENDERING_SHADOW_TEX = false;
		//gameRenderer.loadProjectionMatrix(gameRenderer.getBasicProjectionMatrix(camera, tickDelta, true));
	}

	static int stuff = 0;

	public static void imsRender(Camera camera, MatrixStack matrices, float tickDelta) {
		GlFramebuffer shadowframe = new GlFramebuffer();
		RenderTargets renderTargets = Iris.getPipeline().getRenderTargets();
		//Iris.getPipeline().beginWorldRender();
		Frustum frustum = new Frustum(new Matrix4f(), new Matrix4f()) {
			@Override
			public boolean isVisible(Box box) {
				return true;
			}
		};
		if(stuff == 0) {

			stuff = 1;
		}
		else
		{
			//Iris.logger.warn("pos" + CelestialUniforms.getShadowLightPosition());
			frustum.setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z + CelestialUniforms.getShadowLightPosition().getZ());
		}
		//camera.
		GlStateManager.disableCull();
		ClientWorld world = MinecraftClient.getInstance().world;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		shadowframe.addDepthAttachment(renderTargets.getShadowTexture().getTextureId());
		//shadowframe.bindo();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		shadowframe.bind();
		//MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_BACK);
		ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_FRAMEBUFFER, ARBFramebufferObject.GL_DEPTH_ATTACHMENT, ARBInternalformatQuery2.GL_TEXTURE_2D, renderTargets.getShadowTexture().getTextureId(), 0);
		GL32.glReadBuffer(GL11.GL_NONE);
		GL32.glDrawBuffer(GL11.GL_NONE);
		matrices.push();
		GbufferPrograms.push(GbufferProgram.SHADOW);
		matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
		matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(world.getSkyAngle(tickDelta) * 360.0F));
		matrices.translate(0, 100, 0);
		CapturedRenderingState.INSTANCE.setShadowModelView(matrices.peek().getModel());
		matrices.pop();
		//GL31.glActiveTexture(4);
		//GL31.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 0, 0, 4096, 4096, 0);
		//GL31.glActiveTexture(0);
		//ShadowProject.setupSecondCamera();

		//Matrix4f projectshadow = gameRenderer.getBasicProjectionMatrix(camera2, tickDelta, true);
		GL32.glDepthMask(true);


		//GL32.glViewport(0, 0, 4096, 4096);

		GL11.glPushMatrix();
		GL32.glMatrixMode(GL11.GL_PROJECTION);
		GL32.glLoadIdentity();
		GL32.glOrtho(-160, 160, -160, 160, 0.05D, 256.0D);
		//CapturedRenderingState.INSTANCE.setShadowProjection(Matrix4f.projectionMatrix(1, 1, 0, 0));
		WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
		MinecraftClient.getInstance().chunkCullingEnabled = false;

		((AccessorWorldRenderer) worldRenderer).doSetupTerrain(camera, frustum, false, 0, MinecraftClient.getInstance().player.isSpectator());
		GlStateManager.bindTexture(renderTargets.getShadowTexture().getTextureId());
		GL20C.glCopyTexImage2D(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_DEPTH_COMPONENT, 0, 0, 4096, 4096, 0);
		GL11.glPopMatrix();

		//renderer.renderWorld(tickDelta, 1, matrices);


		//worldRenderer.renderlayer



		//GL32.glOrtho((double)(-halfShadowMapPlane), (double)halfShadowMapPlane, (double)(-halfShadowMapPlane), (double)halfShadowMapPlane, 1D, 1.0D);
		//GL32.glOrtho(-1, 1, -1, 1, -1, 1);
		//MinecraftClient.getInstance().options.setPerspective(Perspective.FIRST_PERSON);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GbufferPrograms.pop(GbufferProgram.SHADOW);
		//ShaderPipeline.baseline.bind();

		shadowframe.destroy();
	}
}
