<#-- Displays a progress bar; assumes that e refers to an experiment -->
<#assign w = (e.getProgression() * 50)>
<#assign p = (e.getProgression() * 100)>
<span class="progress-bar" id="progress-bar-${e.getId()}" style="width:50px<#if !e_running>;display:none</#if>"><span class="done" id="progress-bar-rect-${e.getId()}" style="width:${w}px"></span></span>
<span id="progress-bar-val-${e.getId()}"<#if !e_running> style="display:none"</#if>>${p?round}%</span><span class="text-only"> completed</span>
