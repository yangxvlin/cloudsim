package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

/**
 * Xulin Yang, 904904
 *
 * @create 2021-06-02 13:53
 * description:
 **/

public class PowerModelLinearIntelXeonE3_1240v6 extends PowerModelSpecPower {

    /**
     * http://www.spec.org/power_ssj2008/results/res2016q1/power_ssj2008-20151214-00707.html
     * @see #getPowerData(int)
     */
    private final double[] power = { 16.1, 19.3, 20.6, 22.3, 24.2, 27.0, 30.5, 34.7, 39.2, 43.9, 47.2 };

    @Override
    protected double getPowerData(int index) {
        return power[index];
    }
}
