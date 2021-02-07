package problems.qbf.solvers;

import gurobi.*;
import problems.qbf.QBF;

import java.io.IOException;

public class GUROBI_QBF {

    public static GRBEnv env;
    public static GRBModel model;
    public GRBVar[] x;
    public QBF problem;

    public GUROBI_QBF(String filename) throws IOException{
        this.problem = new QBF(filename);
    }

    protected void populateNewModel(GRBModel model) throws GRBException, IOException {
        // decision variables
        x = new GRBVar[problem.size];

        for (int i = 0; i < problem.size; i ++){
            x[i] = model.addVar(0, 1, 0.0f, GRB.BINARY, "x[" + i + "]");
        }

        model.update();

        // objective function
        GRBQuadExpr obj = new GRBQuadExpr();

        for (int i = 0; i < problem.size; i++){
            for (int j = i; j < problem.size; j++){
                obj.addTerm(problem.A[i][j], x[i], x[j]);
            }
        }

        model.setObjective(obj);
        model.update();

        // maximization objective function
        model.set(GRB.IntAttr.ModelSense, -1);
    }

    public static void main(String[] args) throws IOException, GRBException {
        // read the problem
        GUROBI_QBF gurobi = new GUROBI_QBF("instances/qbf020");

        // create the environment and model
        env = new GRBEnv("mip1");
        model = new GRBModel(env);
        model.getEnv().set(GRB.DoubleParam.TimeLimit, 1800.0);

        // generate the model
        gurobi.populateNewModel(model);

        // solve the model
        model.optimize();

        // print the solution
        System.out.println("Objective function = " + model.get(GRB.DoubleAttr.ObjVal));
        System.out.println("Objective bound = " + model.get(GRB.DoubleAttr.ObjBound));
        System.out.println("Runtime = " + model.get(GRB.DoubleAttr.Runtime));

        // dispose the environment and model
        model.dispose();
        env.dispose();
    }
}
