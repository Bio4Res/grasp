package es.uma.lcc.caesium.grasp.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import es.uma.lcc.caesium.grasp.statistics.GRASPStatistics;

/**
 * Generic superclass for reactive GRASP
 * @author ccottap
 * @version 1.0
 */
public class ReactiveGRASP {
	/**
	 * RNG
	 */
	private Random rng = new Random(1);
	/**
	 * current seed of the RNG
	 */
	private long currentSeed;
	/**
	 * to control verbosity
	 */
	private int verbosityLevel = 0;
	/**
	 * to avoid division by zero
	 */
	private static final double EPSILON1 = 1e-10;
	/**
	 * Laplace-correction
	 */
	private static final double EPSILON2 = 1e-2;
	/**
	 * actual Laplace-correction distributed over all values
	 */
	private double laplace;
	/**
	 * default number of iterations to update probabilities
	 */
	private static final int ITER_UPDATE = 100; 
	/**
	 * number of iterations to update probabilities
	 */
	private int iterUpdate = ITER_UPDATE; 
	/**
	 * default value of the amplification factor when updating probabilities
	 */
	private double AMPLIFICATION = 1.0;
	/**
	 * amplification factor when updating probabilities
	 */
	private double amplification = AMPLIFICATION;
	/**
	 * each of the values for the RCL 
	 */
	private Map<Integer, Double> prob;
	/**
	 * cumulative score of each value  
	 */
	private Map<Integer, Double> score;
	/**
	 * number of times each value has been picked 
	 */
	private Map<Integer, Integer> count;
	/**
	 * best fitness value so far
	 */
	private double bestSoFar;
	/**
	 * each of the values for the RCL 
	 */
	private Set<Integer> values;
	/**
	 * number of iterations
	 */
	private int numIters;
	/**
	 * objective function
	 */
	private GRASPObjectiveFunction gof;
	/**
	 * Statistics
	 */
	private GRASPStatistics stats;
	
	/**
	 * Creates the solver
	 */
	public ReactiveGRASP() {
		prob = new HashMap<Integer, Double>();
		score = new HashMap<Integer, Double>();
		count = new HashMap<Integer, Integer>();
		values = new HashSet<Integer>();
		laplace = 0;
		gof = null;
		stats = new GRASPStatistics();
		setSeed(1);
	}
	
	/**
	 * Sets the seed for the RNG
	 * @param seed the seed for the RNG
	 */
	public void setSeed (long seed) {
		currentSeed = seed;
		rng.setSeed(seed);
	}
	
	/**
	 * Adds a value to the list of RCL control parameters
	 * @param v a value to add to the list
	 */
	public void addValue (int v) {
		values.add(v);
		laplace = EPSILON2 / values.size();
	}
	
	/**
	 * Sets the amplification factor
	 * @param a the amplification factor
	 */
	public void setAmplification (double a) {
		amplification = a;
	}
	
	/**
	 * Sets the number of iterations for updating probabilities 
	 * @param iter number of iteratios for updating probabilities
	 */
	public void setIterUpdate (int iter) {
		iterUpdate = iter;
	}
	
	/**
	 * Sets the number of iterations
	 * @param num number of iterations
	 */
	public void setNumIters (int num) {
		numIters = num;
	}
	
	/**
	 * Sets the objective function
	 * @param gof the objective function
	 */
	public void setObjectiveFunction (GRASPObjectiveFunction gof) {
		this.gof = gof;
	}
	
	/**
	 * Sets the verbosity level (0 = no verbosity)
	 * @param verbosityLevel the verbosity level 
	 */
	public void setVerbosityLevel(int verbosityLevel) {
		this.verbosityLevel = verbosityLevel;
	}
	
	/**
	 * Runs the algorithm. Uses the current seed and increases it, so subsequent invocations will be different.
	 */
	public void run() {
		stats.newRun(currentSeed);
		rng.setSeed(currentSeed++);
				
		prob.clear();
		score.clear();
		count.clear();
		double p = 1.0 / (double)values.size();
		for (int v: values) {
			prob.put (v, p);
			score.put(v, 0.0);
			count.put(v, 0);
		}
		bestSoFar = Double.POSITIVE_INFINITY;
		int n = gof.getNumberOfVariables();
		List<Integer> ranks = new ArrayList<Integer> (n);
		stats.takeProbStats(1, prob);

		double eq = gof.equivalentCost();
		int iter = 0;
		for (double evals = 0; evals < numIters; evals += eq) {
			iter++;
			int i = (int)evals;
			int v = pick (prob);
			ranks.clear();
			for (int j=0; j<n; j++)
				ranks.add(Math.min(rng.nextInt(v+1),n-j-1));

			if (verbosityLevel > 1) {
				System.out.println("value selected: " + v + "\tranks: " + ranks);
			}
			
			Object sol = gof.decode(ranks);
			var ls = gof.improve(sol);
			evals += ls.cost();
			double f = gof.evaluate(ls.solution());

			if (verbosityLevel > 1) {
				System.out.println("solution generated: " + f);
			}
			
			stats.takeStats(i, f, ranks, ls.solution());
			
			if (f < bestSoFar) {
				if (verbosityLevel > 0) {
					System.out.println("new best solution " + f + " (was " + bestSoFar + ")");
				}
				bestSoFar = f;
			}
			score.put(v, score.get(v) + f);
			count.put(v, count.get(v) + 1);
						
			if (iter % iterUpdate == 0) {
				update();
				stats.takeProbStats(i, prob);
			}
		}
		stats.closeRun();
	}
	
	/**
	 * Reactive update of parameter probabilities. 
	 */
	private void update() {
		Map<Integer, Double> Q = new HashMap<Integer, Double>();
		double sigma = 0;
		int n0 = prob.size();
		for (var e: score.entrySet()) {
			int val = e.getKey();
			double avg;
			if (count.get(val) > 0)
				avg = e.getValue()/count.get(val);
			else {
				n0--;
				continue;
			}

			double q = Math.pow(bestSoFar/(avg + EPSILON1), amplification);
			Q.put(val, q);
			sigma += q;
		}
		double correct = EPSILON1 / n0;

		for (var e: prob.entrySet()) {
			int val = e.getKey();
			if (count.get(val) > 0)
				prob.put(val, laplace + (1.0-laplace)*(Q.get(val)+correct)/(sigma + EPSILON1));
			else
				prob.put(val, laplace);
		}

		if (verbosityLevel > 1) {
			System.out.println("Probabilities updated: " + prob);
		}
	}
	
	/**
	 * Runs the algorithm using a specific seed. Saves the current seed and restores it afterwards.
	 * @param i the seed to use in the current run
	 */
	public void run (long i) {
		long oldSeed = currentSeed;
		setSeed(i);
		run();
		setSeed(oldSeed);
	}
	
	/**
	 * Return the statistics of the algorithm.
	 * @return the statistics of the algorithm
	 */
	public GRASPStatistics getStatistics() {
		return stats;
	}
	

	/**
	 * Pick a parameter value given their probabilities
	 * @param prob a map with elements and probabilities
	 * @return an element selected with probability according to the map
	 */
	private int pick(Map<Integer, Double> prob) {
		double r = rng.nextDouble();
		for (var e: prob.entrySet()) {
			r -= e.getValue();
			if (r <= 0)
				return e.getKey();
		}
		assert false;
		return -1;
	}

}
