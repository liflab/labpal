<#include "header.ftlh">

<#if message?has_content>
<p class="message info"><span>${message}</span></p>
</#if>

<h2>Queue</h2>

<p>The queue contains the list of experiments that are have not yet been committed to an assistant run.</p>

<#assign g = lab.getAssistant().getQueueAsGroup()>
<#assign experiments = g.getObjects()>
<#assign claims = lab.getClaims()>

<#if experiments?size == 0>
  <p>No experiment currently in the queue.</p>
<#else>
  <p>${experiments?size} experiment(s) currently in the queue.</p>
  <form name="current-queue" method="post" action="/assistant">
  <p class="line-align">
  <input type="submit" class="btn btn-start" id="start" name="start" value="Start a new run" />
  <#if (claims?size > 0)>
    <label for="enqueue-condition" style="margin-left:10pt;margin-right:10pt">conditional to</label>
    <select name="enqueue-condition">
      <option value="">(None)</option>
    <#list claims as claim>
      <#assign cid = claim.getId()>
      <option value="C${cid}">C${cid}</option>
    </#list>
    </select>
  </#if>
  </p>
  <p class="line-align">
  <input type="submit" class="btn btn-queue" id="scheduler" name="scheduler" value="Schedule" />
  <label for="scheduler-type" style="margin-left:10pt;margin-right:10pt"> using </label>
  <select name="scheduler-type" id="scheduler-type">
    <option value="balance">Balance objects</option>
    <option value="decimate">Decimate 50%</option>
    <option value="shuffle">Shuffle</option>
    <option value="subsample">Sub-sample 50%</option>
  </select>
  
  </p>
  <p class="line-align"><input type="submit" class="btn btn-delete" id="dequeue" name="dequeue" value="Remove from queue" /></p>
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
  <#assign g = r>

  <div class="around-pulldown">
  <h3 class="pulldown line-align" title="Click to show/hide the list of experiments in this run">
  <span>Run ${r_id}</span>
  <@statusChunk e=r prefix="r" />
  </h3>
  <div class="pulldown-contents">

  <#assign experiments = r.dependsOn()>

  <form name="run-${r_id}" method="post" action="/assistant">
  <#if experiments?size == 0>
    <p>No experiment in this run.</p> <#-- Should not happen, just to be safe -->
  <#else>
    <#include "experiment-list.ftlh">
  </#if>
  </div>
  </div>
  
  
  <#if r_running>
  <input type="submit" class="btn btn-stop" id="stop-r-${r_id}" name="stop-r-${r_id}" value="Stop this run" />
  <input type="submit" class="btn btn-delete" id="delete-r-${r_id}" name="delete-r-${r_id}" value="Delete this run" />
  </#if>
  </form>


</#list>

<script type="text/javascript">
var tid_e = setTimeout(updateExperiments, 2000);
var tid_r = setTimeout(updateRuns, 3100);
</script>

<#include "footer.ftlh">