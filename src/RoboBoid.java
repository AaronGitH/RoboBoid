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
	
	private int speedStraight;
    private int speedLeft;
    private int speedRight;
    
    private RobotSpeed robotSpeed;
    
    private AveragePoint center;
    private java.util.Random noise;
	
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

	    speedStraight = 300;// value in degrees/sec, MAX=~ 900
	    speedLeft = 300;  
	    speedRight = 300;
	    
	    robotSpeed = new RobotSpeed();
	    
	    noise = new java.util.Random(85271); //noise
	}
		
		
		

    public void run(){   	    	
		while(Button.getButtons() != Button.ID_ESCAPE){
			
			seek.fetchSample(sample, 0);
	        
	        average.fetchSample(sampleAvg, 0);
	        
	        robotSpeed.setPositionOfAllRobots(sampleAvg);
	        
	        center = worldModel.getCenterPoint(sampleAvg);
	    	float turnAngle = Math.abs(center.getAvgDirection()) * 10;
	    	
	    	//reduce it by turnAngle * 2 
	    	// => make it turn faster if the central point is to the side of the robot
	    	turnAngle = speedStraight - (turnAngle * 2);
	    	
	    	if (center.getAvgDirection() < 5) {
	    		//turn right
	    		speedRight = speedStraight;
	    		speedLeft = (int)turnAngle;
	    	}
	    	else if (center.getAvgDirection() > 5) {
	    		//turn left
	    		speedRight = (int)turnAngle;
	    		speedLeft = speedStraight;
	    	}
	    	else {
	    		//move straight but add noise depending on the distance
	    		
				//noise at low distance ==> between -50 and 50 wheel speed
    			speedLeft = (int)(speedStraight + (noise.nextFloat() * 100) - 50);
    			speedRight = (int)(speedStraight + (noise.nextFloat() * 100) - 50);

    			if (center.getAvgDistance() > 90) {
	    			// the values from the sensors aren't that reliable, so I add some more noise
	    			speedLeft += (int)((noise.nextFloat() * 100) - 50);
	    			speedRight += (int)((noise.nextFloat() * 100) - 50);
	    		}
	    	}
	    	
	    	float distance = center.getAvgDistance();
	    	
	    	if(distance > 100){ distance = 100; }
	        speedLeft = speedLeft + (int)(distance * 2.5);
		    speedRight = speedRight + (int)(distance * 2.5);
	        
			
	        motorLeft.setSpeed(speedLeft);
	        motorRight.setSpeed(speedRight);
<<<<<<< HEAD
	        //motorLeft.forward();  // this method really seems necessary again
	        //motorRight.forward(); 
=======
	        motorLeft.forward();  // this method really seems necessary again
	        motorRight.forward(); 
>>>>>>> 44bcf05d22b15b3174b1021c7f93213cc6c9a911
	        
            
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
		//LCD.drawString("IR Seeker ", 0, 0);
		for (int i = 0; i < sample.length/2; i++) {
			LCD.drawString(i+1+ ": " + sample[i*2] + ", " + sample[(i*2)+1], 0, 1 + i);
		}
		//LCD.drawString("Motor Speed", 0, 6);
		//LCD.drawString("L: " + speedLeft + "  R: " + speedRight, 0, 7);
		
		if (center != null) {
			LCD.drawInt((int)center.getAvgDirection(),0,6);
			LCD.drawInt((int)center.getAvgDistance(), 0, 7);
		}
		
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
