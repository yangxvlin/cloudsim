package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.ArrayList;
import java.util.List;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-03 23:16
 * description:
 **/

public class ContainerVmAllocationPolicyPredefined extends ContainerVmAllocationPolicySimple {
    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public ContainerVmAllocationPolicyPredefined(List<? extends ContainerHost> list) {
        super(list);
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm) {
        boolean result = false;
        int requiredPes = containerVm.getNumberOfPes();
        ContainerHost host = containerVm.getHost();
        List<Integer> freePesTmp = new ArrayList<>(getFreePes());

        if (host != null) {
            int moreFree = Integer.MIN_VALUE;
            int idx = -1;

            // we want the host with less pes in use
            for (int i = 0; i < freePesTmp.size(); i++) {
                if (freePesTmp.get(i) > moreFree) {
                    moreFree = freePesTmp.get(i);
                    idx = i;
                }
            }

            result = host.containerVmCreate(containerVm);

            if (result) { // if vm were succesfully created in the host
                getVmTable().put(containerVm.getUid(), host);
                getUsedPes().put(containerVm.getUid(), requiredPes);
                getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                result = true;
            } else {
                freePesTmp.set(idx, Integer.MIN_VALUE);
            }
        }

        return result;
    }
}
