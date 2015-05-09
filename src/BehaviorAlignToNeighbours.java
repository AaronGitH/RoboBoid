import java.util.ArrayList;
import java.util.List;

import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

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
	
	private final double WHEEL_DIAMETER_cm = 4.32;
	private final double SENSOR_DISTANCE_MAX = 90;

	private double[] currSpeedOfRobots = { 0, 0, 0, 0, 0, 0, 0, 0 };

	// {robot1:direction,distance;robot2:direction,distance;...}
	private double[] currPositionOfRobotsXY = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private double[] lastPositionOfRobotsXY = { 0, 0, 0, 0, 0, 0, 0, 0 };

	private final long delayMillis = 1000;
	private long lastMesurementTimestamp = 0;

	public BehaviorAlignToNeighbours(DifferentialPilot pilot,
			SampleProvider provider) {
		this.pilot = pilot;
		this.provider = provider;
		this.sampleValues = new float[provider.sampleSize()];
	}

	public void setPositionOfAllRobotsTimeInterval() {

		if (System.currentTimeMillis() > lastMesurementTimestamp + delayMillis) {
			lastMesurementTimestamp = System.currentTimeMillis();

			for (int i = 0; i < sampleValues.length / 2; i++) {
				double direction = sampleValues[i * 2];
				double distance = sampleValues[(i * 2) + 1];

				if (direction < -25 || direction < 25) {
					currPositionOfRobotsXY[i * 2] = Double.POSITIVE_INFINITY;
					currPositionOfRobotsXY[(i * 2) + 1] = 0;
				}
				else{
				currPositionOfRobotsXY[i * 2] = (distance * Math.sin(direction));
				currPositionOfRobotsXY[(i * 2) + 1] = (distance * Math
						.cos(direction));
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

		double currX = pilot.getMovementIncrement()
				* Math.sin(currOrientation);
		double currY = pilot.getMovementIncrement()
				* Math.cos(currOrientation);
		double[] currCenterPoint = { currX, currY };
		
		// currPositionOfRobotsXYtranslatedToOldCartesianSystem
		double[] translatedXY = new double[currPositionOfRobotsXY.length];
		
		for (int i = 0; i < currSpeedOfRobots.length / 2; i++) {

			double[] robotXY = { lastPositionOfRobotsXY[i * 2],
					lastPositionOfRobotsXY[(i * 2) + 1] };
			double[] point =  CalculationUtility.translateManeuver(currCenterPoint,
					currOrientation, robotXY);
			
			translatedXY[i * 2] = point[0];
			translatedXY[(i * 2) + 1] = point[1];
			
			currSpeedOfRobots[i] = translatedXY[i * 2] - lastPositionOfRobotsXY[i * 2];
			currSpeedOfRobots[i+1] = translatedXY[(i * 2) + 1] - lastPositionOfRobotsXY[(i * 2) + 1];
		}
		pilot.reset();
	}

	private void matchNeighbour() {
		setPositionOfAllRobotsTimeInterval();
		
		List<Integer> robotsInSight = new ArrayList<Integer>();
		for (int i = 0; i < currSpeedOfRobots.length / 2; i++) {
			double direction = currPositionOfRobotsXY[i * 2];
			double distance = currPositionOfRobotsXY[(i * 2) + 1];
			if(distance < SENSOR_DISTANCE_MAX && (direction > -25 || direction < 25)){
					robotsInSight.add(i);
			}			
		}
		int robotsInSightCounter = robotsInSight.size();
		
		double travelDistance = 0;
		while( !robotsInSight.isEmpty() ) {
		    int robotId= robotsInSight.get(0);
		    robotsInSight.remove(0);
		    double x = currSpeedOfRobots[robotId * 2];
		    double y = currSpeedOfRobots[(robotId * 2) + 1];
		    travelDistance += Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2) );;
		    
		}
		travelDistance = travelDistance / robotsInSightCounter;
		travelDistance = (pilot.getMovementIncrement() + travelDistance) / 2;
		
		// TODO: define WHEEL_DIAMETER
		double travelDistancecentimeter = travelDistance / 2;
		double travelSpeed = (travelDistancecentimeter / WHEEL_DIAMETER_cm) / (delayMillis/1000);
		
		pilot.setTravelSpeed(travelSpeed); 
		
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
