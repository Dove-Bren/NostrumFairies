package com.smanzana.nostrumfairies.entity.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.Location;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
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

	final double MAX_BEACON_DIST = 30 * 30;
	
	private IFeyWorker fey;
	
	// Cache indicators
	private BlockPos lastSource;
	private Set<BlockPos> lastTargets;
	private long lastTicks;
	
	// stupid encapsulation breaking things
	protected PathFinderPublic pathFinder;
	
	public PathNavigatorLogistics(EntityLiving entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
		this.fey = (IFeyWorker) entitylivingIn; // CAST! Fail if not a fey! Need a network!
		lastTicks = -1;
		lastTargets = new HashSet<>();
	}
	
	@Override
	protected PathFinder getPathFinder() {
		super.getPathFinder(); // dumbly sets up stuff so we have to call it still
		this.pathFinder = new PathFinderPublic(this.nodeProcessor);
		return this.pathFinder;
	}
	
	private boolean shouldAttempt(BlockPos target) {
		
		if (!theEntity.getPosition().equals(lastSource)) {
			lastTargets.clear();
		}
		
		// Only invoke all of the expensive failure stuff if it's been a while or if we're attempting from a new location.
		if (lastTargets.contains(target)) {
			if (lastTicks != -1 && (theEntity.ticksExisted - lastTicks) < (20 * 60)) {
				// hasn't been enough time to try again
				return false;
			}
		}
		
		return true;
	}
	
	private void setFailCache(BlockPos target) {
		lastTicks = theEntity.ticksExisted;
		lastTargets.add(target);
		lastSource = theEntity.getPosition();
	}

