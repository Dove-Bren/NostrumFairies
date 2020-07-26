package com.smanzana.nostrumfairies.entity.navigation;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.Location;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Navigator that uses a logistics network to aid path finding.
 * If I were more hardcode, I'd go write my own PathFinder that was more specialized too.
 * Instead, I'm going to use vanilla but use logistics components as beacons
 * @author Skyler
 *
 */
public class PathNavigatorLogistics extends PathNavigatorGroundFixed {

	private IFeyWorker fey;
	
	public PathNavigatorLogistics(EntityLiving entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
		this.fey = (IFeyWorker) entitylivingIn; // CAST! Fail if not a fey! Need a network!
	}

	private double getWeightedDistanceSq(BlockPos pos, BlockPos target) {
		final int entYDiff = target.getY() - (int) theEntity.posY;
		final int yDiff = target.getY() - pos.getY();
		final int yImprov = Math.abs(entYDiff) - Math.abs(yDiff);
		
		// Weight distance with y improvement. 2 blocks for every y. Add sign at the end
		// so that further Y makes the pos less favorable.
		return pos.distanceSq(target) - (Math.pow(yImprov * 2, 2) * Math.signum(yImprov));
	}
	
	@Override
	public Path getPathToPos(BlockPos target) {
		// TODO only use logistics beacons (and increase the cost of failed pathfinding) so often?
		// Maybe hide behind a timer, plus also only if the pos (targ and source) are the same as last time?
		@Nullable Path path = super.getPathToPos(target);
		//System.out.println("Path me please to " + pos);
		// TODO could consider doing this if the path the above got doesn't take us all the way there.
		// Perhaps with higher requirements?
		
		// TODO actually if we could try to path find all the way to the destination instead of just being happy
		// to get closer...
		if (path == null && canNavigate()) {
			
			// Attempt to use logistics beacons
			LogisticsNetwork network = fey.getLogisticsNetwork();
			if (network != null) {
				List<Location> beacons = Lists.newArrayList(network.getBeacons());
				
				final double origDistSq = theEntity.getDistanceSq(target);
				beacons.removeIf((loc) -> {
					if (loc.getDimension() != theEntity.worldObj.provider.getDimension()) {
						return true;
					}
					
					// Don't want to get _further_ away!
					// I think if I did the full path find, I wouldn't need to guess like this.
					double dist = getWeightedDistanceSq(loc.getPos(), target);
					if (dist > origDistSq) {
//						if (loc.getPos().getY() == 229) {
//							System.out.println("Door is " + dist + " but orig: " + origDistSq);
//						}
						
						return true;
					}
					
					// Make sure we're not already there
					if (loc.getPos().distanceSq(theEntity.getPosition()) < 1) {
						return false;
					}
					
					return false;
				});
				
				if (!beacons.isEmpty()) {
					Collections.sort(beacons, (l, r) -> {
						final double ldist = getWeightedDistanceSq(l.getPos(), target);
						final double rdist = getWeightedDistanceSq(r.getPos(), target);
						
						return (int) (ldist - rdist);
					});
					
					for (Location beacon : beacons) {
						path = super.getPathToPos(beacon.getPos());
						if (path != null) {
							System.out.println("Enhanced pathfinding engaged");
							break;
						}
					}
				}
			}
			
		}
		
		return path;
	}
}
