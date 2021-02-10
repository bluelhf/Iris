package net.coderbot.iris.gl.uniform;

import java.util.function.Supplier;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL21;

public class Vector4Uniform extends Uniform {
	private final Vector4f cachedValue;
	private final Supplier<Vector4f> value;

	Vector4Uniform(int location, Supplier<Vector4f> value) {
		super(location);

		this.cachedValue = new Vector4f();
		this.value = value;
	}

	@Override
	public void update() {
		Vector4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z(), newValue.w());
			GL21.glUniform4f(location, cachedValue.x(), cachedValue.y(), cachedValue.z(), cachedValue.w());
		}
	}
}
