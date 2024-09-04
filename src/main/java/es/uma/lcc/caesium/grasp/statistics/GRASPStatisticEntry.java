package es.uma.lcc.caesium.grasp.statistics;


/**
 * An entry in the statistics of reactive GRASP
 * @param iter the current iteration
 * @param best best fitness so far
 * @author ccottap
 * @version 1.0
 */
public record GRASPStatisticEntry(int iter, double best) {
}
