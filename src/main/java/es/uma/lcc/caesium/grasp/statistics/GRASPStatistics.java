package es.uma.lcc.caesium.grasp.statistics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;


/**
 * Statistics for reactive GRASP
 * @author ccottap
 * @version 1.0
 */
public class GRASPStatistics {
	/**
	 * Fitness statistics
	 */
	private List<List<GRASPStatisticEntry>> stats;
	/**
	 * Fitness statistics of the current run
	 */
	private List<GRASPStatisticEntry> currentStats;
	/**
	 * Solution statistics
	 */
	private List<List<GRASPSolutionEntry>> sols;
	/**
	 * Wolution statistics of the current run
	 */
	private List<GRASPSolutionEntry> currentSols;
	/**
	 * GRASP probabilities
	 */
	private List<List<GRASPProbabilityEntry>> probs;
	/**
	 * Current probabilities
	 */
	private List<GRASPProbabilityEntry> currentProbs;
	/**
	 * current best
	 */
	private double currentBest;
	/**
	 * list of seeds used in each run
	 */
	private List<Long> seeds;
	/**
	 * last seed used
	 */
	private long currentSeed;
	/**
	 * to measure computational times
	 */
	private List<Double> runtime;
	/**
	 * time at the beginning of a run
	 */
	private long tic;
	/**
	 * time at the end of a run
	 */
	private long toc;
	
	
	/**
	 * whether a run is active or not 
	 */
	protected boolean runActive;
	
	/**
	 * Default constructor
	 */
	public GRASPStatistics() {
		clear();
	}
	
	/**
	 * Clears all statistics
	 */
	public void clear() {
		stats = new ArrayList<List<GRASPStatisticEntry>> ();
		currentStats = null;
		sols = new ArrayList<List<GRASPSolutionEntry>> ();
		currentSols = null;
		probs = new ArrayList<List<GRASPProbabilityEntry>>();
		currentProbs = null;
		seeds = new LinkedList<Long>();
		runtime = new LinkedList<Double>();
		runActive = false;	
	}
	
	/**
	 * Logs the start of a new run
	 * @param s the current seed
	 */
	public void newRun(long s) {
		if (runActive)
			closeRun();
		currentStats = new ArrayList<GRASPStatisticEntry> ();
		currentSols = new ArrayList<GRASPSolutionEntry>();	
		currentProbs = new ArrayList<GRASPProbabilityEntry>();	
		runActive = true;
		currentBest = Double.POSITIVE_INFINITY;
		currentSeed = s;
		tic = System.nanoTime();
	}
	
	/**
	 * Closes the current run and commits the statistics to the
	 * global record.
	 */
	public void closeRun() {
		if (runActive) {
			seeds.add(currentSeed);
			toc = System.nanoTime();
			runtime.add((toc-tic)/1e9);
			stats.add(currentStats);
			sols.add(currentSols);
			probs.add(currentProbs);
		}
		currentStats = null;
		currentSols = null;
		runActive = false;
	}
	
	
	/**
	 * Takes statistics at a given time
	 * @param iter number of iterations so far
	 * @param f the fitness of the last solution generated
	 * @param ranks the ranks of the last solution generated
	 * @param solution the last solution generated
	 */
	public void takeStats(int iter, double f, List<Integer> ranks, Object solution) {
		currentStats.add(new GRASPStatisticEntry(iter, Math.min(currentBest, f)));
		if (f < currentBest) {
			currentBest = f;
			currentSols.add(new GRASPSolutionEntry(iter, f, new ArrayList<Integer>(ranks), solution));
		}
	}
	
	/**
	 * Takes statistics at a given time of the GRASP probabilities
	 * @param iter number of iterations so far
	 * @param prob the probabilities of each parameter
	 */
	public void takeProbStats (int iter, Map<Integer, Double> prob) {
		currentProbs.add(new GRASPProbabilityEntry(iter, new ArrayList<Double>(prob.values())));
	}
	
	
	/**
	 * Returns the best fitness of a given run
	 * @param i the index of the run
	 * @return the best fitness in the i-th run
	 */
	public Double getBestFitness(int i) {
		List<GRASPSolutionEntry> l = sols.get(i);
		return l.get(l.size()-1).f();
	}
	
	/**
	 * Returns the best fitness of all runs
	 * @return the best fitness of all runs
	 */
	public Double getBestFitness() {
		int n = sols.size();
		double best = Double.POSITIVE_INFINITY;
		for (int i=0; i<n; i++) {
			double f = getBestFitness(i);
			if (f < best) {
				best = f;
			}
		}
		return best;
	}
	
	/**
	 * Returns the ranks of the best solution found so far in the current run
	 * @return the ranks of the best solution found so far in the current run
	 */
	public List<Integer> getCurrentBestRanks() {
		return currentSols.get(currentSols.size()-1).ranks();
	}
	
	/**
	 * Returns the best solution found so far in the current run
	 * @return the best solution found so far in the current run
	 */
	public Object getCurrentBest() {
		return currentSols.get(currentSols.size()-1).solution();
	}
	
	/**
	 * Returns the ranks of the best solution of a given run
	 * @param i the index of the run
	 * @return the ranks of the best solution in the i-th run
	 */
	public List<Integer> getBestRanks(int i) {
		List<GRASPSolutionEntry> l = sols.get(i);
		return l.get(l.size()-1).ranks();
	}
	
