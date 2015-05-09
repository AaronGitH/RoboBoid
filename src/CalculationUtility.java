public class CalculationUtility {

	// translates robot-coordinates
	public static double[] translateManeuver(double[] currCenterPoint,
			double currOrientation, double[] targetWorldCoordinate) {

		double hypotenuse = getDistance(currCenterPoint, targetWorldCoordinate);

		double targetOrientation = getOrientation(currCenterPoint,
				targetWorldCoordinate);
		double angleDiff = getOrientationDifference(currOrientation,
				targetOrientation);

		double x = Math.cos(angleDiff) * hypotenuse;
		double y = Math.sin(angleDiff) * hypotenuse;
		double[] robotCoordinate = { x, y };

		return robotCoordinate;
	}

	public static double getDistance(double[] pointA, double[] pointB) {
		double x1 = pointA[0];
		double y1 = pointA[1];
		double x2 = pointB[0];
		double y2 = pointB[1];
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}

	// 0 Pi -> down
	// 0.5 * Pi -> right
	// -0.5 * Pi -> left
	public static double getOrientation(double[] pointA, double[] pointB) {
		double opposite = pointB[0] - pointA[0]; // X component
		double adjacent = pointB[1] - pointA[1]; // Y component
		return Math.atan2(opposite, adjacent);
	}

	public static double getOrientationDifference(double orientationCurrent,
			double orientationTarget) {
		double a = normalizeAngle2Pi(orientationCurrent);
		double b = normalizeAngle2Pi(orientationTarget);

		double angle = Math.min(normalizeAngle2Pi(a - b), normalizeAngle2Pi(b - a));

		if (isAngleInTolerance(orientationCurrent + angle, orientationTarget,
				0.000000001))
			return angle;
		if (isAngleInTolerance(orientationCurrent - angle, orientationTarget,
				0.000000001))
			return -angle;

		// raise ValueError("angle problems!")
		return 0;
	}

	// normAngle from 0 to 2 Pi
	public static double normalizeAngle2Pi(double angle) {
		return angle % (2.0 * Math.PI);
	}

	// normAngle from -Pi to +Pi
	public static double normalizeAngleToPi(double angle) {
		angle = normalizeAngle2Pi(angle);
		if (angle > Math.PI)
			angle = angle - (Math.PI * 2.0);
		return angle;
	}

	public static boolean isAngleInTolerance(double testee, double target,
			double tolerance) {
		testee = normalizeAngle2Pi(testee);
		target = normalizeAngle2Pi(target);

		double angle = Math.min(normalizeAngle2Pi(testee - target),
				normalizeAngle2Pi(target - testee));

		if (angle <= tolerance)
			return true;
		else
			return false;
	}

}
