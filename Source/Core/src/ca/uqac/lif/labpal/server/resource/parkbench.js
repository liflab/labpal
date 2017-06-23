$(document).ready(function() {
    //slect all
    $("table.exp-table .top-checkbox").click(function() {
      $(this).closest("table.exp-table").find(".side-checkbox").not(this).prop("checked", this.checked);
    });
    
    //authorized inputs
   $("table.exp-table .top-filter").keyup(function() {
    var input = $(this).val();
    var regex = new RegExp("^[0-9|,|-]+$");
    if (!regex.test(input)) $(this).val(input.substr(0, input.length-1));
   });
   
   //filter by enumeration or/and interval
   $("table.exp-table .btn-filter").click(function() {
    //var hideLigne = $("table.exp-table .top-filter").val().trim();
    var hideLigne = $("table.exp-table .top-filter").val();
    //
    if (hideLigne ==""){
     $("table.exp-table .tr").show();
     $("table.exp-table .side-checkbox").prop("disabled", false);
    }
    //
    else {
	    $("table.exp-table .tr").hide();
	    $("table.exp-table .side-checkbox").prop("disabled", true);
	    var hideLigneArray = hideLigne.split(",");
		    for(var i= 0; i < hideLigneArray.length; i++){
		    if (hideLigneArray[i]!='') {//inputs like ',9,'
		     if(hideLigneArray[i].indexOf('-')>-1){
		     var hideLigneArray2 = hideLigneArray[i].split("-");
		     if (hideLigneArray2[0]!='' && hideLigneArray2[1]!='') {//inputs like '-5'
		      for(var k= parseInt(hideLigneArray2[0]); k <= parseInt(hideLigneArray2[1]); k++){	      
		    	$("table.exp-table .tr-"+k).show();
		    	$("table.exp-table .side-checkbox-"+k).prop("disabled", false);
		      }
		     }//if (hideLigneArray2[0]!='' && hideLigneArray2[1]!='')  
		     }
		     else {
		     $("table.exp-table .tr-"+hideLigneArray[i]).show();
		     $("table.exp-table .side-checkbox-"+hideLigneArray[i]).prop("disabled", false);
		     }
		     }//if (hideLigneArray[i]!='')
		    }
	    }
    });
 
    $("table.exp-table").tablesorter();
    $(".pulldown-contents").hide();
    $(".pulldown").addClass("closed");
    $(".pulldown").click(function() {
      $(this).toggleClass("closed");
      $(this).closest("div.around-pulldown").find(".pulldown-contents").toggle();
    });

    $("pre.multiline").click(function() {
      $(this).toggleClass("open");
    });
<<<<<<< HEAD

=======
>>>>>>> 5c05dad6273d5af0ff457fe3273e6f2efd08ee9e
});
