/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Communication;

import Controller.accessibility.RInstructions;
import Controller.algorithms.modasm.RBioBricks;
import Controller.accessibility.ClothoWriter;
import Controller.accessibility.ClothoReader;
import Controller.algorithms.modasm.*;
import Controller.algorithms.nonmodasm.*;
import Controller.datastructures.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Jenhan
 */
public class RavenController {

    public RavenController(String path, String user) {
        _path = path;
        _user = user;
        //temporary default values
        _databaseConfig.add("jdbc:mysql://128.197.164.27");
        _databaseConfig.add("Puppeteerv0 ");
        _databaseConfig.add("cidar.rwdu");
        _databaseConfig.add("cidar");
    }

    public ArrayList<RGraph> runBioBricks() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for BioBricks assembly
        _assemblyGraphs.clear();
        RBioBricks biobricks = new RBioBricks();
        ArrayList<RGraph> optimalGraphs = biobricks.bioBricksClothoWrapper(_goalParts, _required, _recommended, _forbidden, _discouraged, _partLibrary, null);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for Gibson *
     */
    public ArrayList<RGraph> runGibson() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for Gibson assembly
        _assemblyGraphs.clear();
        RGibson gibson = new RGibson();
        ArrayList<RGraph> optimalGraphs = gibson.gibsonClothoWrapper(_goalParts, _required, _recommended, _forbidden, _discouraged, _partLibrary, _efficiency, null);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for CPEC *
     */
    public ArrayList<RGraph> runCPEC() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for CPEC assembly
        _assemblyGraphs.clear();
        RCPEC cpec = new RCPEC();
        ArrayList<RGraph> optimalGraphs = cpec.cpecClothoWrapper(_goalParts, _required, _recommended, _forbidden, _discouraged, _partLibrary, _efficiency, null);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for SLIC *
     */
    public ArrayList<RGraph> runSLIC() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for SLIC assembly
        _assemblyGraphs.clear();
        RSLIC slic = new RSLIC();
        ArrayList<RGraph> optimalGraphs = slic.slicClothoWrapper(_goalParts, _required, _recommended, _forbidden, _discouraged, _partLibrary, _efficiency, null);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for MoClo *
     */
    public ArrayList<RGraph> runMoClo() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for MoClo assembly
        _assemblyGraphs.clear();
        RMoClo moclo = new RMoClo();
        moclo.setForcedOverhangs(_collector, forcedOverhangHash);
        ArrayList<RGraph> optimalGraphs = moclo.mocloClothoWrapper(_goalParts, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false, _efficiency, null);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for Golden Gate *
     */
    public ArrayList<RGraph> runGoldenGate() throws Exception {
        if (_goalParts == null) {
            return null;
        }

        //Run algorithm for Golden Gate assembly
        _assemblyGraphs.clear();
        RGoldenGate gg = new RGoldenGate();
        ArrayList<RGraph> optimalGraphs = gg.goldenGateClothoWrapper(_goalParts, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, _efficiency, null);
        return optimalGraphs;
    }

