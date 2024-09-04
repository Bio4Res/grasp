package es.uma.lcc.caesium.grasp.statistics;

import java.util.List;

/**
 * An entry in the probability statistics of reactive GRASP
 * @param iter the current iteration
 * @param prob the current probabilities of each parameter
 * @author ccottap
 * @version 1.0
 */
public record GRASPProbabilityEntry(int iter, List<Double> prob) {
}
