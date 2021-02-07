package problems.qbfpt.solvers;

import gurobi.*;

import problems.qbf.solvers.GUROBI_QBF;
import problems.qbfpt.triples.ForbiddenTriplesGenerator;
import problems.qbfpt.triples.Triple;

import java.io.FileWriter;
import java.io.IOException;

public class GUROBI_QBFPT_LINEAR extends GUROBI_QBF {

    private ForbiddenTriplesGenerator forbiddenTriplesGenerator;
    private GRBVar[][] w;

    public GUROBI_QBFPT_LINEAR(String filename) throws IOException {
        super(filename);
        forbiddenTriplesGenerator = new ForbiddenTriplesGenerator(problem.size);
    }

    protected void populateNewModel(GRBModel model) throws IOException, GRBException {
        // decision variables
        x = new GRBVar[problem.size];
        w = new GRBVar[problem.size][problem.size];

        for (int i = 0; i < problem.size; i++) {
            x[i] = model.addVar(0, 1, 0.0f, GRB.BINARY, "x[" + i + "]");
            for (int j = 0; j < problem.size; j++) {
                w[i][j] = model.addVar(0, 1, 0.0f, GRB.BINARY, "w[" + i + "," + j + "]");
            }
        }

        model.update();

        // objective function
        GRBQuadExpr obj = new GRBQuadExpr();

        for (int i = 0; i < problem.size; i++) {
            for (int j = i; j < problem.size; j++) {
                obj.addTerm(problem.A[i][j], w[i][j]);
            }
        }

        model.setObjective(obj);
        model.update();

        // constraints
        GRBLinExpr expr1, expr2, expr3;

        for (Triple triple : forbiddenTriplesGenerator.getForbiddenTriple()) {
            expr1 = new GRBLinExpr();
            expr1.addTerm(1, x[triple.getX() - 1]);
            expr1.addTerm(1, x[triple.getY() - 1]);
            expr1.addTerm(1, x[triple.getZ() - 1]);
            model.addConstr(expr1, GRB.LESS_EQUAL, 2.0, String.valueOf("x_" + triple.getX() + "," + triple.getY() + "," + triple.getZ()));
        }

        model.update();

        for (int i = 0; i < problem.size; i++) {
            for (int j = i; j < problem.size; j++) {
                expr2 = new GRBLinExpr();
                expr2.addTerm(1, w[i][j]);
                model.addConstr(expr2, GRB.LESS_EQUAL, x[i], String.valueOf("(i) w_" + i + "," + j + " <= x_" + i));
                model.addConstr(expr2, GRB.LESS_EQUAL, x[j], String.valueOf("(j) w_" + i + "," + j + " <= x_" + j));
                expr3 = new GRBLinExpr();
                expr3.addTerm(-1, w[i][j]);
                expr3.addTerm(1, x[i]);
                expr3.addTerm(1, x[j]);
                model.addConstr(expr3, GRB.LESS_EQUAL, 1, String.valueOf("-w_" + i + "," + j + "+ x_" + i + "," + j + " + x_" + i + "," + j + " <= 1"));
            }
        }

        model.update();

        // maximization objective function
        model.set(GRB.IntAttr.ModelSense, -1);
    }

    public static void main(String[] args) throws IOException, GRBException {

        // instances
        String[] instances = {"qbf020", "qbf040", "qbf060", "qbf080", "qbf100", "qbf200", "qbf400"};

        // create text file
        FileWriter fileWriter = new FileWriter("results/GUROBI_QBFPT_LINEAR.txt");

        for (String instance : instances) {

            // read the problem
            GUROBI_QBFPT_LINEAR gurobi = new GUROBI_QBFPT_LINEAR("instances/" + instance);

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
