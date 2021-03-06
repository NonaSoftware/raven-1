/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cidarlab.raven.accessibility;

import org.cidarlab.raven.algorithms.modasm.RBioBricks;
import org.cidarlab.raven.algorithms.modasm.RGatewayGibson;
import org.cidarlab.raven.algorithms.modasm.RGoldenGate;
import org.cidarlab.raven.algorithms.modasm.RMoClo;
import org.cidarlab.raven.algorithms.core.RHomologyPrimerDesign;
import org.cidarlab.raven.datastructures.Collector;
import org.cidarlab.raven.datastructures.Part;
import org.cidarlab.raven.datastructures.RNode;
import org.cidarlab.raven.datastructures.RVector;
import org.cidarlab.raven.datastructures.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONObject;

/**
 *
 * @author evanappleton
 */
public class RInstructions {

    public static String generateInstructions(ArrayList<RNode> roots, Collector coll, ArrayList<Part> partLib, ArrayList<Vector> vectorLib, JSONObject primerParameters, boolean designPrimers, String method) {

        int oligoCount = 1;
        String instructions = "";
        designPrimers = true;
        String oligoNameRoot = "oligo";
        double meltingTemp = 55.0;
        int targetHomologyLength = 24;
        int minPCRLength = 24;
        int maxPrimerLength = 60;

        if (primerParameters.has("oligoNameRoot")) {
            oligoNameRoot = primerParameters.get("oligoNameRoot").toString();
        }
        
        if (primerParameters.has("meltingTemperature") && !primerParameters.get("meltingTemperature").toString().equals("null")) {
            meltingTemp = Double.valueOf(primerParameters.get("meltingTemperature").toString());
        }
        
        if (primerParameters.has("targetHomologyLength") && !primerParameters.get("targetHomologyLength").toString().equals("null")) {
            targetHomologyLength = Integer.valueOf(primerParameters.get("targetHomologyLength").toString());
        }
        
        if (primerParameters.has("minPCRLength") && !primerParameters.get("minPCRLength").toString().equals("null")) {    
            minPCRLength = Integer.valueOf(primerParameters.get("minPCRLength").toString());
        }
        
        if (primerParameters.has("maxPrimerLength") && !primerParameters.get("maxPrimerLength").toString().equals("null")) {
            maxPrimerLength = Integer.valueOf(primerParameters.get("maxPrimerLength").toString());
        }

        ArrayList<String> oligoNames = new ArrayList<String>();
        ArrayList<String> oligoSequences = new ArrayList<String>();
        HashSet<RVector> newVectors = new HashSet<RVector>();
        HashSet<RNode> newNodes = new HashSet<RNode>();
        HashMap<String, String[]> nodeOligoHash = new HashMap();
        HashMap<String, ArrayList<String>> vectorOligoHash = new HashMap<String, ArrayList<String>>();
        HashSet<String> libraryPartKeys = ClothoReader.getExistingPartKeys(partLib);
        HashSet<String> libraryVectorKeys = ClothoReader.getExistingVectorKeys(vectorLib);

        for (RNode root : roots) {

            HashSet<RNode> l0NodesThisRoot = new HashSet<RNode>();
            HashSet<RVector> vectorsThisRoot = new HashSet<RVector>();
            HashSet<String> seenNodeKeys = new HashSet<String>();
            HashSet<String> seenVecKeys = new HashSet<String>();
            HashMap<RNode, String[]> fusionSites = new HashMap<RNode, String[]>();
            HashMap<RVector, RNode> vecNodeMap = new HashMap<RVector, RNode>();

            //append header for each goal part
            instructions = instructions + "**********************************************"
                    + "\nAssembly Instructions for target part: " + coll.getPart(root.getUUID(), true).getName()
                    + "\n**********************************************\n";
            ArrayList<RNode> queue = new ArrayList<RNode>();
            queue.add(root);

            while (!queue.isEmpty()) {
                RNode currentNode = queue.get(0);
                if (method.equalsIgnoreCase("GoldenGate")) {
                    fusionSites = RGoldenGate.getFusionSites(currentNode, root, coll, fusionSites);
                }
                
                queue.remove(0);

                Part currentPart = coll.getPart(currentNode.getUUID(), true);

                //If the current node is a step in stage 1 or higher instruct the cloning steps
                if (currentNode.getStage() > 0) {

                    //If this node has a new vector, there will need to be a PCR added, but this is done at the end of the file
                    RVector vector = currentNode.getVector();
                    if (!seenVecKeys.contains(vector.getVectorKey("+")) || !seenVecKeys.contains(vector.getVectorKey("-"))) {
                        vectorsThisRoot.add(vector);
                        seenVecKeys.add(vector.getVectorKey("+"));
                        seenVecKeys.add(vector.getVectorKey("-"));
                    }

                    vecNodeMap.put(vector, currentNode);
                    if (!libraryVectorKeys.contains(vector.getVectorKey("+")) || !libraryVectorKeys.contains(vector.getVectorKey("-"))) {
                        newVectors.add(vector);
                    }

                    //Append which parts to use for a cloning reaction
                    if (method.equalsIgnoreCase("GatewayGibson")) {
                        if (currentNode.getStage() > 1) {
                            instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by digesting with I-SceI and performing a Gibson cloning reaction with: "; 
                        } else {
                            instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a Gateway cloning reaction with: "; 
                        }
                    } else if (method.equalsIgnoreCase("gibson") || method.equalsIgnoreCase("cpec") || method.equalsIgnoreCase("slic")) {
                        instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by digesting with NotI and performing a " + method + " cloning reaction with: ";
                    } else {
                        instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a " + method + " cloning reaction with: "; 
                    }
                    
                    for (RNode neighbor : currentNode.getNeighbors()) {

                        if (currentNode.getStage() > neighbor.getStage()) {
                            Part childPart = coll.getPart(neighbor.getUUID(), true);
                            instructions = instructions + childPart.getName() + "|" + childPart.getLeftOverhang() + "|" + childPart.getRightOverhang() + "|" + childPart.getDirections() + ", ";
                            queue.add(neighbor);
                        }
                    }

                    //Assuming there is a vector present, include it in the MoClo reaction (this should always be the case for MoClo assembly)
                    instructions = instructions + vector.getName() + "|" + vector.getLOverhang() + "|" + vector.getROverhang() + "\n";

                //If the node is in stage 0, it must be determined whether or not PCRs need to be done and design primers if necessary    
                } else {

                    //Determine if a new vectors needs to be made
                    String nodeKey = currentNode.getNodeKey("+");
                    RVector vector = currentNode.getVector();
                    if (vector != null) {
                        
                        //Determine if a new vector is needed, i.e. a new PCR step
                        if (!seenVecKeys.contains(vector.getVectorKey("+")) || !seenVecKeys.contains(vector.getVectorKey("-"))) {
                            vectorsThisRoot.add(vector);
                            seenVecKeys.add(vector.getVectorKey("+"));
                            seenVecKeys.add(vector.getVectorKey("-"));
                        }
                        
                        vecNodeMap.put(vector, currentNode);
                        if (!libraryVectorKeys.contains(vector.getVectorKey("+")) || !libraryVectorKeys.contains(vector.getVectorKey("-"))) {
                            newVectors.add(vector);
                        }
                    }
                    
                    //Determine if a new level 0 node is needed, i.e. a new PCR step
                    if (!seenNodeKeys.contains(currentNode.getNodeKey("+")) || !seenNodeKeys.contains(currentNode.getNodeKey("-"))) {
                        l0NodesThisRoot.add(currentNode);
                        seenNodeKeys.add(currentNode.getNodeKey("+"));
                        seenNodeKeys.add(currentNode.getNodeKey("-"));
                    }
                    
                    //Design part primers if this part key is not in the key list, perform asssembly step if the vector or part is not yet PCRed
                    if (!libraryPartKeys.contains(nodeKey)) {
                        newNodes.add(currentNode);

                        //Assuming there is a vector present, include it in the MoClo reaction (this should always be the case for MoClo assembly)
                        if (vector != null) {
                            
                            //Gateway-Gibson exception
                            if (method.equalsIgnoreCase("GatewayGibson")) {
                                instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a Golden Gate cloning reaction with: ";
                                instructions = instructions + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + ", ";
                            } else {
                                instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a " + method + " cloning reaction with: ";
                                instructions = instructions + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + ", ";
                            }
                            instructions = instructions + vector.getName() + "|" + vector.getLOverhang() + "|" + vector.getROverhang() + "\n";
                        }

                    //Else if the part key is in the list, determine if a steps is necessary based upon whether the vector is also present
                    } else {
                        
                        if (vector != null) {
                            if (!libraryVectorKeys.contains(vector.getVectorKey("+")) || !libraryVectorKeys.contains(vector.getVectorKey("-"))) {
                                
                                //Gateway-Gibson exception
                                if (method.equalsIgnoreCase("GatewayGibson")) {
                                    instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a Golden Gate cloning reaction with: ";
                                } else {
                                    instructions = instructions + "\n-> Assemble " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections() + " by performing a " + method + " cloning reaction with: ";
                                }
                            }
                        }
                    }
                }
            }

            //Look at all level 0 nodes for this root
            for (RNode l0Node : l0NodesThisRoot) {

                Part currentPart = coll.getPart(l0Node.getUUID(), true);

                //Design primers for new level 0 nodes
                if (newNodes.contains(l0Node)) {

                    //For small part, just order annealing primers
                    boolean anneal = false;
                    if (coll.getPart(l0Node.getUUID(), true).getSeq().length() <= minPCRLength && !coll.getPart(l0Node.getUUID(), true).getSeq().isEmpty()) {
                        anneal = true;
                    }

                    //If primers for this node have not yet been created (seen in the hash), create them
                    if (!nodeOligoHash.containsKey(l0Node.getNodeKey("+"))) {

                        String[] oligoNamesForNode = new String[2];

                        //Determine which kind of primers to generate
                        String[] oligos;
                        if (method.equalsIgnoreCase("MoClo")) {
                            oligos = RMoClo.generatePartPrimers(l0Node, coll, meltingTemp, targetHomologyLength, minPCRLength, maxPrimerLength);
                        } else if (method.equalsIgnoreCase("GatewayGibson")) {
                            oligos = RGatewayGibson.generatePartPrimers(l0Node, coll, meltingTemp, targetHomologyLength, minPCRLength, maxPrimerLength);
                        } else if (method.equalsIgnoreCase("BioBricks")) {
                            oligos = RBioBricks.generatePartPrimers(l0Node, coll, meltingTemp, targetHomologyLength, minPCRLength, maxPrimerLength);
                        } else if (method.equalsIgnoreCase("GoldenGate")) {
                            fusionSites = RGoldenGate.getFusionSites(l0Node, root, coll, fusionSites);
                            oligos = RGoldenGate.generatePartPrimers(l0Node, fusionSites.get(l0Node), coll, meltingTemp, targetHomologyLength, minPCRLength, maxPrimerLength);
                        } else {
                            oligos = RHomologyPrimerDesign.homolRecombPartPrimers(l0Node, root, coll, meltingTemp, targetHomologyLength, minPCRLength, maxPrimerLength);
                        }

                        //Add to instructions file
                        if (oligos[0].length() > 0 && oligos[1].length() > 0) {
                            String fwdOligo = oligos[0];
                            String revOligo = oligos[1];
                            String forwardOligoName;
                            String reverseOligoName;

                            //If a merged part oligo is longer than the specified amount, 
                            boolean synthesize = false;
                            if (fwdOligo.equalsIgnoreCase("synthesize") || revOligo.equalsIgnoreCase("synthesize")) {
                                synthesize = true;
                            }

                            forwardOligoName = oligoNameRoot + oligoCount;
                            oligoNames.add(forwardOligoName);
                            oligoSequences.add(fwdOligo);
                            oligoCount++;

                            reverseOligoName = oligoNameRoot + oligoCount;
                            oligoNames.add(reverseOligoName);
                            oligoSequences.add(revOligo);
                            oligoCount++;

                            oligoNamesForNode[0] = forwardOligoName;
                            oligoNamesForNode[1] = reverseOligoName;
                            nodeOligoHash.put(l0Node.getNodeKey("+"), oligoNamesForNode);

                            //If the primers are small and therefore annealing primers
                            if (anneal) {
                                instructions = instructions + "\nAnneal oligos: " + forwardOligoName + " and " + reverseOligoName + " or synthesize to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                            } else if (synthesize) {
                                instructions = instructions + "\nSynthesize part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                            } else {
                                if (l0Node.getSpecialSeq() != null) {
                                    instructions = instructions + "\nPCR " + l0Node.getComposition().get(l0Node.getComposition().size() - 1) + " part with oligos: " + forwardOligoName + " and " + reverseOligoName + " to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                                } else {
                                    instructions = instructions + "\nPCR " + currentPart.getName() + " part with oligos: " + forwardOligoName + " and " + reverseOligoName + " to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                                }
                            }
                        }

                    //If primers are being reused
                    } else {
                        String[] nodeOligos = nodeOligoHash.get(l0Node.getNodeKey("+"));
                        boolean synthesize = false;
                        if (nodeOligos[0].equalsIgnoreCase("synthesize") || nodeOligos[1].equalsIgnoreCase("synthesize")) {
                            synthesize = true;
                        }

                        if (anneal) {
                            instructions = instructions + "\nAnneal oligos: " + nodeOligos[0] + " and " + nodeOligos[1] + " or synthesize to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                        } else if (synthesize) {
                            instructions = instructions + "\nSynthesize part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                        } else {
                            if (l0Node.getSpecialSeq() != null) {
                                instructions = instructions + "\nPCR " + l0Node.getComposition().get(l0Node.getComposition().size() - 1) + " part with oligos: " + nodeOligos[0] + " and " + nodeOligos[1] + " to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                            } else {
                                instructions = instructions + "\nPCR " + currentPart.getName() + " part with oligos: " + nodeOligos[0] + " and " + nodeOligos[1] + " to get part (NAME | LEFT OVERHANG | RIGHT OVERHANG | ORIENTATION): " + currentPart.getName() + "|" + currentPart.getLeftOverhang() + "|" + currentPart.getRightOverhang() + "|" + currentPart.getDirections();
                            }
                        }
                    }

                }
            }

            //Look at all vectors for this root
            for (RVector vector : vectorsThisRoot) {

                Vector currentVector = coll.getVector(vector.getUUID(), true);
                String vecName = currentVector.getName();
                RNode node = vecNodeMap.get(vector);

                //Design primers for new vectors
                if (newVectors.contains(vector)) {
                    if (designPrimers) {

                        //If primers for this vector have not yet been created (seen in the hash), create them
                        if (!vectorOligoHash.containsKey(vector.getVectorKey("+"))) {
                            ArrayList<String> vectorOligoNamesForNode = new ArrayList<String>();
//                            RNode node = vecNodeMap.get(vector);
                            
                            //Determine which kind of primers to generate
                            String[] oligos;
                            if (method.equalsIgnoreCase("MoClo")) {
                                oligos = RMoClo.generateVectorPrimers(vector);
                            } else if (method.equalsIgnoreCase("GatewayGibson")) {
                                oligos = RGatewayGibson.generateVectorPrimers(vector);
                            } else if (method.equalsIgnoreCase("BioBricks")) {
                                oligos = RBioBricks.generateVectorPrimers(vector, coll, meltingTemp, targetHomologyLength, maxPrimerLength, minPCRLength);
                            } else if (method.equalsIgnoreCase("GoldenGate")) {
                                oligos = RGoldenGate.generateVectorPrimers(vector, fusionSites.get(node));
                            } else {
                                oligos = RHomologyPrimerDesign.homolRecombVectorPrimers(vector, root, coll, meltingTemp, targetHomologyLength, maxPrimerLength, minPCRLength);
                            }

                            String fwdOligo = oligos[0];
                            String revOligo = oligos[1];
                            String forwardOligoName;
                            String reverseOligoName;

                            forwardOligoName = oligoNameRoot + oligoCount;
                            oligoNames.add(forwardOligoName);
                            oligoSequences.add(fwdOligo);
                            oligoCount++;

                            reverseOligoName = oligoNameRoot + oligoCount;
                            oligoNames.add(reverseOligoName);
                            oligoSequences.add(revOligo);
                            oligoCount++;

                            //pair of oligos used for a part
                            vectorOligoNamesForNode.add(forwardOligoName);
                            vectorOligoNamesForNode.add(reverseOligoName);
                            vectorOligoHash.put(vector.getVectorKey("+"), vectorOligoNamesForNode);

                            //Correct for desination vectors with MoClo and GoldenGate
                            if (method.equalsIgnoreCase("moclo") || method.equalsIgnoreCase("goldengate")) {
                                instructions = instructions + "\nPCR lacZ part with oligos: " + forwardOligoName + " and " + reverseOligoName + " and clone into vector: " + vecName + " with SpeI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            } else if (method.equalsIgnoreCase("gatewaygibson")) { 
                                if (vector.getLevel() == 0) {
                                    instructions = instructions + "\nPCR lacZ part with oligos: " + forwardOligoName + " and " + reverseOligoName + " and clone into vector: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                                } else {
                                    instructions = instructions + "\nPCR " + currentVector.getLeftOverhang() + "-R4-CmR-ccdB-R2-" + currentVector.getRightOverhang() + " part with oligos: " + forwardOligoName + " and " + reverseOligoName + " and clone into vector: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                                }
                            } else {
                                instructions = instructions + "\nPCR " + vecName + " vector with oligos: " + forwardOligoName + " and " + reverseOligoName + " to get vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            }

                        } else {
                            ArrayList<String> oligoHash = vectorOligoHash.get(vector.getVectorKey("+"));
                            
                            //Correct for desination vectors with MoClo and GoldenGate
                            if (method.equalsIgnoreCase("moclo") || method.equalsIgnoreCase("goldengate")) {
                                instructions = instructions + "\nPCR lacZ part with oligos: " + oligoHash.get(0) + " and " + oligoHash.get(1) + " and clone into vector: " + vecName + " with SpeI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            } else if (method.equalsIgnoreCase("gatewaygibson")) { 
                                if (vector.getLevel() == 0) {
                                    instructions = instructions + "\nPCR lacZ part with oligos: " + oligoHash.get(0) + " and " + oligoHash.get(1) + " and clone into vector: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                                } else {
                                    instructions = instructions + "\nPCR " + currentVector.getLeftOverhang() + "-R4-CmR-ccdB-R2-" + currentVector.getRightOverhang() + " part with oligos: " + oligoHash.get(0) + " and " + oligoHash.get(1) + " and clone into vector: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                                }
                            } else {
                                instructions = instructions + "\nPCR " + vecName + " vector with oligos: " + oligoHash.get(0) + " and " + oligoHash.get(1) + " to get vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + vecName + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            }
                        }
                    } else {
                        
                        //Correct for desination vectors with MoClo and GoldenGate
                        if (method.equalsIgnoreCase("moclo") || method.equalsIgnoreCase("goldengate")) {
                            instructions = instructions + "\nPCR lacZ part and clone into vector: " + vecName + " with SpeI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + currentVector.getName() + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                        } else if (method.equalsIgnoreCase("gatewaygibson")) {
                            if (vector.getLevel() == 0) {
                                instructions = instructions + "\nPCR lacZ part and clone into vector: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + currentVector.getName() + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            } else {
                                instructions = instructions + "\nPCR " + currentVector.getLeftOverhang() + "-R4-CmR-ccdB-R2-" + currentVector.getRightOverhang() + " part with oligos: " + vecName + " with BsaI to get destination vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + currentVector.getName() + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                            }
                        } else {
                            instructions = instructions + "\nPCR " + vecName + " vector to get vector (NAME | LEFT OVERHANG | RIGHT OVERHANG): " + currentVector.getName() + "|" + currentVector.getLeftOverhang() + "|" + currentVector.getRightOverhang();
                        }
                    }
                }          
            }

            instructions = instructions + "\n\n";
        }

        if (designPrimers) {

            //append primer designs
            instructions = instructions + "\n**********************************************\nOLIGOS";
            for (int i = 0; i < oligoNames.size(); i++) {
                instructions = instructions + "\n>" + oligoNames.get(i);
                instructions = instructions + "\n" + oligoSequences.get(i);
            }
        }
        return instructions;
    }
}
