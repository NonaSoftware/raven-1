/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller.algorithms;

import Controller.datastructures.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jenhantao, evanappleton
 */
public class Modularity extends Partitioning {

     /**
     * ************************************************************************
     *
     * THIS CLASS HAS MOSTLY SHARING AND HELPER METHODS FOR MAIN ALGORITHM
     *
     *************************************************************************
     */
    
    /** Find sharing score for all possible intermediates for a set of goal parts **/
    protected HashMap<String, Integer> computeIntermediateSharing(ArrayList<RNode> goalParts) {
        HashMap<String, Integer> sharing = new HashMap<String, Integer>();

        //For each goal part
        for (int i = 0; i < goalParts.size(); i++) {
            RNode gp = goalParts.get(i);
            ArrayList<String> gpComposition = gp.getComposition();
            int gpSize = gp.getComposition().size();

            //For all possible intermediates within each goal part
            for (int j = 0; j < gpSize; j++) {
                for (int k = j + 2; k < gpSize + 1; k++) {
                    ArrayList<String> intermediateComposition = new ArrayList<String>();
                    intermediateComposition.addAll(gpComposition.subList(j, k));

                    //See if it has been seen before or not
                    if (sharing.containsKey(intermediateComposition.toString())) {

                        //If seen increment the sharing factor
                        sharing.put(intermediateComposition.toString(), sharing.get(intermediateComposition.toString()) + 1);
                    } else {

                        //If it has not been seen, initialize a place in the hashmap with value 0
                        sharing.put(intermediateComposition.toString(), 0);
                    }
                }
            }
        }
        return sharing;
    }
    
    /**
     * ************************************************************************
     *
     * MODULARITY CALCULATION METHODS
     *
     *************************************************************************
     */

    /**
     * For each node, decompose into transcriptional units, return either part
     * types (1) or compositions (2). << Assumes part starts with a promoter and
     * ends with a terminator >> *
     */
    // type- 1 for name 2 for composition
    protected ArrayList<ArrayList<String>> getTranscriptionalUnits(ArrayList<RNode> goalParts, int type) {

        ArrayList<ArrayList<String>> TUs = new ArrayList<ArrayList<String>>();

        //For each goal part get TUs
        for (int i = 0; i < goalParts.size(); i++) {
            RNode gp = goalParts.get(i);

            ArrayList<String> types = gp.getType();
            ArrayList<String> comps = gp.getComposition();
            ArrayList<Integer> starts = new ArrayList<Integer>();
            starts.add(0);

            //For all the elements of this part's types
            for (int j = 0; j < types.size(); j++) {


                //If the element is a terminator and either it's the last element or there is a promoter directly after it
                if (j < (types.size() - 1)) {
                    if ((types.get(j).equalsIgnoreCase("terminator") || types.get(j).equalsIgnoreCase("t")) && (types.get(j + 1).equalsIgnoreCase("promoter") || types.get(j + 1).equalsIgnoreCase("p"))) {
                        if (type == 1) {
                            for (Integer aStart : starts) {
                                ArrayList<String> aTU = new ArrayList<String>();
                                aTU.addAll(types.subList(aStart, j + 1));
                                TUs.add(aTU);
                            }
                            starts.add(j + 1);
                        } else if (type == 2) {
                            for (Integer aStart : starts) {
                                ArrayList<String> aTU = new ArrayList<String>();
                                aTU.addAll(comps.subList(aStart, j + 1));
                                TUs.add(aTU);
                            }
                            starts.add(j + 1);
                        }
                    }
                } else {
                    if (type == 1) {
                        for (Integer aStart : starts) {
                            ArrayList<String> aTU = new ArrayList<String>();
                            aTU.addAll(types.subList(aStart, j + 1));
                            TUs.add(aTU);
                        }
                    } else if (type == 2) {
                        for (Integer aStart : starts) {
                            ArrayList<String> aTU = new ArrayList<String>();
                            aTU.addAll(comps.subList(aStart, j + 1));
                            TUs.add(aTU);
                        }
                    }
                }
            }
        }

        return TUs;
    }

    /*
     * Get all independent transcriptional units of a construct
     */
    protected ArrayList<ArrayList<String>> getSingleTranscriptionalUnits(ArrayList<RNode> goalParts, int type) {

        ArrayList<ArrayList<String>> TUs = new ArrayList<ArrayList<String>>();

        //For each goal part get TUs
        for (int i = 0; i < goalParts.size(); i++) {
            RNode gp = goalParts.get(i);

            ArrayList<String> types = gp.getType();
            ArrayList<String> comps = gp.getComposition();
            int start = 0;

            //For all the elements of this part's types
            for (int j = 0; j < types.size(); j++) {


                //If the element is a terminator and either it's the last element or there is a promoter directly after it
                if (j < (types.size() - 1)) {
                    if ((types.get(j).equalsIgnoreCase("terminator") || types.get(j).equalsIgnoreCase("t")) && (types.get(j + 1).equalsIgnoreCase("promoter") || types.get(j + 1).equalsIgnoreCase("p"))) {
                        if (type == 1) {
                            ArrayList<String> aTU = new ArrayList<String>();
                            aTU.addAll(types.subList(start, j + 1));
                            TUs.add(aTU);
                            start = j + 1;
                        } else if (type == 2) {
                            ArrayList<String> aTU = new ArrayList<String>();
                            aTU.addAll(comps.subList(start, j + 1));
                            TUs.add(aTU);
                            start = j + 1;
                        }
                    }
                } else {
                    if (type == 1) {
                        ArrayList<String> aTU = new ArrayList<String>();
                        aTU.addAll(types.subList(start, j + 1));
                        TUs.add(aTU);
                    } else if (type == 2) {
                        ArrayList<String> aTU = new ArrayList<String>();
                        aTU.addAll(comps.subList(start, j + 1));
                        TUs.add(aTU);
                    }
                }
            }
        }
        return TUs;
    }

