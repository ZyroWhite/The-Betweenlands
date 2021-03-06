package thebetweenlands.client.render.particle.entity;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thebetweenlands.client.render.particle.ParticleFactory;
import thebetweenlands.client.render.particle.ParticleTextureStitcher;
import thebetweenlands.client.render.particle.ParticleTextureStitcher.IParticleSpriteReceiver;

public class ParticleSimple extends Particle implements IParticleSpriteReceiver {
	private float startAlpha = 1.0F;

	public ParticleSimple(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int maxAge, float scale, boolean fade) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.particleMaxAge = maxAge;
		this.particleScale = scale;
	}

	@Override
	public void setAlphaF(float alpha) {
		super.setAlphaF(alpha);
		this.startAlpha = alpha;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.particleAge > this.particleMaxAge - 40) {
			this.particleAlpha = (this.startAlpha * (this.particleMaxAge - this.particleAge) / 40.0F);
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	public static final class GenericFactory extends ParticleFactory<GenericFactory, ParticleSimple> {
		public GenericFactory(ResourceLocation texture) {
			super(ParticleSimple.class, ParticleTextureStitcher.create(ParticleSimple.class, texture).setSplitAnimations(true));
		}

		@Override
		public ParticleSimple createParticle(ImmutableParticleArgs args) {
			return new ParticleSimple(args.world, args.x, args.y, args.z, args.motionX, args.motionY, args.motionZ, args.data.getInt(0), args.scale, args.data.getBool(1));
		}

		@Override
		protected void setBaseArguments(ParticleArgs<?> args) {
			args.withData(80, false);
		}
	}
}
