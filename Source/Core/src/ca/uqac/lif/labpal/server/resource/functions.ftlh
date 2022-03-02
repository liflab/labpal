<#-- Displays the appropriate icon based on the status of a lab element -->
<#macro statusIcon status prefix id haswarnings=false isqueued=false timeout=false>

<#switch status>
	<#case "UNINITIALIZED">
		<span id="status-icon-${prefix}${id}" class="status-icon status-prereq" title="Prerequisites not fulfilled"><span class="text-only">P</span></span>
		<#break>
	<#case "DONE">
		<#if haswarnings>
			<span id="status-icon-${prefix}${id}" class="status-icon status-warning" title="Done with warnings"><span class="text-only">W</span></span>
		<#else>
			<span id="status-icon-${prefix}${id}" class="status-icon status-done" title="Done"><span class="text-only">D</span></span>
		</#if>
		<#break>
	<#case "FAILED">
		<span id="status-icon-${prefix}${id}" class="status-icon status-failed" title="Failed"><span class="text-only">F</span></span>
		<#break>
	<#case "INTERRUPTED">
		<#if timeout>
			<span id="status-icon-${prefix}${id}" class="status-icon status-timeout" title="Timed out"><span class="text-only">T</span></span>
		<#else>
			<span id="status-icon-${prefix}${id}" class="status-icon status-failed" title="Cancelled"><span class="text-only">C</span></span>
		</#if>
		<#break>
	<#case "READY">
		<span id="status-icon-${prefix}${id}" class="status-icon status-ready" title="Ready"><span class="text-only">r</span></span>
		<#break>
	<#case "RUNNING">
		<span id="status-icon-${prefix}${id}" class="status-icon status-running" title="Running"><span class="text-only">r</span></span>
		<#break>
	<#case "PREPARING">
		<span id="status-icon-${prefix}${id}" class="status-icon status-running" title="Preparing"><span class="text-only">r</span></span>
		<#break>
	<#default>
		<span id="status-icon-${prefix}${id}" class="status-icon status-unknown" title="Unknown"><span class="text-only">?</span></span>
		<#break>
</#switch>
</#macro>

<#-- Displays a CSS progress bar for a lab element -->
<#macro progressBar status prefix id progression>
	<#local p = (progression * 100)>
	<#local w = p/2>
	<#local isrunning = (status == "RUNNING")>
	<span class="progress-bar" id="progress-bar-${prefix}${id}" style="width:50px;<#if !isrunning>display:none;</#if>"><span class="done" id="progress-bar-rect-${prefix}${id}" style="width:${w}px"></span></span>
	<span id="progress-bar-val-${prefix}${id}"<#if !isrunning> style="display:none"</#if>>${p?round}%</span><span class="text-only"> completed</span>
</#macro>

<#-- Displays the "status chunk" of a lab element made of a status icon and
     a progress bar, only if the element is currently running -->
<#macro statusChunk e prefix withbar=true queued=false>
	<#local warnings = false>
	<#local timeout = false>
	<span class="line-align">
	<@statusIcon status=e.getStatus() id=e.getId() prefix=prefix haswarnings=warnings isqueued=queued timeout=timeout />
	<#if withbar>
		<@progressBar status=e.getStatus() prefix=prefix id=e.getId() progression=e.getProgression() />
	</#if>
	</span>
</#macro>