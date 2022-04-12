package org.maps.GA;

public class Comparator {
    static class Cmp_fitness_val implements java.util.Comparator<Chromosome> {
        public int compare(Chromosome c1, Chromosome c2){
            if(c1.fitness > c2.fitness)
                return 1;
            else
                return 0;
        }
    }
}
