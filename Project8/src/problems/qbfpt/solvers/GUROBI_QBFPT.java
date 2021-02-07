package problems.qbfpt.solvers;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBQuadExpr;
import gurobi.GRBVar;
import problems.qbf.solvers.GUROBI_QBF;
import problems.qbfpt.triples.ForbiddenTriplesGenerator;
import problems.qbfpt.triples.Triple;

import java.io.FileWriter;
import java.io.IOException;

public class GUROBI_QBFPT extends GUROBI_QBF {

    private ForbiddenTriplesGenerator forbiddenTriplesGenerator;

    public GUROBI_QBFPT(String filename) throws IOException {
        super(filename);
        forbiddenTriplesGenerator = new ForbiddenTriplesGenerator(problem.size);
    }

    protected void populateNewModel(GRBModel model) throws GRBException {
        // decision variables
        x = new GRBVar[problem.size];

        for (int i = 0; i < problem.size; i++) {
            x[i] = model.addVar(0, 1, 0.0f, GRB.BINARY, "x[" + i + "]");
        }

        model.update();

        // objective function
        GRBQuadExpr obj = new GRBQuadExpr();

        for (int i = 0; i < problem.size; i++) {
            for (int j = i; j < problem.size; j++) {
                obj.addTerm(problem.A[i][j], x[i], x[j]);
            }
        }

        model.setObjective(obj);
        model.update();

        // constraints
        GRBLinExpr expr;

        for (Triple triple : forbiddenTriplesGenerator.getForbiddenTriple()) {
            expr = new GRBLinExpr();
            expr.addTerm(1, x[triple.getX() - 1]);
            expr.addTerm(1, x[triple.getY() - 1]);
            expr.addTerm(1, x[triple.getZ() - 1]);
            model.addConstr(expr, GRB.LESS_EQUAL, 2.0, String.valueOf("x_" + triple.getX() + "," + triple.getY() + "," + triple.getZ()));
        }

        // maximization objective function
        model.set(GRB.IntAttr.ModelSense, -1);
    }

    public static void main(String[] args) throws IOException, GRBException {

        // instances
        String[] instances = {"qbf020", "qbf040", "qbf060", "qbf080", "qbf100", "qbf200", "qbf400"};

        // create text file
        FileWriter fileWriter = new FileWriter("results/GUROBI_QBFPT.txt");

        for (String instance : instances) {
            // read the problem
            GUROBI_QBFPT gurobi = new GUROBI_QBFPT("instances/" + instance);

            // create the environment and model
            env = new GRBEnv();
            model = new GRBModel(env);
            model.getEnv().set(GRB.DoubleParam.TimeLimit, 1800.0);

            // generate the model
            gurobi.populateNewModel(model);

            // solve the model
            model.optimize();

            // save the solution in text file
            fileWriter.append(instance + ";" + model.get(GRB.DoubleAttr.ObjVal) + ";" + model.get(GRB.DoubleAttr.ObjBound) + ";" + model.get(GRB.DoubleAttr.Runtime));

            // dispose the environment and model
            model.dispose();
            env.dispose();
        }
        fileWriter.close();
    }
}
