$(document).ready(function() {

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
 * Gets the icon class corresponding to the status of an object.
 * @param status The status
 * @returns The icon class
 */
function getStatusClass(status) {
	var status_class = "running";
    switch (status) {
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
    return status_class;
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
            		  var status_class = getStatusClass(statuses[k]);
            		  last_experiment_status[k] = statuses[k];
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

/**
 * Updates the progress bars of tables in a list of tables based on
 * a JSON structure returned by the server.
 */
function updateTables() {
  $.ajax({
    url: "/tables/status"
  }).done(function (data) {
            for (var k in data) {
              var status = data[k][0];
              var progression = data[k][1];
        	  var status_class = getStatusClass(status);
        	  var e = $("#status-icon-t" + k);
              $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
              if (status != "RUNNING") {
            	  $("#progress-bar-t" + k).hide();
              }
              else {
            	  $("#progress-bar-t" + k).show();
                  $("#progress-bar-val-t" + k).text(Math.round(progression * 100) + "%");
                  $("#progress-bar-rect-t" + k).css({"width" : (progression * 50) + "px"});            	  
              }
            }
            setTimeout(updateTables, 3100);
  })
};

/**
 * Updates the progress bars of plots in a list of plots based on
 * a JSON structure returned by the server.
 */
function updatePlots() {
  $.ajax({
    url: "/plots/status"
  }).done(function (data) {
	  for (var k in data) {
          var status = data[k][0];
          var progression = data[k][1];
    	  var status_class = getStatusClass(status);
    	  var e = $("#status-icon-p" + k);
          $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
          if (status != "RUNNING") {
        	  $("#progress-bar-p" + k).hide();
          }
          else {
        	  $("#progress-bar-p" + k).show();
              $("#progress-bar-val-p" + k).text(Math.round(progression * 100) + "%");
              $("#progress-bar-rect-p" + k).css({"width" : (progression * 50) + "px"});            	  
          }
        }
        setTimeout(updatePlots, 3100);
  })
};

/**
 * Updates the lab's progress bar based on
 * a JSON structure returned by the server.
 */
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
