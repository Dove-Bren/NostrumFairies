package com.smanzana.nostrumfairies.entity.navigation;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.world.World;

public class PathNavigatorGroundFixed extends GroundPathNavigator {
	private int unused; // eval

	public PathNavigatorGroundFixed(MobEntity entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
	}
	
//	protected void clearStuckEntity() {
//		this.clearPath();
//	}
//
//	@Override
//	protected void checkForStuck(Vec3d positionVec3) {
//		//Field totalTicksField = ReflectionHelper.findField(PathNavigate.class, "totalTicks");
//		final int totalTicks = ObfuscationReflectionHelper.getPrivateValue(PathNavigate.class, this, "field_75510_g");//"totalTicks");
//		Field ticksAtLastPosField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_75520_h");//"ticksAtLastPos");
//		Field lastPosCheckField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_75521_i");//"lastPosCheck");
//		Field timeoutCachedNodeField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_188557_k");//"timeoutCachedNode");
//		Field timeoutLimitField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_188560_n");//"timeoutLimit");
//		Field timeoutTimerField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_188558_l");//"timeoutTimer");
//		Field lastTimeoutCheckField = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_188559_m");//"lastTimeoutCheck");
//		
//		try {
//			if (totalTicks - ticksAtLastPosField.getInt(this) > 100) {
//				if (positionVec3.squareDistanceTo((Vec3d) lastPosCheckField.get(this)) < 2.25D) {
//					this.clearPath();
//				}
//	
//				ticksAtLastPosField.setInt(this, totalTicks);
//				lastPosCheckField.set(this, positionVec3);
//			}
//	
//			if (this.currentPath != null && !this.currentPath.isFinished()) {
//				Vec3d vec3d = this.currentPath.getCurrentPos();
//	
//				if (vec3d.equals(timeoutCachedNodeField.get(this))) {
//					timeoutTimerField.setLong(this, timeoutTimerField.getLong(this) + (System.currentTimeMillis() - lastTimeoutCheckField.getLong(this)));
//					//timeoutTimer += System.currentTimeMillis() - lastTimeoutCheckField.getInt(this);
//				} else {
//					timeoutCachedNodeField.set(this, vec3d);
//					double d0 = positionVec3.distanceTo((Vec3d) timeoutCachedNodeField.get(this));
//					timeoutLimitField.setDouble(this, this.entity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.entity.getAIMoveSpeed() * 1000.0D : 0.0D);
//					timeoutTimerField.setLong(this, 0);
//				}
//	
//				if (timeoutLimitField.getDouble(this) > 0.0D && (double)timeoutTimerField.getLong(this) > timeoutLimitField.getDouble(this) * 3.0D) {
//					timeoutCachedNodeField.set(this, Vec3d.ZERO);
//					timeoutTimerField.setLong(this, 0L);
//					timeoutLimitField.setDouble(this, 0.0D);
//					clearStuckEntity();
//				}
//	
//				lastTimeoutCheckField.setLong(this, System.currentTimeMillis());
//			}
//		} catch (Exception e) {
//			System.out.println("lol: " + e);
//			e.printStackTrace();
//		}
//	}

}
