package org.maps.PSO;

import java.util.Random;

import static org.maps.InputData.Constants.*;

public class Swarm {
    Particle[] swarm = new Particle[MAX_SWARM];
    Particle[] pbest = new Particle[MAX_SWARM];
    public Particle gbest = new Particle();

    public Swarm() {
        gbest.fitness = Integer.MAX_VALUE;
        for (int i = 1; i <= MAX_SWARM; i++) {
            Particle c_rand = new Particle();
            c_rand.generate();
            swarm[i - 1] = c_rand;
            pbest[i - 1] = c_rand;
            if (gbest.fitness > c_rand.fitness) {
                gbest = new Particle(c_rand);
            }
        }
    }

    private void update_pbest() {
        for (int i = 0; i < MAX_SWARM; i++) {
            if (swarm[i].fitness < pbest[i].fitness) {
                pbest[i] = new Particle(swarm[i]);
            }
        }
    }

    private void update_gbest() {
        for(Particle p : swarm) {
            if(p.fitness < gbest.fitness) {
                gbest = new Particle(p);
            }
        }
    }

    public void proceed_generation() {
        Random random = new Random();
        for (int i = 0; i < MAX_SWARM; i++) {
            for (int j = 1; j <= MAX_TASKS; j++) {
                // compare with pbest
                if (swarm[i].gene[j].processor == pbest[i].gene[j].processor) {
                    // reduce velocity
                    swarm[i].velocity[j] -= random.nextFloat(1) * SWARM_C1;
                } else {
                    // increase velocity
                    swarm[i].velocity[j] += random.nextFloat(1) * SWARM_C1;
                }

                // compare with gbest
                if (swarm[i].gene[j].processor == gbest.gene[j].processor) {
                    // reduce velocity
                    swarm[i].velocity[j] -= random.nextFloat(1) * SWARM_C2;
                } else {
                    // increase velocity
                    swarm[i].velocity[j] += random.nextFloat(1) * SWARM_C2;
                }
            }
        }

        // swap velocities
        for (final Particle p : swarm) {
            int idx_max_2 = -1;
            int idx_max_1 = -1;
            float max_velocity_1 = -Float.MAX_VALUE;
            float max_velocity_2 = -Float.MAX_VALUE;
            for (int i = 1; i <= MAX_TASKS; i++) {
                if (p.velocity[i] > max_velocity_2) {
                    if (p.velocity[i] > max_velocity_1) {
                        // update second best values
                        max_velocity_2 = max_velocity_1;
                        idx_max_2 = idx_max_1;

                        // update first best values
                        max_velocity_1 = p.velocity[i];
                        idx_max_1 = i;
                    } else {
                        // update second best values
                        max_velocity_2 = p.velocity[i];
                        idx_max_2 = i;
                    }
                }
            }

            // swap vms of top two velocities

            final int vm_temp = p.gene[idx_max_1].processor;
            p.gene[idx_max_1].processor = p.gene[idx_max_2].processor;
            p.gene[idx_max_2].processor = vm_temp;
            p.calculate_details();
        }
        update_pbest();
        update_gbest();
    }

    public void print_gbest() {
        gbest.calculate_details();
        gbest.print_details();
    }
}
