
import lejos.hardware.Button;
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
import lejos.utility.Delay;

public class RoboBoid {
	
	private SampleProvider seek;
    private float[] sample;
    
    private SampleProvider average;
    private float[] sampleAvg;
   
	WorldModel worldModel = new WorldModel();
    
	private RegulatedMotor motorLeft;
	private RegulatedMotor motorRight;
    private int speedLeft;
    private int speedRight;

	
	public RoboBoid(){
		
		Port port = LocalEV3.get().getPort("S2");
	    @SuppressWarnings("resource")
		SensorModes sensor = new EV3IRSensor(port);
	    seek = sensor.getMode("Seek");
	    sample = new float[seek.sampleSize()];
	    
	    average = new MeanFilter(seek, 5);
	    sampleAvg = new float[average.sampleSize()];
	    
		motorLeft = new EV3LargeRegulatedMotor(MotorPort.D);
		motorRight = new EV3LargeRegulatedMotor(MotorPort.A);
	    speedLeft = 300;  // value in degrees/sec, MAX=~ 900
	    speedRight = 300;
	}
		
		
		

    public void run(){
    	    	
		while(Button.getButtons() != Button.ID_ESCAPE){
			
			seek.fetchSample(sample, 0);
	        
	        average.fetchSample(sampleAvg, 0); // sounds helpful

	    	AveragePoint center = worldModel.getCenterPoint(sampleAvg);
	        
	        //TODO: use all iterations
	        for (int i = 0; i < sample.length/2; i++) {
	        	float direction = sample[i*2];
	        	float distance = sample[(i*2)+1];
	        	
	        	//TODO: Add crazy math stuff here!!!
	        	speedLeft = 250;
		        speedRight = 250;
	        	if(direction < -4.0) {
	        		speedLeft = 300;
			        speedRight = 100; 
	        	}
	        	if(direction > 4.0) {
	        		speedLeft = 100;
			        speedRight = 300; 
	        	}
	        	
	        	if(distance > 10000){ distance = 0; }
	        	if(distance > 100){ distance = 100; }
	        	speedLeft = speedLeft + (int)(distance * 2.5);
		        speedRight = speedRight + (int)(distance * 2.5);
	        	
	        }
			
	        motorLeft.setSpeed(speedLeft);
	        motorRight.setSpeed(speedRight);
	        motorLeft.forward();  // this method really seems necessary again
	        motorRight.forward(); 
		
	        
	        printSensorAndActuatorValues();
			
	        programFlow();
		}
		
    }
    
    public void programFlow(){
    	//TODO: this could be made more advanced (maybe)
    	Delay.msDelay(100);
    }
    
    public void printSensorAndActuatorValues(){    
		LCD.clear();
		LCD.drawString("IR Seeker ", 0, 0);
		for (int i = 0; i < sample.length/2; i++) {
			LCD.drawString(i+1+ ": " + sample[i*2] + ", " + sample[(i*2)+1], 0, 1 + i);
		}
		LCD.drawString("Motor Speed", 0, 6);
		LCD.drawString("L: " + speedLeft + "  R: " + speedRight, 0, 7);
		
		//LCD.refresh(); // do not use, not working well
		//LCD.setAutoRefresh(true);
	    //LCD.setAutoRefreshPeriod(1000);
	}
    
    public void close(){
		motorLeft.stop();
        motorRight.stop();
        motorLeft.close();
        motorRight.close();
    }

}
