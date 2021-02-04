'''
Activity 1 - IP and LP models for paper production and routing problem.

solve_paper_problem.py: solves paper production and routing problem, using Gurobi

Subject:
    MC859/MO824 - Operational Research.
Authors:
    Ricardo Ribeiro Cordeiro - RA 186633
    Sinara Caonetto Pamplona - RA 187101
    Victor Ferreira Ferrari  - RA 187890

University of Campinas - UNICAMP - 2020

Last Modified: 01/10/2020
'''

from gurobipy import *
import json
import numpy as np
from sys import argv, exit


def solver(dict_const):

    factories = range(0,dict_const['F'])
    machines = range(0,dict_const['L'])
    raw_material = range(0, dict_const['M'])
    paper_types = range(0, dict_const['P'])
    clients = range(0, dict_const['J'])


    #creating model
    model = Model("paperproblem")

    #----------------Please, choose an option----------------#
    #variables - INTEGER
    X =  model.addVars(paper_types, machines, factories, lb=0, vtype=GRB.INTEGER, name="X")
    T =  model.addVars(paper_types, factories, clients, lb=0, vtype=GRB.INTEGER, name="T")
    total_cost = model.addVar(lb=0, vtype=GRB.INTEGER,name="total_cost")
    #variables - CONTINUOUS
    # X =  model.addVars(paper_types, machines, factories, lb=0, vtype=GRB.CONTINUOUS, name="X")
    # T =  model.addVars(paper_types, factories, clients, lb=0, vtype=GRB.CONTINUOUS, name="T")
    # total_cost = model.addVar(lb=0, vtype=GRB.CONTINUOUS,name="total_cost")

    #objective function
    model.setObjective(total_cost,GRB.MINIMIZE)

    #time limit to execute 30 min
    model.setParam('TimeLimit',1800)

    #constraints

    #main
    model.addConstr(total_cost >= quicksum(quicksum(quicksum(X.sum(p,l,f) * dict_const['p'][p][l][f] for p in paper_types) for f in factories) for l in machines) + quicksum(quicksum(quicksum(T.sum(p,f,j) * dict_const['t'][p][f][j] for p in paper_types) for f in factories) for j in clients), "MainConstraint")

    #second
    for p in paper_types:
        for j in clients:
            model.addConstr(quicksum(T.sum(p,f,j) for f in factories) == dict_const['D'][j][p], "TransportDemandConstraint")

    #third
    for p in paper_types:
        for f in factories:
            model.addConstr(quicksum(T.sum(p,f,j) for j in clients) == quicksum(X.sum(p,l,f) for l in machines), "TransportProductionConstraint")

    #forth
    for f in factories:
        for m in raw_material:
            model.addConstr(quicksum(quicksum(X.sum(p,l,f) * dict_const['r'][m][p][l] for p in paper_types) for l in machines) <= dict_const['R'][m][f], "RawMaterialAvailability")

    #fifth
    for f in factories:
        for l in machines:
            model.addConstr(quicksum(X.sum(p,l,f) for p in paper_types) <= dict_const['C'][l][f], "ProductionCapacityConstraint")

    #Refresh
    model.update()

    #Saving model
    model.write('paperproblem{}clients.lp'.format(dict_const['J']))

    #Optimizing
    model.optimize()

    #if it has an optimal solution
    print("==========================================")
    if model.status == GRB.Status.OPTIMAL:
        print('\nOptimal Solution:')
        print('\ntotal_cost: {}'.format(model.objVal))

        for p in paper_types:
            for l in machines:
                for f in factories:
                    if X[p,l,f].X > 0.0:
                        print('Quantity of type {} produced by machine {} in factory {} : {}'.format(p,l,f,X[p,l,f].X))

        for p in paper_types:
            for f in factories:
                for j in clients:
                    if T[p,f,j].X > 0.0:
                        print('Quantity of type {} transported by factory {} to client {} : {}'.format(p,f,j,T[p,f,j].X))

        print('\nOptimal Solution:')
        print('\ntotal_cost: {}'.format(model.objVal))
    elif model.status == GRB.Status.TIME_LIMIT:
        print('\nCouldn\'t find an optimal solution. Time limit reached.\n')
        print('\ntotal_cost: {}'.format(model.objVal))
        print('\n')

        for p in paper_types:
            for l in machines:
                for f in factories:
                    if X[p,l,f].X > 0.0:
                        print('Quantity of type {} produced by machine {} in factory {} : {}'.format(p,l,f,X[p,l,f].X))

        for p in paper_types:
            for f in factories:
                for j in clients:
                    if T[p,f,j].X > 0.0:
                        print('Quantity of type {} transported by factory {} to client {} : {}'.format(p,f,j,T[p,f,j].X))

        print('\nCouldn\'t find an optimal solution. Time limit reached.')
        print('\nSolution:')
        print('\ntotal_cost: {}'.format(model.objVal))
    elif model.status == GRB.Status.INFEASIBLE:
        print('\nCouldn\'t find an optimal solution. Status: Infeasible.\n')
    else:
        print('Model Status Code: {}'.format(model.Status))
    print("==========================================")


if __name__ == "__main__":
    if len(argv) < 2:
        print("Please provide the path of the file!")
        exit()

    with open(argv[1]) as file:
        dict_const = json.load(file)
        print(dict_const['J'])
        solver(dict_const)
