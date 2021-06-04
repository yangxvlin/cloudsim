package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

import static org.cloudbus.cloudsim.container.core.containerCloudSimTags.INACTIVE_CONTAINER_DESTROY;
import static org.cloudbus.cloudsim.container.core.containerCloudSimTags.INACTIVE_CONTAINER_DESTROY_ACK;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-04 16:04
 * description:
 **/

public class MyPowerContainerDatacenterCM extends PowerContainerDatacenterCM {
    public MyPowerContainerDatacenterCM(String name,
                                        ContainerDatacenterCharacteristics characteristics,
                                        ContainerVmAllocationPolicy vmAllocationPolicy,
                                        ContainerAllocationPolicy containerAllocationPolicy,
                                        List<Storage> storageList,
                                        double schedulingInterval,
                                        String experimentName,
                                        String logAddress,
                                        double vmStartupDelay,
                                        double containerStartupDelay) throws Exception {
        super(name,
                characteristics,
                vmAllocationPolicy,
                containerAllocationPolicy,
                storageList,
                schedulingInterval,
                experimentName,
                logAddress,
                vmStartupDelay,
                containerStartupDelay
        );
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case INACTIVE_CONTAINER_DESTROY:
                processInactiveContainerDestroy(ev);
                break;
            default:
                super.processOtherEvent(ev);
        }
    }

    public void processInactiveContainerDestroy(SimEvent ev) {
        List<Container> containersToDestroy = (List<Container>) ev.getData();
        Log.printConcatLine(CloudSim.clock(), ": Datacenter going to destroy " + containersToDestroy.size() + " inactive containers");

        for (Container c: containersToDestroy) {
            if (getContainerList().contains(c) && c.getVm() != null) {
                c.getVm().containerDestroy(c);
                sendNow(ev.getSource(), INACTIVE_CONTAINER_DESTROY_ACK, c);
            }
        }

    }
}
