<#include "header.ftlh">

<#assign claims = lab.getClaims()>

<p>Claims are programmatically calculated conditions expressed on the contents of a lab.</p>

<#list claims as claim>
<p>${claim.getId()}: <a href="/explain?id=C${claim.getId()}">${claim.evaluate()}</a></p>
</#list>

<#include "footer.ftlh">