    /**
     * Positional Scoring Heuristic (Not implemented currently)
     */
    protected HashMap<Integer, HashMap<String, Double>> getPositionalScoring(ArrayList<ArrayList<String>> positionParts) {
        HashMap<Integer, HashMap<String, Double>> positionScores = new HashMap<Integer, HashMap<String, Double>>();
        HashMap<Integer, ArrayList<String>> partsByPosition = new HashMap<Integer, ArrayList<String>>();

        //For each part
        for (int i = 0; i < positionParts.size(); i++) {
            ArrayList<String> part = positionParts.get(i);

            //Add it's part composition to the scoring matrix at each position
            for (int j = 0; j < part.size(); j++) {
                if (partsByPosition.get(j) == null) {
                    ArrayList<String> partList = new ArrayList<String>();
                    partList.add(part.get(j));
                    partsByPosition.put(j, partList);
                } else {
                    partsByPosition.get(j).add(part.get(j));
                }
            }
        }

        //Now score each position
        for (int k = 0; k < partsByPosition.size(); k++) {
            
            HashMap<String, Double> scoreThisPos = new HashMap<String, Double>();
            ArrayList<String> partsAtAPosition = partsByPosition.get(k);
            double size = partsAtAPosition.size();

            //Get score of each part type
            for (String part : partsAtAPosition) {
                if (scoreThisPos.containsKey(part)) {
                    double score = scoreThisPos.get(part);
                    scoreThisPos.put(part, score + 1);
                } else {
                    double score = 1.0;
                    scoreThisPos.put(part, score);
                }
            }

            //Normalize scores at this position
            Set<String> keySet = scoreThisPos.keySet();
            double maxVal = 0;
            for (String part : keySet) {
                double val = scoreThisPos.get(part);
                val = val / size;
                if (val > maxVal) {
                    maxVal = val;
                }
                scoreThisPos.put(part, val);
            }
            scoreThisPos.put("maximum", maxVal);

            //Add this position's score to the scoring matrix
            positionScores.put(k, scoreThisPos);
        }
        return positionScores;
    }
    
    /**
     * ************************************************************************
     *
     * OVERHANG ASSIGNMENT METHODS
     *
     *************************************************************************
     */
    
    /*
     * First step of overhang assignment - propagate numeric place holders for overhangs, ie no overhang redundancy in any step
     */
    protected void propagatePrimaryOverhangs(ArrayList<RGraph> optimalGraphs) {

        //Initialize fields that record information to save complexity for future steps
        _parentHash = new HashMap<RNode, RNode>();
        _rootBasicNodeHash = new HashMap<RNode, ArrayList<RNode>>();
        _stageDirectionAssignHash = new HashMap<Integer, HashMap<String, ArrayList<RNode>>>();
        _OHexclusionHash = new HashMap<String, HashSet<String>>(); //key: 1st pass overhang value: all overhangs that cannot be the same

        //Loop through each optimal graph and assign first-pass overhangs recursively
        int count = 0;
        for (RGraph graph : optimalGraphs) {
            RNode root = graph.getRootNode();
            ArrayList<RNode> l0nodes = new ArrayList<RNode>();
            _rootBasicNodeHash.put(root, l0nodes);
            root.setLOverhang(Integer.toString(count));
            count++;
            root.setROverhang(Integer.toString(count));
            count++;
            ArrayList<RNode> neighbors = root.getNeighbors();
            count = assignPrimaryOverhangs(root, neighbors, root, count);
        }
        
        getStageDirectionAssignHash(optimalGraphs);
    }

    /**
     * New overhang sharing method. Nodes that can be shared are determined
     * during this step. Nodes are assigned by stage of impact and direction (strand) 
     */
    protected void maximizeOverhangSharing(ArrayList<RGraph> optimalGraphs) {

        ArrayList<RNode> roots = new ArrayList<RNode>();
        for (RGraph graph : optimalGraphs) {
            roots.add(graph.getRootNode());
        }

        _typeLOHHash = new HashMap<String, ArrayList<String>>(); //key: string type, value: arrayList of abstract overhangs 'reserved' for that composition
        _typeROHHash = new HashMap<String, ArrayList<String>>(); //key: string type, value: arrayList of abstract overhangs 'reserved' for that composition
        _numberHash = new HashMap<String, String>(); //key: overhang from round 1, value: overhang from round 2
        _allLevelOHs = new HashSet<String>();

        Set<Integer> allLevels = _stageDirectionAssignHash.keySet();
        ArrayList<Integer> levels = new ArrayList<Integer>(allLevels);
        Collections.sort(levels);

        //Assign by levels of impact starting from lowest level of impact
        for (Integer level : levels) {
            HashMap<String, ArrayList<RNode>> directionHash = _stageDirectionAssignHash.get(level);
            HashSet<String> currentLevelOHs = new HashSet<String>();

            //Visit each basic node to assign overhangs, assigning forward nodes first going left to right first, then right to left
            ArrayList<RNode> fwdNodes = new ArrayList<RNode>();
            ArrayList<RNode> bkwdNodes = new ArrayList<RNode>();
            if (directionHash.containsKey("+")) {
                fwdNodes = directionHash.get("+");
            }
            if (directionHash.containsKey("-")) {
                bkwdNodes = directionHash.get("-");
                Collections.reverse(bkwdNodes);
            }
            
            //Assign nodes in the forward direction first
            currentLevelOHs = assignSecondaryOverhangs(fwdNodes, "Left", "+", currentLevelOHs, roots);
            currentLevelOHs = assignSecondaryOverhangs(fwdNodes, "Right", "+", currentLevelOHs, roots);
            currentLevelOHs = assignSecondaryOverhangs(bkwdNodes, "Right", "-", currentLevelOHs, roots);
            currentLevelOHs = assignSecondaryOverhangs(bkwdNodes, "Left", "-", currentLevelOHs, roots);

            //Add all overhangs seen in this level to the taken overhang hash of each node
            _allLevelOHs.addAll(currentLevelOHs);
        }

        mapNumberHash(optimalGraphs);
    }
    
    /* 
     * Third step of overhang assignment - A partial cartesian product given a library of parts
     */   
    protected void cartesianLibraryAssignment(ArrayList<RGraph> graphs, HashMap<String, String> forcedOverhangHash) {
        
        //Initialize all hashes for the third pass of overhang assignment
        _nodeOHlibraryOHHash = new HashMap<String, HashSet<String>>(); //key: node overhang, value: set of all library part overhangs that match composition
        _nodeLCompHash = new HashMap<String, HashSet<String>>(); //key: node left overhang, value: set of all compositions associated with that overhang
        _nodeRCompHash = new HashMap<String, HashSet<String>>(); //key: node right overhang, value: set of all compositions associated with that overhang
        _invertedOverhangs = new HashSet<String>(); //inverted (*) overhangs from second pass
        _libraryLCompHash = new HashMap<String, HashSet<String>>(); //key: composition, value: set of all node left overhangs associated with that composition
        _libraryRCompHash = new HashMap<String, HashSet<String>>(); //key: composition, value: set of all node right overhangs associated with that composition       
        
        //Initialize node and library overhang hashes
        initializeNodeOHHashes(graphs);
        HashSet<String> libCompOHDirHash = initializeLibraryPartOHHashes();
    
        //Make a sorted list of the key values of the nodeOH to library OH map, from 0 to highest seen node OH
        ArrayList<String> sortedNodeOverhangs = new ArrayList(_nodeOHlibraryOHHash.keySet());
        Collections.sort(sortedNodeOverhangs); 
        
        //Perform a partial cartesian product on library re-use
        ArrayList<CartesianNode> cartestianRootNodes = makeCartesianGraph(sortedNodeOverhangs);
        ArrayList<ArrayList<String>> completeAssignments = traverseCertesianGraph (cartestianRootNodes, sortedNodeOverhangs.size());
        HashMap<String, String> bestAssignment = scoreAssignments(completeAssignments, sortedNodeOverhangs, forcedOverhangHash, libCompOHDirHash);
        
        //Assign overhang to nodes and vectors
        assignFinalOverhangs(bestAssignment, sortedNodeOverhangs);
        assignVectors(graphs, bestAssignment);
    }
    
