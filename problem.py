from itertools import combinations
from collections import defaultdict
import math
from itertools import product


problems = set()
results1 = defaultdict(set)
results2 = defaultdict(set)
results3 = defaultdict(set)
solution = defaultdict(set)

precision = 10

def calset1(a):
    result = set()
    result.add(a)
    result.add(-a)
    if a == 4 or a == 9:
        result.add(int(math.sqrt(a)))
        solution[f"{int(math.sqrt(a))}"].add(f"sqrt({a})")
        result.add(-int(math.sqrt(a)))
        solution[f"{-int(math.sqrt(a))}"].add(f"-sqrt({a})")
    if a == 8:
        result.add(2)
        result.add(-2)
    return result

def calset2(a,b):
    result = set()
    result_a = calset1(a)
    result_b = calset1(b)
    result.add(10 * a + b)
    n = 10 * a + b
    sqrt_n = math.sqrt(n)
    if sqrt_n == int(math.sqrt(n)):
        result.add(int(sqrt_n))
        result.add(- int(sqrt_n))
    cube_root = round(n ** (1/3))
    if cube_root ** 3 == n:
        result.add(int(cube_root))
        result.add(- int(cube_root))


    for i in result_a:
        for j in result_b:
            result.add(i + j)
            result.add(-i + j)
            result.add(-j + i)
            result.add(- i - j)
            result.add(i * j)
            result.add(- i * j)
            if j != 0:
                if i / j == int(i / j):
                    if i / j == int(i / j):
                        result.add(int(i / j))
                        result.add(-int(i / j))

    return result

def calset3(a,b,c):
    result = set()
    result_a = results1[f"{a}"]
    result_c = results1[f"{c}"]
    result_ab = results2[f"{a}{b}"]
    result_bc = results2[f"{b}{c}"]

    n = 100 * a + 10 * b + c
    result.add(n)

    sqrt_n = math.sqrt(n)

    if sqrt_n == int(math.sqrt(n)):
        #print(n)
        result.add(int(sqrt_n))
        result.add(- int(sqrt_n))
    cube_root = round(n ** (1 / 3))
    if cube_root ** 3 == n:
        result.add(int(cube_root))
        result.add(- int(cube_root))

    for i in result_a:
        for j in result_bc:
            result.add(i + j)
            result.add(-i + j)
            result.add(-j + i)
            result.add(- i - j)
            result.add(i * j)
            result.add(- i * j)
            if j != 0:
                if i / j == int(i / j):
                    if i / j == int(i / j):
                        result.add(int(i / j))
                        result.add(-int(i / j))
    for i in result_ab:
        for j in result_c:
            result.add(i + j)
            result.add(-i + j)
            result.add(-j + i)
            result.add(- i - j)
            result.add(i * j)
            result.add(- i * j)
            if j != 0:
                if i / j == int(i / j):
                    if i / j == int(i / j):
                        result.add(int(i / j))
                        result.add(-int(i / j))
    return result


for i in range(10):
    results1[f"{i}"] = calset1(i)
    print(results1[f"{i}"])


for i in range(10):
    for j in range(10):
        results2[f"{i}{j}"] = calset2(i,j)
        print(f"{i}{j}: {results2[f"{i}{j}"]}")


calset3(0,1,0)
for i in range(10):
    for j in range(10):
        for k in range(10):
            results3[f"{i}{j}{k}"] = calset3(i, j, k)
            print(f"{i}{j}{k}: {results3[f"{i}{j}{k}"]}")


reversed_dict1 = defaultdict(set)
reversed_dict2 = defaultdict(set)
reversed_dict3 = defaultdict(set)

# 키와 값을 바꿔서 새로운 딕셔너리에 추가
for key, value_set in results1.items():
    for value in value_set:
        reversed_dict1[value].add(key)

# 결과 출력
for key in sorted(reversed_dict1.keys()):
    print(f"{key}: {reversed_dict1[key]}")





for key, value_set in results2.items():
    for value in value_set:
        reversed_dict2[value].add(key)

# 결과 출력
for key in sorted(reversed_dict2.keys()):
    print(f"{key}: {reversed_dict2[key]}")

for key, value_set in results3.items():
    for value in value_set:
        reversed_dict3[value].add(key)

# 결과 출력
for key in sorted(reversed_dict3.keys()):
    print(f"{key}: {reversed_dict3[key]}")

problems = set()
problemss = set()
# reversed_dict를 순회하면서 각 value set에서 두 개의 값 조합 생성
for key in sorted(reversed_dict2.keys()):
    value_set = reversed_dict2[key]
    if len(value_set) >= 2:
        comb = list(combinations(value_set, 2))
        for comb in comb:
            problems.add(comb[0] + comb[1])
            #solution[comb[0]+comb[1]].add(f"{comb[0]}={comb[1]}")
            problems.add(comb[1] + comb[0])
            #solution[comb[1]+comb[0]].add(f"{comb[1]}={comb[0]}")


for key in reversed_dict1.keys() & reversed_dict3.keys():
    for value1, value2 in product(reversed_dict1[key], reversed_dict3[key]):
        problemss.add(value1 + value2)
        problemss.add(value2 + value1)

problems.update(problemss)
print(problems)
print(len(problems))