    //returns json array containing all objects in parts list; generates parts list file
    //input: design number refers to the design number on the client
    public JSONArray generatePartsList(String designNumber) throws Exception {
        File file = new File(_path + _user + "/partsList" + designNumber + ".csv");

        //traverse graphs to get uuids
        ArrayList<Part> usedParts = new ArrayList<Part>();
        ArrayList<Vector> usedVectors = new ArrayList<Vector>();
        for (RGraph result : _assemblyGraphs) {
            for (Part p : result.getPartsInGraph(_collector)) {

                if (!usedParts.contains(p)) {
                    usedParts.add(p);
                }
            }
            for (Vector v : result.getVectorsInGraph(_collector)) {
                if (!usedVectors.contains(v)) {
                    usedVectors.add(v);
                }
            }
        }

        //extract information from parts and write file
        String partList = "[";
        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write("Name,Sequence,Left Overhang,Right Overhang,Type,Resistance,Level,Vector,Composition");

        for (Part p : usedParts) {
            ArrayList<String> tags = p.getSearchTags();
            String RO = "";
            String LO = "";
            String type = "";
            ArrayList<String> direction = ClothoReader.parseTags(tags, "Direction:");

            for (int k = 0; k < tags.size(); k++) {
                if (tags.get(k).startsWith("LO:")) {
                    LO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("RO:")) {
                    RO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("Type:")) {
                    type = tags.get(k).substring(6);
                }
            }

            String composition = "";

            if (p.isBasic()) {
                composition = p.getName() + "|" + p.getLeftOverhang() + "|" + p.getRightOverhang() + "|" + direction.get(0);
                out.write("\n" + p.getName() + "," + p.getSeq() + "," + LO + "," + RO + "," + type + ",," + composition);
            } else {
                type = "composite";
                for (int i = 0; i < p.getComposition().size(); i++) {
                    Part subpart = p.getComposition().get(i);

                    ArrayList<String> searchTags = subpart.getSearchTags();
                    ArrayList<String> subPartDirection = ClothoReader.parseTags(searchTags, "Direction:");
                    for (int k = 0; k < tags.size(); k++) {
                        if (tags.get(k).startsWith("LO:")) {
                            LO = tags.get(k).substring(4);
                        } else if (tags.get(k).startsWith("RO:")) {
                            RO = tags.get(k).substring(4);
                        }
                    }
                    composition = composition + "," + subpart.getName() + "|" + subpart.getLeftOverhang() + "|" + subpart.getRightOverhang() + "|" + subPartDirection.get(0);
                }

                composition = composition.substring(1);
                out.write("\n" + p.getName() + "," + p.getSeq() + "," + LO + "," + RO + "," + type + ",,,TODO: addvector," + composition);
            }
            partList = partList
                    + "{\"uuid\":\"" + p.getUUID()
                    + "\",\"Name\":\"" + p.getName()
                    + "\",\"Sequence\":\"" + p.getSeq()
                    + "\",\"LO\":\"" + p.getLeftOverhang()
                    + "\",\"RO\":\"" + p.getRightOverhang()
                    + "\",\"Type\":\"" + p.getType()
                    + "\",\"Composition\":\"" + composition
                    + "\",\"Resistance\":\"\",\"Level\":\"\"},";
        }

        for (Vector v : usedVectors) {
            ArrayList<String> tags = v.getSearchTags();
            String RO = "";
            String LO = "";
            String level = "";
            String resistance = "";
            for (int k = 0; k < tags.size(); k++) {
                if (tags.get(k).startsWith("LO:")) {
                    LO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("RO:")) {
                    RO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("Level:")) {
                    level = tags.get(k).substring(7);
                } else if (tags.get(k).startsWith("Resistance:")) {
                    resistance = tags.get(k).substring(12);
                }
            }
            out.write("\n" + v.getName() + "," + v.getSeq() + "," + LO + "," + RO + ",vector," + resistance + "," + level);
            partList = partList + "{\"uuid\":\"" + v.getUUID()
                    + "\",\"Name\":\"" + v.getName()
                    + "\",\"Sequence\":\"" + v.getSeq()
                    + "\",\"LO\":\"" + v.getLeftoverhang()
                    + "\",\"RO\":\"" + v.getRightOverhang()
                    + "\",\"Type\":\"vector\",\"Composition\":\"\""
                    + ",\"Resistance\":\"" + v.getResistance()
                    + "\",\"Level\":\"" + v.getLevel() + "\"},";
        }
        out.close();
        partList = partList.substring(0, partList.length() - 1);
        partList = partList + "]";
        return new JSONArray(partList);
    }

    //reset collector, all field variales, deletes all files in user's directory
    public void clearData() throws Exception {
        _collector.purge();
        _goalParts = new HashMap<Part, Vector>();//key: target part, value: vector
        _efficiency = new HashMap<Integer, Double>();
        _required = new HashSet<String>();
        _recommended = new HashSet<String>();
        _discouraged = new HashSet<String>();
        _forbidden = new HashSet<String>();
        _statistics = new Statistics();
        _assemblyGraphs = new ArrayList<RGraph>();
        forcedOverhangHash = new HashMap<String, ArrayList<String>>();
        _partLibrary = new ArrayList<Part>();
        _vectorLibrary = new ArrayList<Vector>();
        _instructions = "";
        _error = "";

        String uploadFilePath = _path + _user + "/";
        File[] filesInDirectory = new File(uploadFilePath).listFiles();
        for (File currentFile : filesInDirectory) {
            currentFile.delete();
        }
    }