    /**
     * ************************************************************************
     *
     * FIRST PASS OF OVERHANG ASSIGNMENT HELPER METHODS
     *
     *************************************************************************
     */

    /**
     * This helper method executes the loops necessary to assign overhangs recursively based on parents and children
     */
    private int assignPrimaryOverhangs(RNode parent, ArrayList<RNode> children, RNode root, int count) {

        String nextLOverhang = new String();

        //Loop through each one of the children to assign rule-instructed overhangs... enumerated numbers currently
        for (int i = 0; i < children.size(); i++) {

            RNode child = children.get(i);
            _parentHash.put(child, parent);

            //Pass numeric overhangs down from the parent to the correct child
            if (i == 0) {
                child.setLOverhang(parent.getLOverhang());
            } else if (i == children.size() - 1) {
                child.setROverhang(parent.getROverhang());
            }

            //Assign new left overhang if empty
            if (child.getLOverhang().isEmpty()) {

                //If the nextLOverhangVariable has an overhang waiting
                if (!nextLOverhang.isEmpty()) {
                    child.setLOverhang(nextLOverhang);
                    nextLOverhang = "";
                } else {
                    child.setLOverhang(Integer.toString(count));
                    count++;
                }
            }

            //Assign new right overhang if empty
            if (child.getROverhang().isEmpty()) {
                child.setROverhang(Integer.toString(count));
                nextLOverhang = Integer.toString(count);
                count++;
            }

            //Make recursive call
            if (child.getStage() > 0) {
                ArrayList<RNode> grandChildren = new ArrayList<RNode>();
                grandChildren.addAll(child.getNeighbors());

                //Remove the current parent from the list
                if (grandChildren.contains(parent)) {
                    grandChildren.remove(parent);
                }
                count = assignPrimaryOverhangs(child, grandChildren, root, count);

                //Or record the level zero parts
            } else {
                ArrayList<RNode> l0nodes = _rootBasicNodeHash.get(root);
                l0nodes.add(child);
                _rootBasicNodeHash.put(root, l0nodes);
            }
        }

        return count;
    }
    
    private void getStageDirectionAssignHash (ArrayList<RGraph> optimalGraphs) {
        
         //Determine which nodes impact which level to form the stageDirectionAssignHash
        for (RGraph graph : optimalGraphs) {
            RNode root = graph.getRootNode();
            ArrayList<String> rootDir = new ArrayList<String>();
            ArrayList<String> direction = root.getDirection();
            rootDir.addAll(direction);
            ArrayList<RNode> l0Nodes = _rootBasicNodeHash.get(root);

            //Determine which levels each basic node impacts            
            for (int i = 0; i < l0Nodes.size(); i++) {
                int level = 0;
                RNode l0Node = l0Nodes.get(i);
                RNode parent = _parentHash.get(l0Node);
                
                //Start OH exclusivity set... start with all of parent's children
                HashSet<String> exclusiveL = new HashSet<String>();
                HashSet<String> exclusiveR = new HashSet<String>();
                for (RNode neighbor: parent.getNeighbors()) {
                    if (neighbor.getStage() < parent.getStage()) {
                        exclusiveL.add(neighbor.getLOverhang());
                        exclusiveL.add(neighbor.getROverhang());
                        exclusiveR.add(neighbor.getLOverhang());
                        exclusiveR.add(neighbor.getROverhang());
                    }
                }

                //Go up the parent hash until the parent doesn't have an overhang impacted by the child
                RNode ancestor = parent;
                while (l0Node.getLOverhang().equals(ancestor.getLOverhang()) || l0Node.getROverhang().equals(ancestor.getROverhang())) {                    
                    level = ancestor.getStage();
                    
                    if (_parentHash.containsKey(ancestor)) {
                        ancestor = _parentHash.get(ancestor);
                        
                        //Add exclusive OHs for each relevant ancestor
                        for (RNode neighbor : ancestor.getNeighbors()) {
                            if (neighbor.getStage() < ancestor.getStage()) {
                                exclusiveL.add(neighbor.getLOverhang());
                                exclusiveL.add(neighbor.getROverhang());
                                exclusiveR.add(neighbor.getLOverhang());
                                exclusiveR.add(neighbor.getROverhang());
                            }
                        }     
                        
                    } else {
                        break;
                    }
                }

                //Add to exclusion hash for the left OH
                if (_OHexclusionHash.containsKey(l0Node.getLOverhang())) {
                    HashSet<String> exL = _OHexclusionHash.get(l0Node.getLOverhang());
                    exL.addAll(exclusiveL);
                } else {
                    _OHexclusionHash.put(l0Node.getLOverhang(), exclusiveL);
                }
                
                //Add to exclusion hash for the right OH
                if (_OHexclusionHash.containsKey(l0Node.getROverhang())) {
                    HashSet<String> exR = _OHexclusionHash.get(l0Node.getROverhang());
                    exR.addAll(exclusiveR);
                } else {
                    _OHexclusionHash.put(l0Node.getROverhang(), exclusiveR);
                }
                
                //Determine direction and enter into hash               
                String l0Direction = rootDir.get(0);
                if (l0Node.getComposition().size() == 1) {
                    ArrayList<String> l0Dir = new ArrayList<String>();
                    l0Dir.add(l0Direction);
                    l0Node.setDirection(l0Dir);
                }
                int size = l0Node.getDirection().size();
                rootDir.subList(0, size).clear();

                HashMap<String, ArrayList<RNode>> directionHash;
                ArrayList<RNode> nodeList;

                if (_stageDirectionAssignHash.containsKey(level)) {
                    directionHash = _stageDirectionAssignHash.get(level);
                } else {
                    directionHash = new HashMap<String, ArrayList<RNode>>();
                }

                if (directionHash.containsKey(l0Direction)) {
                    nodeList = directionHash.get(l0Direction);
                } else {
                    nodeList = new ArrayList<RNode>();
                }

                nodeList.add(l0Node);
                directionHash.put(l0Direction, nodeList);
                _stageDirectionAssignHash.put(level, directionHash);
            }
        }
        
    }
    
    /**
     * ************************************************************************
     *
     * SECOND PASS OF OVERHANG ASSIGNMENT HELPER METHODS
     *
     *************************************************************************
     */
    
