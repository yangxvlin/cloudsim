package org.cloudbus.cloudsim.examples.container;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.lists.ContainerList;
import org.cloudbus.cloudsim.container.lists.ContainerVmList;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cloudbus.cloudsim.container.core.containerCloudSimTags.INACTIVE_CONTAINER_DESTROY_ACK;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-03 12:55
 * description:
 **/

/**
 * extension:
 *      1. submit Vm on other data center if creation fail
 *      2. submit container on other Vm if creation fail
 *      3. submit cloudlet if destination for cloudlet is not specified
 * */
public class MyContainerDatacenterBroker extends ContainerDatacenterBroker {
    /**
     * Created a new DatacenterBroker object.
     *
     * @param name              name to be associated with this entity (as required by Sim_entity class from
     *                          simjava package)
     * @param overBookingfactor
     * @pre name != null
     */
    public MyContainerDatacenterBroker(String name, double overBookingfactor) throws Exception {
        super(name, overBookingfactor);
    }

    private int requestedContainers = 0;
    // private List<Container> containerResubmitList = new ArrayList<>();


    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case INACTIVE_CONTAINER_DESTROY_ACK:
                processInactiveContainerDestroyAck(ev);
                break;
            default:
                super.processOtherEvent(ev);
        }
    }

    public void processInactiveContainerDestroyAck(SimEvent ev) {
        Container container = (Container) ev.getData();
        containersToDatacentersMap.remove(container.getId());
        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Successfully destroyed inactive container #", container.getId(), " in broker & datacenter");
    }

    @Override
    public void processContainerCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int vmId = data[0];
        int containerId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            if(vmId ==-1){
                Log.printConcatLine("Error : Where is the VM");
            }else{
                getContainersToVmsMap().put(containerId, vmId);
                getContainersCreatedList().add(ContainerList.getById(getContainerList(), containerId));

                // ContainerVm p= ContainerVmList.getById(getVmsCreatedList(), vmId);
                int hostId = ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId();
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": The Container #", containerId,
                        ", is created on Vm #",vmId
                        , ", On Host#", hostId);
                setContainersCreated(getContainersCreated()+1);}
        } else {
            // Container container = ContainerList.getById(getContainerList(), containerId);
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Failed Creation of Container #", containerId);
            // containerResubmitList.add(container);
        }

        incrementContainersAcks();
        Log.printConcatLine("processContainerCreate: ", getContainersAcks(), " ", requestedContainers);
        if (getContainersAcks() == requestedContainers) {
            // Log.printLine(getContainersCreatedList().size() + "vs asli"+getContainerList().size());
            submitCloudlets();
            // getContainerList().clear();
            setContainersAcks(0);
        }
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        ContainerCloudlet cloudlet = (ContainerCloudlet) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Log.printConcatLine(CloudSim.clock(), ": ", getName(), String.format(": Cloudlet #%4d", cloudlet.getCloudletId()), " finished, ",
                "#cloudlets finished so far: ", getCloudletReceivedList().size());
        cloudletsSubmitted--;
        Log.printConcatLine("cloudlet list size: ", getCloudletList().size(), " container list size: ", containerList.size(), ", cloudletsSubmitted: ", cloudletsSubmitted);
        if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
            clearDatacenters();
            finishExecution();
        } else { // some cloudlets haven't finished yet
            if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
                // all the cloudlets sent finished. It means that some bount
                // cloudlet is waiting its VM be created


                sendNow(datacenterIdsList.get(0), containerCloudSimTags.INACTIVE_CONTAINER_DESTROY, containersCreatedList);
                if(!resubmitContainers()) {
                    clearDatacenters();
                    createVmsInDatacenter(0);
                }
            }

        }
    }

    protected boolean resubmitContainers() {
        List<Container> containerResubmitList = new ArrayList<>();
        for (Container c: getContainerList()) {
            if (!getContainersCreatedList().contains(c)) {
                containerResubmitList.add(c);
            }
        }
        requestedContainers = containerResubmitList.size();
        if (requestedContainers > 0) {
            sendNow(getDatacenterIdsList().get(0), containerCloudSimTags.CONTAINER_SUBMIT, containerResubmitList);
            return true;
        } else {
            return false;
        }
    }

    //    @Override
