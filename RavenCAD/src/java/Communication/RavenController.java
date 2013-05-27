/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Communication;

import Controller.accessibility.ClothoReader;
import Controller.algorithms.modasm.SRSGoldenGate;
import Controller.algorithms.modasm.SRSMoClo;
import Controller.algorithms.nonmodasm.SRSBioBricks;
import Controller.algorithms.nonmodasm.SRSCPEC;
import Controller.algorithms.nonmodasm.SRSGibson;
import Controller.algorithms.nonmodasm.SRSSLIC;
import Controller.datastructures.Collector;
import Controller.datastructures.Part;
import Controller.datastructures.SRSGraph;
import Controller.datastructures.SRSNode;
import Controller.datastructures.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Jenhan
 */
public class RavenController {

    public RavenController(String path, String user) {
        _path = path;
        _user = user;
    }

    public ArrayList<SRSGraph> runBioBricks() throws Exception {

        //Run algorithm for BioBricks assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSBioBricks biobricks = new SRSBioBricks();
        ArrayList<SRSGraph> optimalGraphs = biobricks.bioBricksClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for Gibson *
     */
    public ArrayList<SRSGraph> runGibson() throws Exception {

        //Run algorithm for Gibson assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSGibson gibson = new SRSGibson();

        HashMap<Integer, Double> efficiencies = new HashMap<Integer, Double>();
        efficiencies.put(2, 1.0);
        efficiencies.put(3, 1.0);
        efficiencies.put(4, 1.0);
        efficiencies.put(5, 1.0);
        efficiencies.put(6, 1.0);
        efficiencies.put(7, 1.0);
        efficiencies.put(8, 1.0);
        efficiencies.put(9, 1.0);
        efficiencies.put(10, 1.0);
        ArrayList<SRSGraph> optimalGraphs = gibson.gibsonClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false, efficiencies);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for CPEC *
     */
    public ArrayList<SRSGraph> runCPEC() throws Exception {

        //Run algorithm for CPEC assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSCPEC cpec = new SRSCPEC();

        HashMap<Integer, Double> efficiencies = new HashMap<Integer, Double>();
        efficiencies.put(2, 1.0);
        efficiencies.put(3, 1.0);
        efficiencies.put(4, 1.0);

        ArrayList<SRSGraph> optimalGraphs = cpec.cpecClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false, efficiencies);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for SLIC *
     */
    public ArrayList<SRSGraph> runSLIC() throws Exception {

        //Run algorithm for SLIC assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSSLIC slic = new SRSSLIC();

        HashMap<Integer, Double> efficiencies = new HashMap<Integer, Double>();
        efficiencies.put(2, 1.0);
        efficiencies.put(3, 1.0);
        efficiencies.put(4, 1.0);

        ArrayList<SRSGraph> optimalGraphs = slic.slicClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false, efficiencies);
        return optimalGraphs;
    }

    /**
     * Run SRS algorithm for MoClo *
     */
    public ArrayList<SRSGraph> runMoClo() throws Exception {
        if (_goalParts == null) {
            return null;
        }
        //Run algorithm for MoClo assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSMoClo moclo = new SRSMoClo();

        HashMap<Integer, Double> efficiencies = new HashMap<Integer, Double>();
        efficiencies.put(2, 1.0);
        efficiencies.put(3, 1.0);
        efficiencies.put(4, 1.0);
        efficiencies.put(5, 1.0);
        efficiencies.put(6, 1.0);

        moclo.setForcedOverhangs(_collector, forcedOverhangHash);
        ArrayList<SRSGraph> optimalGraphs = moclo.mocloClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, false, efficiencies);

