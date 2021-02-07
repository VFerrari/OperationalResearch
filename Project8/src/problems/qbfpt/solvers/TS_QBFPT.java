package problems.qbfpt.solvers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import metaheuristics.tabusearch.Intensificator;
import problems.qbf.solvers.TS_QBF;
import problems.qbfpt.QBFPT_TS;
import solutions.Solution;

/**
 * Metaheuristic Tabu Search for obtaining an optimal solution to a QBFPT
 * (Quadractive Binary Function with Prohibited Triples).
 * 
 * @author einnarelli, jmenezes, vferrari
 */
public class TS_QBFPT extends TS_QBF {
	private enum SearchStrategy {
		FI,
		BI
	}
	
	private final Integer fake = -1;
	
	/**
     * Step of iterations on which the penalty will be dynamically adjusted.
     */
	private final Integer penaltyAdjustmentStep = 25;
	
    /**
     * The set T of prohibited triples.
     */
    private final Set<List<Integer>> T;
    
    /**
	 * Value to represent local search type.
	 * Can be first-improving (FI) or best-improving (BI). 
	 */
	private final SearchStrategy searchType;
	
	/**
	 * Variable that indicates if strategic oscillation is active. 
	 */
	private final boolean oscillation;

    /**
     * Constructor for the TS_QBFPT class.
     *
     * @param iterations
     *      The number of iterations which the TS will be executed.
     * @param filename
     *      Name of the file for which the objective function parameters
     *      should be read.
     * @param type
     *      Local search strategy type, being either first improving or
     *      best improving.
     * @param intensificator
     *      Intensificator parameters. If {@code null}, intensification is not
     *      applied.
     * @param oscillation
     * 		Indicates if strategic oscillation is active or not.
     * @param seed
     * 		Seed to initialize random number generator.
     * @throws IOException
     *      Necessary for I/O operations.
     */
    public TS_QBFPT(
        Integer tenure, 
        Integer iterations, 
        String filename,
        SearchStrategy type,
        Intensificator intensificator,
        boolean oscillation,
        Integer seed
    ) throws IOException {

        super(tenure, iterations, filename, intensificator, seed);

        // Instantiate QBFPT problem, store T and update objective reference.
        QBFPT_TS qbfpt = new QBFPT_TS(filename);
        T = qbfpt.getT();
        ObjFunction = qbfpt;
        searchType = type;
        this.oscillation = oscillation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see tabusearch.abstracts.AbstractTS#updateCL()
     */
    @Override
    public void updateCL() {
    	
    	// Adjust the oscillation penalty based on the number of infeasible solutions found
    	// in the previous penaltyAdjustmentStep iterations
    	if (iterationsCount!=null && (iterationsCount+1) % penaltyAdjustmentStep == 0)
    	{
    		double penalty = ((QBFPT_TS)ObjFunction).getPenalty();
    		if(iterationsCount - lastFeasibleIteration >= penaltyAdjustmentStep-1)
    			((QBFPT_TS)ObjFunction).setPenalty(Math.min(penalty*2, 1000000.0));
    		else if((iterationsCount+1)%200 == 0)
    			((QBFPT_TS)ObjFunction).setPenalty(Math.max(penalty/2, 0.0000001));
    	}

        // Store numbers in solution and _CL as hash sets.
        Set<Integer> sol = new HashSet<Integer>(currentSol);
        Set<Integer> _CL = new HashSet<Integer>();
        Integer _violator[] = new Integer[ObjFunction.getDomainSize()];

        // Initialize _CL with all elements not in solution.
        for (Integer e = 0; e < ObjFunction.getDomainSize(); e++) {
        	_violator[e]=0;
            if (!sol.contains(e)) {
                _CL.add(e);
            }
        }

        Integer e1, e2, e3;
        Integer infeasible;
        for (List<Integer> t : T) {
        	infeasible = -1;
        	
            /**
             * Detach elements from (e1, e2, e3). They are stored as numbers 
             * from [0, n-1] in sol. and CL, different than in T ([1, n]).
             */
            e1 = t.get(0) - 1;
            e2 = t.get(1) - 1;
            e3 = t.get(2) - 1;

            // e1 and e2 in solution -> e3 infeasible.
            if (sol.contains(e1) && sol.contains(e2)) {
                infeasible = e3;
            }

            // e1 and e3 in solution -> e2 infeasible.
            else if (sol.contains(e1) && sol.contains(e3)) {
                infeasible = e2;
            }

            // e2 and e3 in solution -> e1 infeasible.
            else if (sol.contains(e2) && sol.contains(e3)) {
                infeasible = e1;
            }
            
            if(infeasible > -1) {
            	if(oscillation) 
            		_violator[infeasible]+=1;
            	else 
            		_CL.remove(infeasible);
            }

        }

        CL = new ArrayList<Integer>(_CL);
        ((QBFPT_TS)ObjFunction).setViolations(_violator);
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Solution<Integer> neighborhoodMove() {
		Solution<Integer> sol;
		
		// Check local search method.
		if (this.searchType == SearchStrategy.BI)
			sol = super.neighborhoodMove();
		else
			sol = firstImprovingNM();
	
		return sol;
	}
	
	/*
	 * First improving neighborhood move.
	 */
	private Solution<Integer> firstImprovingNM(){
		
		// Cost Variables
		Double minDeltaCost, minCost;
		Double inCost, outCost, exCost;
		
		// Cand variables.
		Integer firstCandIn = null, firstCandOut = null;
		Integer firstCandExIn = null, firstCandExOut = null;
		
		// Auxiliary variables.
		Double deltaCost;
		Boolean ignoreCand;
		Boolean done = false;
		
		// Initializing.
		minDeltaCost = 0.0;
		inCost = outCost = exCost = Double.POSITIVE_INFINITY;
		updateCL();
		
		// Evaluate insertions
		for (Integer candIn : CL) {
            deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
            ignoreCand = TL.contains(candIn) || fixed.contains(candIn);

			if (!ignoreCand || currentSol.cost+deltaCost < incumbentSol.cost) {
				if (deltaCost < minDeltaCost) {
					inCost = deltaCost;
					firstCandIn = candIn;
					break;
				}
			}
		}
		
		// Evaluate removals
		for (Integer candOut : currentSol) {
            deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
            ignoreCand = TL.contains(candOut) || fixed.contains(candOut);

			if (!ignoreCand || currentSol.cost+deltaCost < incumbentSol.cost) {
				if (deltaCost < minDeltaCost) {
					outCost = deltaCost;
					firstCandOut = candOut;
					break;
				}
			}
		}
		
		// Evaluate exchanges
		for (Integer candIn : CL) {
			for (Integer candOut : currentSol) {
                deltaCost = ObjFunction.evaluateExchangeCost(candIn, 
                											 candOut,
                											 currentSol);
                ignoreCand =
					TL.contains(candIn) ||
					TL.contains(candOut) ||
					fixed.contains(candIn) ||
					fixed.contains(candOut);

				if (!ignoreCand || 
						currentSol.cost+deltaCost < incumbentSol.cost) {
					if (deltaCost < minDeltaCost) {
						exCost = deltaCost;
						firstCandExIn = candIn;
						firstCandExOut = candOut;
						done = true;
						break;
					}
				}
            }
            
            if (done) break;
            
		}
		
		// Implement the best of the first non-tabu moves.
		TL.poll();
		minCost = Math.min(Math.min(inCost, outCost), exCost);
		
		// In case of tie, insertion is prioritized.
		if(minCost == inCost && firstCandIn != null) {
			firstCandOut = null;
		}
		
		// Removal.
		else if (minCost == outCost && firstCandOut != null) {
			firstCandIn = null;
		}
		
		// Exchange
		else if (firstCandExIn != null) {
			firstCandIn = firstCandExIn;
			firstCandOut = firstCandExOut;
		}
		
		// Make the move.
		if (firstCandOut != null) {
			currentSol.remove(firstCandOut);
			CL.add(firstCandOut);
			TL.add(firstCandOut);
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (firstCandIn != null) {
			currentSol.add(firstCandIn);
			CL.remove(firstCandIn);
			TL.add(firstCandIn);
		} else {
			TL.add(fake);
		}
		
		ObjFunction.evaluate(currentSol);
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Check if any triple restriction is violated.
	 */
	@Override
    public boolean isSolutionFeasible(Solution<Integer> sol) {
    	boolean feasible = true;
        Integer e1, e2, e3;
        
        // Check strategic oscillation.
        if(!oscillation) return true;
        
    	for (List<Integer> t : T) {

            /**
             * Detach elements from (e1, e2, e3). They are stored as numbers 
             * from [0, n-1] in sol., different than in T ([1, n]).
             */
            e1 = t.get(0) - 1;
            e2 = t.get(1) - 1;
            e3 = t.get(2) - 1;

            // e1, e2 and e3 in solution -> infeasible.
            if (sol.contains(e1) && sol.contains(e2) && sol.contains(e3)) {
            	feasible = false;
            	break;
            }
        }
    	
    	return feasible;
    }

	/**
	 * Run Tabu Search for QBFPT.
	 */
	public static void run(Integer tenure, 
						   Integer maxIt, 
						   String filename,
						   SearchStrategy searchType,
						   Intensificator intensify,
						   Boolean oscillation,
						   Double maxTime,
						   Integer refCost,
						   Integer smallCost,
						   Integer seed,
						   FileWriter fileWriter)
					   throws IOException {

		TS_QBFPT tabu = new TS_QBFPT(tenure,
									 maxIt,
									 "instances/qbf" + filename, 
									 searchType,
									 intensify,
									 oscillation,
									 seed
									 );

		Map<String, Object> log = tabu.solve(maxTime, refCost, smallCost);

		if (fileWriter != null) {
			fileWriter.append("qbf" + filename + ";" + log.get("objectiveFunction") + ";" + log.get("targetTime") + ";" + log.get("totalTime") + "\n");
		}
	}
	
	public static void testAll(Integer tenure,
							   Integer maxIt,
							   SearchStrategy searchType, 
							   Intensificator intensify,
							   Boolean oscillation,
							   Double maxTime,
							   Integer refCost,
							   Integer smallCost,
							   Integer seed)
					   throws IOException {
		
		String inst[] = {"020", "040", "060", "080", "100", "200", "400"};
		Intensificator intensify2 = intensify;

		// create a text file
		FileWriter fileWriter = new FileWriter("results/TS_QBFPT_PP.txt");
		
		for(String file : inst) {
			if(file == "200" && intensify != null) {
				intensify2 = new Intensificator(2000, 100);
			}
			else {
				intensify2 = intensify;
			}
			
			TS_QBFPT.run(tenure, maxIt, file, searchType, intensify2, oscillation, 
					     maxTime, refCost, smallCost, seed, fileWriter);
		}

		fileWriter.close();
	}
	
	/**
     * A main method used for testing the Tabu Search metaheuristic.
     */
	public static void main(String[] args) throws IOException {
    	Integer nTimes = 100;
    	final Random seeds = new Random(1327);

    	// Fixed Parameters
		Integer tenure = 30;
    	Integer maxRefCost = Integer.MIN_VALUE;
    	Integer refCost100 = -1263, smallCost100 = -1000;

		// Changeable Parameters
		Integer maxIterations1 = 10000, maxIterations2 = 5000;
		Intensificator intensificator = new Intensificator(1000, 100);
		Double maxTime1 = 1800.0, maxTime2 = 600.0;
		Integer seed0 = 0;
		
		// Testing
		// TS_QBFPT.run(tenure, maxIterations, "020", SearchStrategy.BI,
		//			   intensificator, true, maxTime1, maxRefCost, maxRefCost, 
		//			   seed0, null);
		
		// Performance Profile
		TS_QBFPT.testAll(tenure, maxIterations1, SearchStrategy.BI, intensificator, 
						 true, maxTime1, maxRefCost, maxRefCost, seed0);
		
		// TTT Plots
		FileWriter fileWriter = new FileWriter("results/TS_QBFPT_TTT.txt");
    	for (int i = 0; i < nTimes; i++) {
    		TS_QBFPT.run(tenure, maxIterations2, "100", SearchStrategy.BI, intensificator, true, maxTime2, refCost100, smallCost100, seeds.nextInt(), fileWriter);
    	}

    	fileWriter.close();
	}
}
