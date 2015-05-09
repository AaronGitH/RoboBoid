
public class RobotSpeed {
//	
//	private float[] currSpeedOfRobots = {0, 0, 0, 0, 0, 0, 0, 0};
//	
//	// {robot1:direction,distance;robot2:direction,distance;...}
//	private float[] currPositionOfRobots = {0, 0, 0, 0, 0, 0, 0, 0};
//	private float[] lastPositionOfRobots = {0, 0, 0, 0, 0, 0, 0, 0};
//	
//	private final long delayMillis = 1000;
//	private long lastMesurementTimestamp = 0;
//
//	
//	public void setPositionOfAllRobots(float[] sample){
//		
//		if(lastMesurementTimestamp + delayMillis < System.currentTimeMillis()){
//			lastMesurementTimestamp = System.currentTimeMillis();
//			
//			for (int i = 0; i < sample.length/2; i++) {
//                float direction = sample[i*2];
//                float distance = sample[(i*2)+1];
//	        	
//	        	currPositionOfRobots[i] = direction;
//	        	currPositionOfRobots[i] = distance;
//	        }
//			
//			for (int i = 0; i < sample.length; i++) {
//				lastPositionOfRobots[i] = currPositionOfRobots[i];
//			}
//		}
//	}
//	
//	
//	public float[] getSpeedOfAllRobots(){
//		for (int i = 0; i < currSpeedOfRobots.length/2; i++) {
//			// TODO: calculate speed !!!!!!!!
//			// Maybe better: convert polar- to cartesian- coordinates. 
//			//currSpeedOfRobots[i] = currPositionOfRobots[i] - lastPositionOfRobots[i]; // THIS Direction WON'T WORK!
//			currSpeedOfRobots[i+1] = currPositionOfRobots[i+1] - lastPositionOfRobots[i+1];
//		}
//		return currSpeedOfRobots;
//	}

}