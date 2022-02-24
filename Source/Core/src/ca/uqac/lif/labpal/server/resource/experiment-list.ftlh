<#assign params = g.getInputParameters()>
<#assign gid = g.getId()>
   <table class="exp-table tablesorter">
   <thead>
   <tr><th class="top-td"><input type="checkbox" name="top-checkbox-${gid}" /></th><th>#</th><#list params as p><th>${p}</th></#list><th>Status</th></tr>
   </thead>
   <tbody>
   <#list experiments as e>
   <#assign status = e.getStatus()>
   <#assign id = e.getId()>
   <#assign e_running = (status == "RUNNING" || status == "RUNNING_PREREQ")>
   <tr>
    <td class="exp-chk"><input type="hidden" id="exp-chh-g-${gid}-${id}" name="exp-chh-g-${gid}-${id}" value="0" /><input type="checkbox" class="side-checkbox side-checkbox-${id}" id="exp-chk-g${gid}-${id}" name="exp-chk-g${gid}-${id}"></td>
    <td class="id-cell"><a href="/experiment/${id}">${id}</a></td>
    <#list params as p>
      <td>${e.read(p)}</td>
    </#list>
    <td>
    <#if lab.isQueued(e)>
      <span id="status-icon-${id}" class="status-icon status-queued" title="Queued"><span class="text-only">Q</span></span>
    <#else>
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
    </#if>
    <#include "progress-bar-experiment.ftlh">
    </td>
   </tr>
   </#list>
   </tbody>
   </table>