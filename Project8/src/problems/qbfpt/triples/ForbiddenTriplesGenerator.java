package problems.qbfpt.triples;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenTriplesGenerator {

    private final int n;
    private final List<Triple> triples = new ArrayList<>();

    public ForbiddenTriplesGenerator(int n){
        this.n = n;
        generator();
    }

    private int mod(int value){
        return 1 + (value % n);
    }

    private int l(int value, int pi1, int pi2){
        return 1 + (pi1 * value + pi2) % n;
    }

    private int g(int value){
        int lg = l(value, 131, 1031);
        if (lg != value){
            return lg;
        }
        return mod(value);
    }

    private int h(int firstValue, int secondValue){
        int lh = l(firstValue, 193, 1093);
        if (lh != firstValue && lh != secondValue){
            return lh;
        }
        int lhMod = mod(lh);
        if (lhMod != firstValue && lhMod != secondValue){
            return lhMod;
        }
        return mod(lh + 1);
    }

    public List<Triple> generator(){
        if (triples.isEmpty()){
            for (int i = 0; i < n; i++){
                triples.add(new Triple(i + 1, g(i), h(i, g(i))));
            }
        }
        return triples;
    }

    public List<Integer> getForbiddenValues(Integer x, Integer y){
        List<Integer> values = new ArrayList<>();
        for (Triple triple : triples){
            if (triple.contains(x, y)){
                Integer complement = triple.complement(x, y);
                if (complement != null){
                    values.add(complement);
                }
            }
        }
        return values;
    }

    public List<Triple> getForbiddenTriple() {
        return triples;
    }

    public static void main(String[] args){
        List<Triple> triples = new ForbiddenTriplesGenerator(60).generator();
        for (Triple triple : triples){
            System.out.println(triple);
        }
    }
}
