<#include "header.ftlh">

<div>
<a class="btn-24 btn-all-plots" title="Download all plots as a single PDF file" href="/all-plots">Download all plots</a>
<a class="btn-24 btn-all-plots-latex" title="Download LaTeX macros for using the plots" href="/all-plots-latex">Download LaTeX macros</a>
</div>

<#list lab.getPlots() as plot>
<#assign pid = plot.getId()>

<div class="plot">
<a href="/plot/${pid}" target="_blank" title="Click on plot to view in new window"><img id="img-p${pid}" src="/plot/${pid}" alt="${plot.getNickname()}" /></a>
<div>
<ul>
<li><span class="btn-24 btn-reset" href="plot/${pid}?format=gp&amp;dl=1" title="Refresh this plot" onclick="refreshPlot('${pid}')"><span class="text-only">Refresh</span></span></li>
<li><a class="btn-24 btn-gp" href="plot/${pid}?format=gp&amp;dl=1" title="Download as GnuPlot source file">GP</a></li>
<li><a class="btn-24 btn-gp" href="plot/${pid}?format=dumb" title="View as ASCII art" target="_blank">ASCII</a></li>
<li><a class="btn-24 btn-pdf" href="plot/${pid}?format=pdf&amp;dl=1" title="Download as PDF">PDF</a></li>
<li><a class="btn-24 btn-table" href="table/${plot.getTable().getId()}" title="Show the data for this plot">Table</a></li>
<li><#include "progress-bar-plot.ftlh"></li>
</ul>
</div>
</div>

</#list>

<div style="clear:both"></div>

<script type="text/javascript">
var tid_e = setTimeout(updatePlots, 3100);

function refreshPlot(id) {
  var d = new Date(); // Prevent caching
  $("#img-p" + id).attr("src", "/plot/" + id + "?" + d.getTime());
}
</script>

<#include "footer.ftlh">