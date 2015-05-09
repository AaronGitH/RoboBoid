import lejos.hardware.lcd.LCD;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.subsumption.Behavior;

public class BehaviorFindCenterPoint implements Behavior {

	private DifferentialPilot pilot;
	private SampleProvider provider;
	private float[] sampleValues;

	private boolean suppressed;

	public BehaviorFindCenterPoint(DifferentialPilot pilot,
			SampleProvider provider) {
		this.pilot = pilot;
		this.provider = provider;
		this.sampleValues = new float[provider.sampleSize()];
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
			if (distance > 20 && distance < 99) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void action() {
		suppressed = false;

		LCD.clear(0);
		LCD.drawString("Find Center", 0, 0);
		move();
		while (!suppressed)
			Thread.yield();

		pilot.stop();
	}

	private void move() {
		float avgDirection = 0;
		float avgDistance = 0;
		int count = 0;

		for (int i = 0; i < sampleValues.length / 2; ++i) {

			float direction = sampleValues[i * 2];
			float distance = sampleValues[(i * 2) + 1];

			if (distance > 20 && distance < 99) {
				avgDistance += distance;
				avgDirection += direction;
				count++;
			}
		}

		if (count == 0) {
			//there are no boids in the vicinity anymore
			suppressed = true;
			return;
		}

		avgDirection /= count;
		avgDistance /= count;

		LCD.clear(2);
		LCD.drawInt((int) avgDistance, 0, 2);
		pilot.rotate(avgDirection * 1.5);
		pilot.travel(avgDistance / 3);

		suppressed = true;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
