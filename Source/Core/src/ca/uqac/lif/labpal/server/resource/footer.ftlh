</div> <!-- /content -->
</div> <!-- /main -->

<div id="top-menu">
<h2 class="text-only">Main menu</h2>
<ul>

<#-- Highlighting selected menu element -->
<#assign sel_home = "">
<#assign sel_status = "">
<#assign sel_experiments = "">
<#assign sel_assistant = "">
<#assign sel_plots = "">
<#assign sel_tables = "">
<#assign sel_macros = "">
<#assign sel_find = "">
<#assign sel_help = "">
<#switch menuhighlight!"top-menu-home">
  <#case "top-menu-home">
    <#assign sel_home = "selected">
    <#break>
   <#case "top-menu-status">
    <#assign sel_status = "selected">
    <#break>
   <#case "top-menu-experiments">
    <#assign sel_experiments = "selected">
    <#break>
   <#case "top-menu-assistant">
    <#assign sel_assistant = "selected">
    <#break>
   <#case "top-menu-plots">
    <#assign sel_plots = "selected">
    <#break>
   <#case "top-menu-tables">
    <#assign sel_tables = "selected">
    <#break>
   <#case "top-menu-macros">
    <#assign sel_macros = "selected">
    <#break>
   <#case "top-menu-find">
    <#assign sel_find = "selected">
    <#break>
   <#case "top-menu-help">
    <#assign sel_help = "selected">
    <#break>
</#switch>

<li class="${sel_home}" title="Show the lab's description" ><a href="/index" id="top-menu-home"><span class="menu-text">Home</span></a></li>
<li class="${sel_status}" title="Display various statistics about the lab"><a href="/status" id="top-menu-status"><span class="menu-text">Status</span></a></li>
<li class="${sel_experiments}" title="Show the list of experiments in this lab" ><a href="/experiments" id="top-menu-experiments"><span class="menu-text">Experiments</span></a></li>
<li class="${sel_assistant}" title="Control the execution of experiments"><a href="/assistant" id="top-menu-assistant"><span class="menu-text">Assistant</span></a></li>
<li class="${sel_plots}" title="View the plots created from the lab's data"><a href="/plots" id="top-menu-plots"><span class="menu-text">Plots</span></a></li>
<li class="${sel_tables}" title="View the tables created from the lab's data"><a href="/tables" id="top-menu-tables"><span class="menu-text">Tables</span></a></li>
<li class="${sel_macros}" title="View the macros created from the lab's data"><a href="/macros" id="top-menu-macros"><span class="menu-text">Macros</span></a></li>
<li class="${sel_find}" title="Find a data point by its identifier"><a href="/find" id="top-menu-find"><span class="menu-text">Find</span></a></li>
<li class="${sel_help}" title="Get some help on how to use LabPal"><a href="/help" id="top-menu-help"><span class="menu-text">Help</span></a></li>
</ul>
</div>

<div id="footer">
<div id="logo-container"><a href="https://liflab.github.io/labpal" title="LabPal ${versionstring}"><span id="labpal-logo"><span>LabPal ${versionstring}</span></span></a></div>
<div id="footer-text">&copy; 2014-2022 <a href="https://liflab.ca">Laboratoire d'informatique formelle</a>, Universit&eacute; du Qu&eacute;bec &agrave; Chicoutimi.
</div>
</div> <!-- /footer -->

</body>
</html>