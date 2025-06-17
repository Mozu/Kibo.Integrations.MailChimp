$(document).ready(function () {
	getVersion();
    $("#secondTab-li").hide();
    $("#thirdTab-li").hide();
    $("#fourthTab-li").hide();
    var isfirstTime = "true";
    $(".configureEditMailchimpTxt").each(function () {
        if ($(this).val() != "") {
            isfirstTime = $(this).val();
        }
    });
    if (isfirstTime != "true") {
        $("#secondTab-li").show();
        $("#thirdTab-li").show();
        $("#fourthTab-li").show();
        $("#listMapConfDiv").show();
        getListSiteDD();
    }
    $(".configureEditMailchimpTxt").each(function () {
        $(this).change(function () {
            if ($(this).val() == "" || $(this).val() == null) {
                alert("api key can not be blank");
            }
        });
    });
    $("#infoTab").click(function () {
       	 $("#batchSaveSuccess").hide();
       	 $("#invalidList").hide();
         activateContentPane(this);
    });
    $("#firstTab").click(function () {
   	    $("#batchSaveSuccess").hide();
   	     $("#invalidList").hide();
        activateContentPane(this);
    });
    $("#secondTab").click(function () {
        var url = "api/job/getMailchimpSettings";
        activateContentPane(this);
        getSyncTab(url);
    });
    $("#thirdTab").click(function () {
        activateContentPane(this);
        getJobHistory();
        $("#batchSaveSuccess").hide();
         $("#invalidList").hide();
        $('#historyTable').dataTable().fnClearTable();
        $('#historyTable').dataTable(
				{"bFilter":false,
					 "bSort": true,
					 "bAutoWidth": false,
					 "bDestroy":true,
					 "aaSorting": [[2,'desc']],
					 columnDefs: [
					              { type: 'date-dd-mmm-yyyy', targets: 2 }
					            ]}	
		).fnDraw();
    });
    $("#fourthTab").click(function () {
    	
    	 $("#batchSaveSuccess").hide();
    	  $("#invalidList").hide();
       
        var url = "getCampaigns";
        activateContentPane(this);
        getDiscountTab(url);
      //  geDiscountCampaignMap();
    });
    
    $( ".configureEditMailchimpTxt" )
    .focusout(function(event) {
    	
    	saveFirstTab(event);
    	
    });
    
    $( ".configureEditMailchimpTxt" ).keydown(function (event){
	    if(event.keyCode == 13){
	       saveFirstTab(event);
	    }
});
    
    //site and list drop down change event on first tab
    $( "#mapListId1" ).change(function(event) {
    	
    	if($(this).val() != 0 &&  $( "#mapSiteId1" ).val() != 0)
    		{
    		saveFirstTab(event);
    		}
    	});
    
   
    
    //campaign drop down change event on first tab
    $( "#mcCampaignList" ).change(function() {
    	
    	if($(this).val() != 0 &&  $( "#mcDiscountList" ).val() != 0)
    		{
    		saveDiscountCampaign();
    		}
    	});
    //discount drop down change event 
    $( "#mcDiscountList" ).change(function() {

    	if($(this).val() != 0 &&  $( "#mcCampaignList" ).val() != 0)
		{
    		saveDiscountCampaign();
		}
  	});

    // to save data on second tab
    $("#refreshHistory").click(function (event) {
        event.preventDefault();
        getJobHistory();
        $("#batchSaveSuccess").hide();
        $('#historyTable').dataTable().fnClearTable();
        $('#historyTable').dataTable(
				{"bFilter":false,
					 "bSort": true,
					 "bAutoWidth": false,
					 "bDestroy":true,
					 "aaSorting": [[2,'desc']],
					 columnDefs: [
					              { type: 'date-dd-mmm-yyyy', targets: 2 }
					            ]}	
		).fnDraw();
    });

    // to save data on second tab
    $("#createBatchEntry").click(function (event) {
        event.preventDefault();
        var data =  $("#mcDirectionDD").val();
        var orderExportDate=  $("#datepicker").val();
        
        $.ajax({
            headers: {
                'Content-Type': 'application/javascript'
            },
            url: "api/job/saveBatchEntry?batchEntryParam=" + $("#mcDirectionDD").val() + ":" + $("#datepicker").val(),
            type: "POST",
            data: data,
            success: function (data) {
                if (data == "SUCCESS") {
                    $("#batchSaveSuccess").show();
                } else if (data=="DISABLED") {
                	$("#appDisabled").show();
                } else if (data=="INVALIDLIST") {
                	$("#invalidList").show();
                } else {
                    $("#invalidApiKey1").show();
                }
            },
            error: function (data) {
                $("#content").hide();
            }
        });
    });
    $(".configureEditMailchimpBtn").click(function (event) {
        event.preventDefault();
        $(".cancelBtn").show();
        $(this).hide();
        $(".configureEditMailchimpTxt").each(function () {
            $(this).parent().show();
        });
    });
    
  
    
    
    $("#btnConnect").click(function (event) {
        event.preventDefault();
        $("#secondTab").click();
    });
  
    $("#infoTab").css("color", "#D74536");

    $("#saveDiscount").click(

    		function (event) {
    		    event.preventDefault();

    		    saveDiscountCampaign();
    		});

    		$("#addDiscountSoft").click(

    		function (event) {
    		    event.preventDefault();

    		    var trData = $("<tr/>");
    		    trData.attr("class", "tempRows");
    		    var tdData_1 = $("<td/>");
    		    var p_Data1 = $("<p/>");
    		    $(p_Data1).html(
    		    $("#mcCampaignList option:selected").text());
    		    $(tdData_1).append(p_Data1);
    		    $(trData).append(tdData_1);

    		    var tdData_2 = $("<td/>");
    		    var p_Data2 = $("<p/>");
    		    $(p_Data2).html(
    		    $("#mcDiscountList option:selected").attr("discountName"));
    		    $(tdData_2).append(p_Data2);
    		    $(trData).append(tdData_2);
    		    $("#addDiscountAjaxBody").append(
    		    trData);

    		});

    		$("#backToHistory").click(function(){
    			$("#popUpDiv").fadeOut();
    			$("#mainHistory").fadeIn();
				
    		});
    		
    		$(".removeDisCampMapping").click(function(){
    			var discountId = $(this).attr("id");
    			removeDisCampMapping(discountId);
    		});
	}); //document.ready end

		function getJobHistory() {
			$(".tempRows").each(function() {
				$(this).remove();
			});
			var data = "" + $("#tenantIdHdn").val() + "";
			$.ajax({
				headers : {
					'Content-Type' : 'application/javascript'
				},
				url : "api/jobHistory/getJobHistory",
				type : "GET",
		
				success : function(data) {
				
					var dataStr = data.historyData.substring(1,
							data.historyData.length - 1);
					var hitoryJson = $.parseJSON(dataStr);
					if(hitoryJson != null && hitoryJson != "")
						{
						
						$('#historyTable').dataTable().fnClearTable();
				//	var batchModelList = hitoryJson.batchModelList;
					var index = 0;
					$.each(hitoryJson, function(idx, obj) {
						index = index + 1;
						var trData = $("<tr/>");
						trData.attr("class", "tempRows");
						
						if(obj.errorCount == 0)
							{
							
							
							$('#historyTable').dataTable(
									{"bFilter":false,
										 "bSort": true,
										 "bDestroy":true,
										 "aaSorting": [[2,'desc']],
										 columnDefs: [
										              { type: 'date-dd-mmm-yyyy', targets: 2 }
										            ]}	
									).
						  	fnAddData( [
						    	obj.syncDirection,
						        obj.batchStatus,
						        obj.createdDate,
						        obj.recordCount,
						        obj.errorCount
						        ]
						    );
							}
						else
							{
							$('#historyTable').dataTable(
									{"bFilter":false,
										 "bSort": true,
										 "bDestroy":true,
										 "aaSorting": [[2,'desc']],
										 columnDefs: [
										              { type: 'date-dd-mmm-yyyy', targets: 2 }
										            ]}	
									).
							  	fnAddData( [
							    	obj.syncDirection,
							        obj.batchStatus,
							        obj.createdDate,
							        obj.recordCount,
							        "<a href='#' onClick='javascript:showSkipError("+obj.jobExecutionId+")' id="+obj.jobExecutionId+">"+obj.errorCount+"</a>"
							        ]
							    );
							}
		
					});
					
					 $('#historyTable').dataTable(
								{"bFilter":false,
									 "bSort": true,
									 "bDestroy":true,
									 "aaSorting": [[2,'desc']],
									 columnDefs: [
									              { type: 'date-dd-mmm-yyyy', targets: 2 }
									            ]}	
						).fnDraw();
						}
				
		
				},
				error : function(data) {
		
					$("#content").hide();
				}
			});
			
		
			
		}

