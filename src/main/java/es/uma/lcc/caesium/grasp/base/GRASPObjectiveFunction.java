package es.uma.lcc.caesium.grasp.base;

import java.util.List;

/**
 * Objective function for GRASP
 * @author ccottap
 * @version 1.0
 */
public interface GRASPObjectiveFunction {
	/**
	 * Returns the number of decisions to create a solution
	 * @return the number of decisions to create a solution
	 */
	int getNumberOfVariables();
	
	/**
	 * Returns the number of function evaluations equivalent to
	 * a single iteration of the construction phase. This is used
	 * to provide a fair measure to compare to black-box optimization 
	 * algorithms
	 * @return the number of function evaluations equivalent to
	 * a single iteration of the construction phase
	 */
	double equivalentCost();

	/**
	 * Decodes a list of ranks
	 * @param ranks a list of ranks
	 * @return the decoded solution
	 */
	Object decode(List<Integer> ranks);
	
	/**
	 * Applies local improvement on a solution. Returns the improved solution
	 * and the additional cost incurred. If no local search is performed, the
	 * same solution and cost = 0 are returned.
	 * @param sol the solution to be improved
	 * @return an improved solution and the associated search cost
	 */
	LocalSearchResult improve (Object sol);

	/**
	 * Returns the fitness of a solution
	 * @param sol a solution
	 * @return the fitness of the solution
	 */
	double evaluate(Object sol);
	
}
