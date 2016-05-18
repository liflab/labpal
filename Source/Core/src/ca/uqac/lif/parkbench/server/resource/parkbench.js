$(document).ready(function() {
    $("table.exp-table .top-checkbox").click(function() {
      $(this).closest("table.exp-table").find(".side-checkbox").not(this).prop("checked", this.checked);
    });
    $("table.exp-table").tablesorter();
    $(".pulldown-contents").hide();
    $(".pulldown").addClass("closed");
    $(".pulldown").click(function() {
      $(this).toggleClass("closed");
      $(this).closest("div.around-pulldown").find(".pulldown-contents").toggle();
    });
});
