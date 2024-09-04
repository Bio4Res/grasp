package es.uma.lcc.caesium.grasp.base;

/**
 * Results of the application of local search
 * @param solution the improved solution
 * @param cost the cost of performing local search
 * @author ccottap
 * @version 1.0
 */
public record LocalSearchResult(Object solution, double cost) {

}
