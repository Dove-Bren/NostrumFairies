package com.smanzana.nostrumfairies.entity;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.network.IPacket;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Just a more generalized version of EntityTippedArrow lol. Stupid vanilla.
 * @author Skyler
 *
 */
public class EntityArrowEx extends ArrowEntity {
	
	private static final Predicate<Entity> ARROW_TARGETS_VANILLA = EntityPredicates.NOT_SPECTATING.and(EntityPredicates.IS_ALIVE).and(new Predicate<Entity>() {
		@Override
		public boolean test(@Nullable Entity p_apply_1_) {
			return p_apply_1_.canBeCollidedWith();
		}
	});
	
	public static final String ID = "nostrum_custom_arrow";
	
	protected Predicate<? super Entity> filter;
	
	public EntityArrowEx(EntityType<? extends EntityArrowEx> type, World worldIn) {
		super(type, worldIn);
		filter = ARROW_TARGETS_VANILLA;
	}
	
	public EntityArrowEx(World worldIn) {
		this(FairyEntities.ArrowEx, worldIn);
	}

	public EntityArrowEx(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y , z);
	}

	public EntityArrowEx(World worldIn, LivingEntity shooter) {
		this(worldIn);
		this.setShooter(shooter);
		
		// This is baked in to parent versions that we can't call
		this.setPosition(shooter.posX, shooter.posY + (double)shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ);
	}
	
	public void setFilter(Predicate<Entity> filter) {
		this.filter = filter;
	}
	
	@Nullable
	@Override
	protected EntityRayTraceResult func_213866_a(Vector3d start, Vector3d end) { //findEntityOnPath
		return ProjectileHelper.func_221271_a(this.world, this, start, end, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (ent) -> {
			return this.filter.test(ent);
		});
	}
	
	@Override
	public IPacket<?> createSpawnPacket() {
		// Have to override and use forge to use with non-living Entity types even though parent defines
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
