package com.smanzana.nostrumfairies.entity.navigation;

import java.util.Collection;

import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CompositePath extends Path {

	private final Path[] paths;
	private int compoundPathIndex;
	
	// Path cached stuff
	private int cachedTotalLength;
	private int cachedCurrIndex;
	private Path cachedCurrPath;
	private int cachedCurrPathIndex;
	
	public CompositePath(Path...paths) {
		super(new PathPoint[0]);
		this.paths = fixPaths(paths);
		compoundPathIndex = 0;
		
		refreshCache(true);
	}
	
	private Path[] fixPaths(Path[] inputs) {
		// The pathfinding code treats two points next to eachother as blashphemous.
		// Each path should have a end point that matches the start point of the previous.
		Path[] output = new Path[inputs.length];
		output[0] = inputs[0];
		int i = 1;
		while (i < inputs.length) {
			output[i] = Paths.TrimStart(inputs[i]);
			i++;
		}
		
		return output;
	}
	
	public CompositePath(Collection<Path> paths) {
		this(paths.toArray(new Path[paths.size()]));
	}
	
	protected void refreshCache(boolean changedLength) {
		if (changedLength) {
			cachedTotalLength = calcTotalPathLength(paths);
		}
		
		if (changedLength || cachedCurrIndex != compoundPathIndex) {
			this.cachedCurrIndex = compoundPathIndex;
			PathAndIndex adjusted = calcPathForIndex(paths, cachedCurrIndex);
			this.cachedCurrPath = adjusted == null ? null : adjusted.path;
			this.cachedCurrPathIndex = adjusted == null ? -1 : adjusted.pathIndex;
		}
	}
	
	protected static PathAndIndex calcPathForIndex(Path[] paths, int compoundIndex) {
		int pathIndex = 0;
		while (pathIndex < paths.length) {
			Path path = paths[pathIndex];
			if (path.getCurrentPathLength() > compoundIndex) {
				return new PathAndIndex(path, compoundIndex, pathIndex);
			}
			pathIndex++;
			compoundIndex -= path.getCurrentPathLength();
		}
		
		return null;
	}
	
	protected static int calcTotalPathLength(Path[] paths) {
		int totalLength = 0;
		
		for (Path path : paths) {
			totalLength += path.getCurrentPathLength();
		}
		
		return totalLength;
	}
	
	public Path getCurrentPath() {
		refreshCache(false);
		return cachedCurrPath;
	}
	
	public BlockPos getSegmentStart() {
		// We trim off the start, so we have to reconstruct it
		refreshCache(false);
		if (cachedCurrPathIndex - 1 >= 0) {
			PathPoint point = paths[cachedCurrPathIndex-1].getPathPointFromIndex(0);
			return new BlockPos(point.x, point.y, point.z);
		}
		
		return null;
	}
	
	public BlockPos getSegmentEnd() {
		Path segment = getCurrentPath();
		if (segment != null) {
			PathPoint point = segment.getFinalPathPoint();
			return new BlockPos(point.x, point.y, point.z);
		}
		
		return null;
	}
	
	/**
	 * Directs this path to the next point in its array
	 */
	public void incrementPathIndex() {
		final Path curr = getCurrentPath();
		if (curr != null) {
			curr.incrementPathIndex();
			compoundPathIndex++;
		}
	}

	/**
	 * Returns true if this path has reached the end
	 */
	public boolean isFinished() {
		refreshCache(false);
		return cachedCurrPath == null;
	}

	/**
	 * returns the last PathPoint of the Array
	 */
	public PathPoint getFinalPathPoint() {
		return this.paths.length > 0 ? paths[paths.length - 1].getFinalPathPoint() : null;
	}

	/**
	 * return the PathPoint located at the specified PathIndex, usually the current one
	 */
	public PathPoint getPathPointFromIndex(int index) {
		if (index == cachedCurrIndex) {
			return cachedCurrPath.getPathPointFromIndex(cachedCurrPath.getCurrentPathIndex());
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		return adjusted.path.getPathPointFromIndex(adjusted.index);
	}

	public void setPoint(int index, PathPoint point) {
		if (index == cachedCurrIndex) {
			cachedCurrPath.setPoint(cachedCurrPath.getCurrentPathIndex(), point);
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		adjusted.path.setPoint(adjusted.index, point);
	}

	public int getCurrentPathLength() {
		return this.cachedTotalLength;
	}

	public void setCurrentPathLength(int length) {
		throw new RuntimeException("Can't change path length of compositep path");
	}

	public int getCurrentPathIndex() {
		return this.compoundPathIndex;
	}

	public void setCurrentPathIndex(int currentPathIndexIn) {
		if (currentPathIndexIn != this.compoundPathIndex) {
			if (currentPathIndexIn > this.compoundPathIndex) {
				// Jumping forward
				for (int i = currentPathIndexIn - this.compoundPathIndex; i > 0; i--) {
					this.incrementPathIndex();
				}
				this.compoundPathIndex = currentPathIndexIn;
				return;
			}
			
			// For all paths before it, set index to max
			// For all paths after it, set index to 0
			// for path it's on, set to actual
			int indicies = currentPathIndexIn;
			for (Path p : this.paths) {
				if (indicies == 0) {
					// This is a path after
					p.setCurrentPathIndex(0);
				} else if (p.getCurrentPathLength() > indicies) {
					// This is the path where we are actually at
					p.setCurrentPathIndex(indicies);
					indicies = 0;
				} else {
					p.setCurrentPathIndex(p.getCurrentPathLength());
					indicies -= p.getCurrentPathLength();
				}
			}
			
			this.compoundPathIndex = currentPathIndexIn;
		}
	}

	/**
	 * Gets the vector of the PathPoint associated with the given index.
	 */
	public Vec3d getVectorFromIndex(Entity entityIn, int index) {
		if (index == cachedCurrIndex) {
			return cachedCurrPath.getVectorFromIndex(entityIn, cachedCurrPath.getCurrentPathIndex());
		}
		
		PathAndIndex adjusted = calcPathForIndex(paths, index);
		return adjusted.path.getVectorFromIndex(entityIn, adjusted.index);
	}

	/**
	 * returns the current PathEntity target node as Vec3D
	 */
	public Vec3d getPosition(Entity entityIn) {
		return this.getVectorFromIndex(entityIn, this.compoundPathIndex);
	}

	public Vec3d getCurrentPos() {
		PathPoint pathpoint = this.getPathPointFromIndex(compoundPathIndex);
		return new Vec3d((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z);
	}

	/**
	 * Returns true if the EntityPath are the same. Non instance related equals.
	 */
	public boolean isSamePath(Path pathentityIn) {
		if (pathentityIn == null || !(pathentityIn instanceof CompositePath)) {
			return false;
		}
		
		CompositePath other = (CompositePath) pathentityIn;
		if (other.paths.length != this.paths.length) {
			return false;
		}
		
		for (int i = 0; i < paths.length; i++) {
			if (!paths[i].isSamePath(other.paths[i])) {
				return false;
			}
		}

		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public PathPoint[] getOpenSet() {
		refreshCache(false);
		return this.cachedCurrPath.getOpenSet();
	}

	@OnlyIn(Dist.CLIENT)
	public PathPoint[] getClosedSet() {
		refreshCache(false);
		return this.cachedCurrPath.getClosedSet();
	}

	@OnlyIn(Dist.CLIENT)
	public PathPoint getTarget() {
		refreshCache(false);
		return this.cachedCurrPath.getTarget();
	}
	
	
	protected static final class PathAndIndex {
		public final Path path;
		public final int index;
		public final int pathIndex;
		
		public PathAndIndex(Path path, int index, int pathIndex) {
			this.path = path;
			this.index = index;
			this.pathIndex = pathIndex;
		}
	}
	
}
