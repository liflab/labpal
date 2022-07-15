<#assign gid = g.getId()>
   <p>
   <dl>
   <#list macros as m>
   <#assign status = m.getStatus()>
   <#assign id = m.getId()>
   <#assign m_running = (status == "RUNNING" || status == "PREPARING")>
   <dt><strong>${m.getName()}:</strong> ${m.getValue()!} </dt>
   <dd>${m.getDescription()?no_esc}</dd>
   </#list>
   </dl>
   </p>