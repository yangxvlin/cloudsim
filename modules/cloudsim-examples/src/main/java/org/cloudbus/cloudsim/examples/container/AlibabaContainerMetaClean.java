package org.cloudbus.cloudsim.examples.container;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-02 22:34
 * description:
 **/

public class AlibabaContainerMetaClean {
    public static final String CONTAINER_META = "F://alibaba_trace_data/container_meta.csv";
    public static final String CONTAINER_META_CLEAN = "F://alibaba_trace_data/container_meta_clean.csv";

    private static Set<String> container_id = new HashSet<>();

    public static void main(String[] args) {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(new File(CONTAINER_META)));
            FileWriter fw = new FileWriter(CONTAINER_META_CLEAN);
            while (true) {
                String row = csvReader.readLine();
                if (row == null) {
                    break;
                }
                String[] data = row.split(",");

                if (!container_id.contains(data[0]) && data[4].equals("started")) {
                    fw.write(row + "\n");
                }
                container_id.add(data[0]);
            }

            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
