<#if onlytable>${tablecontents?no_esc}<#else>
<#include "header.ftlh">

<#assign progress = table.getProgression()>
<#assign tid = table.getId()>

<h2 class="line-align"><span>${table.getTitle()}</span>
<#if progress == 1>
  <span id="status-icon-t${tid}" class="status-icon status-done" title="Done"><span class="text-only">Done</span></span>
<#else>
  <span><#include "progress-bar-table.ftlh"></span>
</#if>
</h2>

<#if table.getDescription()?has_content>
<p class="description">${table.getDescription()}</p>
</#if>

<div id="tablecontents">
${tablecontents?no_esc}
</div>

<div>
<ul class="plot-buttons line-align">
<li><a class="btn-24 btn-csv" href="/table/${tid}/csv?dl=1" title="Download as CSV"><span>CSV</span></a></li>
<li><a class="btn-24 btn-tex" href="/table/${tid}/tex?dl=1" title="Download as LaTeX"><span>LaTeX</span></a></li>
<li><a class="btn-24 btn-html" href="/table/${tid}/html?dl=1" title="Download as HTML"><span>HTML</span></a></li>
<li><a class="btn-24 btn-queue" href="/assistant/enqueue/table/${tid}" title="Add experiments linked to this table to the assistant's queue"><span>Enqueue</span></a></li>
</ul>
</div>

<h3>Dependencies</h3>

<p>Below is the list of all the experiments in the lab on which this table depends.</p>

<ul class="running-exps">
<#list table.getExperimentDependencies(true) as exp_dep>
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
var tid_e = setTimeout(updateTableStatus, 3100);

function updateTableStatus() {
  $.ajax({
    url: "/tables/status/${tid}"
  }).done(function (data) {
              var status = data[0];
              var progression = data[1];
              var deps = data[2];
        	  var status_class = getStatusClass(status);
        	  var e = $("#status-icon-t${tid}");
              $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
              if (status != "RUNNING") {
            	  $("#progress-bar-t${tid}").hide();
            	  $("#progress-bar-val-t${tid}").hide();
              }
              else {
            	  $("#progress-bar-t${tid}").show();
                  $("#progress-bar-val-t${tid}").text(Math.round(progression * 100) + "%");
                  $("#progress-bar-rect-t${tid}").css({"width" : (progression * 50) + "px"});            	  
              }
              for (var i = 0; i < deps.length; i++) {
            	  var exp_data = deps[i];
            	  var exp_status_class = getStatusClass(exp_data[1]);
            	  $("#status-icon-" + exp_data[0]).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + exp_status_class);
              }
  })
  $.ajax({
    url: "/table/${tid}/html"
  }).done(function (data) {
              $("#tablecontents").html(data);
              setTimeout(updateTableStatus, 3100);
  })
};
</script>

<#include "footer.ftlh">
</#if>