        return optimalGraphs;


    }

    /**
     * Run SRS algorithm for Golden Gate *
     */
    public ArrayList<SRSGraph> runGoldenGate() throws Exception {

        //  Run algorithm for Golden Gate assembly
        _assemblyGraphs.clear();
        ArrayList<Part> gps = new ArrayList();
        gps.addAll(_goalParts.keySet());
        SRSGoldenGate gg = new SRSGoldenGate();

        HashMap<Integer, Double> efficiencies = new HashMap<Integer, Double>();
        efficiencies.put(2, 1.0);
        efficiencies.put(3, 1.0);
        efficiencies.put(4, 1.0);
        efficiencies.put(5, 1.0);
        efficiencies.put(6, 1.0);
        ArrayList<SRSGraph> optimalGraphs = gg.goldenGateClothoWrapper(gps, _vectorLibrary, _required, _recommended, _forbidden, _discouraged, _partLibrary, true, efficiencies);
        return optimalGraphs;

    }

    //returns json array containing all objects in parts list; generates parts list file
    public String generatePartsList(String designNumber) throws Exception {
        File file = new File(_path + _user + "/partsList" + designNumber + ".csv");
        //traverse graphs to get uuids
        ArrayList<Part> usedPartsHash = new ArrayList<Part>();
        ArrayList<Vector> usedVectorsHash = new ArrayList<Vector>();
        for (SRSGraph result : _assemblyGraphs) {
            for (Part p : result.getPartsInGraph(_collector)) {
                if (!usedPartsHash.contains(p)) {
                    usedPartsHash.add(p);
                }
            }
            for (Vector v : result.getVectorsInGraph(_collector)) {
                if (!usedVectorsHash.contains(v)) {
                    usedVectorsHash.add(v);
                }
            }
        }
        //extract information from parts and write file
        String toReturn = "[";
        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write("Name,Sequence,Left Overhang,Right Overhang,Type,Resistance,Level,Composition");

        for (Part p : usedPartsHash) {
            ArrayList<String> tags = p.getSearchTags();
            String RO = "";
            String LO = "";
            String type = "";
            for (int k = 0; k < tags.size(); k++) {
                if (tags.get(k).startsWith("LO:")) {
                    LO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("RO:")) {
                    RO = tags.get(k).substring(4);
                } else if (tags.get(k).startsWith("Type:")) {
                    type = tags.get(k).substring(6);
                }
            }

            if (p.isBasic()) {
                out.write("\n" + p.getName() + "," + p.getSeq() + "," + LO + "," + RO + "," + type);
            } else {
                String composition = "";
                type = "composite";
                for (Part subpart : p.getComposition()) {
                    composition = composition + "," + subpart.getName() + "|" + subpart.getLeftOverhang() + subpart.getRightOverhang();
                }
                out.write("\n" + p.getName() + "," + p.getSeq() + "," + LO + "," + RO + "," + type + ",," + composition);
            }
            toReturn = toReturn
                    + "{\"uuid\":\"" + p.getUUID()
                    + "\",\"Name\":\"" + p.getName()
                    + "\",\"Sequence\":\"" + p.getSeq()
                    + "\",\"LO\":\"" + p.getLeftOverhang()
                    + "\",\"RO\":\"" + p.getRightOverhang()
                    + "\",\"Type\":\"" + p.getType()
                    + "\",\"Composition\":\"" + p.getStringComposition()
                    + "\",\"Resistance\":\"\",\"Level\":\"\"},";
        }

        for (Vector v : usedVectorsHash) {
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
            toReturn = toReturn + "{\"uuid\":\"" + v.getUUID()
                    + "\",\"Name\":\"" + v.getName()
                    + "\",\"Sequence\":\"" + v.getSeq()
                    + "\",\"LO\":\"" + v.getLeftoverhang()
                    + "\",\"RO\":\"" + v.getRightOverhang()
                    + "\",\"Type\":\"vector\",\"Composition\":\"\""
                    + ",\"Resistance\":\"" + v.getResistance()
                    + "\",\"Level\":\"" + v.getLevel() + "\"},";
        }
        out.close();
        toReturn = toReturn.substring(0, toReturn.length() - 1);
        toReturn = toReturn + "]";
        return toReturn;
    }

    public String generateInstructionsFile(String designNumber) throws Exception {
        File file = new File(_path + _user + "/instructions" + designNumber + ".txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write(_instructions);
        out.close();
        return _instructions;
    }

    public void clearData() throws Exception {
        _collector.purge();
        String uploadFilePath = _path + _user + "/";
        File[] filesInDirectory = new File(uploadFilePath).listFiles();
        for (File currentFile : filesInDirectory) {
            currentFile.delete();
        }
    }

    public String fetchData() throws Exception {
        String toReturn = "[";
        ArrayList<Part> allParts = _collector.getAllParts(false);
        for (Part p : allParts) {
            if (!p.isTransient()) {
                toReturn = toReturn
                        + "{\"uuid\":\"" + p.getUUID()
                        + "\",\"Name\":\"" + p.getName()
                        + "\",\"Sequence\":\"" + p.getSeq()
                        + "\",\"LO\":\"" + p.getLeftOverhang()
                        + "\",\"RO\":\"" + p.getRightOverhang()
                        + "\",\"Type\":\"" + p.getType()
                        + "\",\"Composition\":\"" + p.getStringComposition()
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
                        parseInputFile(currentFile);
                    }
                }
            }
        } catch (Exception e) {
            String exceptionAsString = e.getMessage().replaceAll("[\r\n\t]+", "<br/>");
            _error = exceptionAsString;
        }
    }

    public void loadDesign(String designCount) throws Exception {
        _error = "";
        String filePath = _path + _user + "/partsList" + designCount + ".csv";
        File toLoad = new File(filePath);
        parseInputFile(toLoad);
    }

    private void parseInputFile(File input) throws Exception {
        ArrayList<String> badLines = new ArrayList();
        ArrayList<String[]> compositePartTokens = new ArrayList<String[]>();
        if (forcedOverhangHash == null) {
            forcedOverhangHash = new HashMap();
        }
        BufferedReader reader = new BufferedReader(new FileReader(input.getAbsolutePath()));
        String line = reader.readLine();
        line = reader.readLine(); //skip first line
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
            if (tokenCount > 7) {
                // store line for making composite part
                try {
                    String[] trimmedTokens = new String[tokenCount];
                    System.arraycopy(tokens, 0, trimmedTokens, 0, tokenCount);
                    compositePartTokens.add(trimmedTokens);
                } catch (Exception e) {
                    badLines.add(line);
                }
            } else if (tokenCount == 7) {
                //create vector
                try {
                    String name = tokens[0].trim();
                    String sequence = tokens[1].trim();
                    String leftOverhang = tokens[2].trim();
                    String rightOverhang = tokens[3].trim();
                    String resistance = tokens[5].toLowerCase().trim();
                    int level = -1;
                    try {
                        level = Integer.parseInt(tokens[6]);
                    } catch (NumberFormatException e) {
                        level = -1;
                    }
                    Vector newVector = Vector.generateVector(name, sequence);
//                            System.out.println("creating vector: " + name + " resistance: " + resistance + " LO: " + leftOverhang + " RO: " + rightOverhang + " level: " + String.valueOf(level) + " seq: " + sequence);
                    newVector.addSearchTag("LO: " + leftOverhang);
                    newVector.addSearchTag("RO: " + rightOverhang);
                    newVector.addSearchTag("Level: " + level);
                    newVector.addSearchTag("Resistance: " + resistance);
                    newVector.setTransientStatus(false);
                    Boolean toBreak = !newVector.saveDefault(_collector);
                    if (toBreak) {
                        break;
                    }
                } catch (Exception e) {
                    badLines.add(line);
                }
            } else if (tokenCount == 5) {
                try {
                    //create basic part 
                    String name = tokens[0].trim();
                    String sequence = tokens[1].trim();
                    String leftOverhang = tokens[2].trim();
                    String rightOverhang = tokens[3].trim();
                    String type = tokens[4].trim();
                    Part newBasicPart = Part.generateBasic(name, sequence);
                    newBasicPart.addSearchTag("LO: " + leftOverhang);
                    newBasicPart.addSearchTag("RO: " + rightOverhang);
                    newBasicPart.addSearchTag("Type: " + type);
                    Boolean toBreak = !newBasicPart.saveDefault(_collector);
                    newBasicPart.setTransientStatus(false);
                    if (toBreak) {
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
        //create the composite parts
        for (String[] tokens : compositePartTokens) {
            try {
                ArrayList<Part> composition = new ArrayList<Part>();
                for (int i = 7; i < tokens.length; i++) {
                    String partName = tokens[i].trim();
                    if (partName.contains("|")) {
                        String[] partNameTokens = partName.split("\\|");
                        if (forcedOverhangHash.get(tokens[0]) != null) {
                            forcedOverhangHash.get(tokens[0]).add((i - 7) + "|" + partNameTokens[1] + "|" + partNameTokens[2]);
                        } else {
                            ArrayList<String> toAdd = new ArrayList();
                            if (partNameTokens.length > 2) {
                                toAdd.add((i - 7) + "|" + partNameTokens[1] + "|" + partNameTokens[2]);
                                forcedOverhangHash.put(tokens[0], toAdd);
                            }
                        }
                        partName = partNameTokens[0];
                    }
                    composition.add(_collector.getPartByName(partName, true));
                }
                String name = tokens[0].trim();
                String leftOverhang = tokens[2].trim();
                String rightOverhang = tokens[3].trim();

                Part newComposite = Part.generateComposite(composition, name);
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
        if (badLines.size() > 0) {
            //print warning about bad line
            String badLineMessage = "The following lines in your csv input was malformed. \nPlease check you input spreadsheet.";
            for (String bl : badLines) {
                badLineMessage = badLineMessage + "\n" + bl;
            }
            throw new Exception(badLineMessage);

        }
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
    private void solutionStats(String method) throws Exception {

        //Initialize statistics
        boolean overhangValid = false;
        if (method.equals("biobrick")) {
            overhangValid = SRSBioBricks.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("cpec")) {
            overhangValid = SRSCPEC.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("gibson")) {
            overhangValid = SRSGibson.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("golden gate")) {
            overhangValid = SRSGoldenGate.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("moclo")) {
            overhangValid = SRSMoClo.validateOverhangs(_assemblyGraphs);
        } else if (method.equals("slic")) {
            overhangValid = SRSSLIC.validateOverhangs(_assemblyGraphs);
        }
        boolean valid = validateGraphComposition();
        valid = valid && overhangValid;
        SRSGraph firstSoln = new SRSGraph();
        
        int steps = 0;
        int stages = 0;
        int recCnt = 0;
        int disCnt = 0;
        int shr = 0;
        int rxn = 0;
        ArrayList<Double> effArray = new ArrayList<Double>();
        double eff = 0;
        
        if (!_assemblyGraphs.isEmpty()) {
            
            for (SRSGraph graph : _assemblyGraphs) {              
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
            eff = sum/effArray.size();
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
        _statistics.setValid(valid);
    }

    public String run(String designCount, String method, String[] targetIDs, HashSet<String> required, HashSet<String> recommended, HashSet<String> forbidden, HashSet<String> discouraged, String[] vectorLibraryIDs, String[] partLibraryIDs) throws Exception {
        _designCount++;
        _goalParts = new HashMap();
        _required = required;
        _recommended = recommended;
        _forbidden = forbidden;
        _discouraged = discouraged;
        _statistics = new Statistics();
        _vectorLibrary = new ArrayList();
        _partLibrary = new ArrayList();
        _assemblyGraphs = new ArrayList<SRSGraph>();
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
            _goalParts.put(current, ClothoReader.getComposition(current));
        }

        Statistics.start();
        boolean scarless = false;
        if (method.equals("biobrick")) {
            _assemblyGraphs = runBioBricks();
        } else if (method.equals("cpec")) {
            _assemblyGraphs = runCPEC();
            scarless = true;
        } else if (method.equals("gibson")) {
            _assemblyGraphs = runGibson();
            scarless = true;
        } else if (method.equals("golden gate")) {
            _assemblyGraphs = runGoldenGate();
            scarless = true;
        } else if (method.equals("moclo")) {
            _assemblyGraphs = runMoClo();
        } else if (method.equals("slic")) {
            _assemblyGraphs = runSLIC();
            scarless = true;
        }

        Statistics.stop();
        ClothoReader reader = new ClothoReader();
        ArrayList<String> graphTextFiles = new ArrayList();
        _assemblyGraphs = SRSGraph.mergeGraphs(_assemblyGraphs);
        SRSGraph.getGraphStats(_assemblyGraphs, _partLibrary, _vectorLibrary, _goalParts, _recommended, _discouraged, scarless);
        solutionStats(method);

        if (!_assemblyGraphs.isEmpty()) {
            for (SRSGraph result : _assemblyGraphs) {
                reader.nodesToClothoPartsVectors(_collector, result);
                reader.fixCompositeUUIDs(_collector, result);
                boolean canPigeon = result.canPigeon();
                ArrayList<String> postOrderEdges = result.getPostOrderEdges();
                graphTextFiles.add(result.generateWeyekinFile(_collector, postOrderEdges, canPigeon));
            }
        }
        String mergedGraphText = SRSGraph.mergeWeyekinFiles(graphTextFiles);

        //save graph text file
        File file = new File(_path + _user + "/pigeon" + designCount + ".txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fw);
        out.write(mergedGraphText);
        out.close();
        WeyekinPoster.setDotText(mergedGraphText);
        WeyekinPoster.postMyVision();


        String toReturn;
        toReturn = WeyekinPoster.getmGraphVizURI().toString();
        return toReturn;
    }

    private boolean validateGraphComposition() throws Exception {
        boolean toReturn = true;
        HashSet<String> seenRequired = new HashSet();
        for (SRSGraph graph : _assemblyGraphs) {
            ArrayList<SRSNode> queue = new ArrayList();
            HashSet<SRSNode> seenNodes = new HashSet();
            queue.add(graph.getRootNode());
            while (!queue.isEmpty()) {
                SRSNode current = queue.get(0);
                queue.remove(0);
                seenNodes.add(current);
                if (_forbidden.contains(current.getComposition().toString())) {
                    toReturn = false;
                    break;
                }
                if (_required.contains(current.getComposition().toString())) {
                    seenRequired.add(current.getComposition().toString());
                }
                for (SRSNode neighbor : current.getNeighbors()) {
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

    public String generateStats() throws Exception {
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
        return statString;
    }
    public HashMap<Part, ArrayList<Part>> _goalParts = new HashMap();//key: target part, value: composition
    public HashSet<String> _required = new HashSet();
    public HashSet<String> _recommended = new HashSet();
    public HashSet<String> _discouraged = new HashSet();
    public HashSet<String> _forbidden = new HashSet();
    public Statistics _statistics = new Statistics();
    public ArrayList<SRSGraph> _assemblyGraphs = new ArrayList<SRSGraph>();
    public HashMap<String, ArrayList<String>> forcedOverhangHash = new HashMap();
    public ArrayList<Part> _partLibrary = new ArrayList();
    public ArrayList<Vector> _vectorLibrary = new ArrayList();
    public String _instructions = "";
    public Collector _collector = new Collector(); //key:user, value: collector assocaited with that user
    public String _path;
    public String _user;
    public String _error = "";
    public int _designCount = 0;
}