<#include "header.ftlh">

<#if message?has_content>
<p class="message info"><span>${message}</span></p>
</#if>

<h2>Queue</h2>

<p>The queue contains the list of experiments that are have not yet been committed to an assistant run.</p>

<#assign g = lab.getAssistant().getQueueAsGroup()>
<#assign experiments = g.getObjects()>

<#if experiments?size == 0>
  <p>No experiment currently in the queue.</p>
<#else>
  <form name="current-queue" method="post" action="/assistant">
  <p><input type="submit" class="btn btn-start" id="start" name="start" value="Start a new run" /></p>
  <#include "experiment-list.ftlh">
  </form>
</#if>

<#assign runs = lab.getAssistant().getRuns()>

<h2>Runs</h2>

<p>Runs are sets of experiments scheduled to be executed by the assistant.</p>

<#if runs?size == 0>
  <p>No run in this assistant.</p>
</#if>

<#list runs as r>
  
  <#assign r_id = r.getId()>
  <#assign r_running = r.isRunning()>

  <div class="around-pulldown">
  <h3 class="pulldown line-align" title="Click to show/hide the list of experiments in this run">
  <span>Run ${r_id}</span>
  <#include "progress-bar-run.ftlh">
  </h3>
  <div class="pulldown-contents">

  <#assign experiments = r.getExperiments()>

  <#if experiments?size == 0>
    <p>No experiment in this run.</p> <#-- Should not happen, just to be safe -->
  <#else>
    <#include "experiment-list.ftlh">
  </#if>
  </div>
  </div>
  
  <form name="run-${r_id}" method="post" action="/assistant">
  <#if r_running>
  <input type="submit" class="btn btn-stop" id="stop-${r_id}" name="stop-${r_id}" value="Stop this run" />
  </#if>
  <input type="submit" class="btn btn-delete" id="delete-${r_id}" name="delete-${r_id}" value="Delete this run" />
  </form>


</#list>

<script type="text/javascript">
var tid_e = setTimeout(updateExperiments, 2000);
var tid_r = setTimeout(updateRuns, 3100);
</script>

<#include "footer.ftlh">