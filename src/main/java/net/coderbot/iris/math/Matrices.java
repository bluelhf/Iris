package net.coderbot.iris.math;

import net.coderbot.iris.impl.Matrix4fAccess;
import net.minecraft.util.math.Matrix4f;

/**
 * A class for matrix related utility functions.
 */
public class Matrices {
	/**
	 * Produces a parallel (orthographic) projection
	 * matrix.
	 *
	 * @param l left value
	 * @param r right value
	 * @param b bottom value
	 * @param t top value
	 * @param n near value
	 * @param f far value
	 * @return A parallel projection matrix
	 */
	public static Matrix4f ortho(float l, float r, float b, float t, float n, float f) {
		return of(new float[] {
				/*^/---------------/^^/---------------/^^/-----------------/^^/----------------------/^*/
				/**/ 2 / (r - l),  /**/ 0,            /**/ 0,              /**/ - (r + l) / (r - l), /**/
				/*^/---------------/^^/---------------/^^/-----------------/^^/----------------------/^*/
				/**/ 0,            /**/ 2 / (t - b),  /**/ 0,              /**/ - (t + b) / (t - b), /**/
				/*^/---------------/^^/---------------/^^/-----------------/^^/----------------------/^*/
				/**/ 0,            /**/ 0,            /**/ - 2 / (f - n),  /**/ - (f + n) / (f - n), /**/
				/*^/---------------/^^/---------------/^^/-----------------/^^/----------------------/^*/
				/**/ 0,            /**/ 0,            /**/ 0,              /**/ 1                    /**/
				/*^/---------------/^^/---------------/^^/-----------------/^^/----------------------/^*/
		});
	}

	/**
	 * Produces a matrix with the specified values
	 * in the float array.
	 *
	 * @param m the matrix values
	 * @return A matrix with the provided values
	 */
	public static Matrix4f of(float[] m) {
		Matrix4f matrix = new Matrix4f();
		((Matrix4fAccess)(Object)matrix).setMatrix(m);
		return matrix;
	}

	/**
	 * Creates a float array from a Minecraft
	 * Matrix4f object.
	 *
	 * @param m a matrix
	 * @return A float array with the matrix values
	 */
	public static float[] of(Matrix4f m) {
		return ((Matrix4fAccess)(Object)m).getValues();
	}
}
