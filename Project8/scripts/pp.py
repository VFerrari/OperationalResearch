# %%
import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy
import pandas
import seaborn as sn

logplot = False

# %%
T = pandas.read_csv("runtime.csv")
np = len(T)
ns = len(T.columns)

# %% Minimal performance per solver.

minperf = numpy.zeros(np)

for k in range(np):
    minperf[k] = min(T.to_numpy()[k])

# %% Compute ratios and divide by smallest element in each row.

r = numpy.zeros((np, ns))

for k in range(np):
    r[k, :] = T.to_numpy()[k, :] / minperf[k]

if logplot:
    r = numpy.log2(r)

max_ratio = r.max()

for k in range(ns):
    r[:, k] = numpy.sort(r[ :, k])


# %% Plot stair graphs with markers.

n = []

for k in range(1, np + 1):
    n.append(k / np)

# %%
plt.figure(figsize = (8, 5))
plt.plot(r[:, 0], n, color = '#3CB371', linewidth = 1, label = 'GRASP')
plt.plot(r[:, 1], n, color = '#FFD700', linewidth = 1, label = 'TS')
plt.plot(r[:, 2], n, color = '#4B0082', linewidth = 1, label = 'GA')
plt.plot(r[:, 3], n, color = '#FF6347', linewidth = 1, label = 'GUROBI')
plt.plot(r[:, 4], n, color = '#4169E1', linewidth = 1, label = 'GUROBI_LINEAR')
plt.ylabel('Probabilidade (%)')
sn.despine(left = True, bottom = True)
plt.grid(True, axis = 'x')
plt.grid(True, axis = 'y')
plt.legend()
plt.savefig('objectiveFunction1.eps', dpi = 1500, transparent = True, bbox_inches = 'tight')
plt.show()
# %%



