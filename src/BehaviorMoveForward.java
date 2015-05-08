import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class BehaviorMoveForward implements Behavior {

	private DifferentialPilot pilot;

	private boolean suppressed;

	public BehaviorMoveForward(DifferentialPilot pilot) {
		this.pilot = pilot;
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
			pilot.travel(10);
			Thread.yield();
		}

		pilot.stop();
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
