package kaktusz.kaktuszlogistics.util;

import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

//based on https://www.youtube.com/watch?v=NbSee-XM7WA
/**
 * A class that uses the Digital Differential Analyser algorithm to calculate coordinates intersecting a line segment
 */
public class DDARaycasting {

	/**
	 * @return A queue of integer grid coordinates which are intersected by the given line segment
	 */
	public static Queue<Vector> raycastWorldGrid(Vector start, Vector end) {
		Queue<Vector> intersections = new LinkedList<>();

		Vector dir = end.clone().subtract(start).normalize();
		int currX = start.getBlockX();
		int currY = start.getBlockY();
		int currZ = start.getBlockZ();

		//calculate distance travelled along hypotenuse for a 1-unit step for each axis
		//dx = 1:
		double dy_dx = dir.getY()/dir.getX();
		double dz_dx = dir.getZ()/dir.getX();
		//dy = 1:
		double dx_dy = dir.getX()/dir.getY();
		double dz_dy = dir.getZ()/dir.getY();
		//dz = 1:
		double dx_dz = dir.getX()/dir.getZ();
		double dy_dz = dir.getY()/dir.getZ();
		Vector rayUnitStepSize = new Vector(
				Math.sqrt(1 + dy_dx*dy_dx + dz_dx*dz_dx),
				Math.sqrt(dx_dy*dx_dy + 1 + dz_dy*dz_dy),
				Math.sqrt(dx_dz*dx_dz + dy_dz*dy_dz + 1)
		);

		Vector rayLength = new Vector(0,0,0); //length of ray along hypotenuse for each axis' stepping line

		//calculate step direction, and also modify ray length to account for snapping "current" to grid
		int stepX, stepY, stepZ;
		//X:
		if(dir.getX() < 0) {
			stepX = -1;
			rayLength.setX((start.getX() - currX) * rayUnitStepSize.getX());
		}
		else {
			stepX = 1;
			rayLength.setX((currX+1 - start.getX()) * rayUnitStepSize.getX());
		}
		//Y:
		if(dir.getY() < 0) {
			stepY = -1;
			rayLength.setY((start.getY() - currY) * rayUnitStepSize.getY());
		}
		else {
			stepY = 1;
			rayLength.setY((currY+1 - start.getY()) * rayUnitStepSize.getY());
		}
		//Z:
		if(dir.getZ() < 0) {
			stepZ = -1;
			rayLength.setZ((start.getZ() - currZ) * rayUnitStepSize.getZ());
		}
		else {
			stepZ = 1;
			rayLength.setZ((currZ+1 - start.getZ()) * rayUnitStepSize.getZ());
		}

		final double maxDistance = start.distance(end);
		double currDistance = 0f;
		//get all intersecting coords
		while (currDistance < maxDistance) {
			intersections.add(new Vector(currX,currY,currZ));
			//walk shortest axis
			if(rayLength.getX() < rayLength.getY()) {
				if(rayLength.getX() < rayLength.getZ()) {
					currX += stepX;
					currDistance = rayLength.getX();
					rayLength.setX(rayLength.getX() + rayUnitStepSize.getX());
				}
				else {
					currZ += stepZ;
					currDistance = rayLength.getZ();
					rayLength.setZ(rayLength.getZ() + rayUnitStepSize.getZ());
				}
			}
			else {
				if(rayLength.getY() < rayLength.getZ()) {
					currY += stepY;
					currDistance = rayLength.getY();
					rayLength.setY(rayLength.getY() + rayUnitStepSize.getY());
				}
				else {
					currZ += stepZ;
					currDistance = rayLength.getZ();
					rayLength.setZ(rayLength.getZ() + rayUnitStepSize.getZ());
				}
			}
		}

		return intersections;
	}
}
