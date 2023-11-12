package com.smanzana.nostrumfairies.entity.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CompositePath extends Path {

	private final List<Path> paths;
	private int compoundPathIndex;
	
	// Path cached stuff
	private int cachedTotalLength;
	private int cachedCurrIndex;
	private Path cachedCurrPath;
	private int cachedCurrPathIndex;
	
	public CompositePath(List<Path> paths) {
		super(new ArrayList<>(), paths.get(paths.size() - 1).func_224770_k(), paths.get(paths.size() - 1).func_224771_h());
		this.paths = fixPaths(paths);
		compoundPathIndex = 0;
		
		refreshCache(true);
	}
	
	private List<Path> fixPaths(List<Path> inputs) {
		// The pathfinding code treats two points next to eachother as blashphemous.
		// Each path should have a end point that matches the start point of the previous.
		List<Path> output = new ArrayList<>(inputs.size());
		output.add(inputs.get(0));
		int i = 1;
		while (i < inputs.size()) {
			output.add(Paths.TrimStart(inputs.get(i)));
			i++;
		}
		
		return output;
	}
	
	public CompositePath(Collection<Path> paths) {
		this(Lists.newArrayList(paths));
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
	
	protected static PathAndIndex calcPathForIndex(List<Path> paths, int compoundIndex) {
		int pathIndex = 0;
		while (pathIndex < paths.size()) {
			Path path = paths.get(pathIndex);
			if (path.getCurrentPathLength() > compoundIndex) {
				return new PathAndIndex(path, compoundIndex, pathIndex);
			}
			pathIndex++;
			compoundIndex -= path.getCurrentPathLength();
		}
		
		return null;
	}
	
	protected static int calcTotalPathLength(List<Path> paths) {
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
			PathPoint point = paths.get(cachedCurrPathIndex-1).getPathPointFromIndex(0);
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
		return this.paths.size() > 0 ? paths.get(paths.size() - 1).getFinalPathPoint() : null;
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
		if (other.paths.size() != this.paths.size()) {
			return false;
		}
		
		for (int i = 0; i < paths.size(); i++) {
			if (!paths.get(i).isSamePath(other.paths.get(i))) {
				return false;
			}
		}

		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public PathPoint[] getOpenSet() {
		refreshCache(false);
		return this.cachedCurrPath.getOpenSet();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public PathPoint[] getClosedSet() {
		refreshCache(false);
		return this.cachedCurrPath.getClosedSet();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockPos func_224770_k() { // getTarget
		refreshCache(false);
		return this.cachedCurrPath.func_224770_k();
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
