<#include "header.ftlh">

<p>Every data point in the lab has a unique identifier, called a
<em>LabPal datapoint identifier</em> (LDI). If you know
the identifier of the data point you are looking for, type it in the
box below and LabPal will direct you to the page where you can find it.</p>

<form name="datapoint-search" method="get" action="/find">

${lab.getDoi()} <input type="text" name="id" /> <input class="btn" id="btn-find" type="submit" value="Find" />

<#include "footer.ftlh">