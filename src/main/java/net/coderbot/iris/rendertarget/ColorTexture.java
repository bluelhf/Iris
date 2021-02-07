package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.nio.ByteBuffer;

public class ColorTexture extends GlResource {
	public ColorTexture(int width, int height) {
		super(GL11C.glGenTextures());
		GlStateManager.bindTexture(getGlId());

		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		resize(width, height);

		GlStateManager.bindTexture(0);
	}

	void resize(int width, int height) {
		GlStateManager.bindTexture(getGlId());

		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_COLOR, width, height, 0, GL11C.GL_COLOR, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		GlStateManager.bindTexture(0);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GL11C.glDeleteTextures(getGlId());
	}
}
