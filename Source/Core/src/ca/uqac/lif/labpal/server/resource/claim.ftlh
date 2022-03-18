<#include "header.ftlh">

<#assign cid = claim.getId()>
<#assign verdict = claim.evaluate()>

<h2 class="line-align"><span>Claim #${cid}</span>
<#switch verdict>
    <#case "TRUE">
      <span id="status-icon-${cid}" class="status-icon status-troolean-t" title="True"><span class="text-only">T</span></span>
      <#break>
    <#case "FALSE">
      <span id="status-icon-${cid}" class="status-icon status-troolean-f" title="False"><span class="text-only">F</span></span>
      <#break>
    <#default>
      <span id="status-icon-${cid}" class="status-icon status-troolean-i" title="Inconclusive"><span class="text-only">?</span></span>
      <#break>
  </#switch>
</h2>

<blockquote class="claim">${claim.getStatement()}</blockquote>

<p>This claim is ${verdict}. <a href="/explain?id=C${cid}"><span class="btn-24 btn-info"><span>Explain the verdict</span></span></a></p>

<#assign description =  claim.getDescription()>

<#if description?has_content>

<h3>Description</h3>

<div id="claim-description">
${description?no_esc}
</div>
</#if>

<div>
<ul class="plot-buttons line-align">
<li><a class="btn-24 btn-tex" href="/claim/${cid}/tex?dl=1" title="Download as LaTeX"><span>LaTeX</span></a></li>
<li><a class="btn-24 btn-queue" href="/assistant/enqueue/claim/${cid}" title="Add experiments linked to this claim to the assistant's queue"><span>Enqueue</span></a></li>
</ul>
</div>

<h3>Dependencies</h3>

<p>Below is the list of all the experiments in the lab on which this claim directly or indirectly depends.</p>

<ul class="running-exps">
<#list expdeps as exp_dep>
  <#assign id = exp_dep.getId()>
  <#assign status = exp_dep.getStatus()>
  <li><a class="line-align" href="/experiment/${id}"><span class="id-cell" style="margin-right:10px">${id}</span><@statusChunk e=exp_dep prefix="e" withbar=false queued=lab.isQueued(exp_dep) /></a>
  </li>
</#list>
</ul>

<#include "footer.ftlh">