$(document).ready(function() { //don't run javascript until page is loaded
    var _designCount = 0;
    var _loaded = false;
    var _data = null;
    var _method = "BioBricks";
    var uuidCompositionHash = {}; //really just a json object...key: uuid, value: string composition
    var canRun = true;
    var efficiencyArray = [];
    var _runParameters = {};
    var _destination = ""; //stores the url a user was navigating towards
    var currentActiveTab = 0;
    var _vectorStage = 0;
    /********************EVENT HANDLERS********************/

    //initialize tooltips
    $("[data-toggle=tooltip]").tooltip();
    //prompt dialog when navigating away from design page with unsaved parts
    $('a.losePartLink').click(function(event) {
        var notSaved = $('a[val="notSaved"]');
        if (notSaved.length > 0) {
            event.preventDefault();
            _destination = $(this).attr("href");
            $('#navigateModal').modal();
        }
    });
    $('button#discardModalButton').click(function() {
        window.location.replace(_destination);
    });

    //event handler for modal save all button
    $('button#saveAllModalButton').click(function() {
        //save all parts for each unsaved design
        $('a[val="notSaved"]').each(function() {
            var designNumber = $(this).attr("name");
            $('#saveButton' + designNumber).click();
        });
        $('#navigateModal div p').text("Parts successfully saved!");
        setTimeout(function() {
            window.location.replace(_destination);
        }, 2000);

    });
    //update summary when efficiencies are changed
    $('.input-mini').keyup(function() {
        updateSummary();
    });
    $('button#requireButton').click(function() {
        var toRequire = $.trim($("#intermediatesTypeAhead").val());
        $("#intermediatesTypeAhead").val("");
        var compositionRegExp = /^\[{1}.+\]$/;
        if (compositionRegExp.test(toRequire)) {
            //TODO do more validation on toRequire to make sure it's valid
            //remove toRequire from other intermediates lists
            $('#selectedIntermediates div div div ul#forbiddenList li:contains("' + toRequire + '")').remove();
            $('#selectedIntermediates div div div ul#discouragedList li:contains("' + toRequire + '")').remove();
            $('#selectedIntermediates div div div ul#recommendedList li:contains("' + toRequire + '")').remove();
            if ($('#selectedIntermediates div div div ul#requiredList li:contains("' + toRequire + '")').length === 0) {
                $('#selectedIntermediates div div div ul#requiredList').append('<li><span class="label">' + toRequire + '<a class="close pull-right"><i class="icon-remove-circle"></i></a></span></li>');
                $('a.close').on("click", function() {
                    $(this).parent().remove();
                });
            }
        }
    });
    $('button#forbidButton').click(function() {
        var toForbid = $.trim($("#intermediatesTypeAhead").val());
        $("#intermediatesTypeAhead").val("");
        var compositionRegExp = /^\[{1}.+\]$/;
        if (compositionRegExp.test(toForbid)) {
            //TODO do more validation on toRequire to make sure it's valid
            //remove toRequire from other intermediates lists
            $('#selectedIntermediates div div div ul#requiredList li:contains("' + toForbid + '")').remove();
            $('#selectedIntermediates div div div ul#discouragedList li:contains("' + toForbid + '")').remove();
            $('#selectedIntermediates div div div ul#recommendedList li:contains("' + toForbid + '")').remove();
            if ($('#selectedIntermediates div div div ul#forbiddenList li:contains("' + toForbid + '")').length === 0) {
                $('#selectedIntermediates div div div ul#forbiddenList').append('<li><span class="label">' + toForbid + '<a class="close pull-right"><i class="icon-remove-circle"></i></a></span></li>');
                $('a.close').on("click", function() {
                    $(this).parent().remove();
                });
            }
        }
    });
    $('button#recommendButton').click(function() {
        var toRecommend = $.trim($("#intermediatesTypeAhead").val());
        $("#intermediatesTypeAhead").val("");
        var compositionRegExp = /^\[{1}.+\]$/;
        if (compositionRegExp.test(toRecommend)) {
            //TODO do more validation on toRequire to make sure it's valid
            //remove toRequire from other intermediates lists
            $('#selectedIntermediates div div div ul#forbiddenList li:contains("' + toRecommend + '")').remove();
            $('#selectedIntermediates div div div ul#discouragedList li:contains("' + toRecommend + '")').remove();
            $('#selectedIntermediates div div div ul#requiredList li:contains("' + toRecommend + '")').remove();
            if ($('#selectedIntermediates div div div ul#recommendedList li:contains("' + toRecommend + '")').length === 0) {
                $('#selectedIntermediates div div div ul#recommendedList').append('<li><span class="label">' + toRecommend + '<a class="close pull-right"><i class="icon-remove-circle"></i></a></span></li>');
                $('a.close').on("click", function() {
                    $(this).parent().remove();
                });
            }
        }
    });
    $('button#discourageButton').click(function() {
        var toDiscourage = $.trim($("#intermediatesTypeAhead").val());
        $("#intermediatesTypeAhead").val("");
        var compositionRegExp = /^\[{1}.+\]$/;
        if (compositionRegExp.test(toDiscourage)) {
            //TODO do more validation on toRequire to make sure it's valid
            //remove toRequire from other intermediates lists
            $('#selectedIntermediates div div div ul#forbiddenList li:contains("' + toDiscourage + '")').remove();
            $('#selectedIntermediates div div div ul#requiredList li:contains("' + toDiscourage + '")').remove();
            $('#selectedIntermediates div div div ul#recommendedList li:contains("' + toDiscourage + '")').remove();
            if ($('#selectedIntermediates div div div ul#discouragedList li:contains("' + toDiscourage + '")').length === 0) {
                $('#selectedIntermediates div div div ul#discouragedList').append('<li><span class="label">' + toDiscourage + '<a class="close pull-right"><i class="icon-remove-circle"></i></a></span></li>');
                $('a.close').on("click", function() {
                    $(this).parent().remove();
                });
            }
        }
    });


    $('#sidebar').click(function() {
        $('#designTabHeader a:first').tab('show');
    });
    $('#designTabHeader a:first').click(function() {
        refreshData();
    });
    $('#methodTabHeader li').click(function() {
        _method = $(this).text();
        updateSummary();
    });
    $('.addEfficiencyButton').click(function() {
        var table = $('#methodTabs div.active table');
        table.find('tbody').append('<tr><td>' + (table.find('tr').length + 1) + '</td><td><input class="input-mini" placeholder="1.0"/></td></tr>');
    });
    $('.minusEfficiencyButton').click(function() {
        if ($('#methodTabs div.active table tbody tr').length > 1) {
            $('#methodTabs div.active table tbody tr').last().remove();
        }
    });
    $('.addVectorStageButton').click(function() {
        var table = $('#vectorTable tbody');
        _vectorStage = _vectorStage + 1;
        var vectorOptions = '<select>';
        $.each(_data["result"], function() {
            if (this["Type"] === "vector") {
                vectorOptions = vectorOptions + '<option id="' + this["uuid"] + '">' + this["Name"] + ' - ' + this["Resistance"] + '</option>';
            }
        });

        vectorOptions = vectorOptions + '</select>';
        table.append('<tr><td>' + 'Every (x)n + ' + _vectorStage + '</td><td>' + vectorOptions + '</td></tr>');
    });
    $('.removeVectorStageButton').click(function() {
        if ($('#vectorTable tbody tr').length > 1) {
            $('#vectorTable tbody tr').last().remove();
            _vectorStage = _vectorStage - 1;
        }
    });

    //target part button event handlers
    $('#targetSelectAllButton').click(function() {
        $("#availableTargetPartList option").each(function() {
            $("#availableLibraryPartList #" + $(this).attr("id")).remove();
            $("#libraryPartList #" + $(this).attr("id")).remove();
            //temporary
//            $('#availableLibraryPartList option[title="' + $(this).attr("title")+'"').remove();
//            $('#libraryPartList option[title="' + $(this).attr("title")+'"').remove();
        });
        $('#targetPartList').append($('#availableTargetPartList option'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#targetDeselectAllButton').click(function() {
        $("#targetPartList option").each(function() {
//            $("#availableLibraryPartList").append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
//            $("#availableLibraryPartList #" + $(this).attr("id")).addClass("composite");
            $("#libraryPartList").append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
            $("#libraryPartList #" + $(this).attr("id")).addClass("composite");
        });
        $('#availableTargetPartList').append($('#targetPartList option'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#targetSelectButton').click(function() {
        $('#availableTargetPartList :selected').each(function() {
            $('#availableLibraryPartList #' + $(this).attr("id")).remove();
            $('#libraryPartList #' + $(this).attr("id")).remove();
            //temporary solution
//            $('#availableLibraryPartList option[title="' + $(this).attr("title")+'"').remove();
//            $('#libraryPartList option[title="' + $(this).attr("title")+'"').remove();
        });
        $('#targetPartList').append($('#availableTargetPartList :selected'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#targetDeselectButton').click(function() {
        $('#targetPartList :selected').each(function() {
//            $('#availableLibraryPartList').append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
//            $("#availableLibraryPartList #" + $(this).attr("id")).addClass("composite");
            $('#libraryPartList').append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
            $("#libraryPartList #" + $(this).attr("id")).addClass("composite");
        });
        $('#availableTargetPartList').append($('#targetPartList :selected'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    //library part button event handlers
    $('#libraryPartSelectAllButton').click(function() {
        $('#availableLibraryPartList option').each(function() {
            $('#availableTargetPartList #' + $(this).attr("id")).remove();
            $('#targetPartList #' + $(this).attr("id")).remove();
        });
        $('#libraryPartList').append($('#availableLibraryPartList option'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#libraryPartDeselectAllButton').click(function() {
        $('#libraryPartList option.composite').each(function() {
            $('#availableTargetPartList').append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
        });
        $('#availableLibraryPartList').append($('#libraryPartList option'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#libraryPartSelectButton').click(function() {
        $('#availableLibraryPartList :selected').each(function() {
            $('#availableTargetPartList #' + $(this).attr("id")).remove();
            $('#targetPartList #' + $(this).attr("id")).remove();
        });
        $('#libraryPartList').append($('#availableLibraryPartList :selected'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#libraryPartDeselectButton').click(function() {
        $('#libraryPartList :selected.composite').each(function() {
            $('#availableTargetPartList').append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
        });
        $('#availableLibraryPartList').append($('#libraryPartList :selected'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#libraryVectorSelectAllButton').click(function() {
        $('#libraryVectorList').append($('#availableLibraryVectorList option'));
        sortVectorLists();
    });
    $('#libraryVectorDeselectAllButton').click(function() {
        $('#availableLibraryVectorList').append($('#libraryVectorList option'));
        sortVectorLists();
    });
    $('#libraryVectorSelectButton').click(function() {
        $('#libraryVectorList').append($('#availableLibraryVectorList :selected'));
        sortVectorLists();
    });
    $('#libraryVectorDeselectButton').click(function() {
        $('#availableLibraryVectorList').append($('#libraryVectorList :selected'));
        sortVectorLists();
    });
    $('#resetIntermediatesButton').click(function() {
        refreshIntermediatesTypeAhead();
    });
    $('.btn').click(function() {
        updateSummary();
    });
    $('#runButton').click(function() {
        if (canRun) {
            var targets = ""; //goal parts
            $('#targetPartList option').each(function() {
                targets = targets + $(this).attr("id") + ",";
            });
            if (targets.length > 1) {
                canRun = false; //can only run one design at once
                var currentDesignCount = addDesignTab();
                currentActiveTab = currentDesignCount;

                var partLibrary = ""; //parts to use in library
                var vectorLibrary = ""; //vectors to use in library
                var rec = ""; //recommended intermediates
                var req = ""; //required intermediates
                var forbid = ""; //forbidden intermediates
                var discourage = ""; //discouraged intermediates
                var eug = ""; //eugene file for rec/required forbidden
                var config = ""; //csv configuration file?
                $('#libraryPartList option').each(function() {
                    partLibrary = partLibrary + $(this).attr("id") + ",";
                });
                $('#libraryVectorList option').each(function() {
                    vectorLibrary = vectorLibrary + $(this).attr("id") + ",";
                });
                $('#selectedIntermediates div div div ul#requiredList li').each(function() {
                    req = req + $(this).text() + ";";
                });
                $('#selectedIntermediates div div div ul#recommendedList li').each(function() {
                    rec = rec + $(this).text() + ";";
                });
                $('#selectedIntermediates div div div ul#forbiddenList li').each(function() {
                    forbid = forbid + $(this).text() + ";";
                });
                $('#selectedIntermediates div div div ul#discouragedList li').each(function() {
                    discourage = discourage + $(this).text() + ";";
                });
                $('#selectedIntermediates div div div ul').html("");
                discourage = discourage.substring(0, discourage.length - 1);
                forbid = forbid.substring(0, forbid.length - 1);
                req = req.substring(0, req.length - 1);
                rec = rec.substring(0, rec.length - 1);
                targets = targets.substring(0, targets.length - 1);
                partLibrary = partLibrary.substring(0, partLibrary.length - 1);
                _method = _method.replace(/\s+/g, '');

                //Primer parameters
                var oligoNameRoot = $('input#oligoNameRoot').val();
                var meltingTemperature = $('input#meltingTemperature').val();
                var targetHomologyLength = $('input#targetHomologyLength').val();
                var minPCRLength = $('input#minPCRLength').val();
                var minCloneLength = $('input#minCloneLength').val();
                var maxPrimerLength = $('input#maxPrimerLength').val();

                //If primer parameters are not filled in, use defaults
                if (oligoNameRoot === "") {
                    oligoNameRoot = $('input#oligoNameRoot').attr("placeholder");
                }
                if (meltingTemperature === "") {
                    meltingTemperature = $('input#meltingTemperature').attr("placeholder");
                }
                if (targetHomologyLength === "") {
                    targetHomologyLength = $('input#targetHomologyLength').attr("placeholder");
                }
                if (minPCRLength === "") {
                    minPCRLength = $('input#minPCRLength').attr("placeholder");
                }
                if (minCloneLength === "") {
                    minCloneLength = $('input#minCloneLength').attr("placeholder");
                }
                if (maxPrimerLength === "") {
                    maxPrimerLength = $('input#maxPrimerLength').attr("placeholder");
                }

                //vector_uuid - stage pairs
                var stageVectorArray = [];
                $('table#vectorTable tbody tr').each(function() {
                    var stage = $(this).children('td:first').text();
                    var vectoruuid = $(this).find('select option:selected').attr("id");
                    stageVectorArray.push(vectoruuid);
                });
                var requestInput = {command: "run", designCount: "" + currentDesignCount, targets: "" + targets, method: ""
                            + _method, partLibrary: "" + partLibrary, vectorLibrary: "" + vectorLibrary, recommended: ""
                            + rec, required: "" + req, forbidden: "" + forbid, discouraged: "" + discourage,
                    "stageVectors": "" + stageVectorArray,
                    efficiency: "" + efficiencyArray,
                    "primer": JSON.stringify({oligoNameRoot: oligoNameRoot, meltingTemperature: meltingTemperature, targetHomologyLength: targetHomologyLength, minPCRLength: minPCRLength, minCloneLength : minCloneLength, maxPrimerLength : maxPrimerLength})};
                _runParameters[currentDesignCount] = requestInput;
                $.get("RavenServlet", requestInput, function(data) {
                    interpretDesignResult(currentDesignCount, data);
                    canRun = true;
                });
            } else {
                $('#selectTargetModal').modal();
            }
        } else {
            $('#waitModal').modal();
        }
    });
    /********************FUNCTIONS/********************/
    var refreshData = function() {
        $.get("RavenServlet", {"command": "dataStatus"}, function(data) {
            if (data === "loaded") {
                _loaded = true;
                getData();
            } else {
                _loaded = false;
                //TODO add some sort of popup as a guiding hint
            }
        });
    };
//draw target part options list
    var drawPartVectorLists = function() {
        var targetListBody = '<select id="availableTargetPartList" multiple="multiple" class="fixedHeight">';
        var libraryPartListBody = '<select id="libraryPartList" multiple="multiple" class="fixedHeight">';
        var libraryVectorListBody = '<select id="libraryVectorList" multiple="multiple" class="fixedHeight">';
        $.each(_data["result"], function() {
            //second part guarantees that only composite parts are shown
//            if (this["Type"] === "plasmid" && this["Composition"].split(",").length > 1) {
            if (this["Type"] === "plasmid" && this["Composition"].split(",").length > 1) {
                targetListBody = targetListBody + '<option title="' + this["Composition"] + '|' + this["LO"] + '|' + this["RO"] + '" class="composite ui-state-default" id="' + this["uuid"] + '">' + this["Name"] + '</option>';
            } else if (this["Type"] === "vector") {
                libraryVectorListBody = libraryVectorListBody + '<option title="' + this["Name"] + '|' + this["LO"] + '|' + this["RO"] + '" class="vector ui-state-default" id="' + this["uuid"] + '">' + this["Name"] + '</option>';
            } else if (this["Type"] === "destination vector") {
                libraryVectorListBody = libraryVectorListBody + '<option title="' + this["Name"] + '|' + this["LO"] + '|' + this["RO"] + '" class="vector ui-state-default" id="' + this["uuid"] + '">' + this["Name"] + '</option>';
            } else if (this["Type"] === "composite") {
                libraryPartListBody = libraryPartListBody + '<option title="' + this["Composition"] + '|' + this["LO"] + '|' + this["RO"] + '" class="composite ui-state-default" id="' + this["uuid"] + '">' + this["Name"] + '</option>';
            } else {
                libraryPartListBody = libraryPartListBody + '<option title="' + this["Composition"] + '|' + this["LO"] + '|' + this["RO"] + '" class="basic ui-state-default" id="' + this["uuid"] + '">' + this["Name"] + '</option>';
            }

        });
        targetListBody = targetListBody + '</select>';
        libraryVectorListBody = libraryVectorListBody + '</select>';
        libraryPartListBody = libraryPartListBody + '</select>';
        $("#availableTargetPartListArea").html(targetListBody);
        $("#libraryPartListArea").html(libraryPartListBody);
        $("#libraryVectorListArea").html(libraryVectorListBody);
        //clear lists
        $('#targetPartList').html("");
        $('#availableLibraryPartList').html("");
        $('#availableLibraryVectorList').html("");
        sortPartLists();
        sortVectorLists();
    };
    var getData = function() {
        $.getJSON("RavenServlet", {"command": "fetch"}, function(json) {
            _data = json;
            drawPartVectorLists();
            if (json["params"] !== "none") {
                //load preconfigured params
                interpretParams(json["params"]);
            }

            //generate uuidCompositionHash
            $.each(_data["result"], function() {
                if (this["Type"].toLowerCase() !== "vector") {
                    uuidCompositionHash[this["uuid"]] = this["Composition"];
                }
            });
        });
    };
    refreshData();

    //generates all intermediates for input composition
    var generateIntermediates = function(composition) {
        var toSplit = composition.substring(1, composition.length - 1);
        var toReturn = [];
        var compositionArray = toSplit.split(",");
        var seenIntermediates = {};
        for (var start = 0; start < compositionArray.length; start++) {
            for (var end = start + 1; end < compositionArray.length + 1; end++) {
                var intermediate = compositionArray.slice(start, end);
                var name = "";
                if (intermediate.length > 1) {
                    for (var i = 0; i < intermediate.length; i++) {
                        name = name + intermediate[i] + ",";
                    }
                    name = "[" + name.substring(0, name.length - 1).trim() + "]";
                    if (name !== composition) {
                        if (seenIntermediates[name] !== "seen") {
                            seenIntermediates[name] = "seen";
                            toReturn.push(name);
                        }
                    }
                }
            }
        }
        return toReturn;
    };

    //generates typeahead items for input composition
    var generateIntermediateTokens = function(composition) {
        var toSplit = composition.substring(1, composition.length - 1);
        toSplit = toSplit.replace(/\|[^,\|]+\|[^\|,]+\|/g, "|");
        var toReturn = [];
        var compositionArray = toSplit.split(",");
        var seenIntermediates = {};
        for (var start = 0; start < compositionArray.length; start++) {
            for (var end = start + 1; end < compositionArray.length + 1; end++) {
                var intermediate = compositionArray.slice(start, end);
                var name = "";
                if (intermediate.length > 1) {
                    for (var i = 0; i < intermediate.length; i++) {
                        name = name + intermediate[i] + ",";
                    }
                    name = "[" + name.substring(0, name.length - 1).trim() + "]";
                    if (name !== composition) {
                        if (seenIntermediates[name] !== "seen") {
                            seenIntermediates[name] = "seen";
                            toReturn.push(name);
                        }
                    }
                }
            }
        }
        return toReturn;
    };

    var refreshIntermediatesTypeAhead = function() {
        var targets = "";
        var source = [];
        $("#targetPartList option").each(function() {
            targets = targets + "\n" + uuidCompositionHash[$(this).attr("id")];
            var intermediates = generateIntermediateTokens(uuidCompositionHash[$(this).attr("id")]);
            source = source.concat(intermediates);
        });
        var typeahead = $('#intermediatesTypeAhead').data('typeahead');
        if (typeahead)
            typeahead.source = source;
        else
            $('#intermediatesTypeAhead').typeahead({source: source});
    };

    var drawIntermediates = function() {
        var targets = "";
        var tableBody = "<table id='intermediateTable' class='table table-bordered table-hover'><thead>"
                + "<tr><th>Composition</th><th>Recommended</th><th>Required</th><th>Forbidden</th><th>Discouraged</th></tr></thead><tbody>";
        var seen = {};
        $("#targetPartList option").each(function() {
            targets = targets + "\n" + uuidCompositionHash[$(this).attr("id")];
            var intermediates = generateIntermediates(uuidCompositionHash[$(this).attr("id")]);
            $.each(intermediates, function() {
                if (seen[this] !== "seen") {
                    tableBody = tableBody + '<tr><td>' + this + '<td><input class="recommended" type="checkbox" value="' + this
                            + '"></td><td><input class="required" type="checkbox" value="' + this
                            + '"></td><td><input class="forbidden" type="checkbox" value="' + this
                            + '"></td><td><input class="discouraged" type="checkbox" value="' + this
                            + '"></td></tr>';
                    seen[this] = "seen";
                }
            });
        });
        seen = null;
        tableBody = tableBody + '</tbody>';
        $("#intermediateTable").dataTable({
            "sScrollY": "300px",
            "bPaginate": false,
            "bScrollCollapse": true
        });

    };
    var updateSummary = function() {
        var pattern = /^[\d]+\.*[\d]+/;
        var summary = "<p>You're trying to assemble</p>";
        if ($('#targetPartList option').length > 0) {
            summary = summary + '<ul id="targets" style="max-height:150px;overflow:auto">';
            $('#targetPartList option').each(function() {
                summary = summary + '<li>' + $(this).text() + '</li>';
            });
            summary = summary + "</ul>";
        } else {
            summary = summary + '<div class="alert alert-danger"><strong>Nothing</strong>. Try selecting some target parts</div>';
        }
        var tabMethod = _method.toLowerCase().replace(/\s+/g, '');
//        var tabMethod = _method;
        summary = summary + '<p>You will be using the <strong>' + _method + '</strong> assembly method</p>';
        summary = summary + '<p>You will be using the following efficiency table for your assembly</p>' + $('#' + tabMethod + 'Tab table').parent().html();
        efficiencyArray = [];
        var table = $('#' + tabMethod + 'Tab table').parent();
        table.find('input').each(function() {
            if (pattern.test($(this).val())) {
                efficiencyArray.push($(this).val());
            } else {
                efficiencyArray.push('1.0');
            }

        });
        var recommended = $('#selectedIntermediates div div div ul#recommendedList li');
        summary = summary + '<p>The following intermediates are recommended:</p>';
        summary = summary + '<ul class="recommendedList" style="max-height:150px;overflow:auto">';
        recommended.each(function() {
            summary = summary + '<li>' + $(this).text() + '</li>';
        });
        summary = summary + '</ul>';

        var required = $('#selectedIntermediates div div div ul#requiredList li');
        summary = summary + '<p>The following intermediates are required:</p>';
        summary = summary + '<ul class="requiredList" style="max-height:150px;overflow:auto">';
        required.each(function() {
            summary = summary + '<li>' + $(this).text() + '</li>';
        });
        summary = summary + '</ul>';

        var forbidden = $('#selectedIntermediates div div div ul#forbiddenList li');
        summary = summary + '<p>The following intermediates are forbidden:</p>';
        summary = summary + '<ul class="forbiddenList" style="max-height:150px;overflow:auto">';
        forbidden.each(function() {
            summary = summary + '<li>' + $(this).text() + '</li>';
        });
        summary = summary + '</ul>';

        var discouraged = $('#selectedIntermediates div div div ul#discouragedList li');
        summary = summary + '<p>The following intermediates are discouraged:</p>';
        summary = summary + '<ul class="discouragedList">';
        discouraged.each(function() {
            summary = summary + '<li>' + $(this).text() + '</li>';
        });
        summary = summary + '</ul>';

        summary = summary + '<p>Your library includes the following parts:</p>';
        summary = summary + '<ul class="libraryList" style="max-height:150px;overflow:auto">';
        $('#libraryPartList option').each(function() {
            summary = summary + '<li>' + $(this).val() + "</li>";
        });
        summary = summary + "</ul>";



        $('#designSummaryArea').html(summary);
        $('#designSummaryArea table').css("width", "50%");
        var i = 0;
        $('#designSummaryArea table input').each(function() {
            $(this).parent().html(efficiencyArray[i]);
            i++;
        });
    };

    $("#intermediateTable").dataTable({
        "sScrollY": "300px",
        "bPaginate": false,
        "bScrollCollapse": true
    });

    function partComparator(a, b) {
        if (a.hasClass("composite") && !b.hasClass("composite")) {
            return -1;
        } else {
            if (b.hasClass("composite") && !a.hasClass("composite")) {
                return 1;
            }
            if (a.text() > b.text()) {
                return 1;
            } else {
                return -1;
            }
            return 0;
        }
    }

    function sortPartLists() {
        var items = [];
        //sort part lists
        $('#availableTargetPartList option').each(function() {
            items.push($(this));
        });
        items.sort(partComparator);
        $('#availableTargetPartList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#availableTargetPartList').append(items[i]);
        }
        items = [];
        $('#targetPartList option').each(function() {
            items.push($(this));
        });
        items.sort(partComparator);
        $('#targetPartList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#targetPartList').append(items[i]);
        }
        items = [];
        $('#availableLibraryPartList option').each(function() {
            items.push($(this));
        });
        items.sort(partComparator);
        $('#availableLibraryPartList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#availableLibraryPartList').append(items[i]);
        }
        items = [];
        $('#libraryPartList option').each(function() {
            items.push($(this));
        });
        items.sort(partComparator);
        $('#libraryPartList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#libraryPartList').append(items[i]);
        }
    }

    function sortVectorLists() {
        //sort vector lists
        var items = [];
        $('#availableLibraryVectorList option').each(function() {
            items.push($(this));
        });
        items.sort();
        $('#availableLibraryVectorList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#availableLibraryVectorList').append(items[i]);
        }
        items = [];
        $('#libraryVectorList option').each(function() {
            items.push($(this));
        });
        items.sort();
        $('#libraryVectorList').html("");
        for (var i = 0; i < items.length; i++) {
            $('#libraryVectorList').append(items[i]);
        }
    }


    var addDesignTab = function() {
        _designCount = _designCount + 1;
        $('#designTabHeader').append('<li><a id="designTabHeader_' + _designCount + '" href="#designTab' + _designCount + '" data-toggle="tab">Design ' + _designCount +
                '</a></li>');
        $('#designTabContent').append('<div class="tab-pane" id="designTab' + _designCount + '">');
        $('#designTabHeader a:last').tab('show');

        //generate main skeleton
        $('#designTab' + _designCount).append('<div class="row-fluid"><div class="span12"><div class="tabbable" id="resultTabs' + _designCount +
                '"></div></div></div>' +
                '<div class="row-fluid"><div class="span8"><div class="well" id="stat' + _designCount +
                '"><h4>Assembly Statistics</h4></div></div><div class="span4"><div class="well" id="download' + _designCount + '"></div></div></div>');
        //add menu
        $('#resultTabs' + _designCount).append('<ul id="resultTabsHeader' + _designCount + '" class="nav nav-tabs">' +
                '<li class="active"><a href="#imageTab_' + _designCount + '" data-toggle="tab" id="imageTabHeader_' + _designCount + '">Image</a></li>' +
                '<li><a class="nonImageDesignTabHeader_' + _designCount + '" name="' + _designCount + '" href="#instructionTab' + _designCount + '" data-toggle="tab">Instructions</a></li>' +
                '<li><a class="nonImageDesignTabHeader_' + _designCount + '" name="' + _designCount + '" href="#partsListTab' + _designCount + '" data-toggle="tab">New Parts/Vectors</a></li>' +
                '<li><a class="nonImageDesignTabHeader_' + _designCount + '" name="' + _designCount + '" href="#summaryTab' + _designCount + '" data-toggle="tab">Summary</a></li>' +
                '<li><a href="#discardDialog' + _designCount + '" class="btn" role="button" val="notSaved" id="discardButton' + _designCount + '" name="' + _designCount + '">Discard Design</a></li>' +
                '<li><a class="btn" id="redesignButton' + _designCount + '" name="' + _designCount + '">Mark Failures/Successes</a></li>' +
                '</ul>');
        //append modal dialog
        $('#resultTabs' + _designCount).append('<div id="discardDialog' + _designCount + '" class="modal hide fade" tab-index="-1" role="dialog" aria-labelledby="discardDialogLabel' + _designCount + '" aria-hidden="true">'
                + '<div class="modal-header">'
                + '<h4 id="discardDialogLabel' + _designCount + '">Save Parts?</h4></div>'
                + '<div class="modal-body">There are parts in this design that have not been saved. Do you want to save them?</div>'
                + '<div class="modal-footer">'
                + '<button class="btn btn-danger" data-dismiss="modal" aria-hidden="true" id="modalDiscardButton' + _designCount + '" val="' + _designCount + '">Discard Parts</button>'
                + '<button class="btn btn-success" data-dismiss="modal" aria-hidden="true" id="modalSaveButton' + _designCount + '" val="' + _designCount + '">Save</button>'
                + '<button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>'
                + "</div></div>"
                );
        $('#resultTabs' + _designCount).append(
                '<div class="tab-content" id="resultTabsContent' + _designCount + '">' +
                '<div class="tab-pane active" id="imageTab' + _designCount + '"><div class="well" id="resultImage' + _designCount + '">Please wait while Raven generates your image<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="instructionTab' + _designCount + '"><div class="well" id="instructionArea' + _designCount + '" style="height:360px;overflow:auto">Please wait while Raven generates instructions for your assembly<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="partsListTab' + _designCount + '"><div id="partsListArea' + _designCount + '" style="overflow:visible">Please wait while Raven generates the parts for your assembly<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="summaryTab' + _designCount + '"><div class="well" id="summaryArea' + _designCount + '" style="height:360px;overflow:auto">' + $('#designSummaryArea').html() + '</div></div>' +
                '</div>');
        //add download buttons and bind events to them
        $('#download' + _designCount).append('<h4>Download Options</a></h4>' +
                '<p><a target="_blank" id="downloadImage' + _designCount + '">Download Graph Image</p>' +
                '<p><a target="_blank" id="downloadInstructions' + _designCount + '">Download Instructions File</a></p>' +
                '<p><a target="_blank" id="downloadParts' + _designCount + '">Download Raven File</a></p>' +
                '<p><a target="_blank" id="downloadPigeon' + _designCount + '">Download Pigeon File</a></p>' +
                '<p><a target="_blank" id="downloadArcs' + _designCount + '">Download Robot Instructions File</a></p>' +
                '<p><a target="_blank" id="downloadConfig' + _designCount + '">Download Configured Raven File</a></p>'

                );
        $('#imageTabHeader_' + _designCount).click(function() {
            $('#resultTabsContent' + _designCount).children('.active').removeClass('active');
            $('#resultTabsContent' + _designCount).children('.active').removeClass('active');
            $('#imageTab' + _designCount).addClass('active');
        })
        //deal with image container
        $('#designTabHeader li a#designTabHeader_' + _designCount).click(function() {
            var id = $(this).attr("id").toString();
            var tabNumber = id.substring(id.indexOf("_") + 1);
            changeImage(tabNumber);
        });
        $('.nonImageDesignTabHeader_' + _designCount).click(function() {
            var tabNumber = $(this).attr("name").toString();
            hideImage(tabNumber);
        });
        $('#imageTabHeader_' + _designCount).click(function() {
            var id = $(this).attr("id").toString();
            var tabNumber = id.substring(id.indexOf("_") + 1);
            showImage(tabNumber);
            //manually switch tabs
            $('#resultsTabContent' + tabNumber + ' div.active').removeClass('active');
            $('#imageTab' + tabNumber).addClass('active');
        });
        //event handler for discard modal dialog
        $('#discardButton' + _designCount).click(function() {
            var designNumber = $(this).attr("name");
            if ($(this).attr("val") === "notSaved") {
                $('#discardDialog' + designNumber).modal('show');
            } else {
                $('#discardDialog' + designNumber).modal('hide');
                $('#designTabHeader' + designNumber).remove();
                $('#designTab' + designNumber).remove();
                $('#designTabHeader a:first').tab('show');
                refreshData();
            }
        });
        //event handle for the redesign button
        $('#redesignButton' + _designCount).click(function() {
            var designNumber = $(this).attr("name");
            redesign(designNumber);
        });
        $('#modalSaveButton' + _designCount).click(function() {
            var designNumber = $(this).attr("val");
            var partIDs = [];
            var vectorIDs = [];
            var writeSQL = false;
            if ($('#sqlCheckbox' + designNumber).attr("checked")) {
                writeSQL = true;
            }
            $('#partsListTable' + designNumber + ' tbody tr').each(function() {
                var tokens = $(this).attr("val").split("|");
                if (tokens[0].toLowerCase() === "vector" || tokens[0].toLowerCase() === "destination vector") {
                    vectorIDs.push(tokens[1]);
                } else {
                    partIDs.push(tokens[1]);
                }
            });
            $.get('RavenServlet', {"command": "save", "partIDs": "" + partIDs, "vectorIDs": "" + vectorIDs, "writeSQL": "" + writeSQL}, function(result) {
                if (result === "saved data") {
                    $('#discardButton' + designNumber).attr("val", "saved");
                    $('#saveButton' + designNumber).prop('disabled', true);
                    $('#saveButton' + designNumber).text("Successful Save");
                    $('#designTabHeader' + designNumber).remove();
                    $('#designTab' + designNumber).remove();
                    $('#designTabHeader a:first').tab('show');
                    refreshData();
                } else {
                    alert("Failed to save parts");
                    $('#saveButton' + designNumber).text("Report Error");
                    $('#saveButton' + designNumber).removeClass('btn-success');
                    $('#saveButton' + designNumber).addClass('btn-danger');
                }
            });
        });
        $('#modalDiscardButton' + _designCount).click(function() {
            var designNumber = $(this).attr("val");
            if ($('#discardButton' + designNumber).attr("val") === "notSaved") {
                var toDeleteVectors = [];
                var toDeleteParts = [];
                $('#partsListTable' + designNumber + ' tr').each(function() {
                    var toSplit = $(this).attr("val");
                    if (typeof toSplit !== "undefined") {
                        var tokens = $(this).attr("val").split("|");
                        if (tokens[0].toLowerCase() === "vector" && tokens[0].toLowerCase() !== "undefined") {
                            toDeleteVectors.push(tokens[1]);
                        } else {
                            toDeleteParts.push(tokens[1]);
                        }
                    }
                });
            }
            $('#discardDialog' + designNumber).modal('hide');
            $('#designTabHeader' + designNumber).remove();
            $('#designTab' + designNumber).remove();
            $('#designTabHeader a:first').tab('show');
            refreshData();
        });
        return _designCount;
    };

    var interpretDesignResult = function(currentDesignCount, data) {
        var user = getCookie("user");
        if (data["status"] === "good") {
            //render image
            $("#resultImage" + currentDesignCount).html("<img src='" + data["graph"]["images"] + "'/>");
            $('#resultImage' + currentDesignCount + ' img').elevateZoom({zoomWindowPosition: 6, scrollZoom: true, zoomWindowWidth: 640, zoomWindowHeight: 360});


            $('#instructionArea' + currentDesignCount).html('<div>' + data["instructions"] + '</div>');
            var status = '';
            var saveButtons = '';
            if (data["statistics"]["valid"] === "true") {
                status = '<span class="label label-success">Graph structure verified!</span>';
                saveButtons = '<p><button id="reportButton' + currentDesignCount +
                         '" class ="btn btn-primary" style="width:100%" val="' + currentDesignCount + '">Submit as Example</button></p>' +
                         '<p><button class="btn btn-success" style="width:100%" id="saveButton' + currentDesignCount + '" val="' + currentDesignCount + '">Save to working library</button></p>';
                if (user === "admin") {
                    saveButtons = saveButtons + '<p><label><input id="sqlCheckbox' + currentDesignCount + '" type="checkbox" checked=true/>Write SQL</label></p>';
                } else {
                    saveButtons = saveButtons + '<p class="hidden"><input id="sqlCheckbox"' + currentDesignCount + '" type="checkbox" checked=false/></p>';
                }
                //add the appropriate save buttons
                $('#download' + currentDesignCount).prepend(saveButtons);
                //create the example modal
                $('#resultTabs' + currentDesignCount).append('<div id="exampleModal' + currentDesignCount + '" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="exampleModal' +
                        currentDesignCount + '" aria-hidden="true">' +
                        '<div class="modal-header"><h4 id="exampleModal' + currentDesignCount + '">Submit example</h4></div>' +
                        '<div class="modal-body"><p>Are you sure you want to submit your graph image as a public example?</p><p>Only the image will be shared - we will never disclose your sequence data</p></div>' +
                        '<div class="modal-footer"><button id="submitExampleButton' + currentDesignCount + '" val="' + currentDesignCount + '" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Submit</button>' +
                        '<button class="btn" data-dismiss="modal" aria-hidden="true">Dismiss</button></div></div>');

                //triggers a confirmation modal 
                $('#reportButton' + currentDesignCount).click(function() {
                    var designNumber = $(this).attr("val");
                    $('#exampleModal' + designNumber).modal();

                });

                //this button actually submits the image as an example
                $('#submitExampleButton' + currentDesignCount).click(function() {
                    var designNumber = $(this).attr("val");
                    var imageURL = $('#resultImage' + designNumber + ' span img:first').attr('src');
                    $.get("RavenServlet", {"command": "saveExample", "url": imageURL}, function() {
                        var reportButton = $('#reportButton' + designNumber);
                        reportButton.addClass('disabled');
                        reportButton.text('Example submitted');
                        reportButton.unbind();
                    });
                });
            } else {
                status = '<span class="label label-warning">Graph Structure Invalid!</span>';
                saveButtons = '<p><button id="reportButton' + currentDesignCount + '" class ="btn btn-danger">Report Error</button></p>';
                $('#download' + currentDesignCount).prepend(saveButtons);
                $('#reportButton' + currentDesignCount).click(function() {
                    alert('Thank you. Your error has been logged');
                });
            }
            //prepend status badge and report button
            $('#saveButton' + currentDesignCount).click(function() {
                var designNumber = $(this).attr("val");
                var partIDs = [];
                var vectorIDs = [];
                var writeSQL = false;
                if ($('#sqlCheckbox' + designNumber).attr("checked")) {
                    writeSQL = true;
                }
                $('#partsListTable' + designNumber + ' tbody tr').each(function() {
                    var tokens = $(this).attr("val").split("|");
                    if (tokens[0].toLowerCase() === "vector" || tokens[0].toLowerCase() === "destination vector") {
                        vectorIDs.push(tokens[1]);
                    } else {
                        partIDs.push(tokens[1]);
                    }
                });
                $.get('RavenServlet', {"command": "save", "partIDs": "" + partIDs, "vectorIDs": "" + vectorIDs, "writeSQL": "" + writeSQL}, function(result) {
                    if (result === "saved data") {
                        $('#discardButton' + currentDesignCount).attr("val", "saved");
                        var saveButton = $('#saveButton' + designNumber);
                        saveButton.prop('disabled', true);
                        saveButton.text("Successful Save");
                        saveButton.unbind();
                        refreshData();
                    } else {
                        alert("Failed to save parts");
                        var saveButton = $('#saveButton' + designNumber);
                        saveButton.text("Report Error");
                        saveButton.removeClass('btn-success');
                        saveButton.addClass('btn-danger');
                        saveButton.click(function() {
                            alert('Your error has been logged. Thanks for letting us know.');
                        });
                    }
                });
            });
            //render stats
            $('#stat' + currentDesignCount).html('<h4>Assembly Statistics ' + status + '</h4><table class="table">' +
                    '<tr><td><strong>Number of Goal Parts</strong></td><td>' + data["statistics"]["goalParts"] + '</td></tr>' +
                    '<tr><td><strong>Number of Assembly Steps</strong></td><td>' + data["statistics"]["steps"] + '</td></tr>' +
                    '<tr><td><strong>Number of Assembly Stages</strong></td><td>' + data["statistics"]["stages"] + '</td></tr>' +
                    '<tr><td><strong>Number of PCR/Synthesis Reactions</strong></td><td>' + data["statistics"]["reactions"] + '</td></tr>' +
                    '<tr><td><strong>Number of Recommended Parts</strong></td><td>' + data["statistics"]["recommended"] + '</td></tr>' +
                    '<tr><td><strong>Number of Discouraged Parts</strong></td><td>' + data["statistics"]["discouraged"] + '</td></tr>' +
                    '<tr><td><strong>Assembly Efficiency</strong></td><td>' + data["statistics"]["efficiency"] + '</td></tr>' +
                    '<tr><td><strong>Parts Shared</strong></td><td>' + data["statistics"]["sharing"] + '</td></tr>' +
                    '<tr><td><strong>Algorithm Runtime</strong></td><td>' + data["statistics"]["time"] + '</td></tr></table>');
            $('#downloadImage' + currentDesignCount).attr("href", data["graph"]["images"]);
            $('#downloadInstructions' + currentDesignCount).attr("href", "data/" + user + "/instructions" + currentDesignCount + ".txt");
            $('#downloadParts' + currentDesignCount).attr("href", "data/" + user + "/partsList" + currentDesignCount + ".csv");
            $('#downloadPigeon' + currentDesignCount).attr("href", "data/" + user + "/pigeon" + currentDesignCount + ".txt");
            $('#downloadArcs' + currentDesignCount).attr("href", "data/" + user + "/arcs" + currentDesignCount + ".txt");
            $('#downloadConfig' + currentDesignCount).attr("href", "data/" + user + "/config" + currentDesignCount + ".csv");

            $('#designSummaryArea').html("<p>A summary of your assembly plan will appear here</p>");
            //render parts list
            var partsListTableBody = '<table class="table table-bordered table-hover" id="partsListTable' + currentDesignCount + '"><thead><tr><th>uuid</th><th>Name</th><th>LO</th><th>RO</th><th>Type</th><th>Vector</th><th>Composition</th><th>Resistance</th><th>Level</th></tr></thead><tbody>';
            $.each(data["partsList"], function() {
                partsListTableBody = partsListTableBody + '<tr val="' + this["Type"] + '|' + this["uuid"] + '"><td>'
                        + this["uuid"] + "</td><td>"
                        + this["Name"] + "</td><td>"
                        + this["LO"] + "</td><td>"
                        + this["RO"] + "</td><td>"
                        + this["Type"] + "</td><td>"
                        + this["Vector"] + "</td><td>"
                        + this["Composition"] + "</td><td>"
                        + this["Resistance"] + "</td><td>"
                        + this["Level"] + "</td></tr>";
            });
            partsListTableBody = partsListTableBody + '</tbody></table>';
            $('#partsListArea' + currentDesignCount).html(partsListTableBody);
            $("#partsListTable" + currentDesignCount).dataTable({
                "sScrollY": "300px",
                "bPaginate": false,
                "bScrollCollapse": true
            });
        } else {
            //display error
            $("#designTab" + currentDesignCount).html('<div class="alert alert-danger">' +
                    '<button class="btn" id="discardButton' + currentDesignCount + '" name="' + currentDesignCount + '">Dismiss</button><hr/>' +
                    '<strong>Oops, an error occurred while generating your assembly plan</strong>' +
                    '<p>Please send the following to <a href="mailto:ravencadhelp@gmail.com">ravencadhelp@gmail.com</a></p>' +
                    '<ul><li>The error stacktrace shown below</li><li>Your input file. <small>Feel free to remove all of the sequences</small></li>' +
                    '<li>A brief summary of what you were trying to do</li></ul>' +
                    '<p>We appreciate your feedback. We\'re working to make your experience better</p><hr/>'
                    + data["result"] + '</div>');
            $('#discardButton' + currentDesignCount).click(function() {
                var designNumber = $(this).attr("name");
                $('#designTabHeader' + designNumber).remove();
                $('#designTab' + designNumber).remove();
                $('#designTabHeader a:first').tab('show');

                refreshData();
            });
        }
    };
    var redesign = function(originalDesignNumber) {
        if (canRun) {
            var currentDesignCount = originalDesignNumber;
            hideImage(currentDesignCount);
            //switch tab to parts list
//            $('#resultTabsHeader' + currentDesignCount + ' li.active').removeClass("active");
//            $('#resultTabsHeader' + currentDesignCount + ' li:nth-child(3)').addClass('active');
            $('#resultTabs' + currentDesignCount + ' li:eq(2) a').tab('show');

            //change button text
            $('#redesignButton' + currentDesignCount).text("Redesign");
            //bind new function
            $('#redesignButton' + currentDesignCount).unbind();

            //create new html for parts list
            var redesignPartsList = '<table id="partsListTable' + currentDesignCount + '" class="table"><thead><tr><th>Failure/Success</th><th>UUID</th><th>Name</th><th>LO</th><th>RO</th><th>Type</th><th>Vector</th><th>Composition</th></tr><thead><tbody>';
            var targets = ""; //goal parts
            $('#targetPartList option').each(function() {
                targets = targets + $(this).attr("id") + ",";
            });
            $('#partsListTable' + originalDesignNumber + ' tbody tr').each(function() {
//                var composition = $(this).find('td:nth-child(7)').text().toLowerCase();
                var type = $(this).find('td:nth-child(5)').text().toLowerCase();
                var uuid = $(this).find('td:nth-child(1)').text();
                var isGoalPart = $.inArray(uuid, targets);
                //render only composite parts
//                if (isGoalPart === -1 && composition.split(",").length > 1) {
                if (isGoalPart === -1 && type !== "vector" && type !== "destination vector") {
                    redesignPartsList = redesignPartsList + '<tr val="' + $(this).attr("val") + '"><td><button val="' + currentDesignCount + '" class="btn reqForbidButton" name="neither">Unattempted</button></td>';
                    $(this).find('td').each(function(key, value) {
                        if (key < 7) {
                            var cellData = $(this).text();
                            cellData = cellData.replace(/\|[^,\|]+\|[^\|,]+\|/g, "|");
                            redesignPartsList = redesignPartsList + '<td>' + cellData + '</td>';
                        }
                    });
                    redesignPartsList = redesignPartsList + '</tr>';
                } else if (type === "destination vector") {
                    redesignPartsList = redesignPartsList + '<tr val="' + $(this).attr("val") + '"><td><button val="' + currentDesignCount + '" class="btn-success reqForbidButton" name="succeeded">Succeeded</button></td>';
                    $(this).find('td').each(function(key, value) {
                        if (key < 7) {
                            var cellData = $(this).text();
                            cellData = cellData.replace(/\|[^,\|]+\|[^\|,]+\|/g, "|");
                            redesignPartsList = redesignPartsList + '<td>' + cellData + '</td>';
                        }
                    });
                    redesignPartsList = redesignPartsList + '</tr>';
                }
            });
            redesignPartsList = redesignPartsList + '</tbody></table>';
            //replace current parts list
            $('#partsListTable' + currentDesignCount + '_wrapper').remove();
            $('#partsListTab' + currentDesignCount).html('<div id="partsListArea' + currentDesignCount + '">' + redesignPartsList + '</div>');
            $("#partsListTable" + currentDesignCount).dataTable({
                "sScrollY": "300px",
                "bPaginate": false,
                "bScrollCollapse": true
            });

            $('#redesignButton' + currentDesignCount).click(function(event) {

                event.preventDefault();
                $('#redesignModal').modal();

                hideImage(currentDesignCount)
                $('#designTabHeader a:first').tab('show');
                var designNumber = $(this).attr("name");
                //copy the original design input
                var redesignInput = jQuery.extend(true, {}, _runParameters[currentDesignCount]);

                var forbid = "";
                var require = "";
                var toSaveParts = [];
                var toSaveVectors = [];
                var toAddToPartLibrary = "";
                var toAddToVectorLibrary = "";

                $('#partsListTable' + designNumber + ' tbody tr').each(function() {
                    var failSucceed = $(this).find('td').first().find("button").attr("name");
                    var type = $(this).find('td:nth-child(6)').text().toLowerCase();
                    var composition = $(this).find('td:nth-child(8)').text().toLowerCase();

                    var uuid = $(this).find('td:nth-child(2)').text();

                    if (type === "vector" || type === "destination vector") {
                        toSaveVectors.push(uuid);
                        toAddToVectorLibrary = toAddToVectorLibrary + uuid + ',';
                    }

                    if (type === "plasmid" && failSucceed === "failed" && composition.split(",").length > 1) {
                        var toForbid = $(this).find('td:last').text();
                        var toForbidS = toForbid.split(",");
                        var toForbidF = "";
                        for (var toForb in toForbidS) {
                            var forb = toForbidS[toForb];
                            var tokens = forb.split("|");
                            toForbidF = toForbidF + tokens[0];
                            toForbidF = toForbidF + "|" + tokens[tokens.length - 1] + ",";
                        }
                        toForbid = "[" + toForbidF.substring(0, toForbidF.length - 1) + "]";
                        forbid = forbid + toForbid + ";";
                    } else if (type === "plasmid" && failSucceed === "succeeded" && composition.split(",").length > 1) {
                        var toRequire = $(this).find('td:last').text();
                        var toRequireS = toRequire.split(",");
                        var toRequireF = "";
                        for (var toReq in toRequireS) {
                            var req = toRequireS[toReq];
                            var tokens = req.split("|");
                            toRequireF = toRequireF + tokens[0];
                            toRequireF = toRequireF + "|" + tokens[tokens.length - 1] + ",";
                        }
                        toRequire = "[" + toRequireF.substring(0, toRequireF.length - 1) + "]";
                        require = require + toRequire + ";";
                    }

                    if (failSucceed === "succeeded" && type !== "vector" && type !== "destination vector") {
                        var uuid = $(this).find('td:nth-child(2)').text();
                        toAddToPartLibrary = toAddToPartLibrary + uuid + ',';
                        toSaveParts.push(uuid);
                    }
                });
                toAddToPartLibrary = toAddToPartLibrary.substring(0, toAddToPartLibrary.length - 1);
                toAddToVectorLibrary = toAddToVectorLibrary.substring(0, toAddToVectorLibrary.length - 1);

                forbid = forbid.substring(0, forbid.length - 1);
                require = require.substring(0, require.length - 1);
                redesignInput["designCount"] = (parseInt(redesignInput["designCount"]) + 1) + "";
                redesignInput["forbidden"] = forbid;
                redesignInput["required"] = require;
                redesignInput["partLibrary"] = redesignInput["partLibrary"] + "," + toAddToPartLibrary;
                redesignInput["vectorLibrary"] = redesignInput["vectorLibrary"] + "," + toAddToVectorLibrary;
                _runParameters[designNumber] = redesignInput;
                interpretParams(redesignInput);
                refreshData();
                updateSummary();
            });

            $('.reqForbidButton').click(function() {
                if ($(this).attr("name") === "neither") {

                    //add to library
                    $(this).attr("name", "succeeded");
                    $(this).addClass("btn-success");
                    $(this).text("Succeeded");
                } else if ($(this).attr("name") === 'succeeded') {

                    //add to forbidden
                    $(this).removeClass("btn-success");
                    $(this).attr("name", "failed");
                    $(this).addClass("btn-danger");
                    $(this).text("Failed");
                } else {

                    //return to neither 
                    $(this).attr("name", "neither");
                    $(this).removeClass("btn-danger");
                    $(this).text("Unattempted");
                    $(this).removeClass("btn-success");
                }
            });
        } else {
            $('#waitModal').modal();
        }
    };
    //handler for switching images
    $('#designTabHeader li a#designTabHeader_0').click(function() {
        var id = $(this).attr("id").toString();
        var tabNumber = id.substring(id.indexOf("_") + 1);
        changeImage(tabNumber);
    });
    function interpretParams(params) {
        if (params["method"]) {
            _method = params["method"];
            if (params["efficiency"]) {
                
                //populate efficiency
                var table = $('#' + params["method"] + 'Tab table');
                table.children("tbody").html(""); 
                var efficiencies = params["efficiency"].split(",");
                $.each(efficiencies, function(index, value) {
                    table.children("tbody").append('<tr><td>' + (index + 2) + '</td><td><input class="input-mini" placeholder="' + value + '"></td><tr>')
                });
            }
        } else {
            _method = "biobricks"
        } 
        
        //populate required
        var required = params["required"].split(";");
        $.each(required, function(index, value) {
            $("#intermediatesTypeAhead").val(value);
            $("button#requireButton").click();
        });
        //populate forbidden
        var forbidden = params["forbidden"].split(";");
        $.each(forbidden, function(index, value) {
            $("#intermediatesTypeAhead").val(value);
            $("button#forbidButton").click();
        });
        //populate recommended
        var recommended = params["recommended"].split(";");
        $.each(recommended, function(index, value) {
            $("#intermediatesTypeAhead").val(value);
            $("button#recommendButton").click();
        });
        //populate discouraged
        var discouraged = params["discouraged"].split(";");
        $.each(discouraged, function(index, value) {
            $("#intermediatesTypeAhead").val(value);
            $("button#discourageButton").click();
        });

        //populate library parts        
//        if (params["partLibrary"]) {
//            var partIDs = params["partLibrary"].split(",");
//            var vectorIDs = params["vectorLibrary"].split(",");
//            var writeSQL = false;
//            $.get('RavenServlet', {"command": "save", "partIDs": "" + partIDs, "vectorIDs": "" + vectorIDs, "writeSQL": "" + writeSQL}, function(result) {
//
//            });
//        }
        
        //switch to correct method
        if (params["method"]) {
            $.each($('#methodTabHeader li'), function() {
                $(this).removeClass('active');
            });
            $('#' + params["method"] + 'TabHeader').parent().addClass('active')
            $.each($('#methodTabs div'), function() {
                $(this).removeClass('active');
            });
            $('#' + params["method"] + 'Tab').addClass('active')
        } else {
            $('#' + params["method"] + 'TabHeader').parent().addClass('active')
            $('#' + params["method"] + 'Tab').addClass('active')
        }
        
        //populate primer parameters        
        if (params["primer"]) {
            $('input#oligoNameRoot').val(params["primer"]["oligoNameRoot"]);
            $('input#meltingTemperature').val(params["primer"]["meltingTemperature"]);
            $('input#targetLength').val(params["primer"]["targetLength"]);
            $('input#minPCRLength').val(params["primer"]["minPCRLength"]);
            $('input#minCloneLength').val(params["primer"]["minCloneLength"]);
            $('input#maxPrimerLength').val(params["primer"]["maxPrimerLength"]);
        } 
        updateSummary();
    }

    function changeImage(tabNumber) {
        if (currentActiveTab > 0) {
            var image = $('div#resultImage' + currentActiveTab + ' img');
            $.removeData(image, 'elevateZoom');//remove zoom instance from image
            $('.zoomContainer').remove();// remove zoom container from DOM
        }
        if (tabNumber > 0) {
            var zoomImage = $('div#resultImage' + tabNumber + ' img');
            zoomImage.elevateZoom({zoomWindowPosition: 6, scrollZoom: true, zoomWindowWidth: 640, zoomWindowHeight: 360});
        }
        currentActiveTab = tabNumber;

    }
    function hideImage(tabNumber) {
        if (tabNumber > 0) {
            var image = $('div#resultImage' + tabNumber + ' img');
            $.removeData(image, 'elevateZoom');//remove zoom instance from image
            $('.zoomContainer').remove();// remove zoom container from DOM
        }

    }
    function showImage(tabNumber) {
        if (tabNumber > 0) {
            var zoomImage = $('div#resultImage' + tabNumber + ' img');
            zoomImage.elevateZoom({zoomWindowPosition: 6, scrollZoom: true, zoomWindowWidth: 640, zoomWindowHeight: 360});
        }
        currentActiveTab = tabNumber;
    }
});

