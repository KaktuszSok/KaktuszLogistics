package kaktusz.kaktuszlogistics.util;

import org.bukkit.util.Vector;

//https://introcs.cs.princeton.edu/java/32class/Quaternion.java.html
public class Quaternion {
	private final double x0, x1, x2, x3; //1 scalar & 3 vector components

	// create a new object with the given components
	public Quaternion(double x0, double x1, double x2, double x3) {
		this.x0 = x0;
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
	}

	// return a string representation of the invoking object
	public String toString() {
		return x0 + " + " + x1 + "i + " + x2 + "j + " + x3 + "k";
	}

	// return the quaternion norm
	public double norm() {
		return Math.sqrt(x0*x0 + x1*x1 +x2*x2 + x3*x3);
	}

	// return the quaternion conjugate
	public Quaternion conjugate() {
		return new Quaternion(x0, -x1, -x2, -x3);
	}

	// return a new Quaternion whose value is (this + b)
	public Quaternion plus(Quaternion b) {
		Quaternion a = this;
		return new Quaternion(a.x0+b.x0, a.x1+b.x1, a.x2+b.x2, a.x3+b.x3);
	}


	// return a new Quaternion whose value is (this * b)
	public Quaternion times(Quaternion b) {
		Quaternion a = this;
		double y0 = a.x0*b.x0 - a.x1*b.x1 - a.x2*b.x2 - a.x3*b.x3;
		double y1 = a.x0*b.x1 + a.x1*b.x0 + a.x2*b.x3 - a.x3*b.x2;
		double y2 = a.x0*b.x2 - a.x1*b.x3 + a.x2*b.x0 + a.x3*b.x1;
		double y3 = a.x0*b.x3 + a.x1*b.x2 - a.x2*b.x1 + a.x3*b.x0;
		return new Quaternion(y0, y1, y2, y3);
	}

	// return a new Quaternion whose value is the inverse of this
	public Quaternion inverse() {
		double d = x0*x0 + x1*x1 + x2*x2 + x3*x3;
		return new Quaternion(x0/d, -x1/d, -x2/d, -x3/d);
	}


	// return a / b
	// we use the definition a * b^-1 (as opposed to b^-1 a)
	public Quaternion divides(Quaternion b) {
		Quaternion a = this;
		return a.times(b.inverse());
	}

	public Quaternion normalise() {
		double norm = norm();
		return new Quaternion(x0/norm, x1/norm, x2/norm, x3/norm);
	}

	//VECTOR MATHS
	//https://github.com/IdeoG/quaternion-vector3-java/blob/master/Quaternion.java
	/**
	 * Rotates a vector by this quaternion
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	public Vector rotate(Vector v) {
		Quaternion q = new Quaternion(0, v.getX(), v.getY(), v.getZ());
		Quaternion inverse_self = conjugate();
		Quaternion cross_product = times(q);
		q = cross_product.times(inverse_self);
		v = new Vector(q.x1, q.x2, q.x3);
		return v;
	}

	//https://stackoverflow.com/a/19448840
	/**
	 * Creates a Quaternion representing a rotation from vector a to b
	 */
	public static Quaternion ofRotation(Vector a, Vector b) {
		Vector axis = a.clone().crossProduct(b);
		double lengthsProduct = Math.sqrt(a.lengthSquared() * b.lengthSquared());
		double sinA = axis.length() / lengthsProduct;
		double cosA = a.clone().dot(b) / lengthsProduct;
		return new Quaternion(cosA, axis.getX()*sinA, axis.getY()*sinA, axis.getZ()*sinA);
	}

}
