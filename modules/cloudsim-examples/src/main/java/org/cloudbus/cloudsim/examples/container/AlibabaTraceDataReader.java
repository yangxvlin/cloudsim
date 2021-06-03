package org.cloudbus.cloudsim.examples.container;

import org.apache.commons.math3.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-02 17:30
 * description:
 **/

public class AlibabaTraceDataReader {

    public static final String CONTAINER_META = "F://alibaba_trace_data/container_meta.csv";

    private static Set<String> container_id = new HashSet<>();

    // private static Set<String> container_machine_pair = new HashSet<>();

    private static Map<String, Integer> container_status = new HashMap<>();

    private static Map<Pair<Integer, Double>, Integer> container_resource_type = new HashMap<>();

    private static Map<String, List<String>> machine_containers_mapping = new HashMap<>();

    public static void main(String[] args) {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(new File(CONTAINER_META)));
            while (true) {
                String row = csvReader.readLine();
                if (row == null) {
                    break;
                }

                String[] data = row.split(",");

                if (!container_id.contains(data[0]) && data[4].equals("started")) {
                    Pair<Integer, Double> resource_demand_type = new Pair<>(Integer.parseInt(data[5]), Double.parseDouble(data[7]));
                    container_resource_type.putIfAbsent(resource_demand_type, 0);
                    container_resource_type.put(resource_demand_type, container_resource_type.get(resource_demand_type) + 1);

                    if (!machine_containers_mapping.containsKey(data[1])) {
                        machine_containers_mapping.putIfAbsent(data[1], new ArrayList<>());
                    }
                    if (!machine_containers_mapping.get(data[1]).contains(data[0])) {
                        machine_containers_mapping.get(data[1]).add(data[0]);
                    }
                }
                container_id.add(data[0]);
                // container_machine_pair.add(data[0]+data[1]);

                container_status.putIfAbsent(data[4], 0);
                container_status.put(data[4], container_status.get(data[4]) + 1);

            }

            System.out.println("Total container: " + container_id.size());
            // System.out.println("Total container+machine pair: " + container_machine_pair.size()); // containers & machine pair == #container --> no container migration
            System.out.println(Arrays.toString(container_status.entrySet().toArray()));
            System.out.println(Arrays.toString(container_resource_type.entrySet().toArray()));
            System.out.println("machines: containers");
            int single_machine_containers = 0;
            for (String machine: machine_containers_mapping.keySet()) {
                if (machine_containers_mapping.get(machine).size() > 1) {
                    // System.out.println("    " + machine + ":" + machine_containers_mapping.get(machine).size());
                } else {
                    single_machine_containers++;
                }
            }
            System.out.println("#containers occupy a whole machine: " + single_machine_containers);

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static final String BATCH_TASK = "F://alibaba_trace_data/batch_task.csv";

//    public static void main(String[] args) {
//        try {
//            int totalTaskNum = 0, independentTaskNum = 0;
//            BufferedReader csvReader = new BufferedReader(new FileReader(new File(BATCH_TASK)));
//            while (true) {
//                String row = csvReader.readLine();
//                if (row == null) {
//                    break;
//                }
//
//                String[] data = row.split(",");
//
//                if (row.startsWith("task_")) {
//                    independentTaskNum++;
//                }
//
//                totalTaskNum++;
//            }
//
//            System.out.println("Total number of tasks: " + totalTaskNum + "; " +
//                               independentTaskNum + "/" + totalTaskNum + String.format("(%.2f%%)", (double) independentTaskNum / totalTaskNum * 100)  + " are independent task");
//            csvReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