//	private double getWeightedDistanceSq(BlockPos pos, BlockPos target) {
//		final int entYDiff = target.getY() - (int) theEntity.posY;
//		final int yDiff = target.getY() - pos.getY();
//		final int yImprov = Math.abs(entYDiff) - Math.abs(yDiff);
//		
//		// Weight distance with y improvement. 2 blocks for every y. Add sign at the end
//		// so that further Y makes the pos less favorable.
//		return pos.distanceSq(target) - (Math.pow(yImprov * 2, 2) * Math.signum(yImprov));
//	}
	
	private double getH(BlockPos target, BlockPos pos) {
		return target.distanceSq(pos);
	}
	
	private double getEdgeCost(BlockPos source, BlockPos dest) {
		// just using distance
		return source.distanceSq(dest);
	}
	
	private @Nullable Path getMinorPathTo(BlockPos src, BlockPos dest, float range) {
		Path path = pathFinder.findPath(theEntity.worldObj, theEntity,
				src.getX() + .5, src.getY() + .5, src.getZ() + .5,
				dest.getX() + .5, dest.getY() + .5, dest.getZ() + .5,
				range);
		if (path != null && !Paths.IsComplete(path, dest, 2)) {
			path = null;
		}
		
		return path;
	}
	
	/**
	 * Finds the best node on the network that we can path to and should start from
	 * @return
	 */
	private @Nullable LogisticsNode findStart(BlockPos target) {
		LogisticsNetwork network = fey.getLogisticsNetwork();
		List<Location> beacons = Lists.newArrayList(network.getAllBeacons());
		
		beacons.removeIf((loc) -> {
			return (loc.getDimension() != theEntity.worldObj.provider.getDimension());
		});
		
		if (!beacons.isEmpty()) {
			Collections.sort(beacons, (l, r) -> {
				final double ldist = theEntity.getDistanceSq(l.getPos());
				final double rdist = theEntity.getDistanceSq(r.getPos());
				
				return (int) (ldist - rdist);
			});
			
			for (Location beacon : beacons) {
				Path subpath = super.getPathToPos(beacon.getPos());
				if (subpath != null && Paths.IsComplete(subpath, beacon.getPos(), theEntity.worldObj.isAirBlock(beacon.getPos()) ? 0 : 1)) {
					LogisticsNode node = new LogisticsNode(beacon.getPos(), null, 0, getH(target, beacon.getPos()));
					node.path = subpath;
					return node;
					
				}
			}
		}
		
		return null;
	}
	
	private Path makeTotalPath(LogisticsNode endNode, Path finalPath) {
		List<Path> inputs = new ArrayList<>();
		
		// Important! Need to go from first to last
		
		inputs.add(finalPath);
		do {
			inputs.add(endNode.path);
			endNode = endNode.from;
		} while (endNode != null);
		
		// Reverse order
		Collections.reverse(inputs);
		return Paths.Combine(inputs);
	}
	
	protected @Nullable Path pathfindTo(BlockPos target, float range) {
		/**
		 * Algo is A* with logistics network as graph. Goal is a node that can vanilla pathfind to target.
		 * That is, start at entity and find how to get onto the network.
		 * Then, walk network graph (with beacons! Don't forget!) looking for next smallest to expand.
		 * Each time we expand, check to see if it's the goal. If it is, great!
		 * Otherwise, add "neighbors".
		 * 
		 * Neighbors is weird here because our graph edges don't mean they can be moved between.
		 * We also _could_ move between nodes that aren't linked logistically, but we'll ignore those instead
		 * of pathfinding between all nodes over and over (nxn). But beacons pose an issue.
		 * So 'neighbors' will be all connected nodes + all beacons. And evaluating a 'neighbor' has to include
		 * seeing if we can actually path between it and the previous.
		 * 
		 * Again, we could make edges between all nodes and beacons and then use dist as a heuristic.
		 * Using the logistics edges (which are based on distance) is a good guess though.
		 * 
		 * Also, I was wrong. To get the cost of an edge, we need to pathfind. So expanding neighbors involves pathfinding.
		 * To eliminate beacon madness, I'll ignore beacons if they're too far to be worth it.
		 */
		LogisticsNetwork network = fey.getLogisticsNetwork();
		if (network == null) {
			return null;
		}
		
		// Find start node!
		LogisticsNode start = findStart(target);
		if (start == null) {
			return null;
		}
		
		PriorityQueue<LogisticsNode> openSet = new PriorityQueue<>();
		Set<BlockPos> seen = new HashSet<>();
		openSet.add(start);
		
		LogisticsNode lastNode = null;
		Path finalPath = null;
		while (!openSet.isEmpty()) {
			LogisticsNode node = openSet.remove();
			if (seen.contains(node.pos)) {
				continue;
			}
			
			// See if we can even path to this node.
			if (node.path == null) {
				node.path = getMinorPathTo(node.from.pos, node.pos, range);
			}
			
			if (node.path == null) {
				// We can't reach this node from its predecessor :(
				continue;
			}
			
			// Is this the final node???? AKA can we path from it straight to the destination?
			finalPath = getMinorPathTo(node.pos, target, range);
			if (finalPath != null) {
				lastNode = node;
				break;
			}

			seen.add(node.pos);
			// Adding pos to seen means this is the only node for this pos that can exist now.
			// We ARE the way to get to this pos.
			
			// Now check all of our neighbors. This is the set of all graph neighbors + the set of beacons that
			// are within range
			Collection<ILogisticsComponent> components = network.getConnectedComponents(new Location(theEntity.worldObj, node.pos));
			if (components != null) {
				for (ILogisticsComponent comp : components) {
					if (seen.contains(comp.getPosition())) {
						continue; // optimization
					}
					
					// DUPED BELOW
					double cost = node.cost + getEdgeCost(node.pos, comp.getPosition());
					openSet.add(new LogisticsNode(comp.getPosition(), node, cost, cost + getH(target, comp.getPosition())));
				}
			}
			
			// Remember: beacons (true beacons) we optimize to MAX_BEACON_DIST
			for (Location beacon : network.getOnlyBeacons()) {
				if (seen.contains(beacon.getPos())) {
					continue;
				}
				
				double dist = beacon.getPos().distanceSq(node.pos); // same as HCOST. Ever gonna change?
				if (dist > MAX_BEACON_DIST) {
					continue;
				}
				
				double cost = node.cost + getEdgeCost(node.pos, beacon.getPos());
				openSet.add(new LogisticsNode(beacon.getPos(), node, cost, cost + getH(target, beacon.getPos())));
			}
		}
		
		Path path = null;
		if (lastNode != null) {
			path = makeTotalPath(lastNode, finalPath);
		}
		
		return path;
	}
	
	@Override
	public Path getPathToPos(BlockPos target) {
		@Nullable Path path = super.getPathToPos(target);
		
		// If we've failed too recently on the same request, don't even try
		if (!shouldAttempt(target)) {
			return path;
		}
		
//		if (!this.canNavigate()) {
//			return path;
//		}
		
		// If vanilla pathfinding 'found a path' and actually gets us there, use it!
		if (path != null && Paths.IsComplete(path, target, 2)) {
			return path; // yay!
		}
		
		// Uh oh. We don't like Vanilla's path.
		final Path vanillaPath = path;
		
		// Can we make a better one?
		path = pathfindTo(target, this.getPathSearchRange());
		
		if (path == null) {
			// Nope! We couldn't!
			path = vanillaPath;
			setFailCache(target);
		} else {
			lastTargets.remove(target);
		}
		
		return path;
	}
	
	/**
	 * Records the BEST path to this node
	 * @author Skyler
	 *
	 */
	private final class LogisticsNode implements Comparable<LogisticsNode> {
		
		public BlockPos pos; // Node we represent
		public LogisticsNode from; // Node we got here from
		public Path path; // The path between this node and the 'from' one. Starts out null and is filled in when being evaluated!
		public double cost; // Total cost to get to this node
		public double estimate; // This is the one we sort on!
		
		public LogisticsNode(BlockPos pos, LogisticsNode from, double cost, double estimate) {
			this.pos = pos;
			this.from = from;
			this.path = null;
			this.cost = cost;
			this.estimate = estimate;
		}
		
		@Override
		public int compareTo(LogisticsNode other) {
			return (int) (estimate - other.estimate);
		}
	}
}
