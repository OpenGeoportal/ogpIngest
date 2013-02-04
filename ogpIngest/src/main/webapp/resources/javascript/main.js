/*
 * jQuery File Upload Plugin JS Example 6.7
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, unparam: true, regexp: true */
/*global $, window, document */

pollIngestStatus = function(jobId){
	jQuery.getJSON("ingestStatus?jobId=" + jobId, function(data){
		var success = data.successes;
		if (success.length > 0){
			//see if the success div exists
			if (jQuery("#message").length == 0){
				//create the error div
				jQuery("#status").append('<div id="message" class="success"><h4>Ingest Succeeded:</h4></div>');
			}
			jQuery("#message").html("<h4>Ingest Succeeded:</h4>");
			for (var i in success){
				jQuery("#message").append('<span>' + success[i].layer + '</span><br/>');
			}
		}
		var warnings = data.warnings;
		if (warnings.length > 0){
			//see if the warning div exists
			if (jQuery("#warnings").length == 0){
				//create the error div
				jQuery("#status").append('<div id="warnings" class="warning"><h4>Ingest Warnings:</h4><table></table></div>');
			}
			jQuery("#warnings table").html("");
			for (var i in warnings){
				jQuery("#warnings table").append('<tr><td><span>' + warnings[i].layer + '</span></td><td><span>' + warnings[i].status + '</span></td></tr>');
			}
		}
		var errors = data.errors;
		if (errors.length > 0){
			//see if the error div exists
			if (jQuery("#errors").length == 0){
				//create the error div
				jQuery("#status").append('<div id="errors" class="error"><h4>Ingest Failed:</h4><table></table></div>');
			}
			jQuery("#errors table").html("");
			for (var i in errors){
				jQuery("#errors table").append('<tr><td><span>' + errors[i].layer + '</span></td><td><span>' + errors[i].status + '</span></td></tr>');
			}
		}
		currentStatus = data.jobStatus;
		if (currentStatus == "Processing"){
			var t=setTimeout(function(){pollIngestStatus(jobId);},3000);
			/*
			if (jQuery("td.status").length > 0){
				jQuery("td.status").html("<span>" + currentStatus + "</span>");
			} else {
				 var t=setTimeout(function(){
					 var status$ = jQuery("td.status");
					 status$.each(function(){
							if (jQuery(this).parent().hasClass("processing")){
								 jQuery(this).html("<span>" + currentStatus + "</span>");
							}
					 });
				 },1000);
			}*/
		} else {
			if ((typeof data.returnValue != "undefined")&&(data.returnValue != null)){
				if (data.returnValue.length > 0){
					window.location.href = data.returnValue;
				}
			}
			var t=setTimeout(function(){
				var loader$ = jQuery("td.loader");
				loader$.each(function(){
					if (jQuery(this).parent().hasClass("processing")){
						jQuery(this).html(getExcelLink(jobId));
						jQuery(this).parent().removeClass("processing");
					} 
				});
			},1000);
		}

	});
};

getExcelLink = function(jobId){
	return '<a href="ingestStatus/excel?jobId=' + jobId + '">Ingest Report</a>';
};

