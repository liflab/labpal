<#if outputonly?has_content>

<#-- Display only fragment with output parameters -->
${outparams?no_esc}

<#else>

<#-- Display full experiment page -->

<#include "header.ftlh">
<#include "functions.ftlh">

<#assign e = lab.getExperiment(id)>

<h2>About experiment #${id}</h2>

<#assign e_running = (e.getStatus() == "RUNNING" || e.getStatus() == "PREPARING")>

<#if groupdesc?has_content>
  ${groupdesc?no_esc}
</#if>

<div class="description">${e.getDescription()?no_esc}</div>

<#assign startdate = e.getStartDate()>
<#assign enddate = e.getEndDate()>

<table class="status-table">
<tr><th>Status:</th><td class="line-align">
<#switch e.getStatus()>
  <#case "UNINITIALIZED">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq current"></span>
    <span id="dash-preparing" class="dash"></span>
    <span id="box-preparing" class="status-icon status-running"></span>
    <span id="dash-ready" class="dash"></span>
    <span id="box-ready" class="status-icon status-ready"></span>
    <span id="dash-running" class="dash"></span>
    <span id="box-running" class="status-icon status-running"></span>
    <span id="dash-done" class="dash"></span>
    <span id="box-done" class="status-icon status-done"></span>
    <span class="status-caption">Uninitialized</span>
    </div>
  	<#break>
  <#case "PREPARING">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running current"></span>
    <span id="dash-ready" class="dash"></span>
    <span id="box-ready" class="status-icon status-ready"></span>
    <span id="dash-running" class="dash"></span>
    <span id="box-running" class="status-icon status-running"></span>
    <span id="dash-done" class="dash"></span>
    <span id="box-done" class="status-icon status-done"></span>
    <span class="status-caption">Preparing</span>
    </div>
  	<#break>
  <#case "READY">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running active"></span>
    <span id="dash-ready" class="dash active"></span>
    <span id="box-ready" class="status-icon status-ready current"></span>
    <span id="dash-running" class="dash"></span>
    <span id="box-running" class="status-icon status-running"></span>
    <span id="dash-done" class="dash"></span>
    <span id="box-done" class="status-icon status-done"></span>
    <span class="status-caption">Ready</span>
    </div>
  	<#break>
  <#case "RUNNING">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running active"></span>
    <span id="dash-ready" class="dash active"></span>
    <span id="box-ready" class="status-icon status-ready active"></span>
    <span id="dash-running" class="dash active"></span>
    <span id="box-running" class="status-icon status-running current"></span>
    <span id="dash-done" class="dash"></span>
    <span id="box-done" class="status-icon status-done"></span>
    <span class="status-caption">Running</span>
    </div>
  	<#break>
  <#case "DONE">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running active"></span>
    <span id="dash-ready" class="dash active"></span>
    <span id="box-ready" class="status-icon status-ready active"></span>
    <span id="dash-running" class="dash active"></span>
    <span id="box-running" class="status-icon status-running active"></span>
    <span id="dash-done" class="dash active"></span>
    <span id="box-done" class="status-icon status-done current"></span>
    <span class="status-caption">Done</span>
    </div>
  	<#break>
  <#case "FAILED">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running active"></span>
    <span id="dash-ready" class="dash active"></span>
    <span id="box-ready" class="status-icon status-ready active"></span>
    <span id="dash-running" class="dash active"></span>
    <span id="box-running" class="status-icon status-running active"></span>
    <span id="dash-done" class="dash active"></span>
    <span id="box-done" class="status-icon status-failed current"></span>
    <span class="status-caption">Failed</span>
    </div>
  	<#break>
  <#case "INTERRUPTED">
    <div id="status-line">
    <span id="box-prereq" class="status-icon status-prereq active"></span>
    <span id="dash-preparing" class="dash active"></span>
    <span id="box-preparing" class="status-icon status-running active"></span>
    <span id="dash-ready" class="dash active"></span>
    <span id="box-ready" class="status-icon status-ready active"></span>
    <span id="dash-running" class="dash active"></span>
    <span id="box-running" class="status-icon status-running active"></span>
    <span id="dash-done" class="dash active"></span>
    <span id="box-done" class="status-icon status-timeout current"></span>
    <span class="status-caption">Timed out</span>
    </div>
  	<#break>
</#switch>
<span style="margin-left:16pt"><@progressBar status=e.getStatus() prefix="e" id=e.getId() progression=e.getProgression() /></span>
</span>
</td></tr>
<tr><th>Started on:</th><td id="startdate"><#if startdate?has_content>${startdate}<#else>-</#if></td></tr>
<tr><th>Ended on:</th><td id="enddate"><#if enddate?has_content>${enddate}<#else>-</#if></td></tr>
<tr><th>Duration:</th><td id="duration">${e.getTotalDuration()}</td></tr>
<#if expestimate?has_content>
  <tr><th>Estimated duration:</th><td>${expestimate}</td></tr>
</#if>
<tr><th>Timeout:</th><td>
  <#if (e.getTimeout().get().floatValue() <= 0)>
    No timeout
  <#else>
   ${e.getTimeout()}
  </#if>
 </td></tr>
</table>

