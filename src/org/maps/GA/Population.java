package org.maps.GA;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import static org.maps.Heft.Heft.get_heft_chromosome;
import static org.maps.InputData.Constants.*;

public class Population {
    Vector<Chromosome> population_array = new Vector<>();
    float average_fitness_val;
    Random rn = new Random();

    Vector<Chromosome> population_gen_random() {
        Vector<Chromosome> result = new Vector<>();
        for (int i = 0; i < MAX_POPULATION - 1; i++) {
            Chromosome c = new Chromosome();
            Set<Integer> queued = new HashSet<>();
            c.gene[0] = new Gene(0,0);
            for (int j = 1; j <= MAX_TASKS; j++) {
                int task = rn.nextInt(MAX_TASKS) + 1;
                while (queued.contains(task)) {
                    task = rn.nextInt(MAX_TASKS) + 1;
                }
                queued.add(task);
                c.gene[j] = new Gene(task, rn.nextInt(3)+1);
            }
            c.calculate_details();

            if (c.feasibility) {
                result.add(c);
                c.print_chromosome();
            } else {
                i--;
            }
        }
        return result;
    }

    void population(final Chromosome heft) {
        int i = 0;
        Chromosome temp;
        population_array = population_gen_random();
    }

    Offspring crossover(Chromosome A, Chromosome B) {
        Offspring offspring_chromo = new Offspring();
        Set<Integer> tasks_in_c1 = null;
        Set<Integer> tasks_in_c2 = null;
        int counter_for_c1 = 1;
        int counter_for_c2 = 1;

        int r = rn.nextInt(MAX_TASKS) + 1;

        for (int i = 1; i <= r; i++) {
            offspring_chromo.c1.gene[counter_for_c1] = A.gene[i];
            counter_for_c1++;
            tasks_in_c1.add(A.gene[i].task);

            offspring_chromo.c2.gene[counter_for_c2] = B.gene[i];
            counter_for_c2++;
            tasks_in_c2.add(B.gene[i].task);

        }

        for (int i = 1; i <= MAX_TASKS; i++) {
            if (!tasks_in_c1.contains(B.gene[i].task)) {
                offspring_chromo.c1.gene[i] = B.gene[i];
                counter_for_c1++;
            }

            if (!tasks_in_c2.contains(A.gene[i].task)) {
                offspring_chromo.c2.gene[i] = A.gene[i];
                counter_for_c2++;
            }
        }

        return offspring_chromo;
    }

    Chromosome mutation(Chromosome off_spring, float mutation_rate) {

        float r2 = rn.nextFloat(1);
        if (r2 <= mutation_rate) {
            int a = rn.nextInt(MAX_TASKS) + 1;
            int b = rn.nextInt(MAX_TASKS) + 1;
            if (off_spring.gene[a].processor != off_spring.gene[b].processor || off_spring.gene[a].task != off_spring.gene[b].task) {
                Gene temp = off_spring.gene[a];
                off_spring.gene[a] = off_spring.gene[b];
                off_spring.gene[b] = temp;
            }
        }

        return off_spring;
    }

    Vector<Chromosome> roulette(Vector<Chromosome> population) {
        Vector<Chromosome> result = new Vector<>();
        float max_fitness = 0;
        for (Chromosome chromosome : population) {
            max_fitness = Math.max(chromosome.fitness, max_fitness);
        }
        for (Chromosome chromosome : population) {
            final float rand_0_1 = rn.nextFloat(1);
            final float roulette_v = max_fitness * rand_0_1;
            if (chromosome.fitness >= roulette_v) {
                result.add(chromosome);
            }
        }
        return result;
    }


    void generation() {
        float sum_fitness = 0;
        for (Chromosome chromosome : population_array) {
            chromosome.calculate_details();
            sum_fitness = sum_fitness + chromosome.fitness;
        }

        average_fitness_val = sum_fitness / (float) population_array.size();
        population_array = roulette(population_array);

        for (int i = 0; i < 14; i += 2) {
            Chromosome temp_1 = mutation(crossover(population_array.get(i), population_array.get(i + 1)).c1, MUTATION_RATE);
            Chromosome temp_2 = mutation(crossover(population_array.get(i), population_array.get(i + 1)).c2, MUTATION_RATE);
            if (temp_1.feasibility) {
                temp_1.calculate_details();
                population_array.add(temp_1);
            }
            if (temp_2.feasibility) {
                temp_2.calculate_details();
                population_array.add(temp_2);
            }

        }

        population_array.sort(new Comparator.Cmp_fitness_val());
        if (population_array.size() > MAX_POPULATION) population_array.setSize(20);
        System.out.println(average_fitness_val);
    }

    public void Driver() {
        Chromosome heft = get_heft_chromosome();
        heft.calculate_details();
        heft.print_chromosome();
        System.out.print("makespan heft: ");
        System.out.println(heft.makespan);

        population(heft);
        population_array.add(heft);
        for (int i = 0; i < 200; i++) {
            generation();
        }

        Chromosome final_chromo = population_array.firstElement();
        final_chromo.print_chromosome();
        System.out.print("makespan final: ");
        System.out.println(final_chromo.makespan);

        for (Chromosome chromosome : population_array) {
            chromosome.print_chromosome();
        }
    }
}
