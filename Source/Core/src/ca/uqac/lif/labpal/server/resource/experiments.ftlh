<#include "header.ftlh">

<div id="experiment-filter">
<h2 class="text-only">Filter</h2>
<p>Enter experiment numbers or ranges of numbers to filter the list of experiments.</p>
<form name="exp-filter" method="post" action="/experiments" style="display:inline-block">
<input type="text" class="top-filter" name="filter" /> <input type="submit" class="btn btn-filter" value="Filter" />
</form>
<form name="reset-filter" method="post" action="/experiments" style="display:inline-block">
<input type="hidden" name="reset" value="true" />
<input type="submit" class="btn btn-clean" value="Clear filter" />
</form>
</div>

<#if filter?has_content && filter != "">
<p>The list of experiments is filtered according to the condition:</p>
<blockquote>
<pre>${filter}</pre>
</blockquote>
</#if>

<#-- Fetch groups from lab -->
<form name="experiments" method="post" action="/assistant">

<#if !filter?has_content>
  <#assign filter = "">
</#if>

<input type="hidden" name="filter" value="${filter}" />

<#list groups as g>

<#assign experiments = g.getSortedObjects()>

<#if experiments?size == 0>
  <#continue>
</#if>

<#if (groups?size > 1)>
  <#-- Show group title and description only if there is more than one group -->
  <div class="around-pulldown">
  <h2 class="pulldown" title="Click to show/hide the list of experiments for this group">${g.getName()}</h2>
  <div class="pulldown-contents">
  <p class="group-description">${g.getDescription()}</p>
</#if>

<#include "experiment-list.ftlh">
   
<#if (groups?size > 1)>
  </div>
  </div>
</#if>

</#list>

<div id="select">
<p>With selected experiments&hellip;</p>
<input type="submit" class="btn btn-queue" name="enqueue" value="Add to queue" />
<!--
<input type="submit" class="btn btn-unqueue" name="unqueue" value="Remove from queue" />
-->
<input type="submit" class="btn btn-reset" name="reset" value="Reset" />
<input type="submit" class="btn btn-clean" name="clean" value="Clean" />
</div>

</form>

<script type="text/javascript">
var tid_e = setTimeout(updateExperiments, 2000);
</script>

<#include "footer.ftlh">