//    protected void processVmCreate(SimEvent ev) {
//        int[] data = (int[]) ev.getData();
//        int datacenterId = data[0];
//        int vmId = data[1];
//        int result = data[2];
//
//        if (result == CloudSimTags.TRUE) {
//            getVmsToDatacentersMap().put(vmId, datacenterId);
//            if (getVmsCreatedList().contains(ContainerVmList.getById(getVmList(), vmId))) {
//                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
//                        " already created in Datacenter #", datacenterId, ", Host #",
//                        ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
//            }
//            getVmsCreatedList().add(ContainerVmList.getById(getVmList(), vmId));
//            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
//                    " has been created in Datacenter #", datacenterId, ", Host #",
//                    ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
//            setNumberOfCreatedVMs(getNumberOfCreatedVMs()+1);
//        } else {
//            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
//                    " failed in Datacenter #", datacenterId);
//        }
//
//        incrementVmsAcks();
//
//        // If we have tried creating all of the vms in the data center, we submit the containers.
//        if(vmsRequested == vmsAcks){
//            submitContainers();
//            if (vmsCreatedList.size() < getVmList().size()) {
//                // find id of the next datacenter that has not been tried
//                // Log.printLine(CloudSim.clock() + ": " + Arrays.toString(getDatacenterRequestedIdsList().toArray()));
//                for (int nextDatacenterId : getDatacenterIdsList()) {
//                    if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
//                        createVmsInDatacenterLater(nextDatacenterId);
//                        return;
//                    }
//                }
//
//                // all datacenters already queried
//                if (getVmsCreatedList().size() > 0) { // if some vm were created
//                    submitContainers();
//                } else { // no vms created. abort
//                    Log.printLine(CloudSim.clock() + ": " + getName() + " "
//                            + (vmList.size() - vmsCreatedList.size()) + " of Vm not created among all data center because of not enough capacity");
//                }
//            }
//        }
//    }

    @Override
    protected void submitContainers() {

        int i = 0;
        for(Container container:getContainerList()) {
            ContainerCloudlet cloudlet = getCloudletList().get(i);
            //Log.printLine("Containers Created" + getContainersCreated());

//            UtilizationModelPlanetLabInMemory temp = (UtilizationModelPlanetLabInMemory) cloudlet.getUtilizationModelCpu();
//            double[] cloudletUsage = temp.getData();
//            Percentile percentile = new Percentile();
//            double percentileUsage = percentile.evaluate(cloudletUsage, getOverBookingfactor());
//            //Log.printLine("Container Index" + containerIndex);
//            double newmips = percentileUsage * container.getMips();
////                    double newmips = percentileUsage * container.getMips();
////                    double maxUsage = Doubles.max(cloudletUsage);
////                    double newmips = maxUsage * container.getMips();
            // container.setWorkloadMips(container.getMips());
////                    bindCloudletToContainer(cloudlet.getCloudletId(), container.getId());

            if(cloudlet.getContainerId() != container.getId()){
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": ","Binding Cloudlet #", cloudlet.getCloudletId(), " From Container #",cloudlet.getContainerId(), " -> Container #", container.getId());
            }
            cloudlet.setContainerId(container.getId());

            i++;

        }

        List<Container> successfullySubmitted = new ArrayList<>(getContainerList());
        requestedContainers = successfullySubmitted.size();
        sendNow(getDatacenterIdsList().get(0), containerCloudSimTags.CONTAINER_SUBMIT, successfullySubmitted);
    }

//    /**
//     * Create the virtual machines in a datacenter (delayed).
//     *
//     * @param datacenterId Id of the chosen PowerDatacenter
//     * @pre $none
//     * @post $none
//     */
//    protected void createVmsInDatacenterLater(int datacenterId) {
//        // send as much vms as possible for this datacenter before trying the next one
//        int requestedVms = 0;
//        String datacenterName = CloudSim.getEntityName(datacenterId);
//        for (ContainerVm vm : getVmList()) {
//            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
//                Log.printLine(String.format("%s: %s: Trying to Create VM #%d in %s(#%s)", CloudSim.clock(), getName(), vm.getId(), datacenterName, datacenterId));
//                // sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
//                send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, vm);
//                requestedVms++;
//            }
//        }
//
//        getDatacenterRequestedIdsList().add(datacenterId);
//
//        setVmsRequested(requestedVms);
//        setVmsAcks(0);
//    }


}
