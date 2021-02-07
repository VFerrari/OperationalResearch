package problems.qbfpt.solvers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import problems.qbf.solvers.GA_QBF;
import problems.qbfpt.QBFPT_GA;
import solutions.Solution;


/**
 * Metaheuristic GA (Genetic Algorithm) for obtaining an optimal solution 
 * to a QBFPT (Quadractive Binary Function  with Prohibited Triples).
 *
 * @author rsaraiva, sipamplona, vferrari
 */
public class GA_QBFPT extends GA_QBF {
	
    /**
     * The set T of prohibited triples.
     */
    private final ArrayList<List<Integer[]>> T;

	/**
	 * Constructor for the GA_QBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
     * @param seed
     * 		Seed to initialize random number generator.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_QBFPT(Integer generations, 
			        Integer popSize, 
			        Double mutationRate, 
			        String filename,
			        populationReplacement popMethod,
				    Boolean divMaintenance,
				    Integer seed) throws IOException {
		super(generations, 
			  popSize, 
			  mutationRate, 
			  filename, 
			  popMethod, 
			  divMaintenance, 
			  seed);
		
        // Instantiate QBFPT problem, store T and update objective reference.
        QBFPT_GA qbfpt = new QBFPT_GA(filename);
        T = qbfpt.getT();
        ObjFunction = qbfpt;
	}
	
    /**
     * Viabilizes chromosomes for QBFPT.
     * Checks if a triple is being violated, and removes random element from said triple.
     * @param ind Individual to be viabilized for QBFPT. 
     * @return viable individual.
     */
    private Chromosome viabilize(Chromosome ind) {
        Integer e, i;
    	
		for (i = 0; i < chromosomeSize; i++) {
			
			// If the gene is not active, ignore.
			if(ind.get(i) == 0) continue;
			
			// Check triples
			for (Integer[] t : T.get(i)) {
				
				// If the triple is active (infeasible), set random element as 0.
				if (ind.get(t[0]) == 1 && ind.get(t[1]) == 1 && ind.get(t[2]) == 1) {
					e = rng.nextInt(3);
					ind.set(t[e], 0);
				}
			}
		}
    	
    	return ind;
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {
		Chromosome chromosome = new Chromosome();
		
		// Generate
		for (int i = 0; i < chromosomeSize; i++) {
			chromosome.add(rng.nextInt(2));
		}
		
		// Viabilize and return
		return viabilize(chromosome);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds penalty if solution is infeasible.
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {
		Solution<Integer> sol = decode(chromosome);
				
		return sol.cost; //- (isSolutionFeasible(sol) ? 0 : 1000000);
	}
	
	/**
	 * Check if any triple restriction is violated.
	 */
    private boolean isSolutionFeasible(Solution<Integer> sol) {
        boolean feasible = true;
        Set<Integer> _sol = new HashSet<Integer>(sol);
                
    	for (Integer e : sol) {
    		for(Integer[] t : T.get(e)) {
    			if(_sol.contains(t[1]) && _sol.contains(t[2])) {
    				feasible = false;
    				break;
    			}
    		}
    		if(!feasible) break;
        }
    	
    	return feasible;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Viabilizes infeasible solutions.
     */
    protected Population selectPopulation(Population original, Population offsprings) {
    	
    	// Viabilize
    	for(int i=0; i<offsprings.size(); i++) 
    		offsprings.set(i, viabilize(offsprings.get(i)));
    	
    	// Select pop.
		if(this.popMethod == populationReplacement.ELITE)
			return elitism(offsprings);
		else if(this.popMethod == populationReplacement.STSTATE)
			return steadyState(original, offsprings);
		
		return offsprings;
	}
	
    /**
     * Run GA for QBFPT
     */
	public static void run(Integer generations,
						   Integer popSize,
						   Double mutationRateInd,
						   String filename,
						   populationReplacement popMethod,
						   Boolean divMaintenance,
						   Double maxTime,
						   Integer refCost,
						   Integer smallCost,
						   Integer seed,
						   FileWriter fileWriter)
					   throws IOException {

		Double mutationRate;
		Integer size;
		
		// Mutation rate.
		if(mutationRateInd < 0.0) {
			size = Integer.parseInt(filename);
			mutationRate = 1.0 / (float)size;
		}
		else {
			mutationRate = mutationRateInd;
		}
		
		long startTime = System.currentTimeMillis();
		
		GA_QBFPT ga = new GA_QBFPT(generations,
								   popSize, 
								   mutationRate,
								   "instances/qbf" + filename, 
								   popMethod, 
								   divMaintenance,
								   seed);

		Map<String, Object> log = ga.solve(maxTime, refCost, smallCost);

		if (fileWriter != null) {
			fileWriter.append("qbf" + filename + ";" + log.get("objectiveFunction") + ";" + log.get("targetTime") + ";" + log.get("totalTime") + "\n");
		}
	}
	
	public static void testAll(Integer generations, 
						       Integer popSize, 
						       Double mutationRate, 
						       populationReplacement popMethod,
							   Boolean divMaintenance,
							   Double maxTime,
							   Integer refCost,
							   Integer smallCost,
							   Integer seed) 
					   throws IOException {
				
		String inst[] = {"020", "040", "060", "080", "100", "200", "400"};

		// create a text file
		FileWriter fileWriter = new FileWriter("results/GA_QBFPT_PP.txt");

		for(String file : inst) {
			GA_QBFPT.run(generations, popSize, mutationRate, file, popMethod, 
						 divMaintenance, maxTime, refCost, smallCost, seed, fileWriter);
		}

		fileWriter.close();
	}

	
	/**
     * A main method used for testing the GA metaheuristic.
     */
	public static void main(String args[]) throws IOException {
		Integer nTimes = 100;
		final Random seeds = new Random(1327);
		
		// Fixed parameters
		Integer popSize = 50;
		Double mutationRate = -1.0; 
    	Integer maxRefCost = Integer.MAX_VALUE;
    	Integer refCost100 = 1263, smallCost100 = 1000;

		// Changeable parameters.
    	Integer generations1 = 10000, generations2 = 5000; 
		Double maxTime1 = 1800.0, maxTime2 = 600.0;
		Integer seed0 = 0;
		
		// Testing
//		GA_QBFPT.run(generations, popSize, mutationRate, 
//					 "020", populationReplacement.STSTATE, 
//					 true, maxTime1, maxRefCost, maxRefCost, seed0);
		
		// Performance Profiles
		GA_QBFPT.testAll(generations1, popSize, mutationRate, 
						 populationReplacement.STSTATE, true, 
						 maxTime1, maxRefCost, maxRefCost, seed0);
		
		// TTT Plots
		FileWriter fileWriter = new FileWriter("results/GA_QBFPT_TTT.txt");
    	for(int i = 0; i < nTimes; i++) {
    		GA_QBFPT.run(generations2, popSize, mutationRate, "100", 
    				     populationReplacement.STSTATE, true, maxTime2, 
    				     refCost100, smallCost100, seeds.nextInt(), fileWriter);
    	}

    	fileWriter.close();
	}
}