    private HashSet<String> assignSecondaryOverhangs(ArrayList<RNode> nodes, String LR, String direction, HashSet<String> currentLevelOHs, ArrayList<RNode> roots) {
 
        for (int j = 0; j < nodes.size(); j++) {

            RNode node = nodes.get(j);
            String type = node.getType().toString().toLowerCase();
            HashSet<String> takenOHs = getTakenOHs(node, LR);
            ArrayList<String> reusableOHs = getReusableOHs(node, LR, direction);
            
            ArrayList<String> typeLeftOverhangs;
            ArrayList<String> typeRightOverhangs;
            
            if (direction.equals("+")) {
                typeLeftOverhangs = _typeLOHHash.get(type);
                typeRightOverhangs = _typeROHHash.get(type);
            } else {
                typeLeftOverhangs = _typeROHHash.get(type);
                typeRightOverhangs = _typeLOHHash.get(type);
            }
            
            String numberHashOH;
            if (LR.equals("Left")) {
                numberHashOH = node.getLOverhang();
            } else {
                numberHashOH = node.getROverhang();
            }
            
            //Assign left overhang if it is not selected yet
            if (!_numberHash.containsKey(numberHashOH)) {
                
                String OH = selectOH(reusableOHs, takenOHs);
                
                if (!direction.equals("+")) {
                    String hashOH = OH + "*";
                    _numberHash.put(numberHashOH, hashOH);
                } else {
                    _numberHash.put(numberHashOH, OH);
                    currentLevelOHs.add(OH);
                }                

                //If this overhang is not contained in the part type OHs for right or left, add it to the left
                if (!typeLeftOverhangs.contains(OH) && !typeRightOverhangs.contains(OH)) {
                    if (LR.equals("Left")) {
                        typeLeftOverhangs.add(OH);
                    } else {
                        typeRightOverhangs.add(OH);
                    }
                }
                assignNeighbor(roots, node, OH, LR, direction);
            }
        }
        return currentLevelOHs;
    }
    
    /**
     * Find all overhangs that cannot be assigned in this iteration of part two
     * of overhang assignment *
     */
    private HashSet<String> getTakenOHs(RNode node, String LR) {

        HashSet<String> takenOHs = new HashSet<String>();
        String OH;
        
        if (LR.equals("Left")) {
            OH = node.getLOverhang();
        } else {
            OH = node.getROverhang();
        }
        
        //Search all other overhangs exclusive to this one and add their assigned second overhang to taken OHs
        HashSet<String> exclusiveOHs = _OHexclusionHash.get(OH);
        for (String exclusiveOH : exclusiveOHs) {
            if (_numberHash.containsKey(exclusiveOH)) {
                
                String exclude = _numberHash.get(exclusiveOH);
                takenOHs.add(exclude);
                
                if (exclude.contains("*")) {
                    takenOHs.add(exclude.substring(0, exclude.length()-1));
                } else {
                    takenOHs.add(exclude+"*");
                }
            }
        }
          
        takenOHs.addAll(_allLevelOHs);
        return takenOHs;
    }

    /**
     * Get all overhangs that could be reused for a specific part type in part
     * two of overhang selection *
     */
    private ArrayList<String> getReusableOHs(RNode node, String LR, String direction) {

        //Get overhangs that have been seen before for this part type
        //NOTE: For backwards assignment, the reusable OH hashes are switched
        ArrayList<String> reusableLeftOverhangs = new ArrayList<String>();
        ArrayList<String> reusableRightOverhangs = new ArrayList<String>();
        ArrayList<String> typeRightOverhangs;
        ArrayList<String> typeLeftOverhangs;
        String type = node.getType().toString().toLowerCase();

        //If the direction is forward, return the same side overhangs, else flip the direction
        if (direction.equals("+")) {
            typeLeftOverhangs = _typeLOHHash.get(type);
            typeRightOverhangs = _typeROHHash.get(type);
        } else {
            typeLeftOverhangs = _typeROHHash.get(type);
            typeRightOverhangs = _typeLOHHash.get(type);
        }

        //If there are reusable left overhangs for this part type, add them to the reuable left overhang hash, else add new entry for this type
        if (typeLeftOverhangs != null) {
            reusableLeftOverhangs.addAll(typeLeftOverhangs);
            Collections.sort(reusableLeftOverhangs);
        } else if (typeLeftOverhangs == null) {
            typeLeftOverhangs = new ArrayList<String>();
            if (direction.equals("+")) {
                _typeLOHHash.put(type, typeLeftOverhangs);
            } else {
                _typeROHHash.put(type, typeLeftOverhangs);
            }
        }

        //If there are reusable right overhangs for this part type, add them to the reusable right overhang hash, else add new entry for this type
        if (typeRightOverhangs != null) {
            reusableRightOverhangs.addAll(typeRightOverhangs);
            Collections.sort(reusableRightOverhangs);
        } else if (typeRightOverhangs == null) {
            typeRightOverhangs = new ArrayList<String>();
            if (direction.equals("+")) {
                _typeROHHash.put(type, typeRightOverhangs);
            } else {
                _typeLOHHash.put(type, typeRightOverhangs);
            }
        }

        //Return either the either the left or right re-usable overhangs
        if (LR.equals("Left")) {
            return reusableLeftOverhangs;
        } else {
            return reusableRightOverhangs;
        }
    }

    /**
     * Given the taken and reusable overhangs and overhang count, determine a
     * new overhang to select for part two of overhang assignment *
     */
    private String selectOH(ArrayList<String> reusableOHs, HashSet<String> takenOHs) {

        int count = 0;
        String OH;
        if (!reusableOHs.isEmpty()) {
            OH = reusableOHs.get(0);
            reusableOHs.remove(0);
        } else {
            OH = count + "_";
            count++;
        }

        //Pull from the available overhang list until one does not match one already assigned to the right overhang of the current node or its parent
        //Rule checking loop
        while (takenOHs.contains(OH)) {
            if (!reusableOHs.isEmpty()) {
                OH = reusableOHs.get(0);
                reusableOHs.remove(0);
            } else {
                OH = count + "_";
                count++;
            }
        }
        return OH;
    }

