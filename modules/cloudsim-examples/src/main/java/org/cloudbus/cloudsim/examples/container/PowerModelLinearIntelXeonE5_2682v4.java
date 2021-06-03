package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-02 13:41
 * description:
 **/

public class PowerModelLinearIntelXeonE5_2682v4 extends PowerModelSpecPower {

    /**
     *  55 watts for idle               https://en.wikipedia.org/wiki/Broadwell_(microarchitecture)#Design_and_variants
     * 120 watts for full utilisation   https://en.wikichip.org/wiki/intel/xeon_e5/e5-2682_v4
     * As there is no power spec available, generated based on linear power model.
     * @see #getPowerData(int)
     */
    private final double[] power = { 55, 61.5, 68, 74.5, 81, 87.5, 94, 100.5, 107, 113.5, 120 };

    @Override
    protected double getPowerData(int index) {
        return power[index];
    }
}