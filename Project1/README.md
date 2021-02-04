# Projeto 1

## Gerar Instância

Comando:

```BASH
python3 generator.py <NUMBER-OF-CLIENTS>
```

Este comando cria um arquivo com o nome `inst_<NUMBER-OF-CLIENTS>.json` no diretório `instances`.

## Otimizar Problema Linear

Comando:

```
python3 solve_paper_problem.py <PATH-TO-JSON-FILE>
```

Este comando carrega a instância e devolve no terminal o resultado da otimização do Gurobi e, se existir, os valores atribuídos para as variáveis do problema.