    /**
     * Add the overhang of adjacent part to its type-OH hash *
     */
    private void assignNeighbor(ArrayList<RNode> roots, RNode currentNode, String OH, String LR, String direction) {

        //Find which l0Node set this node is containted in
        for (RNode root : roots) {
            ArrayList<RNode> l0Nodes = _rootBasicNodeHash.get(root);

            if (l0Nodes.contains(currentNode)) {
                int indexOf = l0Nodes.indexOf(currentNode);

                //If assiging right overhang to the type of the node on the left
                if ("Left".equals(LR)) {
                    if (indexOf > 0) {
                        RNode l0Node = l0Nodes.get(indexOf - 1);
                        addTypeOHHash ("Left", l0Node.getType().toString().toLowerCase(), OH, direction);
                    }

                //If assiging right overhang to the type of the node on the left
                } else {
                    if (indexOf < l0Nodes.size() - 1) {
                        RNode l0Node = l0Nodes.get(indexOf + 1);                  
                        addTypeOHHash ("Right", l0Node.getType().toString().toLowerCase(), OH, direction);
                    }
                }
            }
        }
    }
    
    /*
     * Add to typeOH hash based upon direction
     */
    private void addTypeOHHash (String LR, String l0Type, String OH, String direction) {
        
        ArrayList<String> l0TypeLeftOverhangs;
        ArrayList<String> l0TypeRightOverhangs;
        
        //Consider correct direction
        if ("+".equals(direction)) {
            l0TypeLeftOverhangs = _typeLOHHash.get(l0Type);
            l0TypeRightOverhangs = _typeROHHash.get(l0Type);

            if (l0TypeRightOverhangs == null) {
                l0TypeRightOverhangs = new ArrayList<String>();
                _typeROHHash.put(l0Type, l0TypeRightOverhangs);
            }
            if (l0TypeLeftOverhangs == null) {
                l0TypeLeftOverhangs = new ArrayList<String>();
                _typeLOHHash.put(l0Type, l0TypeLeftOverhangs);
            }

        } else {
            l0TypeLeftOverhangs = _typeROHHash.get(l0Type);
            l0TypeRightOverhangs = _typeLOHHash.get(l0Type);

            if (l0TypeRightOverhangs == null) {
                l0TypeRightOverhangs = new ArrayList<String>();
                _typeLOHHash.put(l0Type, l0TypeRightOverhangs);
            }
            if (l0TypeLeftOverhangs == null) {
                l0TypeLeftOverhangs = new ArrayList<String>();
                _typeROHHash.put(l0Type, l0TypeLeftOverhangs);
            }
        }

        //Add this OH to the left overhangs of the type of the node on the right
        if (!l0TypeLeftOverhangs.contains(OH) && !l0TypeRightOverhangs.contains(OH)) {
            if (LR.equals("Left")) {
                l0TypeRightOverhangs.add(OH);
            } else {
                l0TypeLeftOverhangs.add(OH);
            }        
        }
    }
    
    /**
     * Assign all assignments within numberHash map 
     */
    private void mapNumberHash (ArrayList<RGraph> optimalGraphs) {
        
        for (RGraph graph : optimalGraphs) {
            ArrayList<RNode> queue = new ArrayList<RNode>();
            HashSet<RNode> seenNodes = new HashSet<RNode>();
            queue.add(graph.getRootNode());

            //Traverse graph and assign letter overhang to corresponding number placeholder from rule assignment
            while (!queue.isEmpty()) {
                RNode currentNode = queue.get(0);
                queue.remove(0);
                currentNode.setLOverhang(_numberHash.get(currentNode.getLOverhang()));
                currentNode.setROverhang(_numberHash.get(currentNode.getROverhang()));
                seenNodes.add(currentNode);
                ArrayList<RNode> neighbors = currentNode.getNeighbors();

                for (RNode neighbor : neighbors) {
                    if (!seenNodes.contains(neighbor)) {
                        queue.add(neighbor);
                        seenNodes.add(neighbor);
                    }
                }
            }
        }
        
    }
    
    /**
     * ************************************************************************
     *
     * THIRD PASS OF OVERHANG ASSIGNMENT HELPER METHODS
     *
     *************************************************************************
     */
    
    /*
     * For each of the graphs in the solution set, create a hash of overhangs (left and right) seen for each level 0 node's composition
     */    
    private void initializeNodeOHHashes (ArrayList<RGraph> graphs) {
        
        //For each of the graphs in the solution set, create a hash of overhangs (left and right) seen for each level 0 node's composition
        for (RGraph graph : graphs) {
            
            //For each of the l0Nodes in the graph, build hash of overhangs for each composition, ignoring reverse overhangs 
            for (RNode l0Node : _rootBasicNodeHash.get(graph.getRootNode())) {
                
                String LO = l0Node.getLOverhang();
                String RO = l0Node.getROverhang();
                
                //If the left overhang is forward (i.e. not inverted), put it in the abstractConcrete hash and the abstractLeftComposisiton hash
                if (LO.indexOf("*") < 0) {

                    _nodeOHlibraryOHHash.put(LO, new HashSet());

                    //Add left overhang to abstractLeftComposisiton hash if this overhang is already in the map, otherwise initialize with this composition
                    if (_nodeLCompHash.containsKey(LO)) {
                        _nodeLCompHash.get(LO).add(l0Node.getComposition().toString());
                    } else {
                        HashSet<String> toAddLeft = new HashSet();
                        toAddLeft.add(l0Node.getComposition().toString());
                        _nodeLCompHash.put(LO, toAddLeft);
                    }
                
                } else {
                    _invertedOverhangs.add(LO);
                }
                
                //If the right overhang is forward (i.e. not inverted), put it in the abstractConcrete hash and the abstractRightComposisiton hash
                if (RO.indexOf("*") < 0) {

                    _nodeOHlibraryOHHash.put(RO, new HashSet());
                    
                    //Add right overhang to abstractRightComposisiton hash if this overhang is already in the map, otherwise initialize with this composition
                    if (_nodeRCompHash.containsKey(RO)) {
                        _nodeRCompHash.get(RO).add(l0Node.getComposition().toString());
                    } else {
                        HashSet<String> toAddRight = new HashSet();
                        toAddRight.add(l0Node.getComposition().toString());
                        _nodeRCompHash.put(RO, toAddRight);
                    }
                
                } else {
                    _invertedOverhangs.add(RO);
                }
            }
        }
    }
    