<form style="display:inline-block" name="experiments" method="post" action="/assistant">
<input type="submit" class="btn btn-queue" name="enqueue" value="Add to queue" />
<input type="hidden" name="exp-chk-g-0-${id}" value="on" />
</form>
<a title="Resets the state of this experiment" href="/experiment/${id}/reset"><button class="btn btn-reset">Reset</button></a>
<a title="Cleans all data and prerequisites generated by this experiment" href="/experiment/${id}/clean"><button class="btn btn-clean">Clean</button></a>

<#if failmsg?has_content>
  ${failmsg}
</#if>

<#if warnings?has_content>
  ${warnings}
</#if>

<h2>Input Parameters</h2>

<div id="exp-input-params">

${inparams?no_esc}

</div>

<h2>Output Parameters</h2>

<div id="exp-output-params">

${outparams?no_esc}

</div>

<#if exceptionmessage?has_content>
<h2>Error Message</h2>

<p>The experiment ended by throwing the following exception.
<#if exceptionexplainable><a href="/explain?id=X${id}"><span class="btn-24 btn-info"><span>Explain the error</span></span></a></#if></p>

<pre>${exceptionmessage}</pre>
</#if>

<script type="text/javascript">
var tid_e = setTimeout(updateExperimentStatus, 3100);

var last_e_status = "";

function updateExperimentLine(status) {
  switch (status) {
    case "UNINITIALIZED":
      $("#box-prereq").removeClass("active").addClass("current");
      $("#dash-preparing").removeClass("active");
      $("#box-preparing").removeClass("active current");
      $("#dash-ready").removeClass("active");
      $("#box-ready").removeClass("active current");
      $("#dash-running").removeClass("active");
      $("#box-running").removeClass("active current");
      $("#dash-done").removeClass("active");
      $("#box-done").removeClass("active status-failed status-timeout").addClass("status-done");
      $(".status-caption").text("Uninitialized");
      break;
    case "PREPARING":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("active").addClass("current");
      $("#dash-ready").removeClass("active");
      $("#box-ready").removeClass("active current");
      $("#dash-running").removeClass("active");
      $("#box-running").removeClass("active current");
      $("#dash-done").removeClass("active");
      $("#box-done").removeClass("active status-failed status-timeout").addClass("status-done");
      $(".status-caption").text("Preparing");
      break;
    case "READY":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("current").addClass("active");
      $("#dash-ready").addClass("active");
      $("#box-ready").removeClass("active").addClass("current");
      $("#dash-running").removeClass("active");
      $("#box-running").removeClass("active current");
      $("#dash-done").removeClass("active");
      $("#box-done").removeClass("active status-failed status-timeout").addClass("status-done");
      $(".status-caption").text("Ready");
      break;
    case "RUNNING":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("current").addClass("active");
      $("#dash-ready").addClass("active");
      $("#box-ready").removeClass("current").addClass("active");
      $("#dash-running").addClass("active");
      $("#box-running").removeClass("active").addClass("current");
      $("#dash-done").removeClass("active");
      $("#box-done").removeClass("active status-failed status-timeout").addClass("status-done");
      $(".status-caption").text("Running");
      break;
    case "DONE":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("current").addClass("active");
      $("#dash-ready").addClass("active");
      $("#box-ready").removeClass("current").addClass("active");
      $("#dash-running").addClass("active");
      $("#box-running").removeClass("current").addClass("active");
      $("#dash-done").addClass("active");
      $("#box-done").removeClass("active status-failed status-timeout").addClass("status-done current");
      $(".status-caption").text("Done");
      break;
    case "FAILED":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("current").addClass("active");
      $("#dash-ready").addClass("active");
      $("#box-ready").removeClass("current").addClass("active");
      $("#dash-running").addClass("active");
      $("#box-running").removeClass("current").addClass("active");
      $("#dash-done").addClass("active");
      $("#box-done").removeClass("active status-done status-timeout").addClass("status-failed current");
      $(".status-caption").text("Failed");
      break;
    case "INTERRUPTED":
      $("#box-prereq").removeClass("current").addClass("active");
      $("#dash-preparing").addClass("active");
      $("#box-preparing").removeClass("current").addClass("active");
      $("#dash-ready").addClass("active");
      $("#box-ready").removeClass("current").addClass("active");
      $("#dash-running").addClass("active");
      $("#box-running").removeClass("current").addClass("active");
      $("#dash-done").addClass("active");
      $("#box-done").removeClass("active status-done status-failed").addClass("status-timeout current");
      $(".status-caption").text("Interrupted");
      break;
  }
};

function updateExperimentStatus() {
  $.ajax({
    url: "/experiments/status/${id}"
  }).done(function (data) {
    var status = data[0];
    updateExperimentLine(status);
    $("#startdate").text(data[2]);
    $("#enddate").text(data[3]);
    $("#duration").text(data[4]);
    if (status == "RUNNING") {
      $("#progress-bar-${id}").show();
      var prog = data[1];
      var w = Math.round(prog * 50);
      var p = Math.round(prog * 100);
      $("#progress-bar-rect-${id}").css("width", w + "px");
      $("#progress-bar-val-${id}").text(p + "%");
    }
    else {
      $("#progress-bar-${id}").hide();
    }
    if (status == "RUNNING" || status != last_e_status) {
	    $.ajax({
	    url: "/experiment/${id}/output/html<#if highlight?has_content>?highlight=${highlight}</#if>"
	  	}).done(function (data) {
	    	$("#exp-output-params").html(data);
	    })
	}
	last_e_status = status;
    setTimeout(updateExperimentStatus, 3100);
  })
};
</script>

<#include "footer.ftlh">

</#if>