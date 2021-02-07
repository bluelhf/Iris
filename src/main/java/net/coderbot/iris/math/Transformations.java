package net.coderbot.iris.math;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix4f;

public final class Transformations {

	public static void toSunAngle(Matrix4f matrix, float skyAngle) {
		//matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
		matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(skyAngle * 360.0F));
		//matrix.multiply(Matrix4f.translate(0, -100, 0));
	}
}
