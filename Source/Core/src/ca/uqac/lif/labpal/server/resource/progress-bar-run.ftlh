<#-- Displays a progress bar; assumes that r refers to an assistant run -->
<#assign w = (r.getProgression() * 50)>
<#assign p = (r.getProgression() * 100)>
<span class="line-align">
<span class="progress-bar" id="progress-bar-r${r.getId()}" style="width:50px<#if !r_running>;display:none</#if>"><span class="done" id="progress-bar-rect-r${r.getId()}" style="width:${w}px"></span></span>
<span id="progress-bar-val-r${r.getId()}"<#if !r_running> style="display:none"</#if>${p?round}%</span><span class="text-only"> completed</span>
</span>