    //returns all saved parts in the collector as a json array
    public String fetchData() throws Exception {
        String toReturn = "[";
        ArrayList<Part> allParts = _collector.getAllParts(false);
        for (Part p : allParts) {
            if (!p.isTransient()) {

                //Get the composition's overhangs and directions
                ArrayList<Part> composition = p.getComposition();
                ArrayList<String> compositions = new ArrayList<String>();
                String bpDir = new String();
                String bpLO = new String();
                String bpRO = new String();
                ArrayList<String> tags = p.getSearchTags();
                ArrayList<String> direction = ClothoReader.parseTags(tags, "Direction:");

                if (composition.size() > 1) {
                    for (int i = 0; i < composition.size(); i++) {
                        Part part = composition.get(i);

                        if (!direction.isEmpty()) {
                            bpDir = direction.get(i);
                        }

                        ArrayList<String> searchTags = part.getSearchTags();
                        String aPartComp;

                        //Look at all of the search tags
                        for (String tag : searchTags) {
                            if (tag.startsWith("LO:")) {
                                bpLO = tag.substring(4);
                            } else if (tag.startsWith("RO:")) {
                                bpRO = tag.substring(4);
                            }
                        }

                        if (!bpDir.isEmpty()) {
                            if (!bpLO.isEmpty() || !bpRO.isEmpty()) {
                                aPartComp = part.getName() + "|" + bpLO + "|" + bpRO + "|" + bpDir;
                            } else {
                                aPartComp = part.getName() + "|" + bpDir;
                            }
                        } else {
                            if (!bpLO.isEmpty() || !bpRO.isEmpty()) {
                                aPartComp = part.getName() + "|" + bpLO + "|" + bpRO;
                            } else {
                                aPartComp = part.getName();
                            }
                        }
                        compositions.add(aPartComp);
                    }
                } else {
                    compositions.add(p.getName());
                }

                toReturn = toReturn
                        + "{\"uuid\":\"" + p.getUUID()
                        + "\",\"Name\":\"" + p.getName()
                        + "\",\"Sequence\":\"" + p.getSeq()
                        + "\",\"LO\":\"" + p.getLeftOverhang()
                        + "\",\"RO\":\"" + p.getRightOverhang()
                        + "\",\"Type\":\"" + p.getType()
                        + "\",\"Composition\":\"" + compositions
                        + "\",\"Resistance\":\"\",\"Level\":\"\"},";
            }
        }
        ArrayList<Vector> allVectors = _collector.getAllVectors(false);
        for (Vector v : allVectors) {
            toReturn = toReturn + "{\"uuid\":\"" + v.getUUID()
                    + "\",\"Name\":\"" + v.getName()
                    + "\",\"Sequence\":\"" + v.getSeq()
                    + "\",\"LO\":\"" + v.getLeftoverhang()
                    + "\",\"RO\":\"" + v.getRightOverhang()
                    + "\",\"Type\":\"vector\",\"Composition\":\"\""
                    + ",\"Resistance\":\"" + v.getResistance()
                    + "\",\"Level\":\"" + v.getLevel() + "\"},";
        }
        toReturn = toReturn.subSequence(0, toReturn.length() - 1) + "]";
        if (_error.length() > 0) {
            _error = _error.replaceAll("[\r\n\t]+", "<br/>");
            toReturn = "{\"result\":" + toReturn + ",\"status\":\"bad\",\"message\":\"" + _error + "\"}";
        } else {
            toReturn = "{\"result\":" + toReturn + ",\"status\":\"good\"}";
        }
        return toReturn;
    }

