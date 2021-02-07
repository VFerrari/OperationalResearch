package problems.qbfpt.solvers;

import problems.qbf.solvers.GRASP_QBF;
import problems.qbfpt.triples.ForbiddenTriplesGenerator;
import solutions.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GRASP_QBFPT extends GRASP_QBF {

    private final ForbiddenTriplesGenerator forbiddenTriplesGenerator;
    private final Map<Integer, Integer> frequency = new HashMap<>();

    public GRASP_QBFPT(Double alpha, 
    				   Integer iterations, 
    				   String filename,
    				   Integer seed) throws IOException {
        super(alpha, iterations, filename, seed);
        forbiddenTriplesGenerator = new ForbiddenTriplesGenerator(ObjFunction.getDomainSize());
    }

    @Override
    public void updateCL() {
        if (!this.currentSol.isEmpty()) {
            List<Integer> forbiddenValues = new ArrayList<>();
            Integer lastElement = this.currentSol.get(this.currentSol.size() - 1);
            for (int i = 0; i < this.currentSol.size() - 1; i++) {
                forbiddenValues.addAll(forbiddenTriplesGenerator.getForbiddenValues(this.currentSol.get(i) + 1, lastElement + 1));
            }
            for (Integer forbiddenValue : forbiddenValues) {
                int index = CL.indexOf(forbiddenValue - 1);
                if (index >= 0) {
                    CL.remove(index);
                }
            }
        }
    }

    private Integer getFrequency(Integer elem) {
        Integer freq = frequency.get(elem);
        if (freq == null) {
            return Integer.MIN_VALUE;
        }

        return freq;
    }

    private Integer putIncrease(Integer elem) {
        Integer value = frequency.get(elem);
        if (value == null) {
            frequency.put(elem, 1);
            return 1;
        }

        value = value + 1;
        frequency.put(elem, value);
        return value;
    }

    private Double perturbationFunction(Integer elem) {
        Double f = ObjFunction.evaluateInsertionCost(elem, currentSol);
        return f / (Integer.MAX_VALUE * getFrequency(elem));
    }

    private void updateFrequency() {
        currentSol.stream().forEach(elem -> putIncrease(elem));
    }

    @Override
    public Solution<Integer> localSearch() {
        Solution<Integer> sol = super.localSearch();
        updateFrequency();
        return sol;
    }

    @Override
    public Solution<Integer> constructiveHeuristic() {
        CL = makeCL();
        RCL = makeRCL();
        currentSol = createEmptySol();
        currentCost = Double.POSITIVE_INFINITY;

        while (!constructiveStopCriteria()) {
            Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
            currentCost = ObjFunction.evaluate(currentSol);
            updateCL();

            for (Integer c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
                if (deltaCost < minCost) {
                    minCost = deltaCost;
                }
                if (deltaCost > maxCost) {
                    maxCost = deltaCost;
                }
            }

            for (Integer c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, currentSol);
                if (deltaCost <= minCost + alpha * (maxCost - minCost)) {
                    RCL.add(c);
                }
            }

            Double minPertubationCost = Double.MAX_VALUE;
            Integer candidate = null;
            for (Integer value : RCL) {
                Double functionPertubationValue = perturbationFunction(value);
                if (minPertubationCost > functionPertubationValue) {
                    minPertubationCost = functionPertubationValue;
                    candidate = value;
                }
            }

            if (candidate != null) {
                CL.remove(candidate);
                currentSol.add(candidate);
            }
            ObjFunction.evaluate(currentSol);
            RCL.clear();
        }

        return currentSol;
    }

    /**
     * Run GRASP for QBFPT
     */
	public static void run(Double alpha, 
						   Integer maxIterations, 
						   String filename,
						   Double maxTime,
						   Integer refCost,
						   Integer smallCost,
						   Integer seed,
                           FileWriter fileWriter)
					   throws IOException {
		
	  GRASP_QBFPT grasp = new GRASP_QBFPT(alpha,
							       	   	  maxIterations, 
							       	   	  "instances/qbf"+filename,
							       	   	  seed);

	  Map<String, Object> log = grasp.solve(maxTime, refCost, smallCost);

      if (fileWriter != null) {
          fileWriter.append("qbf" + filename + ";" + log.get("objectiveFunction") + ";" + log.get("targetTime") + ";" + log.get("totalTime") + "\n");
      }
	}
	
	public static void testAll(Double alpha, 
							   Integer maxIt,
							   Double maxTime,
							   Integer refCost,
							   Integer smallCost,
							   Integer seed) 
					   throws IOException {
				
		String instances[] = {"020", "040", "060", "080", "100", "200", "400"};

		// create a text file
        FileWriter fileWriter = new FileWriter("results/GRASP_QBFPT_PP.txt");

		for(String instance : instances) {
			GRASP_QBFPT.run(alpha, maxIt, instance, maxTime, refCost, smallCost, seed, fileWriter);
		}

		fileWriter.close();
	}
    
    public static void main(String[] args) throws IOException {
    	Integer nTimes = 100;
    	final Random seeds = new Random(1327);
    	
    	// Fixed Parameters
    	Double alpha = 0.40;
    	Integer maxRefCost = Integer.MIN_VALUE;
    	Integer refCost100 = -1263, smallCost100 = -1000;
    	
    	// Changeable Parameters
    	Integer maxIt1 = 10000, maxIt2 = 5000;
    	Double maxTime1 = 1800.0, maxTime2 = 600.0;
    	Integer seed0 = 0;
        
    	// Testing
    	// GRASP_QBFPT.run(alpha, maxIt1, "020", maxTime1, maxRefCost, maxRefCost, seed0, null);
    	
    	// Performance Profiles
    	GRASP_QBFPT.testAll(alpha, maxIt1, maxTime1, maxRefCost, maxRefCost, seed0);
    	
    	// TTT Plots
        FileWriter fileWriter = new FileWriter("results/GRASP_QBFPT_TTT.txt");
    	for (int i = 0; i < nTimes; i++) {
    		GRASP_QBFPT.run(alpha, maxIt2, "100", maxTime2, refCost100, smallCost100, seeds.nextInt(), fileWriter);
    	}

    	fileWriter.close();
    }
}