    /*
     * For each of the parts in the library, create a hash of overhangs (left and right) seen for each level 0 node's composition
     */
    private HashSet<String> initializeLibraryPartOHHashes() {
        
        HashSet<String> libCompOHDirHash = new HashSet<String>(); //concatentation of composition Overhang and direction seen in the partLibrary
        
        //For each part in the library, build hash of overhangs for each composition
        for (Part libraryPart : _partLibrary) {
            
            libCompOHDirHash.add(libraryPart.getStringComposition() + "|" + libraryPart.getLeftOverhang() + "|" + libraryPart.getRightOverhang() + "|" + libraryPart.getDirections());
            String composition = libraryPart.getStringComposition().toString();
            
            //If the library part composition is seen in the left hash, add it or put a new entry for the composition
            if (_libraryLCompHash.containsKey(composition)) {
                _libraryLCompHash.get(composition).add(libraryPart.getLeftOverhang());                
            } else {
                HashSet<String> toAddLeft = new HashSet();
                toAddLeft.add(libraryPart.getLeftOverhang());
                _libraryLCompHash.put(composition, toAddLeft);
            }
                
            //If the library part composition is seen in the right hash, add it or put a new entry for the composition    
            if (_libraryRCompHash.containsKey(composition))  {
                _libraryRCompHash.get(composition).add(libraryPart.getRightOverhang());            
            } else {
                HashSet<String> toAddRight = new HashSet();
                toAddRight.add(libraryPart.getRightOverhang());
                _libraryRCompHash.put(composition, toAddRight);
            }
        }
        
        //MATCH LIBRARY OVERHANGS TO NODE OVERHANGS
        //For each left overhang in the node hash, loop through associated node compositions and for each composition, build the nodeLO to libraryLO hash 
        for (String nodeLO : _nodeLCompHash.keySet()) {
            
            for (String nodeComp : _nodeLCompHash.get(nodeLO)) {
                
                //If any of the node composition are seen in the library, add all library LOs to the node-library hash map that are not blank
                if (_libraryLCompHash.get(nodeComp) != null) {
                    
                    for (String libraryLO : _libraryLCompHash.get(nodeComp)) {
                        if (!libraryLO.equals("")) {
                            _nodeOHlibraryOHHash.get(nodeLO).add(libraryLO);
                        }
                    }
                }
            }
            
            //Each node LO gets all library matches based on composition and one additional placeholder
            _nodeOHlibraryOHHash.get(nodeLO).add("#");
        }
        
        //For each right overhang in the node hash, loop through associated node compositions and for each composition, build the nodeRO to libraryRO hash
        for (String nodeRO : _nodeRCompHash.keySet()) {
            
            for (String nodeComp : _nodeRCompHash.get(nodeRO)) {
                
                //If any of the node composition are seen in the library, add all library ROs to the node-library hash map that are not blank
                if (_libraryRCompHash.get(nodeComp) != null) {
                    
                    for (String libraryRO : _libraryRCompHash.get(nodeComp)) {
                        if (!libraryRO.equals("")) {
                            _nodeOHlibraryOHHash.get(nodeRO).add(libraryRO);
                        }
                    }
                }
            }
            
            //Each node RO gets all library matches based on composition and one additional placeholder
            _nodeOHlibraryOHHash.get(nodeRO).add("#");
        }
        
        return libCompOHDirHash;
    }
    
    /*
     * Create Cartesian Graph Space
     */ 
    private ArrayList<CartesianNode> makeCartesianGraph(ArrayList<String> sortedNodeOverhangs) {
        
        ArrayList<CartesianNode> previousNodes = null;
        ArrayList<CartesianNode> cartestianRootNodes = new ArrayList<CartesianNode>();
        
        int level = 0;

        //For each of the node overhangs,
        for (String nodeOverhang : sortedNodeOverhangs) {
            
            ArrayList<CartesianNode> currentNodes = new ArrayList<CartesianNode>();
            HashSet<String> libraryOverhangs = _nodeOHlibraryOHHash.get(nodeOverhang);
            
            //For all library overhangs mapping to this node overhang, make cartesian nodes
            for (String libraryOverhang : libraryOverhangs) {
                CartesianNode cartesian = new CartesianNode();
                cartesian.setLevel(level);
                cartesian.setNodeOverhang(nodeOverhang);
                cartesian.setLibraryOverhang(libraryOverhang.trim());
                currentNodes.add(cartesian);
            }
            
            //If this is not the first cartesian node seen, add to space
            if (previousNodes != null) {
                
                //For all current and previous nodes expand space that is not redundant
                for (CartesianNode previousNode : previousNodes) {
                    for (CartesianNode currentNode : currentNodes) {
                        
                        //If the current and previous node aren't matching or the current node is a blank, make current node a neighbor of the previous node
                        if (!previousNode.getLibraryOverhang().equals(currentNode.getLibraryOverhang()) || currentNode.getLibraryOverhang().equals("#")) {
                            previousNode.addNeighbor(currentNode);
                        }
                    }
                }
            } else {
                
                for (CartesianNode root : currentNodes) {
                    cartestianRootNodes.add(root);
                }
            }
            
            previousNodes = currentNodes;
            level++;
        }      
        
        return cartestianRootNodes;
    }
    
    /*
     * Traverse cartesian graph to produce all complete candidate assignments
     */
    private ArrayList<ArrayList<String>> traverseCertesianGraph (ArrayList<CartesianNode> cartestianRootNodes, Integer targetLength) {
        
        ArrayList<ArrayList<String>> completeAssignments = new ArrayList<ArrayList<String>>();
        HashMap<CartesianNode, CartesianNode> parentHash = new HashMap<CartesianNode, CartesianNode>(); //key: node, value: parent node
        ArrayList<String> currentSolution;
        
        //Traverse the Cartesian graph starting at the Cartesian roots
        for (CartesianNode cartesianRoot : cartestianRootNodes) {
            
            currentSolution = new ArrayList<String>();
            ArrayList<CartesianNode> stack = new ArrayList<CartesianNode>();
            stack.add(cartesianRoot);
            boolean toParent = false; // am i returning to a parent node?
            HashSet<String> seenPaths = new HashSet<String>();
            
            while (!stack.isEmpty()) {
                
                CartesianNode currentNode = stack.get(0);
                stack.remove(0);
                String currentPath = currentSolution.toString();
                currentPath = currentPath.substring(1, currentPath.length() - 1).replaceAll(",", "->").replaceAll(" ", "");
                
                //
                if (!toParent) {
                    currentSolution.add(currentNode.getLibraryOverhang());
                    currentPath = currentPath + "->" + currentNode.getLibraryOverhang();
                    seenPaths.add(currentPath);
                } else {
                    toParent = false;
                }
                
                CartesianNode parent = parentHash.get(currentNode);
                int childrenCount = 0;
                
                //Build paths to complete assignment
                for (CartesianNode neighbor : currentNode.getNeighbors()) {
                    
                    //If the current path does not contain this neighbor's overhang or the neighbor is a blank, make a new edge
                    if (currentPath.indexOf(neighbor.getLibraryOverhang()) < 0 || neighbor.getLibraryOverhang().equals("#")) {
                        String edge = currentPath + "->" + neighbor.getLibraryOverhang();
                        
                        //Add to stack and parent hash if the edge hasn't been seen and the neighbor is the next level
                        if (!seenPaths.contains(edge)) {                            
                            if (neighbor.getLevel() > currentNode.getLevel()) {
                                stack.add(0, neighbor);
                                parentHash.put(neighbor, currentNode);
                                childrenCount++;
                            }
                        }
                    }

                }
                
                //If there are no more children, i.e. we've reached the end of a branch
                if (childrenCount == 0) {
                    
                    //If this solution equals the target length (another check), it is a coplete assignment
                    if (currentSolution.size() == targetLength) {
                        completeAssignments.add((ArrayList<String>) currentSolution.clone());
                    }
                    
                    if (currentSolution.size() > 0) {
                        currentSolution.remove(currentSolution.size() - 1);
                    }
                    
                    if (parent != null) {
                        toParent = true;
                        stack.add(0, parent);
                    }
                }
            }
        }
        
        return completeAssignments;
    }
    
