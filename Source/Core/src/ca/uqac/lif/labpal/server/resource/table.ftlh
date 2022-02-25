<#if onlytable>${tablecontents?no_esc}<#else>
<#include "header.ftlh">

<#assign progress = table.getProgression()>
<#assign tid = table.getId()>

<h2>${table.getTitle()}
<#if progress == 1>
  <span id="status-icon-t${tid}" class="status-icon status-done" title="Done"><span class="text-only">Done</span></span>
<#else>
  <#include "progress-bar-table.ftlh">
</#if>
</h2>

<#if table.getDescription()?has_content>
<p class="description">${table.getDescription()}</p>
</#if>

<div id="tablecontents">
${tablecontents?no_esc}
</div>

<script type="text/javascript">
var tid_e = setTimeout(updateTableStatus, 3100);

function updateTableStatus() {
  $.ajax({
    url: "/tables/status/${tid}"
  }).done(function (data) {
              var status = data[0];
              var progression = data[1];
        	  var status_class = getStatusClass(status);
        	  var e = $("#status-icon-t${tid}");
              $(e).removeClass("status-queued status-prereq status-done status-warning status-failed status-timeout status-ready status-running status-unknown").addClass("status-" + status_class);
              if (status != "RUNNING") {
            	  $("#progress-bar-t${tid}").hide();
              }
              else {
            	  $("#progress-bar-t${tid}").show();
                  $("#progress-bar-val-t${tid}").text(Math.round(progression * 100) + "%");
                  $("#progress-bar-rect-t${tid}").css({"width" : (progression * 50) + "px"});            	  
              }
  })
  $.ajax({
    url: "/table/${tid}/html"
  }).done(function (data) {
              $("#tablecontents").html(data);
              setTimeout(updateTableStatus, 3100);
  })
};
</script>

<#include "footer.ftlh">
</#if>