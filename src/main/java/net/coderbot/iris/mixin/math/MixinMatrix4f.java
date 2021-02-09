package net.coderbot.iris.mixin.math;

import net.coderbot.iris.impl.Matrix4fAccess;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class MixinMatrix4f implements Matrix4fAccess {
	@Shadow protected float a00;
	@Shadow protected float a01;
	@Shadow protected float a02;
	@Shadow protected float a03;
	@Shadow protected float a10;
	@Shadow protected float a11;
	@Shadow protected float a12;
	@Shadow protected float a13;
	@Shadow protected float a20;
	@Shadow protected float a21;
	@Shadow protected float a22;
	@Shadow protected float a23;
	@Shadow protected float a30;
	@Shadow protected float a31;
	@Shadow protected float a32;
	@Shadow protected float a33;

	@Override
	public void setMatrix(float[] m) {
		if(m.length != 16) return;
		this.a00 = m[0];
		this.a01 = m[1];
		this.a02 = m[2];
		this.a03 = m[3];
		this.a10 = m[4];
		this.a11 = m[5];
		this.a12 = m[6];
		this.a13 = m[7];
		this.a20 = m[8];
		this.a21 = m[9];
		this.a22 = m[10];
		this.a23 = m[11];
		this.a30 = m[12];
		this.a31 = m[13];
		this.a32 = m[14];
		this.a33 = m[15];
	}

	@Override
	public float[] getValues() {
		return new float[] {
				a00, a01, a02, a03,
				a10, a11, a12, a13,
				a20, a21, a22, a23,
				a30, a31, a32, a33
		};
	}
}
