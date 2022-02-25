<#-- Displays a progress bar; assumes that plot refers to a plot -->
<#assign p = (plot.getProgression() * 100)>
<#assign w = p/2>

<#assign id = plot.getId()>
<#assign status = plot.getStatus()>
  <#switch status>
      <#case "UNINITIALIZED">
        <span id="status-icon-p${id}" class="status-icon status-prereq" title="Prerequisites not fulfilled"><span class="text-only">P</span></span>
        <#break>
      <#case "DONE">
        <span id="status-icon-p${id}" class="status-icon status-done" title="Ready"><span class="text-only">D</span></span>
        <#break>
      <#case "DONE_WARNING">
        <span id="status-icon-p${id}" class="status-icon status-warning" title="Done with warnings"><span class="text-only">W</span></span>
        <#break>
      <#case "FAILED">
        <span id="status-icon-p${id}" class="status-icon status-failed" title="Failed"><span class="text-only">F</span></span>
        <#break>
      <#case "CANCELLED">
        <span id="status-icon-p${id}" class="status-icon status-failed" title="Cancelled"><span class="text-only">C</span></span>
        <#break>
      <#case "TIMEOUT">
        <span id="status-icon-p${id}" class="status-icon status-timeout" title="Timed out"><span class="text-only">T</span></span>
        <#break>
      <#case "READY">
        <span id="status-icon-p${id}" class="status-icon status-ready" title="Ready"><span class="text-only">r</span></span>
        <#break>
       <#case "RUNNING">
        <span id="status-icon-p${id}" class="status-icon status-running" title="Running"><span class="text-only">r</span></span>
        <#break>
       <#case "RUNNING_PREREQ">
        <span id="status-icon-p${id}" class="status-icon status-running" title="Running prerequisites"><span class="text-only">r</span></span>
        <#break>
  </#switch>

<span class="progress-bar" id="progress-bar-p${plot.getId()}" style="width:50px;<#if status != "RUNNING">display:none;</#if>"><span class="done" id="progress-bar-rect-p${plot.getId()}" style="width:${w}px"></span></span>
<span id="progress-bar-val-p${plot.getId()}">${p?round}%</span><span class="text-only"> completed</span>
