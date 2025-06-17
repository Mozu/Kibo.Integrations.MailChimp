ko.bindingHandlers.chosen = 
{
  update: function(element) 
  {
   $(element).chosen({width:"95%"});
   $(element).trigger('liszt:updated');
  }
};
/*
var workflowMode = false;
var currentTabIndex = 0;
function switchTab() {
    var nextTab = "";
    var currentTab = "";
    alert(1);

    $('.nav-tabs li').each(function () {
        if ($(this).data('tab-index') == currentTabIndex + 1) {
            nextTab = $(this);
        }
        else if ($(this).data('tab-index') == currentTabIndex) {
            currentTab = $(this);
        }
    });
    currentTabIndex++;
    currentTab.removeClass('active');
    nextTab.removeClass('inactive');
    nextTab.addClass('active');
    $('#' + currentTab.data('tab-id')).fadeOut('fast', function () {
        $('#' + nextTab.data('tab-id')).fadeIn('fast');
    });

    if (nextTab.data('last-tab')) {
        enableTabs();
        $('#saveBtn').removeClass('hide');
        $('#nextBtn').addClass('hide');
    }
}*/

function enableTabs() {
 
    $('.nav-tabs a').click(function (e) {
        var tabElement = e.target.parentElement;
        var parent = tabElement.parentElement;
        var activeTab = $(parent).find('.active');
        var activeTabId = activeTab.data('tab-id');
        var newTabId = $(tabElement).data('tab-id');

        activeTab.removeClass('active');
        $(tabElement).addClass('active');
      
        $('#' + activeTabId).fadeOut('fast', function () {
            $('#' + newTabId).fadeIn('fast');
        });
    });
}


$(document).ajaxError(function (event, jqxhr, settings, exception) {
 console.log(exception);
 console.log(event);
 console.log(settings);
 console.log(jqxhr);
 if (jqxhr.status >= 200 && jqxhr.status <= 300)
  return;
    if (jqxhr.responseJSON != null)
        $("#serverErrorMessage").html(jqxhr.responseJSON.message);
    else if (jqxhr.statusText != null)
        $("#serverErrorMessage").html(jqxhr.statusText);
    else {
     $("#serverErrorMessage").html(jqxhr.responseText);
    }
    $("#serverError").show();
});

function closeError() {
    $("#serverError").hide();
}


$(function () {
 
 $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
  console.log(originalOptions);
        $("#serverError").hide();
        $("#progressIndicator").show();
        jqXHR.complete(function () {
           $("#progressIndicator").hide();
        });
        
    });
 
 
    if ($("#initalConfig").val() == "False") {
        enableTabs();
        $('#saveBtn').removeClass('hide');

    } else {
        workflowMode = true;
        $('.nav-tabs li').each(function () {
            if ($(this).attr('class') != 'active')
                $(this).addClass("inactive");
        });

        $("#nextBtn").click(function () {
            switchTab();
        });
    }
    
    //window.homeViewModel = new homeViewModel();

});