<#include "header.ftlh">

<#if message?has_content>
<p>${message}</p>
</#if>

<h2>Queue</h2>

<p>The queue contains the list of experiments that are have not yet been committed to an assistant run.</p>

<#assign g = lab.getAssistant().getQueueAsGroup()>
<#assign experiments = g.getObjects()>

<#if experiments?size == 0>
  <p>No experiment currently in the queue.</p>
<#else>
  <form name="current-queue" method="post" action="/assistant">
  <input type="submit" class="btn" id="btn-start" name="start" value="Start a new run" />
  <#include "experiment-list.ftlh">
  </form>
</#if>

<#assign runs = lab.getAssistant().getRuns()>

<h2>Runs</h2>

<p>Runs are sets of experiments scheduled to be executed by the assistant.</p>

<#if runs?size == 0>
  <p>No run in this assistant.</p>
</#if>

<#list runs as run>
  
  <#assign id = run.getId()>

  <div class="around-pulldown">
  <h3 class="pulldown" title="Click to show/hide the list of experiments in this run">Run ${id}</h3>
  <div class="pulldown-contents">

  <#assign experiments = run.getExperiments()>

  <#if experiments?size == 0>
    <p>No experiment in this run.</p> <#-- Should not happen, just to be safe -->
  <#else>
    <#include "experiment-list.ftlh">
  </#if>

  <input type="submit" class="btn" id="btn-stop" name="stop-${id}" value="Stop this run" />
  <input type="submit" class="btn" id="btn-clean" name="delete-${id}" value="Delete this run" />

  </div>
  </div>

</#list>

<#include "footer.ftlh">