package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.resourceAllocators.*;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-03 22:11
 * description:
 **/

public class AutoContainerTest2 {
    /**
     * The cloudlet list.
     */
    private static List<ContainerCloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private static List<ContainerVm> vmList;

    /**
     * The vmlist.
     */

    private static List<Container> containerList;

    public static void main(String[] args) {
        Log.printLine("Starting AutoContainerTest1...");

        try {
            /**
             * number of cloud Users
             */
            int num_user = 1;
            /**
             *  The fields of calender have been initialized with the current date and time.
             */
            Calendar calendar = Calendar.getInstance();
            /**
             * Deactivating the event tracing
             */
            boolean trace_flag = false;

            /**
             * 1- Like CloudSim the first step is initializing the CloudSim Package before creating any entities.
             *
             */
            CloudSim.init(num_user, calendar, trace_flag);

            /**
             * 2-  Defining the container allocation Policy. This policy determines how Containers are
             * allocated to VMs in the data center.
             *
             */
            ContainerAllocationPolicy containerAllocationPolicy = new PowerContainerAllocationPolicySimple();

            /**
             * 3-  Defining the VM selection Policy. This policy determines which VMs should be selected for migration
             * when a host is identified as over-loaded.
             *
             */
//            PowerContainerVmSelectionPolicy vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();


            /**
             * 4-  Defining the host selection Policy. This policy determines which hosts should be selected as
             * migration destination.
             *
             */
//            HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicyFirstFit();

            /**
             * 5- Defining the thresholds for selecting the under-utilized and over-utilized hosts.
             */
//            double overUtilizationThreshold = 0.80;
//            double underUtilizationThreshold = 0.70;

            /**
             * 6- The host list is created considering the number of hosts, and host types which are specified
             * in the {@link ConstantsExamples}.
             */
            List<ContainerHost> hostList = new ArrayList<>();
            ContainerHost smallHost = AlibabaConstant.createHost_ebmhfg5();
            ContainerHost mediumHost = AlibabaConstant.createHost_ebmc4();
            ContainerHost largeHost = AlibabaConstant.createHost_ebmc5s();
            hostList.add(smallHost);
            hostList.add(mediumHost);
            hostList.add(largeHost);

            cloudletList = new ArrayList<ContainerCloudlet>();
            vmList = new ArrayList<ContainerVm>();

            /**
             * 7- The container allocation policy  which defines the allocation of VMs to containers.
             */
            ContainerVmAllocationPolicy vmAllocationPolicy  = new ContainerVmAllocationPolicyPredefined(hostList);

            /**
             * 8- The overbooking factor for allocating containers to VMs. This factor is used by the broker for the
             * allocation process.
             */
            int overBookingFactor = 80;
            ContainerDatacenterBroker broker = createBroker(overBookingFactor);
            int brokerId = broker.getId();

            /**
             * 9- Creating the cloudlet, container and VM lists for submitting to the broker.
             */
            cloudletList = AlibabaConstant.createContainerCloudletListConstant(brokerId, 90);
            containerList = AlibabaConstant.createContainerList(brokerId, 90);
            vmList = AlibabaConstant.createVmList(brokerId);
            vmList.get(0).setHost(smallHost);
            vmList.get(1).setHost(mediumHost);
            vmList.get(2).setHost(largeHost);

            /**
             * 10- The address for logging the statistics of the VMs, containers in the data center.
             */
            String logAddress = "~/Results";

            @SuppressWarnings("unused")
            PowerContainerDatacenter e = (PowerContainerDatacenter) createDatacenter("datacenter",
                    PowerContainerDatacenterCM.class,
                    hostList, vmAllocationPolicy,
                    containerAllocationPolicy,
                    getExperimentName("AutoContainerTest1-1", String.valueOf(overBookingFactor)),
                    org.cloudbus.cloudsim.examples.container.ConstantsExamples.SCHEDULING_INTERVAL,
                    logAddress,
                    org.cloudbus.cloudsim.examples.container.ConstantsExamples.VM_STARTTUP_DELAY,
                    org.cloudbus.cloudsim.examples.container.ConstantsExamples.CONTAINER_STARTTUP_DELAY);


            /**
             * 11- Submitting the cloudlet's , container's , and VM's lists to the broker.
             */
            broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
            broker.submitContainerList(containerList);
            broker.submitVmList(vmList);

            /**
             * 12- Determining the simulation termination time according to the cloudlet's workload.
             */
            CloudSim.terminateSimulation(86400); // 86400s = 24h = 1day
            // CloudSim.terminateSimulation(691200); // 691200s = 192h = 8days
            /**
             * 13- Starting the simualtion.
             */
            CloudSim.startSimulation();
            /**
             * 14- Stopping the simualtion.
             */
            CloudSim.stopSimulation();
            /**
             * 15- Printing the results when the simulation is finished.
             */
            List<ContainerCloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("AutoContainerTest1 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }


    /**
     * It creates a specific name for the experiment which is used for creating the Log address folder.
     */
    private static String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isEmpty()) {
                if (i != 0) {
                    experimentName.append("_");
                }

                experimentName.append(args[i]);
            }
        }

        return experimentName.toString();
    }

