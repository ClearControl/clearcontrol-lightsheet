package clearcontrol.microscope.lightsheet.imaging.interleavedgao;

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
 * InterleavedGAOStateSolution
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class InterleavedGAOStateSolution implements SolutionInterface {

    private final static Random random = new Random();

    HashMap<LightSheetDOF, Double> state = new HashMap<LightSheetDOF, Double>();
    HashMap<LightSheetDOF, Double> stepState = new HashMap<LightSheetDOF, Double>();

    public InterleavedGAOStateSolution(HashMap<LightSheetDOF, Double> state, HashMap<LightSheetDOF, Double> stepState) {
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
    public String toString() {
        String result = this.getClass().getSimpleName() + ": " + state;
        return result;
    }
}
