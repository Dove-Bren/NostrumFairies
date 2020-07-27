package com.smanzana.nostrumfairies.entity.navigation;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.IBlockAccess;

/**
 * I WISH this just made some protected funcs public. But it does not!
 * Defers to vanilla for calls that were already public, and creates public wrappers AND COPIES THE IMPLEMENTATION
 * of the private ones.
 * I could use reflection, but i don't want to.
 * @author Skyler
 *
 */
public class PathFinderPublic extends PathFinder {

	public PathFinderPublic(NodeProcessor processor) {
		super(processor);
		this.nodeProcessor = processor;
	}
	
	/*************** BEGIN COPIED STUFF ***************************/
	
	
	/** The path being generated */
	private final PathHeap path = new PathHeap();
	private final Set<PathPoint> closedSet = Sets.<PathPoint>newHashSet();
	/** Selection of path points to add to the path */
	private final PathPoint[] pathOptions = new PathPoint[32];
	private final NodeProcessor nodeProcessor;

	@Nullable
	public Path findPath(IBlockAccess worldIn, EntityLiving entity,
			double startX, double startY, double startZ,
			double destX, double destY, double destZ,
			float range) {
		this.path.clearPath();
		this.nodeProcessor.initProcessor(worldIn, entity);
		PathPoint pathpoint = this.nodeProcessor.getPathPointToCoords(startX, startY, startZ);
		PathPoint pathpoint1 = this.nodeProcessor.getPathPointToCoords(destX, destY, destZ);
		Path path = this.findPath(pathpoint, pathpoint1, range);
		this.nodeProcessor.postProcess();
		return path;
	}

	// If THIS were protected instead, this would all look a lot nicer.
	// (and the processor var)
	@Nullable
	public Path findPath(PathPoint start, PathPoint end, float range) {
		start.totalPathDistance = 0.0F;
		start.distanceToNext = start.distanceManhattan(end);
		start.distanceToTarget = start.distanceToNext;
		this.path.clearPath();
		this.closedSet.clear();
		this.path.addPoint(start);
		PathPoint pathpoint = start;
		int i = 0;

		while (!this.path.isPathEmpty()) {
			++i;

			if (i >= 200) {
				break;
			}

			PathPoint pathpoint1 = this.path.dequeue();

			if (pathpoint1.equals(end)) {
				pathpoint = end;
				break;
			}

			if (pathpoint1.distanceManhattan(end) < pathpoint.distanceManhattan(end)) {
				pathpoint = pathpoint1;
			}

			pathpoint1.visited = true;
			int j = this.nodeProcessor.findPathOptions(this.pathOptions, pathpoint1, end, range);

			for (int k = 0; k < j; ++k) {
				PathPoint pathpoint2 = this.pathOptions[k];
				float f = pathpoint1.distanceManhattan(pathpoint2);
				pathpoint2.distanceFromOrigin = pathpoint1.distanceFromOrigin + f;
				pathpoint2.cost = f + pathpoint2.costMalus;
				float f1 = pathpoint1.totalPathDistance + pathpoint2.cost;

				if (pathpoint2.distanceFromOrigin < range && (!pathpoint2.isAssigned() || f1 < pathpoint2.totalPathDistance)) {
					pathpoint2.previous = pathpoint1;
					pathpoint2.totalPathDistance = f1;
					pathpoint2.distanceToNext = pathpoint2.distanceManhattan(end) + pathpoint2.costMalus;

					if (pathpoint2.isAssigned()) {
						this.path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext);
					} else {
						pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
						this.path.addPoint(pathpoint2);
					}
				}
			}
		}

		if (pathpoint == start) {
			return null;
		} else {
			Path path = this.createEntityPath(start, pathpoint);
			return path;
		}
	}

	/**
	 * Returns a new PathEntity for a given start and end point
	 */
	private Path createEntityPath(PathPoint start, PathPoint end) {
		int i = 1;

		for (PathPoint pathpoint = end; pathpoint.previous != null; pathpoint = pathpoint.previous) {
			++i;
		}

		PathPoint[] apathpoint = new PathPoint[i];
		PathPoint pathpoint1 = end;
		--i;

		for (apathpoint[i] = end; pathpoint1.previous != null; apathpoint[i] = pathpoint1) {
			pathpoint1 = pathpoint1.previous;
			--i;
		}

		return new Path(apathpoint);
	}

}
