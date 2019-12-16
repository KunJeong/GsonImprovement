import csv
import pandas

def stringToFloat(string):
    # Converts a string formatted as "*m*.*s" to the value as float in seconds
    minutes = int(string.split('m')[0])
    seconds = float(string.split('m')[1][:-1])
    return minutes * 60 + seconds

def createSummaryCsv():
    runtimes_txt = open('./kyr/Runtimes.txt', 'r')
    with open('Summary.csv', 'w', newline='') as csvfile:
        fieldnames = ['PatchName', 
                      'sampler_results_Runtime(s)', 
                      'sampler_results_all_Runtime(s)', 
                      'sampler_results_BestFitness',
                      'sampler_results_all_BestFitness']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
    
        writer = csv.writer(csvfile)
        line_index = 0
        for line in runtimes_txt.readlines():
            if line_index % 12 is 0:
                patch_name = line.split()[1]

                df = pandas.read_csv("./kyr/sampler_results_all_" + patch_name + ".csv")
                fitness_series = df["FitnessImprovement"]
                all_best_fitness = fitness_series.max()

                df = pandas.read_csv("./kyr/sampler_results_" + patch_name + ".csv")
                fitness_series = df["FitnessImprovement"]
                best_fitness = fitness_series.max()
            elif line_index % 12 is 4:
                runtime = stringToFloat(line.split()[1])
            elif line_index % 12 is 9:
                all_runtime = stringToFloat(line.split()[1])
                writer.writerow([patch_name,
                                 runtime, 
                                 all_runtime,
                                 best_fitness,
                                 all_best_fitness])
            line_index += 1

createSummaryCsv()