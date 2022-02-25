<#include "header.ftlh">

<#assign tables = lab.getTables()>

<#if (tables?size > 0)>
<table class="tables">
<#list tables as table>
<#assign tid = table.getId()>
<tr>
<td class="id-cell"><a href="/table/${tid}">${tid}</a></td>
<td class="table-icon"></td>
<td><a href="/table/${tid}">${table.getTitle()}</a></td>
<td><a class="btn-csv" href="/table-export?id=${tid}&amp;format=csv&amp;dl=1" title="Download as CSV"><span class="text-only">CSV</span></a></td>
<td><a class="btn-tex" href="/table-export?id=${tid}&amp;format=tex&amp;dl=1" title="Download as LaTeX"><span class="text-only">LaTeX</span></a></td>
<td><a class="btn-html" href="/table-export?id=${tid}&amp;format=html&amp;dl=1" title="Download as HTML"><span class="text-only">HTML</span></a></td>
<td><a class="nickname" href="/table/${tid}">${table.getNickname()}</a></td>
<td><#include "progress-bar-table.ftlh"></td>
</tr>
</#list>
</table>
</#if>

<script type="text/javascript">
var tid_e = setTimeout(updateTables, 3100);
</script>

<#include "footer.ftlh">