/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller.accessibility;

import java.util.ArrayList;
import java.util.HashSet;
import Controller.datastructures.*;

/**
 * Provides utility methods for exporting Clotho composite parts
 *
 * @author Tao
 */
public class ClothoWriter {

    /** Clotho writer constructor **/
    public ClothoWriter() {
        _allCompositeParts = new ArrayList<Part>();
        _allBasicParts = new ArrayList<Part>();
    }

    /** Generate Clotho parts with uuids from intermediates without uuids **/
    public void nodesToClothoPartsVectors(Collector coll, RGraph graph) throws Exception {
        String nameRoot = coll.getPart(graph.getRootNode().getUUID(), true).getName();
        ArrayList<RNode> queue = new ArrayList<RNode>();
        HashSet<RNode> seenNodes = new HashSet<RNode>();
        queue.add(graph.getRootNode());
        
        //Traverse graph to assign uuids to parts and vectors without them and then save the unsaved parts
        while (!queue.isEmpty()) {
            RNode currentNode = queue.get(0);
            seenNodes.add(currentNode);
            queue.remove(0);
            for (RNode neighbor : currentNode.getNeighbors()) {
                if (!seenNodes.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }

            //If the node has no uuid, i.e. a new intermediate
            if (currentNode.getUUID() == null) {
                
                //Get new intermediate name
                String partName = nameRoot + "_intermediate" + Math.random() * 999999999;
                partName = partName.replaceAll("\\.", "");
                if (partName.length() > 255) {
                    partName = partName.substring(0, 255);
                }

                //Get new intermediate overhangs
                String LO = currentNode.getLOverhang();
                String RO = currentNode.getROverhang();

                //If there's overhangs, add search tags
                Part newPart = generateNewClothoPart(coll, partName, "", currentNode.getComposition(), LO, RO);
                newPart.addSearchTag("Type: composite");
                if (currentNode.getComposition().size() > 1) {
                    newPart.addSearchTag("Direction: " + currentNode.getDirection());
                }
                currentNode.setName(partName);
                newPart.saveDefault(coll);
                currentNode.setUUID(newPart.getUUID());
            }

            //Create new part and with new node uuid if overhangs not match
            Part currentPart = coll.getPart(currentNode.getUUID(), true);
            boolean createNewPart = false;
            
            //If a part matching this node and its existing overhangs is found in the collector, no new node needs to be made, otherwise it does
            if (currentPart != null) {
                if (!currentNode.getLOverhang().equals(currentPart.getLeftOverhang()) || !currentNode.getROverhang().equals(currentPart.getRightOverhang())) {
                    createNewPart = true;
                }
            } else {
                createNewPart = true;
            }
            
            //If a node needs to be made into a new part with different overhangs than the ones in the collector, make a new part with a new uuid
            if (createNewPart) {                                
                               
                String uuid = new String();
                
                if (currentPart != null) {
                    Part newOHPart; 
                    
                    //Create new basic or composite part
                    if (currentPart.isBasic()) {
                        newOHPart = Part.generateBasic(currentPart.getName(), currentPart.getSeq());
                    } else {
                        newOHPart = Part.generateComposite(currentPart.getComposition(), currentPart.getName());
                    }

                    uuid = newOHPart.getUUID();                   
                    ArrayList<String> types = currentNode.getType();
                    String type;
                    
                    if (currentNode.getComposition().size() > 1) {
                        type = "composite";
                        newOHPart.addSearchTag("Direction: " + currentNode.getDirection());
                    } else {
                        type = types.toString();
                        type = type.substring(1, type.length() - 1);
                    }
                    
                    newOHPart.addSearchTag("LO: " + currentNode.getLOverhang());
                    newOHPart.addSearchTag("RO: " + currentNode.getROverhang());
                    newOHPart.addSearchTag("Type: " + type);
                    newOHPart.saveDefault(coll);
                }
                currentNode.setUUID(uuid);
            }


            //Get the vector and save a new vector if it does not have a uuid
            RVector vector = currentNode.getVector();
            if (vector != null) {
                
                //Get new intermediate name
                String vecName = (nameRoot + "_vector" + Math.random() * 999999999);
                vecName = vecName.replaceAll("\\.", "");
                if (vecName.length() > 255) {
                    vecName = vecName.substring(0, 255);
                }
                
                //Get vector overhangs
                String LO = vector.getLOverhang();
                String RO = vector.getROverhang();
                String resistance = vector.getResistance();
                int level = vector.getLevel();
                Vector newVector = generateNewClothoVector(coll, vecName, "", LO, RO, resistance, level);
                newVector.saveDefault(coll);
                vector.setName(newVector.getName());
                vector.setUUID(newVector.getUUID());
                currentNode.setVector(vector);
            }
            seenNodes.add(currentNode);
        }

    }

    /** Make intermediate parts of a graph into Clotho parts (typically only done for solution graphs) **/
    private Part generateNewClothoPart(Collector coll, String name, String description, ArrayList<String> composition, String LO, String RO) throws Exception {
        if (_allCompositeParts.isEmpty() || _allBasicParts.isEmpty()) {
            refreshPartVectorList(coll);
        }
        
        //For each composite part, get the basic part uuids
        //Every time a new composite part can be made, search to see there's nothing made from the same components and overhangs before saving
        for (Part existingPart : _allCompositeParts) {
            ArrayList<String> existingPartComp = new ArrayList<String>();

            //Get an existing part's overhangs
            ArrayList<String> sTags = existingPart.getSearchTags();
            String existingPartLO = "";
            String existingPartRO = "";
            for (int k = 0; k < sTags.size(); k++) {
                if (sTags.get(k).startsWith("LO:")) {
                    existingPartLO = sTags.get(k).substring(4);
                } else if (sTags.get(k).startsWith("RO:")) {
                    existingPartRO = sTags.get(k).substring(4);
                }
            }

            //Obtain the basic part uuids
            ArrayList<Part> existingPartComposition = getComposition(existingPart);
            for (Part basicPart : existingPartComposition) {
                existingPartComp.add(basicPart.getName());
            }

            //If the composition of this new part and the composition of an existing part match
            if (composition.toString().equals(existingPartComp.toString())) {
                if (existingPartLO.equalsIgnoreCase(LO) && existingPartRO.equalsIgnoreCase(RO)) {
                    return existingPart;
                }
            }
        }


        //If a new composite part needs to be made
        if (composition.size() > 1) {
            ArrayList<Part> newComposition = new ArrayList<Part>();
            for (String component : composition) {
                newComposition.add(coll.getPartByName(component, true));
            }
            Part newPart = Part.generateComposite(newComposition, name);
            if (!LO.isEmpty()) {
                newPart.addSearchTag("LO: " + LO);
            }
            if (!RO.isEmpty()) {
                newPart.addSearchTag("RO: " + RO);
            }
            return newPart;

        //Make a new basic part
        } else {
            Part newPart = Part.generateBasic(name, coll.getPart(composition.get(0), true).getSeq());
            if (!LO.isEmpty()) {
                newPart.addSearchTag("LO: " + LO);
            }
            if (!RO.isEmpty()) {
                newPart.addSearchTag("RO: " + RO);
            }
            return newPart;
        }

    }

    /** Make intermediate parts of a graph into Clotho parts (typically only done for solution graphs) **/
    private Vector generateNewClothoVector(Collector coll, String name, String sequence, String LO, String RO, String resistance, int level) {
        _allVectors = coll.getAllVectors(true);
        
        //Search all existing vectors to for vectors with same overhangs and level before saving
        for (Vector vector : _allVectors) {
            
            //Get an existing part's overhangs
            ArrayList<String> sTags = vector.getSearchTags();
            String existingVecLO = vector.getLeftoverhang();
            String existingVecRO = vector.getRightOverhang();
            String existResistance = vector.getResistance();
            int existLevel = vector.getLevel();
            
            //If all of these things match, just return the vector that is found
            if (existingVecLO.equalsIgnoreCase(LO) && existingVecRO.equalsIgnoreCase(RO)) {
                if (existResistance.equalsIgnoreCase(resistance) && existLevel == level) {
                    return vector;
                }
            }
        }
        Vector newVector = Vector.generateVector(name, sequence);
        
        if (!LO.isEmpty()) {
            newVector.addSearchTag("LO: " + LO);
        }
        if (!RO.isEmpty()) {
            newVector.addSearchTag("RO: " + RO);
        }
        if (!resistance.isEmpty()) {
            newVector.addSearchTag("Resistance: " + resistance);
        }
        if (level > -1) {
            newVector.addSearchTag("Level: " + level);
        }

        return newVector;
    }

    /** Refresh a part list (used by the viewer) **/
    private void refreshPartVectorList(Collector coll) {
        _allCompositeParts = new ArrayList<Part>();
        _allBasicParts = new ArrayList<Part>();
        _allVectors = new ArrayList<Vector>();
        ArrayList<Vector> allVectors = coll.getAllVectors(true);
        _allVectors.addAll(allVectors);
        ArrayList<Part> allParts = coll.getAllParts(true);
        for (Part somePart : allParts) {
            if (somePart.isComposite()) {
                _allCompositeParts.add(somePart);
            } else if (somePart.isBasic()) {
                _allBasicParts.add(somePart);
            }
        }
    }

    /** Return the composition of a Clotho part **/
    public static ArrayList<Part> getComposition(Part part) throws Exception {
        ArrayList<Part> toReturn = new ArrayList<Part>();
        if (part.isBasic()) {
            toReturn.add(part);
        } else {
            ArrayList<Part> composition = part.getComposition();
            for (int i = 0; i < composition.size(); i++) {
                Part currentPart = composition.get(i);
                if (currentPart.isBasic()) {
                    toReturn.add(currentPart);
                } else {
                    toReturn = getCompositionHelper(currentPart, toReturn);
                }
            }
        }
        return toReturn;
    }

    /** Helper for recursion method to discover all basic parts **/
    private static ArrayList<Part> getCompositionHelper(Part somePart, ArrayList<Part> partsList) throws Exception {
        
        ArrayList<Part> toReturn = partsList;
        Part compositePart = somePart;
        ArrayList<Part> composition = compositePart.getComposition();
        
        for (int i = 0; i < composition.size(); i++) {
            Part currentPart = composition.get(i);
            if (currentPart.isBasic()) {
                toReturn.add(currentPart);
            } else {
                toReturn = getCompositionHelper(currentPart, toReturn);
            }
        }
        return toReturn;
    }

    /** Correct composite part UUIDs for Clotho export **/
    public void fixCompositeUUIDs(Collector coll, RGraph graph) throws Exception {
        
        ArrayList<RNode> queue = new ArrayList<RNode>();
        HashSet<RNode> seenNodes = new HashSet<RNode>();
        RNode root = graph.getRootNode();
        queue.add(root);
        ArrayList<RNode> sortedQueue = new ArrayList();
        sortedQueue.add(root);
        
        while (!queue.isEmpty()) {
            RNode current = queue.get(0);
            queue.remove(0);
            seenNodes.add(current);
            ArrayList<RNode> neighbors = current.getNeighbors();
            sortedQueue.add(0, current);
            for (RNode neighbor : neighbors) {
                if (!seenNodes.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        seenNodes.clear();
        
        while (!sortedQueue.isEmpty()) {
            RNode current = sortedQueue.get(0);
            sortedQueue.remove(0);
            seenNodes.add(current);
            Part currentPart = coll.getPart(current.getUUID(), true);
            ArrayList<RNode> neighbors = current.getNeighbors();
            
            //second part of if statement is for library parts with large compositions but no child neighbors
            if (currentPart.isComposite() || current.getNeighbors().size()>=currentPart.getComposition().size()) {
                ArrayList<Part> composition = new ArrayList();
                for (RNode neighbor : neighbors) {
                    if (current.getStage() > neighbor.getStage()) {
                        Part p= coll.getPart(neighbor.getUUID(),true);
                        composition.add(coll.getPart(neighbor.getUUID(), true));
                    }
                }
                currentPart.setComposition(composition);
                currentPart.setComposition(getComposition(currentPart));
            }
        }
    }
    
    //FIELDS
    ArrayList<Part> _allCompositeParts;
    ArrayList<Part> _allBasicParts;
    ArrayList<Vector> _allVectors;
}