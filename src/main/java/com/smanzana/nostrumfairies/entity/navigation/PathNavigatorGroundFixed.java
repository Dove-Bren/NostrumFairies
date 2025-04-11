package com.smanzana.nostrumfairies.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

public class PathNavigatorGroundFixed extends GroundPathNavigation {
	private int unused; // eval

	public PathNavigatorGroundFixed(Mob entitylivingIn, Level worldIn) {
		super(entitylivingIn, worldIn);
	}
	
//	protected void clearStuckEntity() {
//		this.clearPath();
//	}
//
//	@Override
//	protected void checkForStuck(Vector3d positionVec3) {
//		//Field totalTicksField = ReflectionHelper.findField(PathNavigate.class, "totalTicks");
//		final int totalTicks = ObfuscationReflectionHelper.getPrivateValue(PathNavigate.class, this, "tick");//"totalTicks");
//		Field ticksAtLastPosField = ObfuscationReflectionHelper.findField(PathNavigate.class, "lastStuckCheck");//"ticksAtLastPos");
//		Field lastPosCheckField = ObfuscationReflectionHelper.findField(PathNavigate.class, "lastStuckCheckPos");//"lastPosCheck");
//		Field timeoutCachedNodeField = ObfuscationReflectionHelper.findField(PathNavigate.class, "timeoutCachedNode");//"timeoutCachedNode");
//		Field timeoutLimitField = ObfuscationReflectionHelper.findField(PathNavigate.class, "timeoutLimit");//"timeoutLimit");
//		Field timeoutTimerField = ObfuscationReflectionHelper.findField(PathNavigate.class, "timeoutTimer");//"timeoutTimer");
//		Field lastTimeoutCheckField = ObfuscationReflectionHelper.findField(PathNavigate.class, "lastTimeoutCheck");//"lastTimeoutCheck");
//		
//		try {
//			if (totalTicks - ticksAtLastPosField.getInt(this) > 100) {
//				if (positionVec3.squareDistanceTo((Vector3d) lastPosCheckField.get(this)) < 2.25D) {
//					this.clearPath();
//				}
//	
//				ticksAtLastPosField.setInt(this, totalTicks);
//				lastPosCheckField.set(this, positionVec3);
//			}
//	
//			if (this.currentPath != null && !this.currentPath.isFinished()) {
//				Vector3d Vector3d = this.currentPath.getCurrentPos();
//	
//				if (Vector3d.equals(timeoutCachedNodeField.get(this))) {
//					timeoutTimerField.setLong(this, timeoutTimerField.getLong(this) + (System.currentTimeMillis() - lastTimeoutCheckField.getLong(this)));
//					//timeoutTimer += System.currentTimeMillis() - lastTimeoutCheckField.getInt(this);
//				} else {
//					timeoutCachedNodeField.set(this, Vector3d);
//					double d0 = positionVec3.distanceTo((Vector3d) timeoutCachedNodeField.get(this));
//					timeoutLimitField.setDouble(this, this.entity.getAIMoveSpeed() > 0.0F ? d0 / (double)this.entity.getAIMoveSpeed() * 1000.0D : 0.0D);
//					timeoutTimerField.setLong(this, 0);
//				}
//	
//				if (timeoutLimitField.getDouble(this) > 0.0D && (double)timeoutTimerField.getLong(this) > timeoutLimitField.getDouble(this) * 3.0D) {
//					timeoutCachedNodeField.set(this, Vector3d.ZERO);
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
