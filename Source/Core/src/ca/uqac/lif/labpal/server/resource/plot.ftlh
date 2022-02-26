<#include "header.ftlh">

<#assign progress = plot.getProgression()>
<#assign pid = plot.getId()>

<h2 class="line-align"><span>${plot.getTitle()}</span>
<#if progress == 1>
  <span id="status-icon-t${pid}" class="status-icon status-done" title="Done"><span class="text-only">Done</span></span>
<#else>
  <span><#include "progress-bar-plot.ftlh"></span>
</#if>
</h2>

<#if plot.getDescription()?has_content>
<p class="description">${plot.getDescription()}</p>
</#if>

<div>
<p>
<#if plottext?has_content>
<pre>${plottext}</pre>
<#else>
<img style="border:solid 1px black" id="plotimage" src="/plot/${pid}/png" alt="Plot" />
</#if>
</p>

<div>
<ul class="plot-buttons line-align">
<li><span style="padding-left:32px" class="btn-24 btn-reset" href="/plot/${pid}?format=gp&amp;dl=1" title="Refresh this plot" onclick="refreshPlot('${pid}')"><span>Refresh</span></span></li>
<li><a class="btn-24 btn-gp" href="/plot/${pid}/gp?dl=1" title="Download as GnuPlot source file"><span>GP</span></a></li>
<li><a class="btn-24 btn-gp" href="/plot/${pid}/dumb" title="View as ASCII art" target="_blank"><span>ASCII</span></a></li>
<li><a class="btn-24 btn-pdf" href="/plot/${pid}/pdf?dl=1" title="Download as PDF"><span>PDF</span></a></li>
<li><a class="btn-24 btn-table" href="/table/${plot.getTable().getId()}" title="Show the data for this plot"><span>Table</span></a></li>
<li><a style="padding-left:32px" class="btn-24 btn-queue" href="/assistant/enqueue/plot/${plot.getId()}" title="Add experiments linked to this plot to the assistant's queue"><span>Enqueue</span></a></li>
</ul>
</div>
</div>
<div style="clear:both"></div>

<h3>Dependencies</h3>

<p>Below is the list of all the experiments in the lab on which this plot depends.</p>

<ul class="running-exps">
<#list plot.getExperimentDependencies(true) as exp_dep>
  <#assign id = exp_dep.getId()>
  <#assign status = exp_dep.getStatus()>
  <li><a class="line-align" href="/experiment/${id}"><span class="id-cell" style="margin-right:10px">${id}</span>
  <#switch status>
      <#case "UNINITIALIZED">
        <span id="status-icon-${id}" class="status-icon status-prereq" title="Prerequisites not fulfilled"><span class="text-only">P</span></span>
        <#break>
      <#case "DONE">
        <span id="status-icon-${id}" class="status-icon status-done" title="Ready"><span class="text-only">D</span></span>
        <#break>
      <#case "DONE_WARNING">
        <span id="status-icon-${id}" class="status-icon status-warning" title="Done with warnings"><span class="text-only">W</span></span>
        <#break>
      <#case "FAILED">
        <span id="status-icon-${id}" class="status-icon status-failed" title="Failed"><span class="text-only">F</span></span>
        <#break>
      <#case "CANCELLED">
        <span id="status-icon-${id}" class="status-icon status-failed" title="Cancelled"><span class="text-only">C</span></span>
        <#break>
      <#case "TIMEOUT">
        <span id="status-icon-${id}" class="status-icon status-timeout" title="Timed out"><span class="text-only">T</span></span>
        <#break>
      <#case "READY">
        <span id="status-icon-${id}" class="status-icon status-ready" title="Ready"><span class="text-only">r</span></span>
        <#break>
       <#case "RUNNING">
        <span id="status-icon-${id}" class="status-icon status-running" title="Running"><span class="text-only">r</span></span>
        <#break>
       <#case "RUNNING_PREREQ">
        <span id="status-icon-${id}" class="status-icon status-running" title="Running prerequisites"><span class="text-only">r</span></span>
        <#break>
      </#switch> 
      </a>
  </li>
</#list>
</ul>

<script type="text/javascript">
var tid_e = setTimeout(updatePlotStatus, 3100);

function updatePlotStatus() {
  $.ajax({
    url: "/plots/status/${pid}"
  }).done(function (data) {
              var status = data[0];
              var progression = data[1];
              var deps = data[2];
        	  var status_class = getStatusClass(status);
        	  var e = $("#status-icon-p${pid}");
              $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
              if (status != "RUNNING") {
            	  $("#progress-bar-p${pid}").hide();
            	  $("#progress-bar-val-p${pid}").hide();
              }
              else {
            	  $("#progress-bar-p${pid}").show();
                  $("#progress-bar-val-p${pid}").text(Math.round(progression * 100) + "%");
                  $("#progress-bar-rect-p${pid}").css({"width" : (progression * 50) + "px"});            	  
              }
              for (var i = 0; i < deps.length; i++) {
            	  var exp_data = deps[i];
            	  var exp_status_class = getStatusClass(exp_data[1]);
            	  $("#status-icon-" + exp_data[0]).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + exp_status_class);
              }
              var d = new Date(); // Prevent caching
  			  $("#plotimage").attr("src", "/plot/" + ${pid} + "/png?" + d.getTime());
  			  setTimeout(updatePlotStatus, 3100);
  })
};
</script>

<#include "footer.ftlh">