<#include "header.ftlh">

<h2>Lab info</h2>

<table class="status-table">
<tr><th title="The name given to this lab">Lab name:</th><td>${lab.getName()}</td></tr>
<tr><th title="The process responsible for managing the queue of experiments to be run">Lab assistant:</th><td>${lab.getAssistant().getName()}</td></tr>
<#if lab.getDoi()?has_content>
<tr><th title="The Digital Object Identifier assigned to this lab">DOI:</th><td>${lab.getDoi()}</td></tr>
</#if>
<tr><th title="The author of this lab">Author:</th><td>${lab.getAuthor()}</td></tr>
<tr><th title="The starting value used to initialize the lab's random number generator">Random seed:</th><td>${lab.getSeed()}</td></tr>
<tr><th title="The total number of discrete data points contained in this lab">Number of data points:</th><td>${lab.countDataPoints()}</td></tr>
</table>

<#if serialization_message?has_content>
${serialization_message?no_esc}
</#if>

<form style="display:inline-block" id ="download" name="download" method="post" enctype="multipart/form-data" action="/download">
<a href="/download"><button class="btn btn-download">Save lab data</button></a>
</form>
<form style="display:inline-block" name="upload" method="post" enctype="multipart/form-data" action="/upload">
<input class="btn btn-upload" id="btn-upload" type="submit" value="Upload lab data"/>
<input type="file" name="filename" size="40" class="input-upload"/>
</form>

<h2>Progress</h2>

<p>The bar shows the status of completed experiments in this lab.</p>

${bar?no_esc}

<h2>Environment parameters</h2>

<#if environment_message?has_content>
${environment_message}
</#if>

<table class="status-table">
<tr><th title="The name of the host machine running the lab">Host name:</th><td>${lab.getHostName()}</td></tr>
<tr><th title="The name of the operating system running the lab">OS name:</th><td>${osname}, version ${osversion}</td></tr>
<tr><th title="Information about the architecture of the machine running the lab">Architecture:</th><td>${osarch}</td></tr>
<tr><th title="A factor of the speed of the host relative to a reference machine">Relative speed:</th><td>${lab.getParkMips()}</td></tr>
</table>

<#assign non_unique = lab.getNonUniqueNicknames()>
<#if (non_unique?size > 0)>
<p class="message warning"><span>Some lab elements have conflicting nicknames:
  <#list non_unique as nick>
  <tt>${nick}</tt>
  </#list>
</span>
</#if>

<h2>Claims</h2>

<p>Claims are user-defined assertions on the lab's data that are automatically checked.</p>

{%CLAIM_DIV%}

<!--
{%REPORTING_DIV%}
-->

<script type="text/javascript">
var tid_e = setTimeout(updateLabBar, 5250);
</script>

<#include "footer.ftlh">