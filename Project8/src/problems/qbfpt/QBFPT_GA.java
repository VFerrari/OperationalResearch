package problems.qbfpt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import problems.qbf.QBF_GA;

/**
 * Quadractic binary function with prohibited triples, which extends the 
 * inverse of the quadractic binary function class ({@link QBF_Inverse})
 * to be used with the TS framework and includes the prohibited triples
 * generation.
 * 
 * @author rsaraiva, sipamplona, vferrari
 */
public class QBFPT_GA extends QBF_GA {

    /**
     * The set T of prohibited triples.
     */
    private final ArrayList<List<Integer[]>> T;

    /**
     * Constructor for the QBFPT class.
     * 
     * @param filename
     *      Name of the file for which the objective function parameters
     *      should be read.
     * 
     * @throws IOException
     *      Necessary for I/O operations.
     */
    public QBFPT_GA(String filename) throws IOException {
        super(filename);
        T = generateTriples();
    }

    /**
     * T getter.
     * 
     * @return {@link #T}.
     */
    public ArrayList<List<Integer[]>> getT() { return T; };
    
    /**
     * Generates the prohibited triples set T, where:
     * T = {(i, j, k) ∈ T : ∀ u ∈ [1, n], (i, j, k) = sort({u, g(u), h(u)})}.
     *  
     * @return The prohibited triples.
     */
    private ArrayList<List<Integer[]>> generateTriples() {

        ArrayList<List<Integer[]>> _T = new ArrayList<List<Integer[]>>(size);
        Integer[] triple;

        // Initializing.
        for(int u=0; u<size; u++) {
        	_T.add(new ArrayList<Integer[]>());
        }
        
        /* 
         * NOTE: 
         * u ∈ [1, n], which is relevant to generate a triple, but we are
         * indexing from 0 to n - 1 in arrays so be careful when mapping a
         * triple element into other arrays (e.g., A, variables).
         */
        for (int u = 1; u <= size; u++) {
            triple = new Integer[] {u-1, g(u)-1, h(u)-1};
            Arrays.sort(triple);
            _T.get(triple[0]).add(triple);
        }

        return _T;

    }

    /**
     * Linear congruence method, used to generate a new number.
     * 
     * @param u
     *      Base number for which a new number will be generated.
     * @param pi1
     *      Multiplier hyperparameter.
     * @param pi2
     *      Sum hyperparameter.
     * 
     * @return l(u) = 1 + ((π1 · (u − 1) + π2) mod n).
     */
    private Integer l(int u, int pi1, int pi2) {
        return 1 + ((pi1 * (u - 1) + pi2) % size);
    }

    /**
     * Function g(u), used to generate the second element in a triple.
     * 
     * @param u
     *      Base number for which a new number will be generated.
     * 
     * @return g(u) = {
     *      1 + (l(u) mod n), if l(u) == u;
     *      l(u) otherwise.
     * }
     */
    private Integer g(int u) {

        int l_res = l(u, 131, 1031);

        if (l_res == u) {
            return 1 + (l_res % size);
        }

        return l_res;

    }

    /**
     * Function h(u), used to generate the third element in a triple.
     * 
     * @param u
     *      Base number for which a new number will be generated.
     * 
     * @return h(u) = {
     *      1 + ((l(u) + 1) mod n), if 1 + (l(u) mod n) == u or g(u); (1)
     *      1 + (l(u) mod n), if l(u) == u or g(u); (2)
     *      l(u) otherwise.
     * }
     */
    private Integer h(int u) {

        int l_res = l(u, 193, 1093);
        int g_res = g(u);

        // Condition (2)
        if (l_res == u || l_res == g_res) {

            int l_mod = 1 + (l_res % size);

            // Condition (1)
            if (l_mod == u || l_mod == g_res) {
                return 1 + ((l_res + 1) % size);
            }

            return l_mod;

        }

        return l_res;

    }    
}
