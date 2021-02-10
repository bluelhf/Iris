package net.coderbot.iris.util;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Conversions {
	private static final FloatBuffer buffer16 = BufferUtils.createFloatBuffer(16);

	public static Matrix4f matrix(net.minecraft.util.math.Matrix4f matrix) {
		matrix.writeToBuffer(buffer16);
		return new Matrix4f(buffer16);
	}
}
