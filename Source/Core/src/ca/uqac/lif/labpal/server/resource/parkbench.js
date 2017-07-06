$(document).ready(function() {
    //select all
    $("table.exp-table .top-checkbox").click(function() {
      for(var j= 1; j <= $(" .side-checkbox").length; j++){
        //top-checkbox check only all enabled side-checkbox
        $("table.exp-table .side-checkbox-"+j).prop("checked", (this.checked && !($(" .side-checkbox-"+j).prop("disabled"))));
      }
      //$(this).closest("table.exp-table").find(".side-checkbox").not(this).prop("checked", this.checked);
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
    
    //get upload file name
    $(" .btn-upload").click(function() {
    var fullPath = $(" .input-upload").val(), expires = new Date(), today = new Date();
    if (fullPath) {
      var startIndex = (fullPath.indexOf('\\') >= 0 ? fullPath.lastIndexOf('\\') : fullPath.lastIndexOf('/'));
      var filename = fullPath.substring(startIndex);
        if (filename.indexOf('\\') === 0 || filename.indexOf('/') === 0) {
        filename = filename.substring(1);
        }
      $(" .input-upload-clone").val(filename);
      //expires.setTime(today.getTime() + (5000));
      //document.cookie = "ck-filename=" + encodeURIComponent(filename) + ";expires=" + expires.toGMTString();
    }
    });
    
    //confirmation popup
    function afficherPopupConfirmationLien(question) {
    $('body').append('<div id="popupconfirmation" class="popup" title="Save lab data"></div>');
    $("#popupconfirmation").html(question);

    var popup = $("#popupconfirmation").dialog({
        autoOpen: true,
        width: 300,
        dialogClass: 'dialogstyleperso',
        hide: "fade",
        buttons: [
            {
                text: ".ZIP",
                class: "ui-state-question",
                click: function () {
                    $(" .input-download").val("zip");
                    $('#download').submit();//window.location="download";
                    $(this).dialog("close");
                    $("#popupconfirmation").remove();
 
                }
            },
            {
                text: ".JSON",
                class: "ui-state-question",
                click: function () {
                    $(" .input-download").val("json");
                    $('#download').submit();
                    $(this).dialog("close");
                    $("#popupconfirmation").remove();
                }
            }
        ]
    });
 
    $("#popupconfirmation").prev().addClass('ui-state-question-title');
 
    return popup;
  }
  
  //open popup on click of save button
  $(" .btn-download").click(function(event) {
       event.preventDefault();
       afficherPopupConfirmationLien('Select the type of file to download ');
     });

    
    
    
    
    
});
