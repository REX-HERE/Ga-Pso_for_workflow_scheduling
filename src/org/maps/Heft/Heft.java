package org.maps.Heft;

import org.maps.GA.Chromosome;
import org.maps.GA.Gene;
import org.maps.GA.ScheduledTaskDetails;
import org.maps.InputData.Comm_cost_pair;
import org.maps.InputData.Inputs;

import java.util.Arrays;
import java.util.Vector;

import static org.maps.InputData.Constants.MAX_PROCESSORS;
import static org.maps.InputData.Constants.MAX_TASKS;
import static org.maps.InputData.Inputs.dag;
import static org.maps.InputData.Inputs.processing_cost;

public class Heft {
    static UpwardRankPair[] upward_ranks = new UpwardRankPair[MAX_TASKS];
    static float[] upward_ranks_temp = new float[MAX_TASKS + 1];
    static float[] mean_wt = new float[MAX_TASKS + 1];

    private static void set_mean_wt() {
        for (int i = 1; i < Inputs.processing_cost.length; i++) {
            mean_wt[i] = 0;
            for (int j : Inputs.processing_cost[i]) {
                mean_wt[i] += j;
            }
            mean_wt[i] /= (float) MAX_PROCESSORS;
        }
    }

    static float calculateUpwardRank(int i) {
        if (upward_ranks_temp[i] > 0) {
            return upward_ranks_temp[i];
        }
        float max_cost = 0;
        for (Comm_cost_pair a : dag[i]) {
            // ignore the 0th index
            if(a.to_node == 0) continue;
            max_cost = Float.max(calculateUpwardRank(a.to_node) + a.comm_cost, max_cost);
        }
        upward_ranks_temp[i] = max_cost + mean_wt[i];
        return upward_ranks_temp[i];
    }

    private static void set_upward_rank() {
        for (int i = 1; i <= MAX_TASKS; i++) {
            upward_ranks[i-1] = new UpwardRankPair();
            upward_ranks[i-1].task = i;
            upward_ranks[i-1].upward_rank = calculateUpwardRank(i);
        }
    }

    public static Chromosome get_heft_chromosome() {
        Chromosome heft = new Chromosome();
        Inputs.generate_dependency_table();
        set_mean_wt();
        Arrays.fill(upward_ranks_temp, 0);
        for(int i = 0; i<= MAX_TASKS; i++) {
            Gene temp = new Gene(0,0);
            heft.gene[i] = temp;
        }
        set_upward_rank();
        Arrays.sort(upward_ranks);

        // initialize list of done tasks
        Vector<ScheduledTaskDetails> completed_tasks_details = new Vector<>(MAX_TASKS+1);
        for (int i = 0; i<= MAX_TASKS; i++) {
            Gene g_empty = new Gene(0, 0);
            ScheduledTaskDetails temp = new ScheduledTaskDetails(g_empty, 0, 0);
            completed_tasks_details.add(temp);
        }

        // initialize schedule
        for (int i = 0; i <= MAX_PROCESSORS; i++) {
            Gene g_empty = new Gene(0, 0);
            ScheduledTaskDetails temp = new ScheduledTaskDetails(g_empty, 0, 0);
            Vector<ScheduledTaskDetails> v = new Vector<>(1);
            v.add(temp);
            heft.schedule.add(v);
        }

        int heft_gene_index = 1;
        // schedule based on the upward ranks
        for(UpwardRankPair urp : upward_ranks) {
            // minimise the time by testing scheduling on
            // different processors

            // assumptions :
            // dependencies have already completed tasks

            // estimated finish time on different processors
            Vector<ScheduledTaskDetails> comparison_of_schedule = new Vector<>(MAX_PROCESSORS);
            for(int proc = 1; proc <= MAX_PROCESSORS; proc++) {
                // step 1 : calculate communication costs from each dependency
                int comm_eft = Integer.MIN_VALUE;
                for(int d : Inputs.dependency.get(urp.task)) {
                    // check if dep already scheduled in the same processor
                    ScheduledTaskDetails dep = completed_tasks_details.get(d);
                    if(dep.g.processor != proc) {
                        // communication delay incurred
                        // communication end time
                        final int comm_end_time = dep.end_time + get_comm_cost(dep.g.task,urp.task);
                        comm_eft = Integer.max(comm_end_time, comm_eft);
                    } else {
                        comm_eft = Integer.max(dep.end_time, comm_eft);
                    }
                }
                // step 2 : get cost of running that job on that processor
                int running_time = processing_cost[urp.task][proc];

                // step 3 : get max(eft_last_running_process_on_that_processor,
                // end_time_of_dependency)
                int start_time = Integer.MIN_VALUE;
                for(int d : Inputs.dependency.get(urp.task)) {
                    ScheduledTaskDetails dep = completed_tasks_details.get(d);
                    start_time = Integer.max(dep.end_time, start_time);
                }
                start_time = Integer.max(heft.schedule.get(proc).lastElement().end_time, start_time);

                // step 4 : get end time;
                int end_time = Integer.max(start_time, comm_eft) + running_time;
                Gene g_temp = new Gene(urp.task, proc);
                ScheduledTaskDetails sd_temp = new ScheduledTaskDetails(g_temp, start_time, end_time);
                comparison_of_schedule.add(sd_temp);
            }
            // minimum end_time
            ScheduledTaskDetails final_sched_of_task = new ScheduledTaskDetails(new Gene(-1,-1), -1, Integer.MAX_VALUE);
            for(ScheduledTaskDetails sd : comparison_of_schedule) {
                if (final_sched_of_task.end_time > sd.end_time) {
                    final_sched_of_task = sd;
                }
            }
            Vector <ScheduledTaskDetails> ttemp = heft.schedule.get(final_sched_of_task.g.processor);
            ttemp.add(final_sched_of_task);
            heft.schedule.set(final_sched_of_task.g.processor, ttemp);
            heft.gene[heft_gene_index] = final_sched_of_task.g;
            completed_tasks_details.set(final_sched_of_task.g.task, final_sched_of_task);
            heft_gene_index++;
        }
        return heft;
    }

    static int get_comm_cost(int dep, int task) {
        for(Comm_cost_pair ccp : dag[dep]) {
            if(ccp.to_node == task) {
                return ccp.comm_cost;
            }
        }
        return -1;
    }

    static class UpwardRankPair implements Comparable<UpwardRankPair> {
        int task;
        float upward_rank;

        @Override
        public int compareTo(UpwardRankPair o) {
            return Float.compare(o.upward_rank, this.upward_rank);
        }
    }
}
