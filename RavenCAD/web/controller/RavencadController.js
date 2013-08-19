$(document).ready(function() { //don't run javascript until page is loaded
    var _designCount = 0;
    var _loaded = false;
    var _data = null;
    var _method = "BioBricks";
    var uuidCompositionHash = {}; //really just a json object...key: uuid, value: string composition
    var canRun = true;
    var efficiencyArray = [];
    var _runParameters = {};
    var _redesignDesignHash = {}; //key design number of redesign tab, value- design number of original tab
    /********************EVENT HANDLERS********************/
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
    //target part button event handlers
    $('#targetSelectAllButton').click(function() {
        $("#availableTargetPartList option").each(function() {
            $("#availableLibraryPartList #" + $(this).attr("id")).remove();
            $("#libraryPartList #" + $(this).attr("id")).remove();
        });
        $('#targetPartList').append($('#availableTargetPartList option'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#targetDeselectAllButton').click(function() {
        $("#targetPartList option").each(function() {
            $("#availableLibraryPartList").append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
            $("#availableLibraryPartList #" + $(this).attr("id")).addClass("composite");
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
        });
        $('#targetPartList').append($('#availableTargetPartList :selected'));
        sortPartLists();
        //drawIntermediates();
        refreshIntermediatesTypeAhead();
    });
    $('#targetDeselectButton').click(function() {
        $('#targetPartList :selected').each(function() {
            $('#availableLibraryPartList').append('<option id="' + $(this).attr("id") + '">' + $(this).text() + '</option>');
            $("#availableLibraryPartList #" + $(this).attr("id")).addClass("composite");
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
                _method = _method.toLowerCase().replace(/\s+/g, '');

                //primer parameters
                var oligoNameRoot = $('input#oligoNameRoot').val();
                var meltingTemperature = $('input#meltingTemperature').val();
                var targetLength = $('input#targetLength').val();
                var forwardPrefix = $('input#forwardPrefix').val();
                var forwardCutSite = $('input#forwardCutSite').val();
                var forwardCutDistance = $('input#forwardCutDistance').val();
                var reversePrefix = $('input#reversePrefix').val();
                var reverseCutSite = $('input#reverseCutSite').val();
                var reverseCutDistance = $('input#reverseCutDistance').val();
                //if they are primer parameters are not filled in, use defaults

                if (oligoNameRoot === undefined) {
                    oligoNameRoot = $('input#oligoNameRoot').attr("placeholder");
                }
                if (meltingTemperature === undefined) {
                    meltingTemperature = $('input#meltingTemperature').attr("placeholder");
                }
                if (targetLength === undefined) {
                    targetLength = $('input#targetLength').attr("placeholder");
                }
                if (forwardPrefix === undefined) {
                    forwardPrefix = $('input#forwardPrefix').attr("placeholder");
                }
                if (forwardCutSite === undefined) {
                    forwardCutSite = $('input#forwardCutSite').attr("placeholder");
                }
                if (forwardCutDistance === undefined) {
                    forwardCutDistance = $('input#forwardCutDistance').attr("placeholder");
                }
                if (reversePrefix === undefined) {
                    reversePrefix = $('input#reversePrefix').attr("placeholder");
                }
                if (reverseCutSite === undefined) {
                    reverseCutSite = $('input#reverseCutSite').attr("placeholder");
                }
                if (reverseCutDistance === undefined) {
                    reverseCutDistance = $('input#reverseCutDistance').attr("placeholder");
                }
                var requestInput = {command: "run", designCount: "" + currentDesignCount, targets: "" + targets, method: ""
                            + _method, partLibrary: "" + partLibrary, vectorLibrary: "" + vectorLibrary, recommended: ""
                            + rec, required: "" + req, forbidden: "" + forbid, discouraged: "" + discourage,
                    efficiency: "" + efficiencyArray,
                    "primer": JSON.stringify({oligoNameRoot: "",
                        meltingTemperature: "meltingTemperature",
                        targetLength: "targetLength",
                        forwardPrefix: "forwardPrefix",
                        forwardCutSite: "forwardCutSite",
                        forwardCutDistance: "forwardCutDistance",
                        reversePrefix: "reversePrefix",
                        reverseCutSite: "reverseCutSite",
                        reverseCutDistance: "reverseCutDistance"
                    })};
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
        var targetListBody = "<select id=\"availableTargetPartList\" multiple=\"multiple\" class=\"fixedHeight\">";
        var libraryPartListBody = "<select id=\"libraryPartList\" multiple=\"multiple\" class=\"fixedHeight\">";
        var libraryVectorListBody = "<select id=\"libraryVectorList\" multiple=\"multiple\" class=\"fixedHeight\">";
        $.each(_data["result"], function() {
            if (this["Type"] === "composite") {
                targetListBody = targetListBody + "<option class=\"composite ui-state-default\" id=\"" + this["uuid"] + "\">" + this["Name"] + "</option>";
            } else if (this["Type"] === "vector") {
                libraryVectorListBody = libraryVectorListBody + "<option class=\"vector ui-state-default\" id=\"" + this["uuid"] + "\">" + this["Name"] + "</option>";
            } else {
                libraryPartListBody = libraryPartListBody + "<option class=\"basic ui-state-default\" id=\"" + this["uuid"] + "\">" + this["Name"] + "</option>";
            }

        });
        targetListBody = targetListBody + "</select>";
        libraryVectorListBody = libraryVectorListBody + "</select>";
        libraryPartListBody = libraryPartListBody + "</select>";
        $("#availableTargetPartListArea").html(targetListBody);
        $("#libraryPartListArea").html(libraryPartListBody);
        $("#libraryVectorListArea").html(libraryVectorListBody);
        //clear lists
        $('#targetPartList').html("");
        $('#availableLibraryPartList').html(targetListBody);
        $('#availableLibraryVectorList').html("");
        sortPartLists();
        sortVectorLists();
    };
    var getData = function() {
        $.getJSON("RavenServlet", {"command": "fetch"}, function(json) {
            _data = json;
            drawPartVectorLists();
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
        var pattern = /^[\d]+\.[\d]+/;
        var summary = "<p>You're trying to assemble</p>";
        if ($('#targetPartList option').length > 0) {
            summary = summary + '<ul style="max-height:150px;overflow:auto">';
            $('#targetPartList option').each(function() {
                summary = summary + '<li>' + $(this).text() + '</li>';
            });
            summary = summary + "</ul>";
        } else {
            summary = summary + '<div class="alert alert-danger"><strong>Nothing</strong>. Try selecting some target parts</div>';
        }
        var tabMethod = _method.toLowerCase().replace(/\s+/g, '');
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
        summary = summary + '<p>This is the efficiency matrix that you are using for your assembly</p>';
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

        if ($('#libraryPartList option').length > 0) {
            summary = summary + '<p>Your library includes the following parts:</p>';
            summary = summary + '<ul style="max-height:150px;overflow:auto">';
            $('#libraryPartList option').each(function() {
                summary = summary + '<li>' + $(this).val() + "</li>";
            });
            summary = summary + "</ul>";
        } else {
            summary = summary + '<p>You library includes no parts</p>';
        }


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
    function setCookie(c_name, value, exdays) {
        var exdate = new Date();
        exdate.setDate(exdate.getDate() + exdays);
        var c_value = escape(value) + ((exdays === null) ? "" : "; expires=" + exdate.toUTCString());
        document.cookie = c_name + "=" + c_value;
    }

    function getCookie(c_name) {
        var c_value = document.cookie;
        var c_start = c_value.indexOf(" " + c_name + "=");
        if (c_start === -1) {
            c_start = c_value.indexOf(c_name + "=");
        }
        if (c_start === -1) {
            c_value = null;
        }
        else {
            c_start = c_value.indexOf("=", c_start) + 1;
            var c_end = c_value.indexOf(";", c_start);
            if (c_end === -1) {
                c_end = c_value.length;
            }
            c_value = unescape(c_value.substring(c_start, c_end));
        }
        return c_value;
    }
    function deleteCookie(key) {
// Delete a cookie by setting the date of expiry to yesterday
        date = new Date();
        date.setDate(date.getDate() - 1);
        document.cookie = escape(key) + '=;expires=' + date;
    }

    if (getCookie("authenticate") !== "authenticated") {
        deleteCookie("user");
    }

    if (getCookie("authenticate") === "authenticated") {
        $('#loginArea').html('<p class="pull-right">You are logged in as <strong>' + getCookie("user") + '</strong> <a id="logout">Log Out</a></p>');
        $('#logout').click(function() {
            $.get("RavenServlet", {"command": "logout"}, function() {
                deleteCookie("authenticate");
                deleteCookie("user");
                window.location.replace("index.html");
            });
        });
    } else if (getCookie("authenticate") === "failed") {
        window.location.replace("login.html");
    }

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
        $('#designTabHeader').append('<li><a id="designTabHeader' + _designCount + '" href="#designTab' + _designCount + '" data-toggle="tab">Design ' + _designCount +
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
                '<li class="active"><a href="#imageTab' + _designCount + '" data-toggle="tab" >Image</a></li>' +
                '<li><a href="#instructionTab' + _designCount + '" data-toggle="tab">Instructions</a></li>' +
                '<li><a href="#partsListTab' + _designCount + '" data-toggle="tab">Parts List</a></li>' +
                '<li><a href="#summaryTab' + _designCount + '" data-toggle="tab">Summary</a></li>' +
                '<li><a href="#discardDialog' + _designCount + '" class="btn" role="button" val="notSaved" id="discardButton' + _designCount + '" name="' + _designCount + '">Discard Design</a></li>' +
                '<li><button class="btn btn-primary" id="redesignButton' + _designCount + '" name="' + _designCount + '">Redesign</button></li>' +
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
                '<div class="tab-pane active" id="imageTab' + _designCount + '"><div class="well" id="resultImage' + _designCount + '">Please wait while RavenCAD generates your image<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="instructionTab' + _designCount + '"><div class="well" id="instructionArea' + _designCount + '" style="height:360px;overflow:auto">Please wait while RavenCAD generates instructions for your assembly<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="partsListTab' + _designCount + '"><div id="partsListArea' + _designCount + '" style="overflow:visible">Please wait while RavenCAD generates the parts for your assembly<div class="progress progress-striped active"><div class="bar" style="width:100%"></div></div></div></div>' +
                '<div class="tab-pane" id="summaryTab' + _designCount + '"><div class="well" id="summaryArea' + _designCount + '" style="height:360px;overflow:auto">' + $('#designSummaryArea').html() + '</div></div>' +
                '</div>');
        //add download buttons and bind events to them
        $('#download' + _designCount).append('<h4>Download Options</h4>' +
                '<p><small>Please use right-click, then save as to download the files</small></p>' +
                '<p><a id="downloadImage' + _designCount + '">Download Graph Image</a></p>' +
                '<p><a id="downloadInstructions' + _designCount + '">Download Instructions</a></p>' +
                '<p><a id="downloadParts' + _designCount + '">Download Parts/Vectors List</a></p>' +
                '<p><a id="downloadArcs' + _designCount + '">Download Puppeteer Arcs File</a></p>'

                );
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
                if (tokens[0].toLowerCase() === "vector") {
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
                    $('#saveButton' + designNumber).click(function() {
                        alert('this feature will be coming soon');
                    });
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
            $('#resultImage' + currentDesignCount + ' img').wrap('<span style="width:640;height:360px;display:inline-block"></span>').css('display', 'block').parent().zoom();

//                        $.each(data["graph"]["images"], function(key, value) {
//                            window.open(value, key);
//                        })

            $('#instructionArea' + currentDesignCount).html('<div>' + data["instructions"] + '</div>');
            var status = '';
            var saveButtons = '';
            if (data["statistics"]["valid"] === "true") {
                status = '<span class="label label-success">Graph structure verified!</span>';
                saveButtons = '<p><button id="reportButton' + currentDesignCount +
                        '" class ="btn btn-primary" style="width:100%" >Submit as Example</button></p>' +
                        '<p><button class="btn btn-success" style="width:100%" id="saveButton' + currentDesignCount + '" val="' + currentDesignCount + '">Save to working library</button></p>';
                if (user === "admin") {
                    saveButtons = saveButtons + '<p><label><input id="sqlCheckbox' + currentDesignCount + '" type="checkbox" checked=true/>Write SQL</label></p>';
                } else {
                    saveButtons = saveButtons + '<p class="hidden"><input id="sqlCheckbox"' + currentDesignCount + '" type="checkbox" checked=false/></p>';
                }
                $('#download' + currentDesignCount).prepend(saveButtons);
                $('#reportButton' + currentDesignCount).click(function() {
                    alert("this feature will be coming soon");
                });
            } else {
                status = '<span class="label label-warning">Graph Structure Invalid!</span>';
                saveButtons = '<p><button id="reportButton' + currentDesignCount + '" class ="btn btn-danger">Report Error</button></p>';
                $('#download' + currentDesignCount).prepend(saveButtons);
                $('#reportButton' + currentDesignCount).click(function() {
                    alert("this feature will be coming soon");
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
                    if (tokens[0].toLowerCase() === "vector") {
                        vectorIDs.push(tokens[1]);
                    } else {
                        partIDs.push(tokens[1]);
                    }
                });
                $.get('RavenServlet', {"command": "save", "partIDs": "" + partIDs, "vectorIDs": "" + vectorIDs, "writeSQL": "" + writeSQL}, function(result) {
                    if (result === "saved data") {
                        $('#discardButton' + currentDesignCount).attr("val", "saved");
                        $('#saveButton' + designNumber).prop('disabled', true);
                        $('#saveButton' + designNumber).text("Successful Save");
                        refreshData();
                    } else {
                        alert("Failed to save parts");
                        $('#saveButton' + designNumber).text("Report Error");
                        $('#saveButton' + designNumber).removeClass('btn-success');
                        $('#saveButton' + designNumber).addClass('btn-danger');
                        $('#saveButton' + designNumber).click(function() {
                            alert('this feature will be coming soon');
                        });
                    }
                });
            });
            //render stats
            $('#stat' + currentDesignCount).html('<h4>Assembly Statistics ' + status + '</h4><table class="table">' +
                    '<tr><td><strong>Number of Goal Parts</strong></td><td>' + data["statistics"]["goalParts"] + '</td></tr>' +
                    '<tr><td><strong>Number of Assembly Steps</strong></td><td>' + data["statistics"]["steps"] + '</td></tr>' +
                    '<tr><td><strong>Number of Assembly Stages</strong></td><td>' + data["statistics"]["stages"] + '</td></tr>' +
                    '<tr><td><strong>Number of Reactions</strong></td><td>' + data["statistics"]["reactions"] + '</td></tr>' +
                    '<tr><td><strong>Number of Recommended Parts</strong></td><td>' + data["statistics"]["recommended"] + '</td></tr>' +
                    '<tr><td><strong>Number of Discouraged Parts</strong></td><td>' + data["statistics"]["discouraged"] + '</td></tr>' +
                    '<tr><td><strong>Assembly Efficiency</strong></td><td>' + data["statistics"]["efficiency"] + '</td></tr>' +
                    '<tr><td><strong>Parts Shared</strong></td><td>' + data["statistics"]["sharing"] + '</td></tr>' +
                    '<tr><td><strong>Algorithm Runtime</strong></td><td>' + data["statistics"]["time"] + '</td></tr></table>');
            $('#downloadImage' + currentDesignCount).attr("href", data["result"]);
            $('#downloadInstructions' + currentDesignCount).attr("href", "data/" + user + "/instructions" + currentDesignCount + ".txt");
            $('#downloadParts' + currentDesignCount).attr("href", "data/" + user + "/partsList" + currentDesignCount + ".csv");
            $('#downloadPigeon' + currentDesignCount).attr("href", "data/" + user + "/pigeon" + currentDesignCount + ".txt");
            $('#downloadArcs' + currentDesignCount).attr("href", "data/" + user + "/arcs" + currentDesignCount + ".txt");

            $('#designSummaryArea').html("<p>A summary of your assembly plan will appear here</p>");
            //render parts list
            var partsListTableBody = '<table class="table table-bordered table-hover" id="partsListTable' + currentDesignCount + '"><thead><tr><th>uuid</th><th>Name</th><th>LO</th><th>RO</th><th>Type</th><th>Composition</th><th>Resistance</th><th>Level</th></tr></thead><tbody>';
            $.each(data["partsList"], function() {
                partsListTableBody = partsListTableBody + '<tr val="' + this["Type"] + '|' + this["uuid"] + '"><td>'
                        + this["uuid"] + "</td><td>"
                        + this["Name"] + "</td><td>"
                        + this["LO"] + "</td><td>"
                        + this["RO"] + "</td><td>"
                        + this["Type"] + "</td><td>"
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
        var currentDesignCount = addDesignTab();
        _redesignDesignHash[currentDesignCount] = originalDesignNumber;
        $('#resultTabsHeader' + currentDesignCount + ' li:nth-child(2)').addClass("hidden");
        $('#resultsTabCountent' + currentDesignCount + ' li:nth-child(2)').addClass("hidden");
        $('#resultTabsHeader' + currentDesignCount + ' li:last').addClass("hidden");
        $('#resultTabsHeader' + currentDesignCount + ' li:last').addClass("hidden");
        $('div#download' + currentDesignCount).addClass("hidden");
        $('#resultTabsHeader' + currentDesignCount).append('<li><button id="redesignRun' + currentDesignCount + '" class="btn btn-success" val="' + currentDesignCount + '">Run</button>');
        $('div#summaryTab' + currentDesignCount).html($('div#summaryTab' + originalDesignNumber).html());
        $('div#imageTab' + currentDesignCount).html($('div#imageTabTab' + originalDesignNumber).html());
        $('div#stat' + currentDesignCount).html($('div#stat' + originalDesignNumber).html());
        $('div#partsListTab' + currentDesignCount).html();
        var redesignPartsList = '<table id="partsListTable' + currentDesignCount + '" class="table"><thead><tr><th>Require/Forbid</th><th>UUID</th><th>Name</th><th>LO</th><th>RO</th><th>Type</th><th>Composition</th></tr><thead><tbody>';
        $('#partsListTable' + originalDesignNumber + ' tbody tr').each(function() {
//            alert($(this).find('td:nth-child(5)').text().toLowerCase());
            if ($(this).find('td:nth-child(5)').text().toLowerCase() !== "vector") {
                redesignPartsList = redesignPartsList + '<tr><td><button val="' + currentDesignCount + '" class="btn reqForbidButton" name="neither">Click to Require/Forbid</button></td>';
                $(this).find('td').each(function(key, value) {
                    if (key < 6) {
                        redesignPartsList = redesignPartsList + '<td>' + $(this).text() + '</td>';
                    }
                });
                redesignPartsList = redesignPartsList + '</tr>';
            }
        });
        redesignPartsList = redesignPartsList + '</tbody></table>';

        $('#partsListTab' + currentDesignCount).html('<div id="partsListArea' + currentDesignCount + '">' + redesignPartsList + '</div>');
        $("#partsListTable" + currentDesignCount).dataTable({
            "sScrollY": "300px",
            "bPaginate": false,
            "bScrollCollapse": true
        });

        $('#redesignRun' + currentDesignCount).click(function() {
            $(this).parent().remove();
            $('#resultTabsHeader' + currentDesignCount + ' li:nth-child(2)').removeClass("hidden");
            $('#resultsTabCountent' + currentDesignCount + ' li:nth-child(2)').removeClass("hidden");
            $('#resultTabsHeader' + currentDesignCount + ' li:last').removeClass("hidden");
            $('#resultTabsHeader' + currentDesignCount + ' li:last').removeClass("hidden");
            $('div#download' + currentDesignCount).removeClass("hidden");

            var designNumber = $(this).attr("val");
            var redesignInput = _runParameters[designNumber - 1];
            var forbid = "";
            var req = "";

            $('#partsListTable' + designNumber + ' tbody tr').each(function() {
                var forbidRequire = $(this).find('td').first().find("button").attr("name");
                if (forbidRequire === "forbidden") {
                    forbid = forbid + $(this).find('td:last').text() + ";";
                } else if (forbidRequire === "required") {
                    req = req + $(this).find('td:last').text() + ";";
                }
            });
            forbid = forbid.substring(0, forbid.length - 1);
            req = req.substring(0, req.length - 1);
            redesignInput["forbidden"] = redesignInput["forbidden"] + forbid;
            redesignInput["required"] = redesignInput["required"] + req;
            if (canRun) {
                $.get("RavenServlet", redesignInput, function(data) {
                    interpretDesignResult(currentDesignCount, data);
                    canRun = true;
                });
            }

        });
        $('.reqForbidButton').click(function() {
            var designNumber = $(this).attr("val");
            var originalDesignNumber = _redesignDesignHash[designNumber];
            if ($(this).attr("name") === "neither") {
                //add to require
                $(this).attr("name", "forbidden");
                $(this).addClass("btn-danger");
                $(this).text("Forbidden");
            } else if ($(this).attr("name") === 'forbidden') {
                //add to forbidden
                $(this).removeClass("btn-danger");
                $(this).attr("name", "required");
                $(this).addClass("btn-success");
                $(this).text("Required");
            } else {
                //return to neither
                $(this).attr("name", "neither");
                $(this).removeClass("btn-danger");
                $(this).text("Click to Require/Forbid");//add to forbidden
                $(this).removeClass("btn-success");
            }
            $('#summaryTab' + designNumber + ' div ul.requiredList').html($('#summaryTab' + originalDesignNumber + ' div ul.requiredList').html());
            $('#summaryTab' + designNumber + ' div ul.forbiddenList').html($('#summaryTab' + originalDesignNumber + ' div ul.forbiddenList').html());
            $('#partsListTable' + designNumber + ' tbody tr').each(function() {
                var forbidRequire = $(this).find('td').first().find("button").attr("name");
                if (forbidRequire === "forbidden") {
                    $('#summaryTab' + designNumber + ' div ul.forbiddenList').append('<li>' + $(this).find('td:last').text() + '</li>');
                } else if (forbidRequire === "required") {
                    $('#summaryTab' + designNumber + ' div ul.requiredList').append('<li>' + $(this).find('td:last').text() + '</li>');
                }
            });

        });
    };
});


