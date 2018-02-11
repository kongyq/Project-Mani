package homology;

import edu.stanford.math.plex4.api.Plex4;
import edu.stanford.math.plex4.homology.barcodes.BarcodeCollection;
import edu.stanford.math.plex4.homology.barcodes.Interval;
import edu.stanford.math.plex4.homology.chain_basis.Simplex;
import edu.stanford.math.plex4.homology.interfaces.AbstractPersistenceAlgorithm;
import edu.stanford.math.plex4.streams.impl.ExplicitSimplexStream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mike on 12/5/17.
 */
public class Plex {

    private ExplicitSimplexStream stream;
    private BarcodeCollection barcode;

    public Plex() {
        this.stream = new ExplicitSimplexStream(1.0);
    }

    public void addElement(Integer vertex1, Integer vertex2, double filtrationValue){
        this.stream.addElement(new int[]{vertex1, vertex2}, filtrationValue);
    }

    public void addVertex(Integer vertex, double filtrationValue){
        this.stream.addVertex(vertex, filtrationValue);
    }

    public void finalizeStream(){
        this.stream.finalizeStream();
    }

    public ExplicitSimplexStream getStream() {
        return this.stream;
    }

    public BarcodeCollection getBarcode(){
        AbstractPersistenceAlgorithm<Simplex> algorithm = Plex4.getModularSimplicialAlgorithm(2,2);
        this.barcode = algorithm.computeIntervals(stream);
        return this.barcode;
    }

    public List<Interval<Double>> getBarcode(Integer dimension){
        return this.getBarcode().getIntervalsAtDimension(dimension);
    }

    public Double getLengthOfInterval(Interval<Double> interval){
        return interval.getEnd()-interval.getStart();
    }

 /*   public List<Double> getLengthOfIntervals(List<Interval> intervals){
        return intervals.stream().map(interval -> getLengthOfInterval(interval)).collect(Collectors.toList());
    }*/
}