    /**
     * Creates the broker.
     *
     * @param overBookingFactor
     * @return the datacenter broker
     */
    private static ContainerDatacenterBroker createBroker(int overBookingFactor) {

        ContainerDatacenterBroker broker = null;

        try {
            broker = new MyContainerDatacenterBroker("Broker", overBookingFactor);
            // broker = new ContainerDatacenterBroker("Broker", overBookingFactor);
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<ContainerCloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine(String.format("|%15s", "Cloudlet ID") +
                String.format("|%10s", "STATUS") +
                String.format("|%15s", "Data center ID") +
                String.format("|%5s", "VM ID") +
                String.format("|%15s", "Container ID") +
                String.format("|%15s", "Submit Time") +
                String.format("|%15s", "Start Time") +
                String.format("|%15s", "Waiting Time") +
                String.format("|%15s", "Finish Time") +
                String.format("|%15s", "Actual CPU Time")
        );

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);

            if (cloudlet.getCloudletStatusString() == "Success") {

                Log.printLine(String.format("|%15s", cloudlet.getCloudletId()) +
                        String.format("|%10s", "SUCCESS") +
                        String.format("|%15s", cloudlet.getResourceId()) +
                        String.format("|%5s", cloudlet.getVmId()) +
                        String.format("|%15s", ((ContainerCloudlet) cloudlet).getContainerId()) +
                        String.format("|%15s", dft.format(cloudlet.getSubmissionTime())) +
                        String.format("|%15s", dft.format(cloudlet.getExecStartTime())) +
                        String.format("|%15s", dft.format(cloudlet.getWaitingTime())) +
                        String.format("|%15s", dft.format(cloudlet.getFinishTime())) +
                        String.format("|%15s", dft.format(cloudlet.getActualCPUTime()))
                );
            }
        }
    }

    /**
     * Create the data center
     *
     * @param name
     * @param datacenterClass
     * @param hostList
     * @param vmAllocationPolicy
     * @param containerAllocationPolicy
     * @param experimentName
     * @param logAddress
     * @return
     * @throws Exception
     */

    public static ContainerDatacenter createDatacenter(String name, Class<? extends ContainerDatacenter> datacenterClass,
                                                       List<ContainerHost> hostList,
                                                       ContainerVmAllocationPolicy vmAllocationPolicy,
                                                       ContainerAllocationPolicy containerAllocationPolicy,
                                                       String experimentName, double schedulingInterval,
                                                       String logAddress,
                                                       double VMStartupDelay,
                                                       double ContainerStartupDelay) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0D;
        double cost = 3.0D;
        double costPerMem = 0.05D;
        double costPerStorage = 0.001D;
        double costPerBw = 0.0D;
        ContainerDatacenterCharacteristics characteristics = new
                ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage,
                costPerBw);
        ContainerDatacenter datacenter = new MyPowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, experimentName, logAddress,
                VMStartupDelay, ContainerStartupDelay);
//        ContainerDatacenter datacenter = new PowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
//                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, experimentName, logAddress,
//                VMStartupDelay, ContainerStartupDelay);
//        ContainerDatacenter datacenter = new PowerContainerDatacenter(name, characteristics, vmAllocationPolicy,
//                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, experimentName, logAddress
//        );

        return datacenter;
    }
}