	/**
	 * Returns the best solution of a given run
	 * @param i the index of the run
	 * @return the best solution in the i-th run
	 */
	public Object getBest(int i) {
		List<GRASPSolutionEntry> l = sols.get(i);
		return l.get(l.size()-1).solution();
	}

	/**
	 * Returns the ranks of the best solution of all runs
	 * @return the ranks of the best solution of all runs
	 */
	public List<Integer> getBestRanks() {
		int n = sols.size();
		int ind = -1;
		double best = Double.POSITIVE_INFINITY;
		for (int i=0; i<n; i++) {
			List<GRASPSolutionEntry> l = sols.get(i);
			double f = l.get(l.size()-1).f();
			if (f < best) {
				best = f;
				ind = i;
			}
		}
		List<GRASPSolutionEntry> l = sols.get(ind);
		return l.get(l.size()-1).ranks();
	}
	
	/**
	 * Returns the best solution of all runs
	 * @return the best solution of all runs
	 */
	public Object getBest() {
		int n = sols.size();
		int ind = -1;
		double best = Double.POSITIVE_INFINITY;
		for (int i=0; i<n; i++) {
			List<GRASPSolutionEntry> l = sols.get(i);
			double f = l.get(l.size()-1).f();
			if (f < best) {
				best = f;
				ind = i;
			}
		}
		List<GRASPSolutionEntry> l = sols.get(ind);
		return l.get(l.size()-1).solution();
	}
	
	/**
	 * Returns the CPU time of a certain run
	 * @param i the index of the run
	 * @return the CPU time of the i-th run
	 */
	public double getTime(int i) {
		return runtime.get(i);
	}
	
	/**
	 * Returns a list of double values in JSON format
	 * @param prob a list of double values
	 * @return a list of double values in JSON format
	 */
	private JsonArray doubleList2JsonArray (List<Double> prob) {
		JsonArray array = new JsonArray();
		for (double p: prob)
			array.add(p);
		return array;
	}
	
	/**
	 * Returns a list of integer values in JSON format
	 * @param prob a list of integer values
	 * @return a list of integer values in JSON format
	 */
	private JsonArray intList2JsonArray (List<Integer> prob) {
		JsonArray array = new JsonArray();
		for (int p: prob)
			array.add(p);
		return array;
	}
	
	/**
	 * Returns the data of a certain run in JSON format
	 * @param i the run index
	 * @return a JSON object with the data of the i-th run
	 */
	public JsonObject toJSON(int i) {
		JsonObject run = new JsonObject();
		run.put("run", i);
		run.put("seed", seeds.get(i));
		run.put("time", runtime.get(i));
		
		JsonObject json = new JsonObject();
		
		JsonObject jsonstats = new JsonObject();		
		JsonArray jsonevals = new JsonArray();
		JsonArray jsonbest = new JsonArray();
		List<GRASPStatisticEntry> data = stats.get(i);
		for (GRASPStatisticEntry s: data) {
			jsonevals.add(s.iter());
			jsonbest.add(s.best());
		}		
		jsonstats.put("evals", jsonevals);
		jsonstats.put("best", jsonbest);
		json.put("idata", jsonstats);
		
		JsonObject jsonsols = new JsonObject();		
		JsonArray jsonsolsevals = new JsonArray();
		JsonArray jsonsolsfitness = new JsonArray();
		JsonArray jsonsolsranks = new JsonArray();
		List<GRASPSolutionEntry> soldata = sols.get(i);
		for (GRASPSolutionEntry p: soldata) {
			jsonsolsevals.add(p.iter());
			jsonsolsfitness.add(p.f());
			jsonsolsranks.add(intList2JsonArray(p.ranks()));
		}		
		jsonsols.put("evals", jsonsolsevals);
		jsonsols.put("fitness", jsonsolsfitness);
		jsonsols.put("genome", jsonsolsranks);
		json.put("isols", jsonsols);
		
		JsonObject jsonprobabilities = new JsonObject();		
		JsonArray jsonprobs = new JsonArray();
		JsonArray jsonprobsevals = new JsonArray();
		List<GRASPProbabilityEntry> dataProb = probs.get(i);
		for (GRASPProbabilityEntry s: dataProb) {
			jsonprobsevals.add(s.iter());
			jsonprobs.add(doubleList2JsonArray(s.prob()));
		}
		jsonprobabilities.put("evals", jsonprobsevals);
		jsonprobabilities.put("prob", jsonprobs);
		json.put("probdata", jsonprobabilities);
		JsonArray arr = new JsonArray();
		arr.add(json);
		run.put("rundata", arr);

		return run;
	}
	
	/**
	 * Returns the data of all runs in JSON format. Any active, non-closed run is not recorded.
	 * @return a JSON array with the data of all runs
	 */
	public JsonArray toJSON() {
		JsonArray jsondata = new JsonArray();
		int n = stats.size();
		for (int i=0; i<n; i++)
			jsondata.add(toJSON(i));
		return jsondata;
	}
	
	@Override
	public String toString() {
		String str = "";
		int runs = stats.size();
		for (int i=0; i<runs; i++) {
			List<GRASPStatisticEntry> runstats = stats.get(i);
			str += "Run " + i + "\n=======\n";
			str += "#evals\tbest\n------\t----\n";
			for (GRASPStatisticEntry s: runstats) {
				str += s.iter() + "\t" + s.best() + "\n";
			}
		}
		return str;
	}

}
