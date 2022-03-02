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
	case "INTERRUPTED":
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
	case "PREPARING":
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

/**
 * Updates the progress bars and status icons of a set of elements based on
 * a JSON structure returned by the server.
 * @param data The JSON structure
 * @param prefix A prefix to append to element names
 * @param last_experiment_status An optional map containing the status of
 * the elements the last time the method was called
 * @returns A map containing the current status of the elements (which can be
 * passed back on the next call to the method)
 */
function updateBars(data, prefix, last_element_status = {}) {
	var statuses = {};
	for (var k in data) {
		var status = data[k][0];
		var progression = data[k][1];
		statuses[k] = status;
		if (status == "RUNNING" || status == "PREPARING") {
			$("#progress-bar-" + prefix + k).show();
			$("#progress-bar-val-" + prefix + k).show();
			$("#progress-bar-val-" + prefix + k).text(Math.round(progression * 100) + "%");
			$("#progress-bar-rect-" + prefix + k).css({"width" : (progression * 50) + "px"});
		}
		else {
			$("#progress-bar-" + prefix + k).hide();
			$("#progress-bar-val-" + prefix + k).hide();
		}
		for (var k in data) {
			if (statuses[k] != last_element_status[k]) {
				var status_class = getStatusClass(statuses[k]);
				last_experiment_status[k] = statuses[k];
				var e = $("#status-icon-" + prefix + k);
				$(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
			}
		}
	}
	return statuses;
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
		last_experiment_status = updateBars(data, "e", last_experiment_status);
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
		updateBars(data, "r");
		for (var k in data) {
			var status = data[k][0];
			if (status == "DONE" || status == "FAILED" || status == "INTERRUPTED") {
				$("#stop-r-" + k).hide();
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
		updateBars(data, "t");
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
		updateBars(data, "p");
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
