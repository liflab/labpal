<#assign params = g.getInputParameters()>
<#assign gid = g.getId()>
   <p>
   <table class="exp-table tablesorter">
   <thead>
   <tr><th class="top-td"><input type="checkbox" id="top-checkbox-${gid}" name="top-checkbox-${gid}" /></th><th>#</th><#list params as p><th>${p}</th></#list><th>Status</th></tr>
   </thead>
   <tbody>
   <#list experiments as e>
   <#assign status = e.getStatus()>
   <#assign id = e.getId()>
   <#assign e_running = (status == "RUNNING" || status == "PREPARING")>
   <tr>
    <td class="exp-chk"><input type="hidden" id="exp-chh-g-${gid}-${id}" name="exp-chh-g-${gid}-${id}" value="0" /><input type="checkbox" class="side-checkbox side-checkbox-${id}" id="exp-chk-g-${gid}-${id}" name="exp-chk-g-${gid}-${id}"></td>
    <td class="id-cell"><a href="/experiment/${id}">${id}</a></td>
    <#list params as p>
      <td>${e.read(p)}</td>
    </#list>
    <td><@statusChunk e=e prefix="e" withbar=true queued=lab.isQueued(e) /></td>
   </tr>
   </#list>
   </tbody>
   </table>
   </p>