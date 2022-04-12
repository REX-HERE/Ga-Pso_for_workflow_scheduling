package org.maps.GA;

import org.maps.InputData.Comm_cost_pair;
import org.maps.InputData.Inputs;

import java.util.*;

import static org.maps.InputData.Constants.MAX_PROCESSORS;
import static org.maps.InputData.Constants.MAX_TASKS;

public class Chromosome {
    public Gene[] gene = new Gene[MAX_TASKS + 1];
    public Integer makespan = -1;
    public boolean feasibility = false;
    public float fitness = -1;
    public float average_cost = -1;
    public Vector<Vector<ScheduledTaskDetails>> schedule = new Vector<>(MAX_PROCESSORS + 1);

    private void set_schedule() {
        // 3 queue for task
        Vector<Queue<Gene>> taskQueueOnProcessor = new Vector<>(MAX_PROCESSORS + 1);
        Set<Integer> completed_tasks = new HashSet<>();
        Map<Integer, Integer> task_to_processor = new HashMap<>();
        Map<Integer, Integer> end_time_of_task = new HashMap<>();

        for (int i = 0; i <= MAX_PROCESSORS; i++) {
            Queue<Gene> q_temp = new LinkedList<>();
            schedule.add(i, new Vector<>());
            taskQueueOnProcessor.add(i, q_temp);
        }

        // initialize schedule with empty values
        for (Vector<ScheduledTaskDetails> v : schedule) {
            Gene g = new Gene(0, 0);
            ScheduledTaskDetails sds = new ScheduledTaskDetails(g, 0, 0);
            v.add(sds);
        }

        // adding genes to processor queue
        for (Gene g : gene) {
            if (g.processor == 0 || g.task == 0) continue;
            Queue<Gene> q = taskQueueOnProcessor.get(g.processor);
            q.add(g);
            task_to_processor.put(g.task, g.processor);
            taskQueueOnProcessor.set(g.processor, q);
        }

        // trying to schedule front genes from each processor queue
        boolean has_any_task_completed;
        do {
            has_any_task_completed = false;
            for (int i = 1; i <= MAX_PROCESSORS; i++) {
                Queue<Gene> q = taskQueueOnProcessor.get(i);
                if (q.isEmpty()) continue;
                final Gene g = q.peek();
                int max_comm_ends = 0;
                boolean dependencies_satisfied = true;
                // check all dependencies have completed or not
                assert g != null;
                Set<Integer> dependency_list = Inputs.dependency.get(g.task);
                for (Integer d_task : dependency_list) {
                    if (!completed_tasks.contains(d_task)) {
                        dependencies_satisfied = false;
                        break;
                    }
                    for (Comm_cost_pair ccp : Inputs.dag[d_task]) {
                        if (ccp.to_node == g.task) {
                            if (task_to_processor.get(d_task) != g.processor) {
                                int comm_ends = ccp.comm_cost + end_time_of_task.get(d_task);
                                max_comm_ends = Integer.max(max_comm_ends, comm_ends);
                            }
                            break;
                        }
                    }
                }
                if (dependencies_satisfied) {
                    has_any_task_completed = true;
                    q.remove();
                    taskQueueOnProcessor.set(i, q);
                    completed_tasks.add(g.task);
                    int start_time = Integer.max(schedule.get(g.processor).lastElement().end_time, max_comm_ends);
                    int end_time = start_time + Inputs.processing_cost[g.task][g.processor];
                    ScheduledTaskDetails sd = new ScheduledTaskDetails(g, start_time, end_time);

                    Vector<ScheduledTaskDetails> processor_schedule = schedule.get(g.processor);
                    processor_schedule.add(sd);
                    schedule.set(g.processor, processor_schedule);

                    // update maps
                    end_time_of_task.put(g.task, sd.end_time);
                }
            }
        } while (has_any_task_completed);

        // check if the queue is empty
        feasibility = true;
        for (Queue<Gene> top : taskQueueOnProcessor) {
            if (!top.isEmpty()) {
                feasibility = false;
                break;
            }
        }
    }

    private void set_makespan() {
        int max_end_time = 0;
        for (Vector<ScheduledTaskDetails> processor : schedule) {
            max_end_time = Integer.max(processor.lastElement().end_time, max_end_time);
        }
        makespan = max_end_time;
    }

    private void set_average_cost() {
        assert feasibility : "feasibility is not set or is false";
        average_cost = 0;
        for (Gene g : gene) {
            average_cost += Inputs.processing_cost[g.task][g.processor];
        }
        average_cost /= MAX_TASKS;
    }

    public void set_fitness() {
        assert average_cost != -1 : "average cost is not calculated";
        assert makespan != -1 : "makespan is not calculated";
        fitness = (float) (1.0 / (1.0 + average_cost * makespan));
    }

    public void calculate_details() {
        set_schedule();
        if (feasibility) {
            set_makespan();
            set_average_cost();
            set_fitness();
        }
    }

    public void print_chromosome() {
        for (Gene g : gene) {
            System.out.print("(" + g.task + ", " + g.processor + ")");
        }
    }

    public void print_details() {
        System.out.println("fitness: " + fitness + ", makespan: " + makespan + ", average_cost: " + average_cost);
    }

    public void print_schedule() {
        System.out.println("Task Proc Strt End");
        for (Vector<ScheduledTaskDetails> sv : schedule) {
            for (ScheduledTaskDetails s : sv) {
                if (s.g.task == 0 || s.g.processor == 0) continue;
                System.out.println(s.g.task + "\t" + s.g.processor + "\t" + s.start_time + "\t" + s.end_time);
            }
        }
    }

    public void generate() {
        Random random = new Random();
        Set<Integer> already_added_task = new HashSet<>();
        gene[0] = new Gene(0,0);
        already_added_task.add(0);
        feasibility = false;
        while(!feasibility) {
            for (int i = 1; i <= MAX_TASKS; i++) {
                int task = 0;
                while(already_added_task.contains(task)) {
                    task = random.nextInt(MAX_TASKS)+1;
                }
                gene[i] = new Gene(task, random.nextInt(MAX_PROCESSORS) + 1);
            }
            calculate_details();
        }
    }
}
