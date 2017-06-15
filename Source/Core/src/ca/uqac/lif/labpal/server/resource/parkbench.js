$(document).ready(function() {
    //selectionner tout
    $("table.exp-table .top-checkbox").click(function() {
      $(this).closest("table.exp-table").find(".side-checkbox").not(this).prop("checked", this.checked);
    });
    
    //filter par énumération ou intervalle
   $("table.exp-table .top-filter").keyup(function() {
    var hideLigne = $("table.exp-table .top-filter").val();
    if (hideLigne ==""){
     $("table.exp-table .tr").show();
    }
    else {
	    $("table.exp-table .tr").hide();
	    //vérifier si énumération ou intervalle
	    if(hideLigne.indexOf(',')>-1){
	    //cas énumértion 
	    var hideLigneArray = hideLigne.split(",");
		    for(var i= 0; i < hideLigneArray.length; i++){
		    	$("table.exp-table .tr_"+hideLigneArray[i]).show();
		    }
	    }
	    else if(hideLigne.indexOf('-')>-1){
	    //intervalle
	    var hideLigneArray = hideLigne.split("-");
		    for(var i= hideLigneArray[0]; i <= hideLigneArray[1]; i++){
		    	$("table.exp-table .tr_"+i).show();
		    }	    
	    }
	    else $("table.exp-table .tr_"+hideLigne).show();
	    }
    });
 /*
    $("table.exp-table").tablesorter();
    $(".pulldown-contents").hide();
    $(".pulldown").addClass("closed");
    $(".pulldown").click(function() {
      $(this).toggleClass("closed");
      $(this).closest("div.around-pulldown").find(".pulldown-contents").toggle();
    });*/
});
