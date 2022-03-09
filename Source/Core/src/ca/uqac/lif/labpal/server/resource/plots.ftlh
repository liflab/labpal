<#include "header.ftlh">

<div>
<a class="btn-24 btn-all-plots" title="Download all plots as a single PDF file" href="/plots/pdf">Download all plots</a>
<a class="btn-24 btn-all-plots-latex" title="Download LaTeX macros for using the plots" href="/plots/tex">Download LaTeX macros</a>
</div>

<ul class="plotlist">
<#list lab.getPlots() as plot>
<#assign pid = plot.getId()>

<li>
<a href="/plot/${pid}" target="_blank" title="Click on plot to view in new window"><img id="img-p${pid}" src="/plot/${pid}/png" alt="<#if plot.getNickname()?has_content>${plot.getNickname()}<#else>Plot ${pid}</#if>" loading="lazy" /></a>
<div>
<ul class="line-align">
<li><span style="padding-left:32px;padding-right:0" class="btn-24 btn-reset" href="/plot/${pid}?format=gp&amp;dl=1" title="Refresh this plot" onclick="refreshPlot('${pid}')"><span class="text-only">Refresh</span></span></li>
<li><a style="padding-right:0" class="btn-24 btn-gp" href="/plot/${pid}?format=gp&amp;dl=1" title="Download as GnuPlot source file"><span class="text-only">GP</span></a></li>
<li><a style="padding-right:0" class="btn-24 btn-gp" href="/plot/${pid}?format=dumb" title="View as ASCII art" target="_blank"><span class="text-only">ASCII</span></a></li>
<li><a style="padding-right:0" class="btn-24 btn-pdf" href="/plot/${pid}?format=pdf&amp;dl=1" title="Download as PDF"><span class="text-only">PDF</span></a></li>
<li><a style="padding-right:0" class="btn-24 btn-table" href="/table/${plot.getTable().getId()}" title="Show the data for this plot"><span class="text-only">Table</span></a></li>
<li><a style="padding-left:32px;padding-right:0" class="btn-24 btn-queue" href="/assistant/enqueue/plot/${plot.getId()}" title="Add experiments linked to this plot to the assistant's queue"><span class="text-only">Enqueue</span></a></li>
<li><@statusChunk e=plot prefix="p" /></li>
</ul>
<p>${plot.getTitle()}</p>
</div>
</li>

</#list>
</ul>

<div style="clear:both"></div>

<script type="text/javascript">
var tid_e = setTimeout(updatePlots, 3100);

function refreshPlot(id) {
  var d = new Date(); // Prevent caching
  $("#img-p" + id).attr("src", "/plot/" + id + "?" + d.getTime());
}
</script>

<#include "footer.ftlh">