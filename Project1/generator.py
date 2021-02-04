'''
Activity 1 - IP and LP models for paper production and routing problem.

generator.py: instance generator, for a certain amount of clients.

Subject:
    MC859/MO824 - Operational Research.
Authors:
    Ricardo Ribeiro Cordeiro - RA 186633
    Sinara Caonetto Pamplona - RA 187101
    Victor Ferreira Ferrari  - RA 187890

University of Campinas - UNICAMP - 2020

Last Modified: 03/02/2021
'''

from json import dump
import numpy as np
from sys import argv, exit
from os.path import join

def generate_instance(n_clients):
    inst = {}
    #All ranges were adapted to create feasible instances
    inst['J'] = n_clients
    inst['F'] = np.random.randint(inst['J'], 2*inst['J'])
    inst['L'] = np.random.randint(5,10)
    inst['M'] = np.random.randint(5,10)
    inst['P'] = np.random.randint(5,10)
    inst['D'] = np.random.randint(10,    21, (inst['J'], inst['P']           )).tolist()
    inst['r'] = np.random.randint(1,      6, (inst['M'], inst['P'], inst['L'])).tolist()
    inst['R'] = np.random.randint(800, 1001, (inst['M'], inst['F']           )).tolist()
    inst['C'] = np.random.randint(80,   101, (inst['L'], inst['F']           )).tolist()
    inst['p'] = np.random.randint(10,   101, (inst['P'], inst['L'], inst['F'])).tolist()
    inst['t'] = np.random.randint(10,    21, (inst['P'], inst['F'], inst['J'])).tolist()

    return inst

if __name__ == "__main__":
    if len(argv) < 2:
        print("Please provide the amount of clients for the instance!")
        exit()

    cli  = int(argv[1])
    inst = generate_instance(cli)

    #Writing instance in a json file
    filename = 'inst_' + str(cli) + '.json'
    filename = join('instances', filename)
    with open(filename, 'w') as f:
        dump(inst, f)
