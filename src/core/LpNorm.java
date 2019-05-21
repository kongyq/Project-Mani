package core;

import edu.stanford.math.plex4.homology.barcodes.Interval;
import org.jfree.ui.RefineryUtilities;
import plot.Plot;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by anonymous on 12/11/17.
 */
public class LpNorm {
    private List<Interval<Double>> intervals;
    private Double P;
    private Double Score;

    public LpNorm(List<Interval<Double>> intervals, Double P){

        this.intervals = intervals;
        this.P = P;
        this.computeLpNorm();
    }

    private void computeLpNorm(){
        List<Double> lengths = this.intervals.stream().map(interval -> 1D - interval.getStart()).collect(Collectors.toList());
        Collections.sort(lengths,Collections.reverseOrder());
        Plot plot = new Plot(lengths);
        this.Score = Math.pow(lengths.stream().mapToDouble(length -> Math.pow(length, this.P)).sum(), 1D/P);

        plot.pack( );
        RefineryUtilities.centerFrameOnScreen( plot );
        plot.setVisible( true );
    }

    public Double getScore(){
        return this.Score;
    }


}
