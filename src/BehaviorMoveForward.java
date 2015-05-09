import java.util.Random;

import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class BehaviorMoveForward implements Behavior {

	private DifferentialPilot pilot;
	private Random random;

	private boolean suppressed;

	public BehaviorMoveForward(DifferentialPilot pilot) {
		this.pilot = pilot;
		
		this.random = new Random(19726);
	}

	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void action() {
		suppressed = false;

		LCD.clear(0);
		LCD.drawString("Move Forward", 0, 0);

		while (!suppressed) {
			move();
			Thread.yield();
		}

		pilot.stop();
	}

	private void move(){
		pilot.travel(random.nextInt(25) + 10);
		
		while (pilot.isMoving()) {
			if (random.nextBoolean()) {
				//here we make some random movements to the left/right
				pilot.steer(random.nextInt(100) - 50); //steer between -50..50
			}
		}
	}
	
	@Override
	public void suppress() {
		suppressed = true;
	}

}
