import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;


public class BehaviorExitProgram implements Behavior{
	
	@Override
	public boolean takeControl() {
		return Button.getButtons() == Button.ID_ESCAPE;
	}


	@Override
	public void action() {
		LCD.drawString("Exiting....", 0, 0);
		System.exit(0);
	}

	@Override
	public void suppress() {
	}

}
