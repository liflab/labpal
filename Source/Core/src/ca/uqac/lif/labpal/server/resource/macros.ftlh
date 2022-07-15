<#include "header.ftlh">

<p><a class="btn-24 btn-all-tables" title="Download all macros as a single LaTeX file" href="/macros/tex">Download all macros</a></p>

<#list groups as g>

<#assign macros = g.getSortedObjects()>

<#if macros?size == 0>
  <#continue>
</#if>

<#if (groups?size > 1)>
  <#-- Show group title and description only if there is more than one group -->
  <div class="around-pulldown">
  <h2 class="pulldown" title="Click to show/hide the list of macros for this group">${g.getName()}</h2>
  <div class="pulldown-contents">
  <p class="group-description">${g.getDescription()}</p>
</#if>

<p>
<#include "macro-list.ftlh">
</p>

<#if (groups?size > 1)>
  </div>
  </div>
</#if>

</#list>

<script type="text/javascript">
//var mid_e = setTimeout(updateMacros, 3100);
</script>

<#include "footer.ftlh">