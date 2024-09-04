package es.uma.lcc.caesium.grasp.statistics;

import java.util.List;

/**
 * An entry in the solution statistics of reactive GRASP
 * @param iter the current iteration
 * @param f fitness of the best solution so far
 * @param ranks the ranks of the best solution so far
 * @param solution the best solution so far
 * @author ccottap
 * @version 1.0
 */
public record GRASPSolutionEntry(int iter, double f, List<Integer> ranks, Object solution) {
}
