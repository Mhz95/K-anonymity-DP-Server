
import java.util.Random;

public class GaussianNoiseGenerator {

	public static final double DEFAULT_MU_PARAMETER = 0.0;
	
	public static final double DEFAULT_VAR_PARAMETER = 1.0; // Scale
	
	public static final long DEFAULT_RANDOM_SEED = 11235813; //just some number
	
	private double mu;
	
	private double var;
		
	private Random randomGenerator;
	
	public GaussianNoiseGenerator() {
		this(DEFAULT_MU_PARAMETER, DEFAULT_VAR_PARAMETER);
	}
	
	public GaussianNoiseGenerator(long randomSeed) {
		this(randomSeed, DEFAULT_MU_PARAMETER, DEFAULT_VAR_PARAMETER);
	}
	
	public GaussianNoiseGenerator(double mu, double var) {
		this(DEFAULT_RANDOM_SEED, mu, var);
	}
	
	public GaussianNoiseGenerator(long randomSeed, double mu, double var) {
		this.mu = mu;
		this.var = var;
		this.randomGenerator = new Random(randomSeed);
	}
	
	public double getMu() {
		return mu;
	}
	
	public double getVar() {
		return var;
	}
	
	public void setMu(double mu) {
		this.mu = mu;
	}
	
	public void setVar(double var) {
		this.var = var;
	}
	
	public void setRandomSeed(long randomSeed) {
		this.randomGenerator = new Random(randomSeed);
	}
	
	/**
	 * Returns the next pseudorandom, Gaussian ("normally") distributed double value 
	 * with mean 0.0 and standard deviation 1.0 from this random number generator's sequence.
	 * The general contract of nextGaussian is that one double value, chosen from (approximately) 
	 * the usual normal distribution with mean 0.0 and standard deviation 1.0, is pseudorandomly generated and returned.
	 * The method nextGaussian is implemented by class Random. 
	 *
	 * @return the next pseudorandom "Gaussian-distributed" double value from this random number
	 * generator's sequence
	 */	
	public double nextGaussian(double mu, double var) {

		return randomGenerator.nextGaussian() * Math.sqrt(var) + mu;
		
	}

	public double calculateVariance(double sensitivity, double epsilon, double delta) { // Scale
		
		return ((sensitivity * Math.sqrt(2 *  Math.log(1.25 / delta))) / epsilon);
	}
	
}
