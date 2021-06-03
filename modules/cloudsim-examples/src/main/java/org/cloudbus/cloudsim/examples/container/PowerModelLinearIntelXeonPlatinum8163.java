package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-01 22:27
 * description:
 **/

public class PowerModelLinearIntelXeonPlatinum8163 extends PowerModelSpecPower {

    /**
     *  95 watts for idle               https://en.wikipedia.org/wiki/Skylake_(microarchitecture)
     * 165 watts for full utilisation   "Alita: Comprehensive Performance Isolation through Bias Resource Management for Public Clouds"
     * As there is no power spec available, generated based on linear power model.
     * @see #getPowerData(int)
     */
    private final double[] power = { 95, 102, 109, 116, 123, 130, 137, 144, 151, 158, 165 };

    @Override
    protected double getPowerData(int index) {
        return power[index];
    }
}
