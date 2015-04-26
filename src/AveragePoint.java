
public class AveragePoint {
		private float avgDistance = 0.0f;
		private float avgDirection = 0.0f;
		
		public AveragePoint(float distance, float direction){
			this.avgDistance = distance;
			this.avgDirection = direction;
		}
		
		public float getAvgDistance(){
			return avgDistance;
		}
		
		public float getAvgDirection(){
			return avgDirection;
		}
}
