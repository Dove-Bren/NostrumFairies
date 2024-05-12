package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.navigation.PathPublic;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public final class Paths {
	
	public static Path TrimStart(Path path) {
		final int len = path.getCurrentPathLength();
		List<PathPoint> points = new ArrayList<PathPoint>(len);
		// NOTE: start at 1 to chop of 'start' which is usually in a block lol
		for (int i = 0; i < len; i++) {
			final PathPoint point = path.getPathPointFromIndex(i);
			points.add(new PathPoint(point.x, point.y, point.z));
		}
		
		return new Path(points, path.getTarget(), path.reachesTarget());
	}

	/**
	 * Create one big path with all the individual points of the provided paths.
	 * @param paths
	 * @return
	 */
	public static Path Combine(Path ... paths) {
		return Combine(Lists.newArrayList(paths));
	}
	
	public static Path Combine(Collection<Path> paths) {
		ArrayList<PathPoint> points = new ArrayList<>(paths.size() * 5); // estimate
		boolean first = true;
		BlockPos finalDest = null;
		boolean finalFlagged = false;
		for (Path path : paths) {
			// NOTE: start at 1 to chop of 'start' which is usually in a block lol
			for (int i = first ? 0 : 1; i < path.getCurrentPathLength(); i++) {
				// isn't this dumb? lol. Wish there was .getPathPoints();
				PathPoint point = path.getPathPointFromIndex(i);
				points.add(new PathPoint(point.x, point.y, point.z));
			}
			first = false;
			finalDest = path.getTarget();
			finalFlagged = path.reachesTarget();
		}
		
		return new Path(points, finalDest, finalFlagged);
	}
	
	/**
	 * Checks if the path provided actaully reached the intended destination
	 * @param path
	 * @return
	 */
	public static boolean IsComplete(Path path, BlockPos target, int maxDistance) {
		PathPoint end = path.getFinalPathPoint();
		return target.distanceSq(end.x, end.y, end.z, true) <= Math.pow(maxDistance, 2); // 0 or 1 blocks away, please!
	}
	
	public static PathPublic ClonePath(Path path) {
		if (path == null) {
			return null;
		}
		
		if (path instanceof PathPublic) {
			return new PathPublic(((PathPublic) path).getPathPoints(), path.getTarget(), path.reachesTarget());
		}
		
		int size = path.getCurrentPathLength();
		List<PathPoint> points = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			points.add(path.getPathPointFromIndex(i));
		}
		
		return new PathPublic(points, path.getTarget(), path.reachesTarget());
	}
	
}
