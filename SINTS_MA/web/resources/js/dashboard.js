/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


$a = jQuery.noConflict();
$a(document).ready(function() {
    
    var heightWindow = $a(window).height();
    var heightBody   = document.body.clientHeight;
    if(heightWindow > heightBody){
        $a("#footer").css('position', 'fixed');
        $a("#footer").css('bottom', '0');
    } 
    
    //Menu Oficios e memorandos
    // create Editor from textarea HTML element with default set of tools
    //Open and hide submenu of oficios/memos
    $a("#btn-menu-documento").click(function(){
        $a("#documento-submenu").slideToggle("slow");
    });
    
       
    //Text Editor
    $a("#editor").kendoEditor({ 
        tools: [
                "bold",
                "italic",
                "underline",
                "justifyLeft",
                "justifyCenter",
                "justifyRight",
                "justifyFull",
                "insertUnorderedList",
                "insertOrderedList",
                "indent",
                "outdent",
                "subscript",
                "superscript",
                "pdf",
                "print",
//                "createTable",
//                "addRowAbove",
//                "addRowBelow",
//                "addColumnLeft",
//                "addColumnRight",
//                "deleteRow",
//                "deleteColumn",
                "formatting"
//                "fontName"
//                "fontSize",
//                "foreColor"
            ]
    });    
 
    var editor = $a("#editor").data("kendoEditor");

    if(editor != null)
    {
        //$a(editor.body).html($a("#memorando-template").html());
        console.log("entrei aqui");
        if($a("#form-doc\\:editor-hidden").val() != "")
        {
            var memo_number = $a("#doc-memo-create-number").html();
            var oficio_number = $a("#doc-oficio-create-number").html();
            var current_date = $a("#doc-dt").html();
            $a("#editor").html($a("#form-doc\\:editor-hidden").val());
            $a("#editor").find("#doc-memo-create-number").html(memo_number);
            $a("#editor").find("#doc-oficio-create-number").html(oficio_number);
            $a("#doc-dt").html(current_date);
        }
        else
        {
            if($a( "input:checked" ).val() == '1')
                $a(editor.body).html($a("#memorando-template").html());
            else if($a( "input:checked" ).val() == '2')
                $a(editor.body).html($a("#oficio-template").html());
        }
        
        if($a("#form-doc\\:setores" ) != "" || $a( "#form-doc\\:setores" ) != null)
            fillRecipientField();
    }
    
    //Open input for inserting new recipient into database
    $a("#btn-add-recipient").click(function(){
        $a("#doc-create-create_recipient").toggle("show");
    });
    
    //loading doc templates
    $a("input[value='1']").click(function() {
        $a.ajax({
            url : "../../documentoOficial/memorando_template.txt",
            success : function (data) {
                $a(editor.body).html($a("#memorando-template").html());
            }
        });
    });
    $a("input[value='2']").click(function() {
        $a.ajax({
            url : "../../documentoOficial/oficio_template.txt",
            success : function (data) {
                $a(editor.body).html($a("#oficio-template").html());
            }
        });
    });
    
    //Autocomplete for Recipient Name
    
    $a(function() {        
        var availableTags = [];
        $a("#doc-create-destinatarioList").find("li").each(function(){
           availableTags.push($a(this).html()); 
        });
        
        //console.log(availableTags);
        $a( "#form-doc\\:setores" ).autocomplete({
            source: availableTags
        });
    });
    
    //Filling the recipient field in the document body
    function fillRecipientField()
    {
        var recipients = $a(".doc-recipient");
        for(var i =0; i < recipients.length; i++)
           $a(recipients[i]).html("PARA: " + $a("#form-doc\\:setores" ).val());
    }
    
    $a(function() {
        $a( "html" ).on("click", function(){
            fillRecipientField();
        });
    });
    
    
    //Event click on 'salvar' button on the doc page
    $a("#form-doc\\:btn-salvar_doc").hover(function() {        
        var doc_message = $a("#editor");
        $a(doc_message).find(".doc-recipient").attr("class", "doc-recipient-done");
        //store id to 'destinatario' inputHidden id
        var currentRecipient = $a( "#form-doc\\:setores" ).val();
        var recipientForDB = currentRecipient.trim();
        
        
        
        $a("#doc-create-destinatarioList").find("li").each(function(){           
           var listRecipient = $a(this).html();
           listRecipient = listRecipient.trim();
           if(recipientForDB.localeCompare(listRecipient) == 0)
               $a( "#form-doc\\:inputHidden-setor" ).val($a(this).attr("id"));
        });
        
        $a("#form-doc\\:editor-hidden").val($a(doc_message).html());
    }, function(){});
    
    
    //---- Visualizar Documento section
    //adding attributes to the docs list table
    $a("#table-doc-list").attr("data-sort-name", "name");
    $a("#table-doc-list").attr("data-sort-order", "desc");
    $a("#table-doc-list").find("th").attr("data-align", "center");
    
    
    //Changing language and disabling general search feature
    var dataTableList = $a("#table-doc-list");
    var table;

    table = $a("#table-doc-list").DataTable({
                "language": {
                    "lengthMenu": "Mostrar _MENU_ por página",
                    "zeroRecords": "Busca não encontrada",
                    "info": "Mostrando página _PAGE_ de _PAGES_",
                    "infoEmpty": "Dados não cadastrados",
                    "infoFiltered": "(Total de _MAX_ dados)",
                    "search": "Procurar",
                    "previous": "Anterior",
                    "next": "Próximo"
                }
            });
    $a("#table-doc-list_filter").attr("style", "display:none");
    $a("#table-doc-list_paginate").removeClass("dataTables_paginate");
   
    //implementing column filtering
    function filterColumn ( i, val ) {
        $a("#table-doc-list").DataTable().column(i).search(
            val,
            false,
            true
        ).draw();
    }
    
    $a('input.column_filter').on( 'keyup change', function () {
        filterColumn( $a(this).attr('title'),  $a(this).val());
    });
    
    // --- Opening document for viewing and generating pdf
    $a("#btn-view-exportar").click(function(){
        var source = $a("#doc-view-panel")[0];
        var pdf = new jsPDF('p', 'pt', 'a4');
        
      
        var margins = {
            top: 30,
            bottom: 30,
            left: 30,
            width: 612
        };
        
        var elementHandler = {
            '#doc-view-panel': function (element, renderer) {
              return true;
            }
        };
        
        pdf.addHTML(
            source, 
            margins.left,
            margins.top,
            {'width': margins.width, 'elementHandlers': elementHandler, 'pagesplit': true, "background": "white"}, function(){
                pdf.output("dataurlnewwindow");
            }          
        );
    });
});