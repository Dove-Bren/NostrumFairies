package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.navigation.PathPublic;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public final class Paths {
	
	public static Path TrimStart(Path path) {
		PathPoint points[] = new PathPoint[path.getCurrentPathLength() - 1];
		// NOTE: start at 1 to chop of 'start' which is usually in a block lol
		for (int i = 1; i < path.getCurrentPathLength(); i++) {
			// isn't this dumb? lol. Wish there was .getPathPoints();
			PathPoint point = path.getPathPointFromIndex(i);
			points[i-1] = new PathPoint(point.xCoord, point.yCoord, point.zCoord);
		}
		
		return new Path(points);
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
		for (Path path : paths) {
			// NOTE: start at 1 to chop of 'start' which is usually in a block lol
			for (int i = first ? 0 : 1; i < path.getCurrentPathLength(); i++) {
				// isn't this dumb? lol. Wish there was .getPathPoints();
				PathPoint point = path.getPathPointFromIndex(i);
				points.add(new PathPoint(point.xCoord, point.yCoord, point.zCoord));
			}
			first = false;
		}
		
		return new Path(points.toArray(new PathPoint[points.size()]));
	}
	
	/**
	 * Checks if the path provided actaully reached the intended destination
	 * @param path
	 * @return
	 */
	public static boolean IsComplete(Path path, BlockPos target, int maxDistance) {
		PathPoint end = path.getFinalPathPoint();
		return target.distanceSq(end.xCoord, end.yCoord, end.zCoord) <= Math.pow(maxDistance, 2); // 0 or 1 blocks away, please!
	}
	
	public static PathPublic ClonePath(Path path) {
		if (path == null) {
			return null;
		}
		
		if (path instanceof PathPublic) {
			return new PathPublic(((PathPublic) path).getPathPoints());
		}
		
		int size = path.getCurrentPathLength();
		PathPoint[] points = new PathPoint[size];
		for (int i = 0; i < size; i++) {
			points[i] = path.getPathPointFromIndex(i);
		}
		
		return new PathPublic(points);
	}
	
}
