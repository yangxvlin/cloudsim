package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerDatacenterBroker;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.lists.ContainerVmList;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.Arrays;

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

    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            if (getVmsCreatedList().contains(ContainerVmList.getById(getVmList(), vmId))) {
                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
                        " already created in Datacenter #", datacenterId, ", Host #",
                        ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
            }
            getVmsCreatedList().add(ContainerVmList.getById(getVmList(), vmId));
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
                    " has been created in Datacenter #", datacenterId, ", Host #",
                    ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
            setNumberOfCreatedVMs(getNumberOfCreatedVMs()+1);
        } else {
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
                    " failed in Datacenter #", datacenterId);
        }

        incrementVmsAcks();

        // If we have tried creating all of the vms in the data center, we submit the containers.
        if(vmsRequested == vmsAcks){
            submitContainers();
            if (vmsCreatedList.size() < getVmList().size()) {
                // find id of the next datacenter that has not been tried
                // Log.printLine(CloudSim.clock() + ": " + Arrays.toString(getDatacenterRequestedIdsList().toArray()));
                for (int nextDatacenterId : getDatacenterIdsList()) {
                    if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
                        createVmsInDatacenterLater(nextDatacenterId);
                        return;
                    }
                }

                // all datacenters already queried
                if (getVmsCreatedList().size() > 0) { // if some vm were created
                    submitContainers();
                } else { // no vms created. abort
                    Log.printLine(CloudSim.clock() + ": " + getName() + " "
                            + (vmList.size() - vmsCreatedList.size()) + " of Vm not created among all data center because of not enough capacity");
                }
            }
        }
    }

    /**
     * Create the virtual machines in a datacenter.
     *
     * @param datacenterId Id of the chosen PowerDatacenter
     * @pre $none
     * @post $none
     */
    protected void createVmsInDatacenterLater(int datacenterId) {
        // send as much vms as possible for this datacenter before trying the next one
        int requestedVms = 0;
        String datacenterName = CloudSim.getEntityName(datacenterId);
        for (ContainerVm vm : getVmList()) {
            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                Log.printLine(String.format("%s: %s: Trying to Create VM #%d in %s(#%s)", CloudSim.clock(), getName(), vm.getId(), datacenterName, datacenterId));
                // sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                send(datacenterId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, vm);
                requestedVms++;
            }
        }

        getDatacenterRequestedIdsList().add(datacenterId);

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }


}
