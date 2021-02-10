package net.coderbot.iris.uniforms;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

import java.util.function.Supplier;

import net.coderbot.iris.gl.uniform.UniformHolder;

import net.coderbot.iris.util.Abstractions;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector3d;

/**
 * @see <a href="https://github.com/IrisShaders/ShaderDoc/blob/master/uniforms.md#celestial-bodies">Uniforms: Camera</a>
 */
public class CameraUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CameraUniforms() {
	}

	public static void addCameraUniforms(UniformHolder uniforms) {
		uniforms
			.uniform1f(ONCE, "near", () -> 0.05)
			.uniform1f(PER_FRAME, "far", CameraUniforms::getRenderDistanceInBlocks)
			.uniform3d(PER_FRAME, "cameraPosition", CameraUniforms::getCameraPosition)
			.uniform3d(PER_FRAME, "previousCameraPosition", new PreviousCameraPosition());
	}

	private static int getRenderDistanceInBlocks() {
		return client.options.viewDistance * 16;
	}

	private static Vector3d getCameraPosition() {
		return Abstractions.getClientCameraPos();
	}

	private static class PreviousCameraPosition implements Supplier<Vector3d> {
		private Vector3d previousCameraPosition = new Vector3d(0.0, 0.0, 0.0);

		@Override
		public Vector3d get() {
			Vector3d previous = previousCameraPosition;

			previousCameraPosition = getCameraPosition();

			return previous;
		}
	}
}
