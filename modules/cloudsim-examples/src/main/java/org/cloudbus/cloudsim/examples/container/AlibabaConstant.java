package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.CotainerPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisionerSimple;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeShared;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.cloudbus.cloudsim.examples.container.AlibabaContainerMetaClean.CONTAINER_META_CLEAN;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-01 21:02
 * description:
 **/

public class AlibabaConstant {

    // ******************************* create cloudlet from Alibaba trace file

    /**
     * create cloudlet with constant workload
     */
    public static List<ContainerCloudlet> createContainerCloudletListConstant(int brokerId, int numberOfCloudlets) {
        List<ContainerCloudlet> containerCloudletList = new ArrayList<>();

        long fileSize = 300L;
        long outputSize = 300L;
        UtilizationModelNull utilizationModelNull = new UtilizationModelNull();
        UtilizationModelFull utilizationModelFull = new UtilizationModelFull();

        for (int i = 0; i < numberOfCloudlets; i++) {
            ContainerCloudlet cloudlet = new ContainerCloudlet(
                    IDs.pollId(ContainerCloudlet.class),
                    // MIPS_UNIT * (i+1) + 5000000,
                    MIPS_UNIT * (i+1),
                    1,
                    fileSize,
                    outputSize,
                    utilizationModelFull,
                    utilizationModelFull,
                    utilizationModelFull
            );
            containerCloudletList.add(cloudlet);
            cloudlet.setUserId(brokerId);
        }

        return containerCloudletList;
    }

    // ******************************* create container from file *************
    /**
     * in Alibaba trace data CPU used are normalized between [0, 100], so we define
     * the base as 5000 MIPS for the hardware configuration considered
     * because we have (average of 5000 MIPS for one 2.5 GHz CPU)
     */
    public static final int MIPS_UNIT = 5000;

    /**
     * in Alibaba trace data RAM are normalized between [0, 100], so we define
     * the base as 4Gb for the hardware configuration considered
     * because we have (4Gb for 1 Cpu)
     */
    public static final int MEMORY_UNIT = 4000;

    public static List<Container> createContainerList(int brokerId, int containersNumber) {
        ArrayList<Container> containerList = new ArrayList<>();

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(new File(CONTAINER_META_CLEAN)));
            int counter = 0;
            while (counter < containersNumber) {
                String row = csvReader.readLine();
                if (row == null) {
                    break;
                }
                String[] data = row.split(",");

                // in Alibaba trace data 100 represent a core, so divide by 100
                int numPe = (int) (((float) Integer.parseInt(data[5])) / 100);

                containerList.add(new PowerContainer(
                        IDs.pollId(Container.class),
                        brokerId,
                        MIPS_UNIT,
                        numPe,
                        (int) (Float.parseFloat(data[7]) * MEMORY_UNIT),
                        0L, // no data
                        0L, // no data
                        "Xen",
                        new ContainerCloudletSchedulerDynamicWorkload(numPe * MIPS_UNIT, numPe),
                        org.cloudbus.cloudsim.examples.container.ConstantsExamples.SCHEDULING_INTERVAL
                ));

                counter ++;
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return containerList;
    }

    // ******************************* create vm for each host ****************
    public static ArrayList<ContainerVm> createVmList(int brokerId) {
        ArrayList<ContainerVm> vmList = new ArrayList<>();

        ArrayList<ContainerPe> peList;

        // ebmhfg5
        peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_8; j++) {
            peList.add(new ContainerPe(j, new CotainerPeProvisionerSimple(7400)));
        }
        vmList.add(new PowerContainerVm(
                IDs.pollId(ContainerVm.class),
                brokerId,
                7400,
                32000,
                6291456,
                90000L, // VM storage
                "Xen",
                new ContainerSchedulerTimeShared(peList),
                new ContainerRamProvisionerSimple(32000),
                new ContainerBwProvisionerSimple(6291456),
                peList,
                org.cloudbus.cloudsim.examples.container.ConstantsExamples.SCHEDULING_INTERVAL
        ));

