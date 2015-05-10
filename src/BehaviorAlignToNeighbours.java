import java.util.ArrayList;
import java.util.List;

import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

/*
 * This class is the hardest to implement because of the many possible scenarios
 * The boid has to first identify that it is within close range on another boid
 * Then it must track the movement of the other boid to see where it is moving towards
 * And then match the same velocity
 */

public class BehaviorAlignToNeighbours implements Behavior {
	private boolean suppressed;
	private DifferentialPilot pilot;
	private SampleProvider provider;
	private float[] sampleValues;

	// private final double WHEEL_DIAMETER_cm = 4.32;
	private final double SENSOR_DISTANCE_MAX = 50; // defines what is valid data
	private final double SENSOR_ANGLE_MAX = 25; // defines what is valid data
	private final double ANGLE_ERROR_COEFFICIENT = 1.5;
	private final double VELOCITY_COEFFICIENT = 4;
	private final double TURNRATE = 40;
	private final long DELAY_MILLIS = 1000;
	private long lastMesurementTimestamp = 0;
	private double[] currSpeedOfRobotsXY = { 0, 0, 0, 0, 0, 0, 0, 0 };

	// {robot1:direction,distance;robot2:direction,distance;...}
	private double[] currPositionOfRobotsXY = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private double[] lastPositionOfRobotsXY = { 0, 0, 0, 0, 0, 0, 0, 0 };

	public BehaviorAlignToNeighbours(DifferentialPilot pilot,
			SampleProvider provider) {
		this.pilot = pilot;
		this.provider = provider;
		this.sampleValues = new float[provider.sampleSize()];
	}

	public void setPositionOfAllRobotsTimeInterval() {

		if (System.currentTimeMillis() > lastMesurementTimestamp + DELAY_MILLIS) {
			lastMesurementTimestamp = System.currentTimeMillis();

			for (int i = 0; i < sampleValues.length / 2; i++) {
				double direction = sampleValues[i * 2] * ANGLE_ERROR_COEFFICIENT;
				double distance = sampleValues[(i * 2) + 1];

				if (direction > -SENSOR_ANGLE_MAX
						&& direction < SENSOR_ANGLE_MAX && distance < SENSOR_DISTANCE_MAX) {
					currPositionOfRobotsXY[i * 2] = (distance * Math.sin(Math.toRadians(direction)));
					currPositionOfRobotsXY[(i * 2) + 1] = (distance * Math.cos(Math.toRadians(direction)));
				} else {
					currPositionOfRobotsXY[i * 2] = 0;
					currPositionOfRobotsXY[(i * 2) + 1] = Double.POSITIVE_INFINITY;
				}
			}

			setSpeedOfAllRobots();

			for (int i = 0; i < sampleValues.length; i++) {
				lastPositionOfRobotsXY[i] = currPositionOfRobotsXY[i];
			}
		}
	}

	public void setSpeedOfAllRobots() {

		double currOrientation = pilot.getAngleIncrement();

		double currX = pilot.getMovementIncrement() * Math.sin(Math.toRadians(currOrientation));
		double currY = pilot.getMovementIncrement() * Math.cos(Math.toRadians(currOrientation));
		double[] currCenterPoint = { currX, currY };

		// currPositionOfRobotsXYtranslatedToOldCartesianSystem
		double[] translatedOldXY = new double[currPositionOfRobotsXY.length];

		for (int i = 0; i < currSpeedOfRobotsXY.length / 2; i++) {

			double[] robotXY = { lastPositionOfRobotsXY[i * 2],
					lastPositionOfRobotsXY[(i * 2) + 1] };
			double[] point = CalculationUtility.translateManeuver(currCenterPoint, Math.toRadians(currOrientation), robotXY);

			// without translation ->
			//double[] point = {lastPositionOfRobotsXY[i * 2], lastPositionOfRobotsXY[(i * 2) + 1]};
			
			translatedOldXY[i * 2] = point[0];
			translatedOldXY[(i * 2) + 1] = point[1];

			currSpeedOfRobotsXY[i * 2] = currPositionOfRobotsXY[i * 2]
					- translatedOldXY[i * 2];
			currSpeedOfRobotsXY[(i * 2) + 1] = currPositionOfRobotsXY[(i * 2) + 1]
					- translatedOldXY[(i * 2) + 1];
		}
		pilot.reset();
	}

	private void matchNeighbour() {
		setPositionOfAllRobotsTimeInterval();

		List<Integer> robotsInSight = new ArrayList<Integer>();
		for (int i = 0; i < currSpeedOfRobotsXY.length / 2; i++) {
			double direction = currPositionOfRobotsXY[i * 2];
			double distance = currPositionOfRobotsXY[(i * 2) + 1];
			if (distance < SENSOR_DISTANCE_MAX
					&& (direction > -SENSOR_ANGLE_MAX && direction < SENSOR_ANGLE_MAX)) {
				robotsInSight.add(i);
			}
		}
		int robotsInSightCounter = robotsInSight.size();

		double travelDirection = 0; // avgAngle
		double travelSpeed;
		
		if(robotsInSightCounter > 0){
			double travelDistance = 0;
			double travelDistanceCentimeter;
			
			while (!robotsInSight.isEmpty()) {
				int robotId = robotsInSight.get(0);
				robotsInSight.remove(0);
				double x = currSpeedOfRobotsXY[robotId * 2];
				double y = currSpeedOfRobotsXY[(robotId * 2) + 1];
				travelDistance += Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
				
				if(x < 0.0001 || x > 0.0001){
					travelDirection += Math.atan(y / x);
				}
			}
			travelDistance = travelDistance / robotsInSightCounter;
			//travelDistance = (pilot.getMovementIncrement() + travelDistance) / 2;
	
			travelDirection = Math.toDegrees(travelDirection / robotsInSightCounter);
	
			travelDistanceCentimeter = travelDistance / 2;
			travelSpeed = (travelDistanceCentimeter * VELOCITY_COEFFICIENT) / (DELAY_MILLIS / 1000);
		}
		else{ // initial
			travelSpeed = 4.44;
		}
		
		// LCD.clear();
		LCD.drawString("Direction: "+ travelDirection  + "   ", 0, 1);
		LCD.drawString("Speed: "+ travelSpeed + "   ", 0, 2);

		for (int i = 0; i < currPositionOfRobotsXY.length / 2; i++) {
			LCD.drawString(
					i + 1 + ": " + Math.floor(currSpeedOfRobotsXY[i * 2])
							+ " , "
							+ Math.floor(currSpeedOfRobotsXY[(i * 2) + 1]) + "   ",
					0, 3 + i);
		}

		
		if(travelSpeed < 1){
			pilot.setTravelSpeed(4.44);
		}
		else{
			pilot.setTravelSpeed(travelSpeed);
		}
		
		if(travelDirection > -1 && travelDirection < 1){
			pilot.forward();
		}
		else{
			pilot.steer(TURNRATE * (travelDirection / Math.abs(travelDirection)),travelDirection);
		}
		
		suppressed = true;
	}

	@Override
	public boolean takeControl() {

		provider.fetchSample(sampleValues, 0);
		/*
		 * check whether there are any robots in the vicinity if yes, then this
		 * Behavior takes over
		 */
		for (int i = 0; i < sampleValues.length / 2; ++i) {
			float distance = sampleValues[(i * 2) + 1];

			// return TRUE as soon as at least 1 robot is found to be near
			if (distance >= 0 && distance < 20) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void action() {
		suppressed = false;

		LCD.drawString("Aligning...", 0, 0);

		matchNeighbour();
		while (!suppressed) {
			Thread.yield();
		}
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