function getVersion() {
		$.ajax({
			url : "version",
			type : "GET",
			dataType : "text",
			success : function(data) {
			var obj = jQuery.parseJSON(data);
			$('#version').html(obj.buildVersion);
				
			},

			error : function() {
				$("#content").hide();
			}
		});
	}
	    function getSyncTab(url) {
	        $("#invalidApiKey1").hide();
	        $.ajax({
	            headers: {
	                'Content-Type': 'application/javascript'
	            },
	            url: url,
	            type: "GET",
	            success: function (data) {
	                if (data.status == "SUCCESS") {

	                    $("#mcListId").children().each(

	                    function () {
	                        $(this).remove();
	                    });
	                    $("#mcSyncJobSetupTabBack").hide();
	                    $("#mcSyncJobSetupTabMain").show();
	                    var ddNode = data.ddList;
	                    $.each(
	                    ddNode, function (i, obj) {
	                        var div_data = "<option value=" + obj.value + ">" + obj.text + "</option>";
	                        $(div_data).appendTo('#mcListId');
	                    });

	                } else {
	                    $("#mcSyncJobSetupTabBack").show();
	                    $("#mcSyncJobSetupTabMain").hide();
	                    $("#loaderImg").hide();
	                }
	            },
	            error: function (data) {

	                $("#content").hide();
	            }
	        });
	    }

	    function getDiscountTab(url) {

	        $.ajax({
	            headers: {
	                'Content-Type': 'application/javascript'
	            },
	            url: url,
	            type: "GET",
	            success: function (data) {
	                if (data.status == "SUCCESS") {
	                    var dataStr = data.campaignDD.substring(
	                    1, data.campaignDD.length - 1);
	                    var ddNode = $.parseJSON(dataStr);
	                    $("#mcCampaignList").children().each(function () {
	                        $(this).remove();
	                    });

	                    $("#mcDiscountList").children().each(function () {
	                        $(this).remove();
	                    });
	                    
	                    var div_data = "<option  value=0>Please Select</option>";
                        $(div_data).attr("class", "tempDDValue");
                        $(div_data).appendTo('#mcCampaignList');
                        
                        var div_data = "<option class=0  value=0>Please Select</option>";
                        $(div_data).attr("class", "tempDDValue");
                        $(div_data).appendTo('#mcDiscountList');
                        
	                    $.each(
	                    ddNode, function (i, obj) {

	                        var div_data = "<option  value=" + obj.keyStr + ">" + obj.valStr + "</option>";
	                        $(div_data).attr("class", "tempDDValue");
	                        $(div_data).appendTo('#mcCampaignList');
	                    });

	                    // create discounts drop down
	                    var dataDiscount = data.discountDD.discountData.substring(
	                    1, data.discountDD.discountData.length - 1);
	                    var ddDiscountNode = $.parseJSON(dataDiscount);

	                    $.each(
	                    ddDiscountNode, function (i, obj) {

	                        var div_data = "<option discountName='"+obj.name+"' discountAmount='"+obj.amount+"' class='"+obj.discountType+"' value='" + obj.discountCode + "'>" + obj.name + "</option>";
	                        $(div_data).appendTo('#mcDiscountList');
	                        $(div_data).attr("class", "tempDDValue");
	                    });
	                    
	                   

	                }
	                 //get discount and campaign mapping
	                    geDiscountCampaignMap();
	            },
	            error: function (data) {

	                $("#content").hide();
	            }
	        });
    }

	function activateContentPane(paneObject) {
	    var id = $(paneObject).attr("id");
	    $(".tab-pane").removeClass("active");
	    $(".tabLi").removeClass("active");

	    $("#" + id + "-content").addClass("active");
	    $(".tabLink").css("color", "#333");

	    $("#" + id).css("color", "#D74536");

	}

	function getListSiteDD() {
	    $("#progressIndicator").show();

	    $.ajax({
	        headers: {
	            'Content-Type': 'application/javascript'
	        },
	        url: "api/config/getSiteListDD",
	        type: "GET",

	        success: function (data) {

	            if (data.status == "SUCCESS") {
	                $("#secondTab-li").show();
	                $("#thirdTab-li").show();
	                $("#fourthTab-li").show();
	                $("#siteListMapDiv").show();
	                
                    var ddListNode = data.listDD;
	                var selectedList=data.selectedList;
	            
	                $("#listMapConfDiv").show();

	                $("#mapListId1").children().each(function () {
	                    $(this).remove();
	                });

	                $.each(ddListNode, function (i, obj) {

	                	
	                		
	                		if( obj.id == selectedList ) {
	                			 var div_data = "<option selected='selected' value=" + obj.id + ">" + obj.value + "</option>";
	                		} else {
	                			 var div_data = "<option value=" + obj.id + ">" + obj.value + "</option>";
	                		}
	                   
	                    $(div_data).appendTo('#mapListId1');
	                });

	             $("#progressIndicator").hide();

	            } else {
	                $("#discountSaveError").show();
	            }
	        },
	        error: function (data) {

	            $("#content").hide();
	        }
	    });

	}

	function rescheduleBatch(batchId) {

	    $.ajax({
	        headers: {
	            'Content-Type': 'application/javascript'
	        },
	        url: "api/job/reScheduleBatch?resschedulePrm=" + $("#tenantIdHdn").val() + ":" + $("#mcDirectionDD").val(),
	        type: "POST",
	        success: function (data) {

	            if (data == "SUCCESS") {

	                $("#thirdTab").click();
	            } else {

	            }
	        },
	        error: function (data) {

	            $("#content").hide();
	        }
	    });
	}

	function getMappedData() {
	    $("#progressIndicator").show();

	    $.ajax({
	        headers: {
	            'Content-Type': 'application/javascript'
	        },
	        url: "getMappedData?tenantId=" + $("#tenantIdHdn").val(),
	        type: "GET",

	        success: function (data) {

	            if (data.status == "SUCCESS") {
	                var ddNode = data.returnData;
	                $(".tempMapped").each(function () {
	                    $(this).remove();
	                });
	                $.each(
	                ddNode, function (i, obj) {
	                    $("#headerMapping").show();
	                    var trData = $("<tr/>");
	                    trData.attr("class", "tempMapped");
	                    var tdData_1 = $("<td/>");
	                    var p_Data1 = $("<p/>");
	                    tdData_1.attr("class", "tdClass");
	                    $(p_Data1).html(obj.listName);
	                    $(tdData_1).append(p_Data1);
	                    $(trData).append(tdData_1);

	                    var tdData_2 = $("<td/>");
	                    var p_Data2 = $("<p/>");
	                    tdData_2.attr("class", "tdClass");
	                    $(p_Data2).html(obj.siteName);
	                    $(tdData_2).append(p_Data2);
	                    $(trData).append(tdData_2);

	                    var tdData_3 = $("<td/>");
	                    var delImg = $("<img/>");
	                    $(delImg).attr("src", "images/icon_remove.png");
	                    var p_Data3 = $("<a/>");
	                    tdData_3.attr("class", "tdClass");
	                    $(p_Data3).attr("id", obj.siteId);
	                    $(p_Data3).css('cursor', 'pointer');
	                    $(p_Data3).append(delImg);
	                    $(tdData_3).append(p_Data3);

	                    $(p_Data3).attr('onclick', "javascript:deleteMapping(" + obj.siteId + ")");
	                    $(trData).append(tdData_3);

	                    $("#siteMapBody").append(trData);
	                });
	                //to append drop down to mapping table
	                var trData1 = $("<tr/>");
	                var tdData_4 = $("<td/>");
	                $(tdData_4).append($("#mapListId1"))
	                $(trData1).append(tdData_4);
	                
	                var tdData_5 = $("<td/>");
	                $(tdData_5).append($("#mapSiteId1"))
	                $(trData1).append(tdData_5);
	                
	                var tdData_6 = $("<td/>");
	                $(trData1).append(tdData_6);
	                $("#siteMapBody").append(trData1);

	            } else {
	                $("#mappedData").show();
	            }
	        },
	        error: function (data) {

	            $("#content").hide();
	        }
	    });

	}

	function deleteMapping(siteId) {
	    $.ajax({
	        headers: {
	            'Content-Type': 'application/javascript'
	        },
	        url: "deleteMapping?deletePrm=" + $("#tenantIdHdn").val() + ":" + siteId,
	        type: "GET",

	        success: function (data) {

	            if (data.status == "SUCCESS") {
	                var ddNode = data.returnData;
  	            } else {

	                $("#mappedData").show();
	            }
	        },
	        error: function (data) {

	            $("#content").hide();
	        }
	    });
	    
	  
	}
	
	  function saveFirstTab(event)
	    {
	    	 $("#successSaveApikey").hide();
	    	 $("#invalidApiKey1").hide();
	    	 $("#redCross1").hide();
	    	 $("#redCross2").hide();
	    	 
	    	 $("#greenTick1").hide();
	    	 $("#greenTick2").hide();

	        event.preventDefault();
	        $("#loaderImg").show();
	        $("#invalidApiKey2").hide();
	        $("#invalidApiKey1").hide();
	        $.ajax({
	            headers: {
	                'Content-Type': 'application/javascript'
	            },
	            url: "api/config/saveMailchimpKey?tenantParam=" + $(".configureEditMailchimpTxt").val() + ":" + $("#mapSiteId1").val() + ":" + $("#mapListId1").val(),
	            type: "GET",
	            success: function (data) {
	                if (data == "SUCCESS") {
	                	$("#siteListMapDiv").show();
	       	    	 $("#greenTick1").show();
	       	    	 $("#greenTick2").show();
	       	    	 $("#mappedData").show();
	       	         getListSiteDD();
	                } else {
	                	
	                 $("#redCross1").show();
	       	    	 $("#redCross2").show();
	       	    	 
	       	    	 $("#greenTick1").hide();
	       	    	 $("#greenTick2").hide();
	       	    	 $("#mappedData").hide();
	                }
	            },
	            error: function (data) {
	                $("#content").hide();
	            }
	        });
	    }
	  
	  function showSkipError(data)
	  { 
		  console.log("In showSkippError "+data);
		  
		  
		  $.ajax({
	            headers: {
	                'Content-Type': 'application/javascript'
	            },
	            url: "api/jobHistory/getErrorDataForBatch?jobExecutionId=" + data,
	            type: "GET",
	            success: function (data) {
	           
	            	console.log("data "+data);
	            	var dataStr = data.errorData.substring(1,data.errorData.length - 1);
					var errorJson = $.parseJSON(dataStr);
					
					if(errorJson != null && errorJson != "")
					{
						$(".tempErrRow").each(function(){
							$(this).remove();
						});
						$.each(errorJson, function(idx, obj) {
						 var trData =$("<tr></tr>");
						 $(trData).attr("class","tempErrRow");
			 				var tdData_1 =$("<td></td>");
			 				var p_Data1 = $("<p></p>");
			 				$(p_Data1).append(document.createTextNode(obj.type));
			 				$(tdData_1).append(p_Data1);
			 				$(trData).append(tdData_1);
			 				
			 				var tdData_2 =$("<td></td>");
			 				var p_Data2 = $("<p></p>");
			 				$(p_Data2).append(document.createTextNode(obj.msg));
			 				$(tdData_2).append(p_Data2);
			 				$(trData).append(tdData_2);
			 					 							 				
			 				$("#errorSkipBody").append(trData);
				
					});
						$("#mainHistory").fadeOut();
						$("#popUpDiv").fadeIn();
					}
					
				
	            },
	            error: function (data) {
	                $("#content").hide();
	            }
	        });
	  }
	  
	  function geDiscountCampaignMap()
	  {
		  
		  
		  $.ajax({
	            headers: {
	                'Content-Type': 'application/javascript'
	            },
	            url: "getDiscountMapping",
	            type: "GET",
	            success: function (data) {
	         
	            	console.log("data "+data);
					var mappingJson = data.discCampMapping;
					
					$(".tempRow").each(function(){
						$(this).remove();
					});
					
					if(mappingJson != null && mappingJson != "")
					{						
						$.each(mappingJson, function(idx, obj) {
							var campaignDisc = obj.campaignCode.split(":")[1];
						 var trData =$("<tr></tr>");
						 $(trData).attr("class","tempRow");
			 				var tdData_1 =$("<td></td>");
			 				var p_Data1 = $("<p></p>");
			 				$(p_Data1).append(document.createTextNode(campaignDisc));
			 				$(tdData_1).append(p_Data1);
			 				$(trData).append(tdData_1);
			 				
			 				var tdData_2 =$("<td></td>");
			 				var p_Data2 = $("<p></p>");
			 				$(p_Data2).append(document.createTextNode(obj.discountName));
			 				$(tdData_2).append(p_Data2);
			 				$(trData).append(tdData_2);
			 				
			 				 
			                   
				 				
			                    if(obj.campaignStatus == "NS")
			                    	{
			                    	
			                    	 var tdData_3 = $("<td/>");
					                    var delImg = $("<img/>");
					                    $(delImg).attr("src", "images/icon_remove.png");			                  
					                    var p_Data3 = $("<a/>"); 
					                    $(p_Data3	).attr("class","removeDisCampMapping");
					                    $(p_Data3).attr('onclick', "javascript:removeDisCampMapping('" + obj.discountCode +"','"+obj.campaignCode+"')");
					                    tdData_3.attr("class", "tdClass");
					                   $(p_Data3).attr("id", obj.discountCode);
					                    $(p_Data3).css('cursor', 'pointer');
					                    $(p_Data3).append(delImg);
					                    $(tdData_3).append(p_Data3);
					                    $(trData).append(tdData_3);
			                    	 
				                    //send button
				                    var tdData_4 =$("<td></td>");
			                    	var btn_Data2 = $("<a></a>");
				 				$(btn_Data2).append(document.createTextNode("SEND"));
				 				 $(btn_Data2).attr('onclick', "javascript:sendCampaign('"+obj.discountCode+"' , '"+obj.campaignCode+"')");
			                    	
				 				$(tdData_4).append(btn_Data2);
				 				$(trData).append(tdData_4);
			                    	}
			                    else
			                    	{
			                    	
			                    	 var tdData_3 = $("<td/>");
					                    var delP = $("<p/>");
					                    var p_Data3 = $("<a/>"); 
					                    tdData_3.attr("class", "tdClass");
					                    $(p_Data3).append(delP);
					                    $(tdData_3).append(p_Data3);
					                    $(trData).append(tdData_3);
			                    	 
				                    //send button
				                    var tdData_4 =$("<td></td>");
			                    	var btn_Data2 = $("<p></p>");
			                    	$(btn_Data2).append(document.createTextNode("SENT"));
			                    	$(tdData_4).append(btn_Data2);
					 				$(trData).append(tdData_4);
				                    
			                    	}
				 				
			 				
			 				$("#addDiscountAjaxBody").append(trData);
				
					});
						
						   //to append drop down to mapping table
		                var trData1 = $("<tr/>");
		                var tdData_4 = $("<td/>");
		                $(tdData_4).append($("#mcCampaignList"))
		                $(trData1).append(tdData_4);
		                
		                var tdData_5 = $("<td/>");
		                $(tdData_5).append($("#mcDiscountList"))
		                $(trData1).append(tdData_5);
		                
		                var tdData_6 = $("<td/>");
		                $(trData1).append(tdData_6);
		                $("#addDiscountAjaxBody").append(trData1);
					}
					
				
	            },
	            error: function (data) {
	                $("#content").hide();
	            }
	        });
		  
	  }
	  
 function removeDisCampMapping(discountId , campaignPrm){
	 var campaignCode = campaignPrm.split(":")[0];
	 var campaignDisc = campaignPrm.split(":")[1];
	 console.log("campaignDisc "+campaignDisc);
		  
		  var data = "" + $("#tenantIdHdn").val() + ":" +discountId+":"+campaignCode+"";

		    $.ajax({
		        headers: {
		            'Content-Type': 'application/javascript'
		        },
		        url: "deleteDiscountMapping?discountParam=" + data,
		        type: "POST",
		        data: data,
		        success: function (data) {

		            if (data.status == "SUCCESS") {
		            	
		                $("#discountSaveSuccess").show();
		              
		                var div_data2 = "<option  value=" + campaignCode + ">" + campaignDisc + "</option>";
                        $(div_data2).attr("class", "tempDDValue");
                        $(div_data2).appendTo('#mcCampaignList');
           
		                geDiscountCampaignMap();

		            } else {
		                $("#discountSaveError").show();
		            }
		        },
		        error: function (data) {

		            $("#content").hide();
		        }
		    });
	  }
	  
	  
	  
	  
	  
	  function saveDiscountCampaign()
	  {
		  var discountType = $("#mcDiscountList option:selected").attr("class");
		  var discountAmt = $("#mcDiscountList option:selected").attr("discountAmount");
		  var discountName = $("#mcDiscountList option:selected").text();
		  console.log("discountName "+discountName);
		 var data = "" + $("#tenantIdHdn").val() + ":" + $("#mcCampaignList").val() + ":" + $("#mcDiscountList").val() +":" + discountType+":" +discountAmt +":"+discountName+ "";
		 
		 
		 console.log("saveDiscountMapping "+data);
		 //var data = "";
		    $.ajax({
		        headers: {
		            'Content-Type': 'application/javascript'
		        },
		        url: "saveDiscountMapping",
		        type: "POST",
		        data: data,
		        success: function (data) {

		            if (data == "SUCCESS") {
		            	
		            	geDiscountCampaignMap();
		            	$("#mcCampaignList").find("option[value='"+$("#mcCampaignList").val()+"']").remove();
		            	$("#mcDiscountList")[0].selectedIndex = 0;
		            	
		            	 var data = {
		            	            "tenantId": $("#tenantIdHdn").val()
		            	        };
		            	       // getDiscountTab(data,  "getCampaigns");
		                $("#discountSaveSuccess").show();

		            } else {
		                $("#discountSaveError").show();
		            }
		        },
		        error: function (data) {

		            $("#content").hide();
		        }
		    });
	  }
	  
	  function sendCampaign(discountCode, campaignCode)
	  {
		  console.log("discountCode "+discountCode);
		  var campaignPrm = campaignCode.split(":")[0];
		  var data = "" +$("#tenantIdHdn").val()+":" + discountCode + ":" + campaignPrm + "";
		  console.log("data "+data);
		    $.ajax({
		        headers: {
		            'Content-Type': 'application/javascript'
		        },
		        url: "sendCampaign?sendCampaignPrm=" + data,
		        type: "POST",
		        data: data,
		        success: function (data) {

		            if (data == "SUCCESS") {
		            	geDiscountCampaignMap();
		            	//resetMergeTags(data);
		                $("#discountSaveSuccess").show();

		            } else {
		                $("#discountSaveError").show();
		            }
		        },
		        error: function (data) {

		            $("#content").hide();
		        }
		    });
		  
	  }
	  
	  function resetMergeTags(data)
	  {
		  console.log("data "+data);
		    $.ajax({
		        headers: {
		            'Content-Type': 'application/javascript'
		        },
		        url: "resetMergeTags?sendCampaignPrm=" + data,
		        type: "POST",
		        data: data,
		        success: function (data) {

		            if (data == "SUCCESS") {
		            	geDiscountCampaignMap();
		            	deleteMergeTags(data);
		                $("#discountSaveSuccess").show();

		            } else {
		                $("#discountSaveError").show();
		            }
		        },
		        error: function (data) {

		            $("#content").hide();
		        }
		    });
	  }
	  
