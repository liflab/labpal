<#include "header.ftlh">

<#assign claims = lab.getClaims()>

<p>Claims are programmatically calculated conditions expressed on the contents of a lab. Below is the list of claims that have been registered for this lab, along with the truth value associated with them at the moment the page was loaded.</p>

<#if claims?size == 0>
<p>No claim has been registered for this lab.</p>

<#else>

<table>
<#list claims as claim>
<#assign cid = claim.getId()>
<#assign verdict = claim.evaluate()>
<tr>
  <td class="id-cell"><a href="/claim/${cid}">${cid}</a></td>
  <td><a href="/claim/${cid}"><#switch verdict>
    <#case "TRUE">
      <span id="status-icon-${cid}" class="status-icon status-done" title="True"><span class="text-only">T</span></span>
      <#break>
    <#case "FALSE">
      <span id="status-icon-${cid}" class="status-icon status-failed" title="False"><span class="text-only">F</span></span>
      <#break>
    <#default>
      <span id="status-icon-${cid}" class="status-icon status-unknown" title="Inconclusive"><span class="text-only">?</span></span>
      <#break>
  </#switch></a>
  </td>
  <td>${claim.getStatement()?no_esc}</td>
  <td><a href="/assistant/enqueue/claim/${cid}" title="Add experiments linked to this claim to the assistant's queue"><span class="btn-24 btn-queue"><span class="text-only">Enqueue</span></span></a></td>
</tr>
</#list>
</table>

</#if>

<#include "footer.ftlh">