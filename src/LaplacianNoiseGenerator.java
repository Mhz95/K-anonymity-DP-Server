
import java.util.Random;

public class LaplacianNoiseGenerator {

	public static final double DEFAULT_MU_PARAMETER = 0.0;
	
	public static final double DEFAULT_B_PARAMETER = 1.0; // Scale
	
	public static final long DEFAULT_RANDOM_SEED = 11235813; //just some number
	
	private double mu;
	
	private double b;
		
	private Random randomGenerator;
	
	public LaplacianNoiseGenerator() {
		this(DEFAULT_MU_PARAMETER, DEFAULT_B_PARAMETER);
	}
	
	public LaplacianNoiseGenerator(long randomSeed) {
		this(randomSeed, DEFAULT_MU_PARAMETER, DEFAULT_B_PARAMETER);
	}
	
	public LaplacianNoiseGenerator(double mu, double b) {
		this(DEFAULT_RANDOM_SEED, mu, b);
	}
	
	public LaplacianNoiseGenerator(long randomSeed, double mu, double b) {
		this.mu = mu;
		this.b = b;
		this.randomGenerator = new Random(randomSeed);
	}
	
	public double getMu() {
		return mu;
	}
	
	public double getB() {
		return b;
	}
	
	public void setMu(double mu) {
		this.mu = mu;
	}
	
	public void setB(double b) {
		this.b = b;
	}
	
	public void setRandomSeed(long randomSeed) {
		this.randomGenerator = new Random(randomSeed);
	}
	
	/**
	 * Returns the next pseudorandom "laplacian-distributed" double value from this random number
	 * generator's sequence.
	 * The following calculation is performed to generate the value:
	 * 
	 * X := mu - b * sign(U) * ln(1 - 2 * abs(U))
	 * 
	 * where mu and b are the associated parameters of this random generator, X is 
	 * the returned value and U is a uniformly distributed random number in the [-0.5,0.5) range.
	 *  
	 * @return the next pseudorandom "laplacian-distributed" double value from this random number
	 * generator's sequence
	 */
	public double nextLaplacian() {
		//generate uniformly distributed value within the [-0.5, 0.5) range
		double uniform = randomGenerator.nextDouble() - 0.5;
		
		//compute the difference, avoiding 0 values, in order to avoid the
		// logarithm to return infinity
		double difference = Math.max(Double.MIN_VALUE, (1.0 - 2.0 * Math.abs(uniform)));
		
		//compute the logarithm
		double logarithm = Math.log(difference);
		
		//we do not use Math.signum(), because in case it is 0, it would return
		// 0, but we want it to be strictly greater than 0!
		double sign = sign(uniform);
		
		//compute the Laplacian random number 
		return mu - b * sign * logarithm;
	}
	
	
	public double nextLaplacian(double mu, double b) {
		setMu(mu);
		setB(b);
		return nextLaplacian();
	}
	
	public static double nextLaplacian(Random randomGenerator, double mu, double b) {
		//generate uniformly distributed value within the [-0.5, 0.5) range
		double uniform = randomGenerator.nextDouble() - 0.5;
		
		//compute the difference, avoiding 0 values, in order to avoid the
		// logarithm to return infinity
		double difference = Math.max(Double.MIN_VALUE, (1.0 - 2.0 * Math.abs(uniform)));
		
		//compute the logarithm
		double logarithm = Math.log(difference);
		
		//we do not use Math.signum(), because in case it is 0, it would return
		// 0, but we want it to be strictly greater than 0!
		double sign = sign(uniform);
		
		//compute the Laplacian random number
		return mu - b * sign * logarithm;
	}
	
	private static double sign(double value) {
		return value < 0.0 ? -1.0 : 1.0;
	}
	
}

//OLD

//try {
//System.out.println("\n************ DP *************\n");
//
//int sensitivity = 1;
//double quota = Laplace.quota[0];
//
//double epsilonOne = 0.01;
//Laplace laplaceOne = new Laplace(epsilonOne);
//double noiseOne = laplaceOne.genNoise(sensitivity, quota * epsilonOne);
//double disruptedLatitude = msg.getLatitude() + noiseOne;
//
//System.out.println("Non perturbed value = (" + msg.getLatitude() + "," + msg.getLongitude()
//		+ ")" + "\nFor epsilon = " + epsilonOne + "\nperturbation generated = " + noiseOne
//		+ " perturbed Latitude value = " + disruptedLatitude + "\n");
//
//double epsilonTwo = 0.1;
//Laplace laplaceTwo = new Laplace(epsilonTwo);
//double noiseTwo = laplaceTwo.genNoise(sensitivity, quota * epsilonTwo);
//double disruptedLongitude = msg.getLongitude() + noiseTwo;
//System.out.println("For epsilon = " + epsilonTwo + "\nperturbation generated = " + noiseTwo
//		+ " perturbed Longitude value = " + disruptedLongitude + "\n");
//
//AnonymizedMessage m = new AnonymizedMessage(msg.getID(), disruptedLatitude, disruptedLongitude,
//		msg.getResponse());
//
////AnonymizedMessage tmp_m = new AnonymizedMessage(msg.getID(), disruptedLatitude,
////		disruptedLongitude, msg.getResponse());
//
//AnonymityQueues.outgoingTable.put(m.getID(), m);
//
//AnonymityQueues.outgoingQueue.offer(m);
//
//System.out.println("-Message id Out = " + m.getID());
//myWriter.write("(DP) Message id: " + m.getID() + " New Lat: " + m.getLatitude()
//		+ " New Long: " + m.getLongitude());
//System.out.println("Successfully wrote to the file.");
//} catch (IOException e) {
//System.out.println("An error occurred.");
//e.printStackTrace();
//}