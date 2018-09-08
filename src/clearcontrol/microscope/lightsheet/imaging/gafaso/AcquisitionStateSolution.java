package clearcontrol.microscope.lightsheet.imaging.gafaso;

import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionInterface;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * AcquisitionStateSolution
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class AcquisitionStateSolution implements SolutionInterface {

    private final static Random random = new Random();

    HashMap<LightSheetDOF, Double> state = new HashMap<LightSheetDOF, Double>();
    HashMap<LightSheetDOF, Double> stepState = new HashMap<LightSheetDOF, Double>();

    public AcquisitionStateSolution(HashMap<LightSheetDOF, Double> state, HashMap<LightSheetDOF, Double> stepState) {
        for (LightSheetDOF key : state.keySet()) {
            this.state.put(key, state.get(key));
        }
        for (LightSheetDOF key : stepState.keySet()) {
            this.stepState.put(key, stepState.get(key));
        }
    }

    StackInterface stack;

    public void setStack(StackInterface stack) {
        this.stack = stack;
    }

    @Override
    public double fitness() {
        if (stack == null) {
            return 0;
        }

        DCTS2D dcts2D = new DCTS2D();
        double[] quality = dcts2D.computeImageQualityMetric((OffHeapPlanarStack) stack);

        return new Mean().evaluate(quality);
    }

    @Override
    public void mutate() {
        int index = random.nextInt(state.keySet().size());

        boolean presign = random.nextBoolean();

        Iterator<LightSheetDOF> iterator = state.keySet().iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }

        LightSheetDOF key = iterator.next();

        double value = state.get(key);
        if (presign) {
            value += stepState.get(key);
        } else {
            value -= stepState.get(key);
        }
        state.remove(key);
        state.put(key, value);
        stack = null;
    }

    @Override
    public boolean isSimilar(SolutionInterface s, double similarityTolerance) {
        if (! (s instanceof AcquisitionStateSolution)) {
            return false;
        }

        for (LightSheetDOF key : state.keySet()) {
            if (Math.abs(state.get(key) - ((AcquisitionStateSolution) s).state.get(key)) > similarityTolerance) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        String result = this.getClass().getSimpleName() + ": " + state;
        return result;
    }

}
