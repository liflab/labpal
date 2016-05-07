function select_all() {
  if ($("#top-checkbox").prop("checked"))
  {
    $("table.exp-table tr td input:checkbox").prop("checked", true);
  }
  else
  {
    $("table.exp-table tr td input:checkbox").prop("checked", false);
  }
}