    /*
     * Score all complete assignments for overhang assignment
     */
    private HashMap<String, String> scoreAssignments(ArrayList<ArrayList<String>> completeAssignments, ArrayList<String> sortedNodeOverhangs, HashMap<String, String> forcedOHHash, HashSet<String> libCompOHDirHash) {
        
        //Get all basic nodes into a list and initialize arbitrarily high best score
        ArrayList<RNode> basicNodes = new ArrayList();
        for (RNode rootNodes : _rootBasicNodeHash.keySet()) {
            for (RNode basicNode : _rootBasicNodeHash.get(rootNodes)) {
                if (!basicNodes.contains(basicNode)) {
                    basicNodes.add(basicNode);
                }
            }
        }
        int bestScore = 1000000000;
        HashMap<String, String> bestAssignment = null;
        
        //Loop through each complete assignment and score the solutions
        for (ArrayList<String> assignment : completeAssignments) {
            HashMap<String, String> currentAssignment = new HashMap();
            int currentScore = 0;
            
            //Forced overhangs
            for (int i = 0; i < sortedNodeOverhangs.size(); i++) {
                String currentNodeOverhang = sortedNodeOverhangs.get(i);
      
                //If final overhang hash includes this node overhang, give the current assignment that value otherwise a new compelte assignment value
                if (forcedOHHash.containsKey(currentNodeOverhang)) {
                    currentAssignment.put(currentNodeOverhang, forcedOHHash.get(currentNodeOverhang));
                } else {
                    currentAssignment.put(sortedNodeOverhangs.get(i), assignment.get(i));
                }
            }
            
            //TODO: DEAL WITH THIS
            //handle inverted overhangs
            for (String invertedOverhang : _invertedOverhangs) {
                
                //If the inverted overhang is forced put it in the current assignment
                if (forcedOHHash.containsKey(invertedOverhang)) {
                    currentAssignment.put(invertedOverhang, forcedOHHash.get(invertedOverhang));
                
                } else {
                    String uninvertedOverhang = invertedOverhang.substring(0, invertedOverhang.indexOf("*"));
                    
                    if (currentAssignment.containsKey(uninvertedOverhang)) {
                        String uninvertedOverhangAssignment = currentAssignment.get(uninvertedOverhang);
                        String invertedOverhangAssignment = "";
                        
                        if (uninvertedOverhangAssignment.equals("#")) {
                            //# markes new overhang
                            currentAssignment.put(invertedOverhang, "#");
                        } else {
                            
                            if (uninvertedOverhangAssignment.indexOf("*") > -1) {
                                invertedOverhangAssignment = uninvertedOverhangAssignment.substring(0, uninvertedOverhangAssignment.indexOf("*"));
                            } else {
                                invertedOverhangAssignment = uninvertedOverhangAssignment + "*";
                            }
                            currentAssignment.put(invertedOverhang, invertedOverhangAssignment);
                        }
                    } else {
                        //# marks new overhang
                        currentAssignment.put(invertedOverhang, "#");
                    }
                }
            }
            
            HashSet<String> matched = new HashSet();
            
            //Score this assignment for each basic node
            for (RNode basicNode : basicNodes) {
                String compositionOverhangDirectionString = basicNode.getComposition() + "|" + currentAssignment.get(basicNode.getLOverhang()) + "|" + currentAssignment.get(basicNode.getROverhang()) + "|" + basicNode.getDirection();
                if (!libCompOHDirHash.contains(compositionOverhangDirectionString)) {
                    currentScore++;
                } else {
                    matched.add(compositionOverhangDirectionString);
                }
            }
            currentScore = currentScore - matched.size();
            
            //If this is the new best score, replace the former best score and assignment
            if (currentScore < bestScore) {
                bestScore = currentScore;
                bestAssignment = currentAssignment;
            } 
        }
        
        return bestAssignment;
    }
    
    private void assignFinalOverhangs (HashMap<String, String> bestAssignment, ArrayList<String> sortedNodeOverhangs) {
        
        //generate new overhangs
        HashSet<String> assignedOverhangs = new HashSet(bestAssignment.values());
        int newOverhang = 0;
        
        for (String starAbstract : sortedNodeOverhangs) {
            if (bestAssignment.get(starAbstract).equals("#")) {
                while (assignedOverhangs.contains(String.valueOf(newOverhang))) {
                    newOverhang++;
                }
                bestAssignment.put(starAbstract, String.valueOf(newOverhang));
                assignedOverhangs.add(String.valueOf(newOverhang));
            }
        }

        //generate matching new overhangs for inverted overhans
        for (String invertedOverhang : _invertedOverhangs) {
            
            if (bestAssignment.get(invertedOverhang).equals("#")) {
                String uninvertedOverhang = invertedOverhang.substring(0, invertedOverhang.indexOf("*"));
                
                if (bestAssignment.containsKey(uninvertedOverhang)) {
                    bestAssignment.put(invertedOverhang, bestAssignment.get(uninvertedOverhang) + "*");
                } else {
                    while (assignedOverhangs.contains(String.valueOf(newOverhang))) {
                        newOverhang++;
                    }
                    bestAssignment.put(invertedOverhang, String.valueOf(newOverhang));
                    assignedOverhangs.add(String.valueOf(newOverhang));
                }
            }
        }        
    }
    
