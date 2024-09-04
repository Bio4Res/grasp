package es.uma.lcc.caesium.grasp.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.uma.lcc.caesium.grasp.base.GRASPObjectiveFunction;
import es.uma.lcc.caesium.grasp.base.LocalSearchResult;
import es.uma.lcc.caesium.problem.permutation.taskassignment.TaskAssignment;


/**
 * Problem specific functions to solve the Task Assignment Problem with GRASP
 * @author ccottap
 * @version 1.1
 */
public class TaskAssignmentGRASPObjectiveFunction implements GRASPObjectiveFunction {
	/**
	 * a task-assignment problem instance
	 */
	private TaskAssignment data;
	/**
	 * default value of the number of neighbors to explore during local search
	 */
	private final static int NUM_NEIGHBORS = 0;
	/**
	 * number of neighbors to explore during local search
	 */
	private int numNeighbors = 0;
	/**
	 * to control verbosity
	 */
	private int verbosityLevel = 0;

	/**
	 * default constructor
	 */
	public TaskAssignmentGRASPObjectiveFunction() {
		data = null;
	}
	
	/**
	 * Creates the objective function given a problem instance
	 * @param data the problem instance
	 */
	public TaskAssignmentGRASPObjectiveFunction(TaskAssignment data) {
		this();
		setProblemData(data);
		setNumNeighbors(NUM_NEIGHBORS);
	}
	
	/**
	 * Sets the verbosity level (0 = no verbosity)
	 * @param verbosityLevel the verbosity level 
	 */
	public void setVerbosityLevel(int verbosityLevel) {
		this.verbosityLevel = verbosityLevel;
	}

	/**
	 * Sets the problem data
	 * @param data a task assignment problem instance
	 */
	public void setProblemData (TaskAssignment data) {
		this.data = data;
	}
	
	/**
	 * Sets the number of neighbors to explore during local search
	 * @param num number of neighbors to explore during local search
	 */
	public void setNumNeighbors (int num) {
		numNeighbors = num;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfVariables() {
		return data.getNumTasks();
	}
	
	@Override
	public double equivalentCost() {
		// There are (n-i+1) candidates in the i-th stage (i from 1 to n).
		// The total number of candidates checked is therefore (n+1)*n/2.
		// A solution involves n variables. Therefore, a single iteration of the 
		// construction phase is equivalent to (n+1)*n/(2*n) = (n+1)/2 evaluations
		return (double)(data.getNumTasks()+1)/2.0;
	}

	/**
	 * Candidate decision
	 * @author ccottap
	 * @version 1.0
	 */
	private record Candidate (int agent, int cost) {} 
	
	/**
	 * Creates a task assignment given a sequence of ranks for the decisions at each stage.
	 * Ranks 0 means the best possible decision, rank 1 the second-best, and so on. If a certain rank
	 * exceeds the number of possibilities, the last one is picked.
	 * @param ranks a list of ranks for each decision
	 * @return the task assignment
	 */
	public List<Integer> decode (List<Integer> ranks) {
		int n = data.getNumTasks();
		assert (ranks.size() == n);
		if (verbosityLevel > 0) {
			System.out.println("Ranks: " + ranks);
		}
		List<Integer> info = new ArrayList<Integer>(n);
		
		Set<Integer> remaining = new HashSet<Integer>(n); // the agents
		for (int i=0; i<n; i++) {
			remaining.add(i);
		}
		
		for (int i=0; i<n; i++) {
			List<Candidate> candidates = getCandidates (info, remaining);
			int d = Math.min(candidates.size()-1, ranks.get(i));
			int agent = candidates.get(d).agent();
			info.add(agent);
			remaining.remove(agent);
		}
		
		return info;	
	}
	
	/**
	 * Return a list of candidates to extend the solution
	 * @param info the current solution
	 * @param remaining the remaining agents to be assigned
	 * @return an ordered list of candidates (better first)
	 */
	private List<Candidate> getCandidates(List<Integer> info, Set<Integer> remaining) {
		int task = info.size();
		List<Candidate> candidates = new ArrayList<Candidate> (data.getNumTasks()-task);
		for (int agent: remaining) {
			candidates.add(new Candidate(agent, data.getCost(agent, task)));
		}
		candidates.sort(Comparator.comparing(Candidate::cost));
		return candidates;
	}

	@Override
	public LocalSearchResult improve(Object sol) {
		if (numNeighbors > 0) {
			@SuppressWarnings("unchecked")
			List<Integer> info = (List<Integer>) sol;
			List<Integer> newInfo = new ArrayList<Integer>(data.getNumTasks());
			int cost = localSearch(info, newInfo);
			return new LocalSearchResult(newInfo, 2.0*(double)cost/(double)data.getNumTasks());
			// multiply by 2 because each neighbor involves modifying two agents
		}

		return new LocalSearchResult(sol, 0);
	}

	/**
	 * Performs steepest-ascent local search on a solution
	 * @param info the original solution
	 * @param newInfo the improved solution
	 * @return the number of neighbors considered
	 */
	private int localSearch(List<Integer> info, List<Integer> newInfo) {
		int n = data.getNumTasks();
		int iter = 0;
		for (int i=0; i<n; i++) {
			newInfo.add(info.get(i));
		}
		while (iter < numNeighbors) {
			int best = 0;
			int bi = -1;
			int bj = -1;
			for (int i=1; i<n; i++) {
				int agent1 = newInfo.get(i);
				int c1 = data.getCost(agent1, i);
				for (int j=0; j<i; j++) {
					int agent2 = newInfo.get(j);
					int net = data.getCost(agent1, j) + data.getCost(agent2, i) - data.getCost(agent2, j) - c1;
					if (net < best) {
						best = net;
						bi = i;
						bj = j;
					}
				}
			}
			if (best < 0) {
				int tmp = newInfo.get(bi);
				newInfo.set(bi, newInfo.get(bj));
				newInfo.set(bj, tmp);
			}
			else 
				break;
		}
		
		
		return iter;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public double evaluate(Object sol) {
		List<Integer> info = (List<Integer>) sol;
		int n = data.getNumTasks();
		int total = 0;
		for (int i=0; i<n; i++) {
			total += data.getCost(info.get(i), i);
		}
		return total;
	}


}
