/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller.algorithms.nonmodasm;

import Controller.algorithms.PrimerDesign;
import Controller.datastructures.Collector;
import Controller.datastructures.Part;
import Controller.datastructures.RGraph;
import Controller.datastructures.RNode;
import java.util.ArrayList;

/**
 *
 * @author evanappleton
 */
public class RHomologyPrimerDesign {
    
    public static ArrayList<String> homologousRecombinationPrimers(RNode node, RNode root, Collector coll, Double meltingTemp, Integer targetLength) {
        
        //initialize primer parameters
        ArrayList<String> oligos = new ArrayList<String>(2);
        String forwardOligoSequence;
        String reverseOligoSequence;
        String leftNeighborSeq;
        String rightNeighborSeq;
        Part currentPart = coll.getPart(node.getUUID(), true);
        Part rootPart = coll.getPart(root.getUUID(), true);
        ArrayList<Part> composition = rootPart.getComposition();
        int indexOf = composition.indexOf(currentPart);
        
        //Get the seqeunces of the neighbors
        if (indexOf == 0) {
            leftNeighborSeq = composition.get(composition.size()-1).getSeq();
            rightNeighborSeq = composition.get(1).getSeq();
        } else if (indexOf == composition.size() - 1) {
            leftNeighborSeq = composition.get(indexOf - 1).getSeq();
            rightNeighborSeq = composition.get(0).getSeq();
        } else {
            leftNeighborSeq = composition.get(indexOf - 1).getSeq();
            rightNeighborSeq = composition.get(indexOf + 1).getSeq();
        }

        int leftNeighborHomologyLength = PrimerDesign.getPrimerHomologyLength(meltingTemp, targetLength, leftNeighborSeq, false);
        int rightNeighborHomologyLength = PrimerDesign.getPrimerHomologyLength(meltingTemp, targetLength, PrimerDesign.reverseComplement(rightNeighborSeq), false);
        int currentPartLeftHomologyLength = PrimerDesign.getPrimerHomologyLength(meltingTemp, targetLength, currentPart.getSeq(), true);
        int currentPartRightHomologyLength = PrimerDesign.getPrimerHomologyLength(meltingTemp, targetLength, PrimerDesign.reverseComplement(currentPart.getSeq()), true);  
        
        forwardOligoSequence = leftNeighborSeq.substring(leftNeighborSeq.length() - leftNeighborHomologyLength) + currentPart.getSeq().substring(0, currentPartLeftHomologyLength);
        reverseOligoSequence = PrimerDesign.reverseComplement(currentPart.getSeq().substring(currentPart.getSeq().length() - currentPartRightHomologyLength) + rightNeighborSeq.substring(0, rightNeighborHomologyLength));

        oligos.add(forwardOligoSequence);
        oligos.add(reverseOligoSequence);
        
        return oligos;
    }
    
    /** Traverses graphs and looks for parts that are too small for homologous recombination and merges them with their neighbor(s) **/
    public void smallPartHomologyGraphMerge (ArrayList<RGraph> optimalGraphs) {
        
    }
}
