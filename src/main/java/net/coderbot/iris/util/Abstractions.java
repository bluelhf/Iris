package net.coderbot.iris.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class Abstractions {
	private static Vec3d prevCamPos = null;
	private static Vector3d camPosCache = getClientCameraPos();

	public static Vector3d getClientCameraPos() {
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		if(!prevCamPos.equals(camera.getPos())) {
			prevCamPos = camera.getPos();
			camPosCache = new Vector3d(camera.getPos().x, camera.getPos().y, camera.getPos().z);
		}
		return camPosCache;
	}
}