    //handles multiple file upload from the client
    //parses each file for parts and saves them
    public void loadUploadedFiles(ArrayList<File> filesToRead) {
        _error = "";
//        String uploadFilePath = _path + _user + "/";
//        File[] filesInDirectory = new File(uploadFilePath).listFiles();
        try {
            if (filesToRead != null) {
                for (File currentFile : filesToRead) {
                    String filePath = currentFile.getAbsolutePath();
                    String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length()).toLowerCase();
                    if ("csv".equals(fileExtension)) {
                        parseRavenFile(currentFile);
                    }
                }
            }
        } catch (Exception e) {
            String exceptionAsString = e.getMessage().replaceAll("[\r\n\t]+", "<br/>");
            _error = exceptionAsString;
        }
    }

    //parses a newly generated parts list from a new design and saves the parts in that parts list
    public void saveNewDesign(String designCount) throws Exception {
        _error = "";
        String filePath = _path + _user + "/partsList" + designCount + ".csv";
        File toLoad = new File(filePath);
        parseRavenFile(toLoad);
    }

    /**
     * Parse an input Raven file *
     */
    private void parseRavenFile(File input) throws Exception {
        ArrayList<String> badLines = new ArrayList();
        ArrayList<String[]> compositePartTokens = new ArrayList<String[]>();
        if (forcedOverhangHash == null) {
            forcedOverhangHash = new HashMap<String, ArrayList<String>>();
        }
        BufferedReader reader = new BufferedReader(new FileReader(input.getAbsolutePath()));
        String line = reader.readLine();
        line = reader.readLine(); //skip first line

        //Read each line of the input file to parse parts
        while (line != null) {
            while (line.matches("^[\\s,]+")) {
                line = reader.readLine();
            }
            String[] tokens = line.split(",");
            int tokenCount = tokens.length; //keeps track of how many columns are filled by counting backwards
            for (int i = tokens.length - 1; i > -1; i--) {
                if (tokens[i].trim().matches("[\\s]*")) {
                    tokenCount--;
                } else {
                    break;
                }
            }

            //Composite parts - read, but do not generate
            if (tokenCount > 9) {

                try {
                    String[] trimmedTokens = new String[tokenCount];
                    System.arraycopy(tokens, 0, trimmedTokens, 0, tokenCount);
                    compositePartTokens.add(trimmedTokens);
                } catch (Exception e) {
                    badLines.add(line);
                }

                //Vectors - read and generate new vector
            } else if (tokenCount == 7) {

                try {
                    String name = tokens[0].trim();
                    String sequence = tokens[1].trim();
                    String leftOverhang = tokens[2].trim();
                    String rightOverhang = tokens[3].trim();
                    String resistance = tokens[5].toLowerCase().trim();
                    int level;
                    try {
                        level = Integer.parseInt(tokens[6]);
                    } catch (NumberFormatException e) {
                        level = -1;
                    }
                    Vector newVector = Vector.generateVector(name, sequence);
                    newVector.addSearchTag("LO: " + leftOverhang);
                    newVector.addSearchTag("RO: " + rightOverhang);
                    newVector.addSearchTag("Level: " + level);
                    newVector.addSearchTag("Resistance: " + resistance);
                    newVector.setTransientStatus(false);
                    Vector toBreak = newVector.saveDefault(_collector);
                    if (toBreak == null) {
                        break;
                    }
                } catch (Exception e) {
                    badLines.add(line);
                }

                //Basic part - read and generate new part
            } else if (tokenCount == 5) {

                try {
                    String name = tokens[0].trim();
                    String sequence = tokens[1].trim();
                    String leftOverhang = tokens[2].trim();
                    String rightOverhang = tokens[3].trim();
                    String type = tokens[4].trim();
                    Part newBasicPart = Part.generateBasic(name, sequence);
                    newBasicPart.addSearchTag("LO: " + leftOverhang);
                    newBasicPart.addSearchTag("RO: " + rightOverhang);
                    newBasicPart.addSearchTag("Type: " + type);
                    Part toBreak = newBasicPart.saveDefault(_collector);
                    newBasicPart.setTransientStatus(false);
                    if (toBreak == null) {
                        break;
                    }
                } catch (Exception e) {
                    badLines.add(line);
                }

                //Basic parts with direction and overhangs
            } else if (tokenCount == 9) {

                try {
                    String name = tokens[0].trim();
                    String sequence = tokens[1].trim();
                    String leftOverhang = tokens[2].trim();
                    String rightOverhang = tokens[3].trim();
                    String type = tokens[4].trim();
                    String vectorName = tokens[7].trim();
                    String composition = tokens[8].trim();
                    Part newBasicPart = Part.generateBasic(name, sequence);
                    newBasicPart.addSearchTag("LO: " + leftOverhang);
                    newBasicPart.addSearchTag("RO: " + rightOverhang);
                    newBasicPart.addSearchTag("Direction: [" + composition.substring(composition.length() - 1) + "]");
                    newBasicPart.addSearchTag("Type: " + type);
                    Vector vector = null;
                    ArrayList<Vector> allVectorsWithName = _collector.getAllVectorsWithName(vectorName, true);
                    if (!allVectorsWithName.isEmpty()) {
                        //TODO do i need an exact match?
                        vector = allVectorsWithName.get(0);
                    }
                    _compPartsVectors.put(newBasicPart, vector);
                    Part toBreak = newBasicPart.saveDefault(_collector);
                    newBasicPart.setTransientStatus(false);
                    if (toBreak == null) {
                        break;
                    }
                } catch (Exception e) {
                    badLines.add(line);
                }

            } else {
                //poorly formed line
                badLines.add(line);

            }
            line = reader.readLine();
        }
        reader.close();

        //Create the composite parts
        for (String[] tokens : compositePartTokens) {
            try {
                ArrayList<Part> composition = new ArrayList<Part>();

                //For all of the basic parts in the composite part composition
                String name = tokens[0].trim();
                String leftOverhang = tokens[2].trim();
                String rightOverhang = tokens[3].trim();
                String vectorName = tokens[7].trim();
                ArrayList<String> directions = new ArrayList<String>();

                //Parse composition tokens
                for (int i = 8; i < tokens.length; i++) {
                    String basicPartString = tokens[i].trim();
                    String[] partNameTokens = basicPartString.split("\\|");
                    String bpForcedLeft = " ";
                    String bpForcedRight = " ";
                    String bpDirection = "+";
                    String compositePartName = tokens[0];
                    String basicPartName = partNameTokens[0];

                    //Check for forced overhangs and direction
                    if (partNameTokens.length > 1) {
                        if (partNameTokens.length == 2) {
                            if ("+".equals(partNameTokens[1]) || "-".equals(partNameTokens[1])) {
                                bpDirection = partNameTokens[1];
                            }
                        } else if (partNameTokens.length == 3) {
                            bpForcedLeft = partNameTokens[1];
                            bpForcedRight = partNameTokens[2];
                        } else if (partNameTokens.length == 4) {
                            bpDirection = partNameTokens[1];
                            bpForcedLeft = partNameTokens[2];
                            bpForcedRight = partNameTokens[3];
                        }
                    }
                    if (forcedOverhangHash.get(compositePartName) != null) {
                        forcedOverhangHash.get(compositePartName).add(bpForcedLeft + "|" + bpForcedRight);
                    } else {
                        ArrayList<String> toAdd = new ArrayList();
                        toAdd.add(bpForcedLeft + "|" + bpForcedRight);
                        forcedOverhangHash.put(compositePartName, toAdd);
                    }

                    directions.add(bpDirection);
                    composition.add(_collector.getAllPartsWithName(basicPartName, true).get(0));
                }

                Part newComposite = Part.generateComposite(composition, name);
                Vector vector = null;
                ArrayList<Vector> vectors = _collector.getAllVectorsWithName(vectorName, true);
                if (vectors.size() > 0) {
                    //TODO do we need an exact match?
                    vector = vectors.get(0);
                }
                _compPartsVectors.put(newComposite, vector);
                newComposite.addSearchTag("Direction: " + directions);
                newComposite.addSearchTag("LO: " + leftOverhang);
                newComposite.addSearchTag("RO: " + rightOverhang);
                newComposite.addSearchTag("Type: composite");
                newComposite.saveDefault(_collector);
                newComposite.setTransientStatus(false);
            } catch (NullPointerException e) {
                String badLine = "";

                for (int j = 0; j < tokens.length; j++) {
                    badLine = badLine + tokens[j] + ",";
                }
                badLines.add(badLine.substring(0, badLine.length() - 1));//trim the last comma
            }
        }

        //Print warning about bad line
        if (badLines.size() > 0) {

            String badLineMessage = "The following lines in your csv input was malformed. \nPlease check you input spreadsheet.";

            for (String bl : badLines) {
                badLineMessage = badLineMessage + "\n" + bl;
            }
            throw new Exception(badLineMessage);
        }
    }

    //given an array of part and vector uuids, save each of them and set their transient status to false
    //saved parts and vectors are typically associated with a new design on the client
    //writeSQL indicates whether or not parts will be saved in an sql database; url hardcoded for puppeteer team testing
    public String save(String[] partIDs, String[] vectorIDs, boolean writeSQL) {
        ArrayList<Part> toSaveParts = new ArrayList();
        ArrayList<Vector> toSaveVectors = new ArrayList();
        if (partIDs.length > 0) {
            for (int i = 0; i < partIDs.length; i++) {
                Part p = _collector.getPart(partIDs[i], true);
                p.setTransientStatus(false);
                toSaveParts.add(p);
            }
        }
        if (vectorIDs.length > 0) {
            for (int i = 0; i < vectorIDs.length; i++) {
                Vector v = _collector.getVector(vectorIDs[i], true);
                v.setTransientStatus(false);
                toSaveVectors.add(v);
            }
        }
        if (writeSQL) {
            PuppeteerWriter.saveParts(toSaveParts, _databaseConfig);
            PuppeteerWriter.saveVectors(toSaveVectors, _databaseConfig);
        }
        return "saved data";

    }

    //returns "loaded" or "not loaded" depending on whether there are objects in the collector
    public String getDataStatus() throws Exception {
        String toReturn = "not loaded";
        if (_collector.getAllParts(false).size() > 0 || _collector.getAllVectors(false).size() > 0) {
            toReturn = "loaded";
        }
        return toReturn;
    }

    /**
     * Traverse a solution graph for statistics *
     */
    private void getSolutionStats() throws Exception {

        int steps = 0;
        int stages = 0;
        int recCnt = 0;
        int disCnt = 0;
        int shr = 0;
        int rxn = 0;
        ArrayList<Double> effArray = new ArrayList<Double>();
        double eff = 0;

        if (!_assemblyGraphs.isEmpty()) {

            for (RGraph graph : _assemblyGraphs) {
                if (graph.getStages() > stages) {
                    stages = graph.getStages();
                }
                steps = steps + graph.getSteps();
                recCnt = recCnt + graph.getReccomendedCount();
                disCnt = disCnt + graph.getDiscouragedCount();
                shr = shr + graph.getSharing();
                rxn = rxn + graph.getReaction();
                effArray.addAll(graph.getEfficiencyArray());
            }
            double sum = 0;

            for (Double anEff : effArray) {
                sum = sum + anEff;
            }
            eff = sum / effArray.size();
        }

        _statistics.setEfficiency(eff);
        _statistics.setRecommended(recCnt);
        _statistics.setDiscouraged(disCnt);
        _statistics.setStages(stages);
        _statistics.setSteps(steps);
        _statistics.setSharing(shr);
        _statistics.setGoalParts(_goalParts.keySet().size());
        _statistics.setExecutionTime(Statistics.getTime());
        _statistics.setReaction(rxn);
        _statistics.setValid(_valid);
        System.out.println("Steps: " + steps + " Stages: " + stages + " Shared: " + shr + " PCRs: " + rxn + " Time: " + Statistics.getTime() + " valid: " + _valid);
    }

    //using parameters from the client, run the algorithm
    public JSONObject run(String designCount, String method, String[] targetIDs, HashSet<String> required, HashSet<String> recommended, HashSet<String> forbidden, HashSet<String> discouraged, String[] partLibraryIDs, String[] vectorLibraryIDs, HashMap<Integer, Double> efficiencyHash, ArrayList<String> primerParameters) throws Exception {
        _goalParts = new HashMap<Part, Vector>();
        _required = required;
        _recommended = recommended;
        _forbidden = forbidden;
        _discouraged = discouraged;
        _statistics = new Statistics();
        _vectorLibrary = new ArrayList<Vector>();
        _partLibrary = new ArrayList<Part>();
        _assemblyGraphs = new ArrayList<RGraph>();
        _efficiency = efficiencyHash;
        _valid = false;
        method = method.toLowerCase().trim();

        if (partLibraryIDs.length > 0) {
            for (int i = 0; i < partLibraryIDs.length; i++) {
                Part current = _collector.getPart(partLibraryIDs[i], false);
                if (current != null) {
                    _partLibrary.add(current);
                }
            }
        }

        if (vectorLibraryIDs.length > 0) {
            for (int i = 0; i < vectorLibraryIDs.length; i++) {
                Vector current = _collector.getVector(vectorLibraryIDs[i], false);
                if (current != null) {
                    _vectorLibrary.add(current);
                }
            }
        }

        for (int i = 0; i < targetIDs.length; i++) {
            Part current = _collector.getPart(targetIDs[i], false);
            Vector vector = _compPartsVectors.get(current);
            _goalParts.put(current, vector);
        }

        Statistics.start();
        boolean scarless = false;
        if (method.equals("biobricks")) {
            _assemblyGraphs = runBioBricks();
        } else if (method.equals("cpec")) {
            _assemblyGraphs = runCPEC();
            scarless = true;
        } else if (method.equals("gibson")) {
            _assemblyGraphs = runGibson();
            scarless = true;
        } else if (method.equals("goldengate")) {
            _assemblyGraphs = runGoldenGate();
            scarless = true;
        } else if (method.equals("moclo")) {
            _assemblyGraphs = runMoClo();
        } else if (method.equals("slic")) {
            _assemblyGraphs = runSLIC();
            scarless = true;
        }

        Statistics.stop();
        ClothoWriter writer = new ClothoWriter();
        ArrayList<String> graphTextFiles = new ArrayList();
        ArrayList<String> arcTextFiles = new ArrayList<String>();
        ArrayList<RNode> targetRootNodes = new ArrayList();
        if (!_assemblyGraphs.isEmpty()) {
            for (RGraph result : _assemblyGraphs) {
                targetRootNodes.add(result.getRootNode());
            }
        }

        //Initialize statistics
        boolean overhangValid = false;
        if (method.equals("biobricks")) {
            overhangValid = RBioBricks.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("cpec")) {
            overhangValid = RCPEC.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("gibson")) {
            overhangValid = RGibson.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("goldengate")) {
            overhangValid = RGoldenGate.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("moclo")) {
            overhangValid = RMoClo.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("slic")) {
            overhangValid = RSLIC.validateOverhangs(_assemblyGraphs);
        }
        boolean valid = validateGraphComposition();
        _valid = valid && overhangValid;
        _assemblyGraphs = RGraph.mergeGraphs(_assemblyGraphs);
        RGraph.getGraphStats(_assemblyGraphs, _partLibrary, _vectorLibrary, _goalParts, _recommended, _discouraged, scarless, 0.0, 0.0, 0.0, 0.0);
        getSolutionStats();
        if (!_assemblyGraphs.isEmpty()) {
            for (RGraph result : _assemblyGraphs) {
                writer.nodesToClothoPartsVectors(_collector, result);
                writer.fixCompositeUUIDs(_collector, result);
//                ArrayList<String> postOrderEdges = result.getPostOrderEdges();
//                arcTextFiles.add(result.printArcsFile(_collector, postOrderEdges, method));
            }
        }
        JSONObject d3Graph = RGraph.generateD3Graph(_assemblyGraphs, _partLibrary, _vectorLibrary);

        System.out.println("GRAPH AND ARCS FILES CREATED");
//        String mergedArcText = RGraph.mergeArcFiles(arcTextFiles);
        String mergedArcText = "";

        //generate instructions
        _instructions = RInstructions.generateInstructions (targetRootNodes, _collector, _partLibrary, _vectorLibrary, null, true, method);        
        if (_instructions == null) {
            _instructions = "Assembly instructions for RavenCAD are coming soon! Please stay tuned.";
        }
        
        File file = new File(_path + _user + "/instructions" + designCount + ".txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write(_instructions);
        out.close();

        //write graph file text file
        file = new File(_path + _user + "/pigeon" + designCount + ".txt");
        fw = new FileWriter(file);
        out = new BufferedWriter(fw);
        JSONArray edges = d3Graph.getJSONArray("edges");
        JSONObject images = d3Graph.getJSONObject("images");
        out.write("digraph{\n");
        Iterator keys = images.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            out.write("\"" + key + "\" [label=\"" + images.getString(key) + "\"]\n");
        }
        for (int i = 0; i < edges.length(); i++) {
            out.write(edges.getString(i) + "\n");
        }
        out.write("}");
        out.close();
        //write arcs text file
        file = new File(_path + _user + "/arcs" + designCount + ".txt");
        fw = new FileWriter(file);
        out = new BufferedWriter(fw);
        out.write(mergedArcText);
        out.close();

//        return new JSONObject();
        return d3Graph;
    }

    //traverse the graph and return a boolean indicating whether or not hte graph is valid in terms of composition
    private boolean validateGraphComposition() throws Exception {
        boolean toReturn = true;
        HashSet<String> seenRequired = new HashSet();
        for (RGraph graph : _assemblyGraphs) {
            ArrayList<RNode> queue = new ArrayList();
            HashSet<RNode> seenNodes = new HashSet();
            queue.add(graph.getRootNode());
            while (!queue.isEmpty()) {
                RNode current = queue.get(0);
                queue.remove(0);
                seenNodes.add(current);
                if (_forbidden.contains(current.getComposition().toString())) {
                    toReturn = false;
                    break;
                }
                if (_required.contains(current.getComposition().toString())) {
                    seenRequired.add(current.getComposition().toString());
                }
                for (RNode neighbor : current.getNeighbors()) {
                    if (!seenNodes.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            if (toReturn == false) {
                break;
            }
        }

        if (toReturn && _required.size() == seenRequired.size()) {
            return true;
        } else {
            return false;
        }

    }

    //getter for accessing the instructions from RavenServlet
    public String getInstructions() {
        return _instructions;
    }

    public String importClotho(JSONArray toImport) {
        String toReturn = "";
        try {
            for (int i = 0; i < toImport.length(); i++) {
                JSONObject currentPart = toImport.getJSONObject(i);
                if (currentPart.getString("schema").equals("BasicPart")) {
                    Part newBasicPart = Part.generateBasic(currentPart.getString("name"), currentPart.getJSONObject("sequence").getString("sequence"));
                    String type = "gene";
                    if (currentPart.has("type")) {
                        type = currentPart.getString("type");
                    }
                    newBasicPart.addSearchTag("Type: " + type);
                    newBasicPart.setUuid(currentPart.getString("id"));
                    newBasicPart.saveDefault(_collector);
                    newBasicPart.setTransientStatus(false);
                } else if (currentPart.getString("schema").equals("CompositePart")) {
                    JSONArray compositionArray = currentPart.getJSONArray("composition");
                    ArrayList<Part> composition = new ArrayList();
                    ArrayList<String> direction = new ArrayList();
                    for (int j = 0; j < compositionArray.length(); j++) {
                        composition.add(_collector.getPart(compositionArray.getString(j), false));
                        direction.add("+");
                    }
                    Part newComposite = Part.generateComposite(composition, currentPart.getString("name"));
                    newComposite.setUuid(currentPart.getString("id"));
                    newComposite.addSearchTag("Type: composite");
                    newComposite.addSearchTag("Direction: " + direction);
                    newComposite.saveDefault(_collector);
                    newComposite.setTransientStatus(false);
                }


            }
        } catch (JSONException ex) {
            Logger.getLogger(RavenController.class.getName()).log(Level.SEVERE, null, ex);
            return "bad";
        }

        return "good";
    }

    public JSONObject generateStats() throws Exception {
        String statString = "{\"goalParts\":\"" + _statistics.getGoalParts()
                + "\",\"steps\":\"" + _statistics.getSteps()
                + "\",\"stages\":\"" + _statistics.getStages()
                + "\",\"reactions\":\"" + _statistics.getReactions()
                + "\",\"recommended\":\"" + _statistics.getRecommended()
                + "\",\"discouraged\":\"" + _statistics.getDiscouraged()
                + "\",\"efficiency\":\"" + _statistics.getEfficiency()
                + "\",\"sharing\":\"" + _statistics.getSharing()
                + "\",\"time\":\"" + _statistics.getExecutionTime()
                + "\",\"valid\":\"" + _statistics.isValid() + "\"}";
        return new JSONObject(statString);
    }
    //FIELDS
    private HashMap<Part, Vector> _goalParts = new HashMap<Part, Vector>();//key: target part, value: composition
    private HashMap<Part, Vector> _compPartsVectors = new HashMap<Part, Vector>();
    private HashMap<Integer, Double> _efficiency = new HashMap<Integer, Double>();
    private HashSet<String> _required = new HashSet<String>();
    private HashSet<String> _recommended = new HashSet<String>();
    private HashSet<String> _discouraged = new HashSet<String>();
    private HashSet<String> _forbidden = new HashSet<String>();
    private Statistics _statistics = new Statistics();
    private ArrayList<RGraph> _assemblyGraphs = new ArrayList<RGraph>();
    private HashMap<String, ArrayList<String>> forcedOverhangHash = new HashMap<String, ArrayList<String>>();
    private ArrayList<Part> _partLibrary = new ArrayList<Part>();
    private ArrayList<Vector> _vectorLibrary = new ArrayList<Vector>();
    private String _instructions = "";
    protected Collector _collector = new Collector(); //key:user, value: collector assocaited with that user
    private String _path;
    private String _user;
    private String _error = "";
    private boolean _valid = false;
    private ArrayList<String> _databaseConfig = new ArrayList(); //0:database url, 1:database schema, 2:user, 3:password
}