    /*
     * Assign vectors to each node on a graph given the final overhang assignments
     */
    private void assignVectors(ArrayList<RGraph> graphs, HashMap<String, String> bestAssignment) {
        
        //traverse graph and assign overhangs generate vectors        
        HashMap<Integer, String> levelResistanceHash = new HashMap<Integer, String>(); //key: level, value: antibiotic resistance
        ArrayList<String> freeAntibiotics = new ArrayList(Arrays.asList("ampicillin, kanamycin, ampicillin, kanamycin, ampicillin, kanamycin, ampicillin, kanamycin".toLowerCase().split(", "))); //overhangs that don't exist in part or vector library
        ArrayList<String> existingAntibiotics = new ArrayList<String>();
        HashMap<Integer, ArrayList<String>> existingAntibioticsHash = new HashMap();

        for (Vector vector : _vectorLibrary) {

            if (!existingAntibiotics.contains(vector.getResistance())) {
                existingAntibiotics.add(vector.getResistance());

                if (existingAntibioticsHash.get(vector.getLevel()) == null) {
                    existingAntibioticsHash.put(vector.getLevel(), new ArrayList());
                }
                if (!existingAntibioticsHash.get(vector.getLevel()).contains(vector.getResistance())) {
                    existingAntibioticsHash.get(vector.getLevel()).add(vector.getResistance());
                }
                freeAntibiotics.remove(vector.getResistance());
            }
        }
        int maxStage = 0;

        for (RGraph graph : graphs) {
            if (graph.getStages() > maxStage) {
                maxStage = graph.getStages();
            }
        }

        for (int i = 0; i <= maxStage; i++) {
            String resistance = "";

            if (existingAntibioticsHash.get(i) != null) {
                if (existingAntibioticsHash.get(i).size() > 0) {
                    resistance = existingAntibioticsHash.get(i).get(0);
                    existingAntibioticsHash.get(i).remove(0);
                }
            } else {
                resistance = freeAntibiotics.get(0);
                freeAntibiotics.remove(0);
            }
            levelResistanceHash.put(i, resistance);
        }

        //Assign vectors for all graphs
        for (RGraph graph : graphs) {
            ArrayList<RNode> queue = new ArrayList();
            HashSet<RNode> seenNodes = new HashSet();
            queue.add(graph.getRootNode());

            while (!queue.isEmpty()) {
                RNode current = queue.get(0);
                queue.remove(0);
                seenNodes.add(current);
                for (RNode neighbor : current.getNeighbors()) {
                    if (!seenNodes.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }

                String currentLeftOverhang = current.getLOverhang();
                String currentRightOverhang = current.getROverhang();
                current.setLOverhang(bestAssignment.get(currentLeftOverhang));
                current.setROverhang(bestAssignment.get(currentRightOverhang));
                currentLeftOverhang = current.getLOverhang();
                currentRightOverhang = current.getROverhang();

                RVector newVector = new RVector(currentLeftOverhang, currentRightOverhang, current.getStage(), "DVL" + current.getStage(), null);
                newVector.setStringResistance(levelResistanceHash.get(current.getStage()));
                current.setVector(newVector);
            }
        }
    }
    
    /**
     * ************************************************************************
     *
     * FORCED OVERHANGS
     *
     *************************************************************************
     */
    
    //sets user specified overhangs before algorithm computes the rest
    protected HashMap<String, String> assignForcedOverhangs(ArrayList<RGraph> optimalGraphs, HashMap<String, ArrayList<String>> forcedHash) {
        HashMap<String, String> toReturn = new HashMap(); //precursor for the finalOverhangHash used in the optimizeOverhangVectors method
        for (RGraph graph : optimalGraphs) {
            RNode root = graph.getRootNode();
            if (forcedHash.containsKey(root.getComposition().toString())) {
                
                //traverse the graph and find all of the basic parts and then put them in order
                ArrayList<RNode> stack = new ArrayList();
                HashSet<RNode> seenNodes = new HashSet();
                ArrayList<RNode> basicParts = new ArrayList();
                stack.add(root);

                while (!stack.isEmpty()) {
                    RNode current = stack.get(0);
                    stack.remove(0);
                    seenNodes.add(current);

                    if (current.getStage() == 0) {
                        basicParts.add(0, current);
                    }

                    for (RNode neighbor : current.getNeighbors()) {
                        if (!seenNodes.contains(neighbor)) {
                            stack.add(0, neighbor);
                        }
                    }
                }
                ArrayList<String> forcedOverhangs = forcedHash.get(root.getComposition().toString());
                for (int i = 0; i < basicParts.size(); i++) {
                    String[] forcedTokens = forcedOverhangs.get(i).split("\\|");
                    String forcedLeft = forcedTokens[0].trim();
                    String forcedRight = forcedTokens[1].trim();
                    RNode basicNode = basicParts.get(i);
                    if (forcedLeft.length() > 0) {
                        toReturn.put(basicNode.getLOverhang(), forcedLeft);
                    }
                    if (forcedRight.length() > 0) {
                        toReturn.put(basicNode.getROverhang(), forcedRight);
                    }
                }
            }
        }

        return toReturn;
    }

    public void setForcedOverhangs(Collector coll, HashMap<String, ArrayList<String>> requiredOverhangs) {
        if (requiredOverhangs != null) {
            _forcedOverhangHash = new HashMap();
            for (String key : requiredOverhangs.keySet()) {
                Part part = coll.getAllPartsWithName(key, false).get(0);
                if (part != null) {
                    _forcedOverhangHash.put(part.getStringComposition().toString(), requiredOverhangs.get(key));
                }
            }
        }
    }
    
    //FIELDS
    private HashMap<RNode, RNode> _parentHash; //key: node, value: parent node
    private HashMap<Integer, HashMap<String, ArrayList<RNode>>> _stageDirectionAssignHash; //key: stage, value: HashMap: key: direction, value: nodes to visit    
    private HashMap<RNode, ArrayList<RNode>> _rootBasicNodeHash; //key: root node, value: ordered arrayList of level0 nodes in graph that root node belongs to
    
    private HashMap<String, HashSet<String>> _OHexclusionHash; //key: parent node, value: all overhangs that have been seen in this step
    private HashMap<String, ArrayList<String>> _typeROHHash; //key: part type, value: all right overhangs seen for this part type
    private HashMap<String, ArrayList<String>> _typeLOHHash; //key: part type, value: all left overhangs seen for this part type
    private HashSet<String> _allLevelOHs;
    private HashMap<String, String> _numberHash; //key: first pass overhang, value: assigned overhang in second pass
    
    private HashMap<String, HashSet<String>> _nodeOHlibraryOHHash; //key: node overhang, value: set of all library part overhangs that match composition
    private HashMap<String, HashSet<String>> _nodeLCompHash; //key: node overhang, value: set of all compositions associated with that overhang
    private HashMap<String, HashSet<String>> _nodeRCompHash; //key: node overhang, value: set of all compositions associated with that overhang
    private HashMap<String, HashSet<String>> _libraryLCompHash; //key: composition, value: set of all abstract overhangs associated with that composition
    private HashMap<String, HashSet<String>> _libraryRCompHash; //key: composition, value: set of all abstract overhangs associated with that composition
    protected HashMap<String, ArrayList<String>> _forcedOverhangHash = new HashMap<String, ArrayList<String>>(); //key: composite part composition, value: forced overhang set
    private HashSet<String> _invertedOverhangs; //all overhangs that are assigned as inverted
    
    protected ArrayList<Part> _partLibrary = new ArrayList<Part>();
    protected ArrayList<Vector> _vectorLibrary = new ArrayList<Vector>();
}