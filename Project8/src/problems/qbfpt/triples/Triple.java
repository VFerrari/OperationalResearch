package problems.qbfpt.triples;

import java.util.*;

public class Triple {

    private int x;
    private int y;
    private int z;

    public Triple(int x, int y, int z){
        if (x > y){
            int u = x;
            x = y;
            y = u;
        }

        if (x > z){
            int u = x;
            x = z;
            z = u;
        }

        if (y > z){
            int u = y;
            y = z;
            z = u;
        }

        this.x = x;
        this.y = y;
        this.z = z;
        List<Integer> triple = Arrays.asList(this.x, this.y, this.z);
        Collections.sort(triple);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean contains(int u){
        return  x == u || y == u || z == u;
    }

    public boolean contains(int u, int v){
        return contains(u) || contains(v);
    }

    public Integer complement(int u, int v){
        if (x == u && y == v){
            return z;
        }

        if (x == u && z == v){
            return y;
        }

        if (y == u && z == v){
            return x;
        }

        if (x == v && y == u){
            return z;
        }

        if (x == v && z == u){
            return y;
        }

        if (y == v && z == u){
            return x;
        }

        return null;
    }

    @Override
    public String toString(){
        return "[" + x + ", " + y + ", " + z + "]";
    }
}