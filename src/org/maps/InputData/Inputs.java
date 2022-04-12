package org.maps.InputData;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Inputs {
    public static final int[][] processing_cost = {
            //  1,  2,  3
            {0, 0, 0, 0},    // 0
            {0, 14, 16, 9},  // 1
            {0, 13, 19, 18}, // 2
            {0, 11, 13, 19}, // 3
            {0, 13, 8, 17},  // 4
            {0, 12, 13, 10}, // 5
            {0, 13, 16, 9},  // 6
            {0, 7, 15, 11},  // 7
            {0, 5, 11, 14},  // 8
            {0, 18, 12, 20}, // 9
            {0, 21, 7, 16},  // 10

    };
    public static final Comm_cost_pair[][] dag = new Comm_cost_pair[][]{
            {new Comm_cost_pair(0, 0)}, // 0
            {new Comm_cost_pair(2, 18), new Comm_cost_pair(3, 12), new Comm_cost_pair(4, 9), new Comm_cost_pair(5, 11), new Comm_cost_pair(6, 14)}, // 1
            {new Comm_cost_pair(8, 19), new Comm_cost_pair(9, 16)}, // 2
            {new Comm_cost_pair(7, 23)},  // 3
            {new Comm_cost_pair(8, 27), new Comm_cost_pair(9, 23)},  // 4
            {new Comm_cost_pair(9, 13)},  // 5
            {new Comm_cost_pair(8, 15)},  // 6
            {new Comm_cost_pair(10, 17)},  // 7
            {new Comm_cost_pair(10, 11)},  // 8
            {new Comm_cost_pair(10, 13)},  // 9
            {new Comm_cost_pair(0, 0)}
    };

    public static Vector<Set<Integer>> dependency = new Vector<>(Constants.MAX_TASKS + 1);

    public static void main(String[] args) {
        System.out.println("Processing costs");
        for (int[] pc_cost : processing_cost) {
            System.out.println();
            for (int cost_on_processor : pc_cost) {
                System.out.print(cost_on_processor + ", ");
            }
        }

        System.out.println("Comm cost pair");
        for (Comm_cost_pair[] pairs : dag) {
            System.out.println();
            for (Comm_cost_pair p : pairs) {
                System.out.print(p.to_node + " : " + p.to_node + ", ");
            }
        }
    }

    public static void generate_dependency_table() {
        //
        for (int i = 0; i <= Constants.MAX_TASKS; i++) {
            dependency.add(i, new HashSet<>());
        }
        for (int i = 1; i <= Constants.MAX_TASKS; i++) {
            Comm_cost_pair[] ccp = dag[i];
            for (Comm_cost_pair p : ccp) {
                Set<Integer> s = dependency.get(p.to_node);
                s.add(i);
                dependency.set(p.to_node, s);
            }
        }
    }
}
