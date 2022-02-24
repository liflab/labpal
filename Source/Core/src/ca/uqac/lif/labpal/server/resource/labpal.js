$(document).ready(function() {

    /* Checkbox: select all experiments */
	/*
    $("table.exp-table .top-checkbox").click(function() {
      $(".side-checkbox").each(function() { 
	    $(this).prop("checked",(!($(this).prop("disabled")) && $(" .top-checkbox").prop("checked")));
	  });
    });
    */
	/* Filter by enumeration or/and interval */
	$("#experiment-filter .btn-filter").click(filter_experiments);
	

	/* Sort an experiment table by clicking on its headers */
	$("table.exp-table").tablesorter({
		headers: {
			0: {sorter: false} // Except 1st column
		}
	});
	
	/* Pull-down divs */
	$(".pulldown-contents").hide();
	$(".pulldown").addClass("closed");
	$(".pulldown").click(function() {
		$(this).toggleClass("closed");
		$(this).closest("div.around-pulldown").find(".pulldown-contents").toggle();
	});
	
	/* The "report results" button in the status page is overridden
	   to send an Ajax request instead of reloading the page */
	$("#btn-report-results").click(function() {
	  $.ajax("/report-results");
	  return false;
	});

	/* Button to toggle multi-line display of output parameter */
	$("pre.multiline").click(function() {
		$(this).toggleClass("open");
	});   
	resize_top_menu();
});

/**
 * Filters the list of experiments in a page based on a text
 * string specifiying range
 * @returns Nothing
 */
function filter_experiments() 
{
	var hideLigne = $(".top-filter").val().trim();
	if (hideLigne =="")
	{
		$("table.exp-table .tr").show();
		$("table.exp-table .side-checkbox").prop("disabled", false);
	}
	else
	{
		$("table.exp-table .tr").hide();
		$("table.exp-table .side-checkbox").prop("disabled", true);
		hideLigne = hideLigne.replace(/[^0-9,\-]/g, "");
		console.log(hideLigne);
		var hideLigneArray = hideLigne.split(",");
		for (var i = 0; i < hideLigneArray.length; i++)
		{
			if (hideLigneArray[i]!='') {//inputs like ',9,'
				if(hideLigneArray[i].indexOf('-')>-1)
				{
					var hideLigneArray2 = hideLigneArray[i].split("-");
					if (hideLigneArray2[0]!='' && hideLigneArray2[1]!='') 
					{//inputs like '-5'
						for (var k = parseInt(hideLigneArray2[0]); k <= parseInt(hideLigneArray2[1]); k++)
						{	      
							$("table.exp-table .tr-"+k).show();
							$("table.exp-table .side-checkbox-"+k).prop("disabled", false);
						}
					}
				}
				else
				{
					$("table.exp-table .tr-"+hideLigneArray[i]).show();
					$("table.exp-table .side-checkbox-"+hideLigneArray[i]).prop("disabled", false);
				}
			}
		}
	}
};

var last_experiment_status = {};

/**
 * Updates the progress bars of experiments in an experiment list based on
 * a JSON structure returned by the server.
 */
function updateExperiments() {
  $.ajax({
    url: "/experiments/status"
  }).done(function (data) {
	  		var statuses = {};
            for (var k in data) {
              var status = data[k][0]
              var progression = data[k][1];
              if (!(k in last_experiment_status)) {
            	  last_experiment_status[k] = status;
              }
              statuses[k] = status;
              if (status == "RUNNING" || status == "RUNNING_PREREQ") {
                $("#progress-bar-" + k).show();
                $("#progress-bar-val-" + k).show();
                $("#progress-bar-val-" + k).text(Math.round(progression * 100) + "%");
                $("#progress-bar-rect-" + k).css({"width" : (progression * 50) + "px"});
              }
              else {
                $("#progress-bar-" + k).hide();
                $("#progress-bar-val-" + k).hide();
              }
              for (var k in data) {
            	  if (statuses[k] != last_experiment_status[k]) {
            		  var status_class = "running";
            		  last_experiment_status[k] = statuses[k];
                      switch (statuses[k]) {
                      case "UNINITIALIZED":
                    	  status_class = "prereq";
                    	  break;
                      case "DONE":
                    	  status_class = "done";
                    	  break;
                      case "DONE_WARNING":
                    	  status_class = "warning";
                    	  break;
                      case "FAILED":
                    	  status_class = "failed";
                    	  break;
                      case "CANCELLED":
                    	  status_class = "failed";
                    	  break;
                      case "TIMEOUT":
                    	  status_class = "timeout";
                    	  break;
                      case "READY":
                    	  status_class = "ready";
                    	  break;
                      case "RUNNING":
                    	  status_class = "running";
                    	  break;
                      case "RUNNING_PREREQ":
                    	  status_class = "running";
                    	  break;
                      case "QUEUED":
                    	  status_class = "queued";
                    	  break;
                      default:
                    	  status_class = "unknown";
                      	  break;
                      }
                      var e = $("#status-icon-" + k);
                      $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
            	  }
              }
            }
            setTimeout(updateExperiments, 2000);
  })
};

/**
 * Updates the progress bars of runs in a list of runs based on
 * a JSON structure returned by the server.
 */
function updateRuns() {
  $.ajax({
    url: "/assistant/status"
  }).done(function (data) {
            for (var k in data) {
              var running = data[k][0]
              var progression = data[k][1];
              if (running) {
                $("#progress-bar-r" + k).show();
                $("#progress-bar-val-r" + k).show();
                $("#progress-bar-val-r" + k).text(Math.round(progression * 100) + "%");
                $("#progress-bar-rect-r" + k).css({"width" : (progression * 50) + "px"});
              }
              else {
                $("#progress-bar-r" + k).hide();
                $("#progress-bar-val-r" + k).hide();
              }
            }
            setTimeout(updateRuns, 3100);
  })
};

function updateLabBar() {
	  $.ajax({
	    url: "/lab/status"
	  }).done(function (data) {
		  		$(".progress-bar li.done").css({width : (data["done"] / data["total"]) * 400 + "px"});
		  		$(".progress-bar li.queued").css({width : (data["queued"] / data["total"]) * 400 + "px"});
		  		$(".progress-bar li.failed").css({width : (data["failed"] / data["total"]) * 400 + "px"});
		  		$(".progress-bar li.running").css({width : (data["running"] / data["total"]) * 400 + "px"});
		  		$("#numdone").text(data["done"] + "/" + data["total"]);
		  		if (data["ids"].length == 0) {
		  			$(".running-exps").html("<li class=\"none\">None</li>");
		  		}
		  		else {
		  		  var list = "";
		  		  for (var i = 0; i < data["ids"].length; i++) {
		  			  list += "<li><a href=\"/experiment/" + data["ids"][i] + "\">" + data["ids"][i] + "</a></li>\n";
		  		  }
		  		  $(".running-exps").html(list);
		  		}
	            setTimeout(updateLabBar, 5250);
	  })
	};

/**
 * Toggle the captions on the top menu
 * depending on the width of the viewport
 */
function resize_top_menu() {
	if ($(window).width() < 1175) {
		$("#top-menu li").addClass("small-screen");
	}
	else {
		$("#top-menu li").removeClass("small-screen");
	}
};

/**
 * Resize menu on window resize
 */
$(window).resize(resize_top_menu);