        // ebmc4
        peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_32; j++) {
            peList.add(new ContainerPe(j, new CotainerPeProvisionerSimple(5000)));
        }
        vmList.add(new PowerContainerVm(
                IDs.pollId(ContainerVm.class),
                brokerId,
                5000,
                64000,
                10485760,
                90000L, // VM storage
                "Xen",
                new ContainerSchedulerTimeShared(peList),
                new ContainerRamProvisionerSimple(64000),
                new ContainerBwProvisionerSimple(10485760),
                peList,
                org.cloudbus.cloudsim.examples.container.ConstantsExamples.SCHEDULING_INTERVAL
        ));

        // ebmc5s
        peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_96; j++) {
            peList.add(new ContainerPe(j, new CotainerPeProvisionerSimple(5000)));
        }
        vmList.add(new PowerContainerVm(
                IDs.pollId(ContainerVm.class),
                brokerId,
                5000,
                192000,
                10485760,
                90000L, // VM storage
                "Xen",
                new ContainerSchedulerTimeShared(peList),
                new ContainerRamProvisionerSimple(192000),
                new ContainerBwProvisionerSimple(33554432),
                peList,
                org.cloudbus.cloudsim.examples.container.ConstantsExamples.SCHEDULING_INTERVAL
        ));

        return vmList;
    }

    // ******************************* host creation **************************

    /**
     * 96 vCPUs for ecs.ebm*5s.24xlarge
     */
    public static final int V_CPUS_96 = 96;

    /**
     * 32 vCPUs for ecs.ebmc4.8xlarge
     */
    public static final int V_CPUS_32 = 32;

    /**
     * 8 vCPUs for ecs.ebmhfg5.2xlarge
     */
    public static final int V_CPUS_8 = 8;


    /**
     * https://www.alibabacloud.com/help/doc-detail/25378.htm#d18e3449
     */
    public static PowerContainerHostUtilizationHistory createHost_ebmhfg5() {
        ArrayList<ContainerVmPe> peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_8; ++j) {
            peList.add(new ContainerVmPe(j,
                    // 3.7 GHz = 7400 MIPS by https://stackoverflow.com/a/16226441
                    new ContainerVmPeProvisionerSimple(7400)));
        }

        return new PowerContainerHostUtilizationHistory(
                IDs.pollId(ContainerHost.class),
                new ContainerVmRamProvisionerSimple(32000), // 32.0 GiB
                new ContainerVmBwProvisionerSimple(6291456), // 6.0 Gbit/s = 6291456 kbps
                1000000L,
                peList,
                new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                new PowerModelLinearIntelXeonE3_1240v6()
        );
    }

    /**
     * https://www.alibabacloud.com/help/doc-detail/25378.htm#ebmc4
     */
    public static PowerContainerHostUtilizationHistory createHost_ebmc4() {
        ArrayList<ContainerVmPe> peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_32; ++j) {
            peList.add(new ContainerVmPe(j,
                    // 2.5 GHz = 5,000 MIPS by https://stackoverflow.com/a/16226441
                    new ContainerVmPeProvisionerSimple(5000)));
        }

        return new PowerContainerHostUtilizationHistory(
                IDs.pollId(ContainerHost.class),
                new ContainerVmRamProvisionerSimple(64000), // 64.0 GiB
                new ContainerVmBwProvisionerSimple(10485760), // 10.0 Gbit/s = 10485760 kbps
                1000000L,
                peList,
                new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                new PowerModelLinearIntelXeonE5_2682v4()
        );
    }

    /**
     * https://www.alibabacloud.com/help/doc-detail/25378.htm#d18e3114
     */
    public static PowerContainerHostUtilizationHistory createHost_ebmc5s() {
        ArrayList<ContainerVmPe> peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_96; ++j) {
            peList.add(new ContainerVmPe(j,
                                         // 2.5 GHz = 5,000 MIPS by https://stackoverflow.com/a/16226441
                                         new ContainerVmPeProvisionerSimple(5000)));
        }

        return new PowerContainerHostUtilizationHistory(
                IDs.pollId(ContainerHost.class),
                new ContainerVmRamProvisionerSimple(192000), // 192.0 GiB
                new ContainerVmBwProvisionerSimple(33554432), // 32.0 Gbit/s = 1048576 kbps
                1000000L,
                peList,
                new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                new PowerModelLinearIntelXeonPlatinum8163()
        );
    }

    /**
     * https://www.alibabacloud.com/help/doc-detail/25378.htm#d18e2898
     */
    public static PowerContainerHostUtilizationHistory createHost_ebmg5s() {
        ArrayList<ContainerVmPe> peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_96; ++j) {
            peList.add(new ContainerVmPe(j,
                    // 2.5 GHz = 5,000 MIPS by https://stackoverflow.com/a/16226441
                    new ContainerVmPeProvisionerSimple(5000)));
        }

        return new PowerContainerHostUtilizationHistory(
                IDs.pollId(ContainerHost.class),
                new ContainerVmRamProvisionerSimple(384000), // 384.0 GiB
                new ContainerVmBwProvisionerSimple(33554432), // 32.0 Gbit/s = 1048576 kbps
                1000000L,
                peList,
                new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                new PowerModelLinearIntelXeonPlatinum8163()
        );
    }

    /**
     * https://www.alibabacloud.com/help/doc-detail/25378.htm#d18e3330
     */
    public static PowerContainerHostUtilizationHistory createHost_ebmr5s() {
        ArrayList<ContainerVmPe> peList = new ArrayList<>();
        for (int j = 0; j < V_CPUS_96; ++j) {
            peList.add(new ContainerVmPe(j,
                    // 2.5 GHz = 5,000 MIPS by https://stackoverflow.com/a/16226441
                    new ContainerVmPeProvisionerSimple(5000)));
        }

        return new PowerContainerHostUtilizationHistory(
                IDs.pollId(ContainerHost.class),
                new ContainerVmRamProvisionerSimple(768000), // 768.0 GiB
                new ContainerVmBwProvisionerSimple(33554432), // 32.0 Gbit/s = 1048576 kbps
                1000000L,
                peList,
                new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                new PowerModelLinearIntelXeonPlatinum8163()
        );
    }

}
