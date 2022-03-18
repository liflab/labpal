<#include "header.ftlh">

<p><a class="btn-24 btn-all-tables" title="Download all tables as a single LaTeX file" href="/tables/tex">Download all tables</a></p>

<#assign tables = lab.getTables()>

<#if (tables?size > 0)>
<p>
<table class="tables">
<#list tables as table>
<#assign tid = table.getId()>
<tr>
<td class="id-cell"><a href="/table/${tid}">${tid}</a></td>
<td class="table-icon"></td>
<td><a href="/table/${tid}">${table.getTitle()}</a></td>
<td><a class="btn-24 btn-csv" href="/table/${tid}/csv?dl=1" title="Download as CSV"><span class="text-only">CSV</span></a></td>
<td><a class="btn-24 btn-tex" href="/table/${tid}/tex?dl=1" title="Download as LaTeX"><span class="text-only">LaTeX</span></a></td>
<td><a class="btn-24 btn-html" href="/table/${tid}/html?dl=1" title="Download as HTML"><span class="text-only">HTML</span></a></td>
<td><a class="nickname" href="/table/${tid}">${table.getNickname()}</a></td>
<td><@statusChunk e=table prefix="t" /></td>
</tr>
</#list>
</table>
</p>
</#if>

<script type="text/javascript">
var tid_e = setTimeout(updateTables, 3100);
</script>

<#include "footer.ftlh">