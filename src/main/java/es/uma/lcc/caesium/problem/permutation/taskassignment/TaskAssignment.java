package es.uma.lcc.caesium.problem.permutation.taskassignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

/**
 * Task assignment problem
 * 
 * @author ccottap
 *
 */
public class TaskAssignment {
	/**
	 * number of tasks
	 */
	protected int numTasks;
	/**
	 * cost of assigning each task to each agent
	 */
	protected int[][] cost;
	/**
	 * class-level random generator;
	 */
	protected static Random r = new Random(1);
	/**
	 * minimal cost value used when generating random instances
	 */
	static final int MINCOST = 10;

	/**
	 * Main constructor. Randomizes the costs
	 * 
	 * @param n the number of agents/tasks
	 */
	public TaskAssignment(int n) {
		numTasks = n;
		int val = Math.max(n, MINCOST);
		cost = new int[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				cost[i][j] = r.nextInt(val) + 1;
	}

	/**
	 * Reads the problem instance from a file
	 * 
	 * @param filename the name of the file
	 * @throws FileNotFoundException if the file cannot be read
	 */
	public TaskAssignment(String filename) throws FileNotFoundException {
		Scanner inputFile = new Scanner(new File(filename));

		numTasks = inputFile.nextInt();
		cost = new int[numTasks][numTasks];
		for (int i = 0; i < numTasks; i++)
			for (int j = 0; j < numTasks; j++)
				cost[i][j] = inputFile.nextInt();

		inputFile.close();
	}

	/**
	 * Returns the cost of assigning a task to an agent
	 * 
	 * @param agent the agent index
	 * @param task  the task index
	 * @return the cost of the assignment
	 */
	public int getCost(int agent, int task) {
		return cost[agent][task];
	}

	/**
	 * Returns the number of tasks
	 * 
	 * @return the number of tasks
	 */
	public int getNumTasks() {
		return numTasks;
	}

	/**
	 * Returns a printable representation of the problem instance
	 * 
	 * @return a string representing the problem instance
	 */
	public String toString() {
		String cad = "" + numTasks + "\n";
		for (int i = 0; i < numTasks; i++) {
			for (int j = 0; j < numTasks; j++)
				cad = cad + cost[i][j] + "\t";
			cad = cad + "\n";
		}
		return cad;
	}

	/**
	 * Writes the problem instance to a file
	 * 
	 * @param filename the name of the file
	 * @throws IOException if the file cannot be written
	 */
	public void writeToFile(String filename) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(filename));
		out.print(this);
		out.close();
	}
	
	
	/**
	 * Creates a random instance and writes it to a file
	 * @param args command-line parameters: number of agents, seed (optional)
	 * @throws IOException if the file cannot be written
	 */
	public static void main (String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Parameter: <num-agents> [<seed>]");
		}
		else {
			int n = Integer.parseInt(args[0]);
			if (args.length > 1) {
				long seed = Long.parseLong(args[1]);
				r.setSeed(seed);
			}
			TaskAssignment p = new TaskAssignment(n);
			p.writeToFile("random" + n + ".tap");
		}
	}

}
