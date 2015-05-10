import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.Delay;

public class RoboBoid {

	private SampleProvider seek;
	private float[] sample;

	private SampleProvider average;
	private float[] sampleAvg;

	WorldModel worldModel = new WorldModel();

	private RegulatedMotor motorLeft;
	private RegulatedMotor motorRight;
	private DifferentialPilot pilot;

	//private RobotSpeed robotSpeed;

	private AveragePoint center;

	public RoboBoid() {

		Port port = LocalEV3.get().getPort("S2");
		@SuppressWarnings("resource")
		SensorModes sensor = new EV3IRSensor(port);
		seek = sensor.getMode("Seek");
		sample = new float[seek.sampleSize()];

		average = new MeanFilter(seek, 5);
		sampleAvg = new float[average.sampleSize()];

		motorLeft = new EV3LargeRegulatedMotor(MotorPort.D);
		motorRight = new EV3LargeRegulatedMotor(MotorPort.A);
		pilot = new DifferentialPilot(4.32f, 10.3f, motorLeft, motorRight);
		pilot.setTravelSpeed(15);

		//robotSpeed = new RobotSpeed();
	}

	public void run() {

		Behavior exitProgram = new BehaviorExitProgram();
		Behavior moveForward = new BehaviorMoveForward(pilot);
		Behavior findCenter = new BehaviorFindCenterPoint(pilot, average);
		Behavior alignToNeighbour = new BehaviorAlignToNeighbours(pilot, average);
		
		Behavior[] bArray = { moveForward, findCenter, alignToNeighbour, exitProgram };
		Arbitrator arby = new Arbitrator(bArray, false);
		arby.start();

	}

	public void programFlow() {
		// TODO: this could be made more advanced (maybe)
		Delay.msDelay(100);
	}

	public void printSensorAndActuatorValues() {
		LCD.clear();
		// LCD.drawString("IR Seeker ", 0, 0);
		for (int i = 0; i < sample.length / 2; i++) {
			LCD.drawString(i + 1 + ": " + sample[i * 2] + ", "
					+ sample[(i * 2) + 1], 0, 1 + i);
		}
		// LCD.drawString("Motor Speed", 0, 6);
		// LCD.drawString("L: " + speedLeft + "  R: " + speedRight, 0, 7);

		if (center != null) {
			LCD.drawInt((int) center.getAvgDirection(), 0, 6);
			LCD.drawInt((int) center.getAvgDistance(), 0, 7);
		}

		// LCD.refresh(); // do not use, not working well
		// LCD.setAutoRefresh(true);
		// LCD.setAutoRefreshPeriod(1000);
	}

	public void close() {
		pilot.stop();
		motorLeft.stop();
		motorRight.stop();
		motorLeft.close();
		motorRight.close();
	}

}
