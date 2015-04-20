var socket = io.connect();

// DOM Ready =============================================================
$(document).ready(function() {
    
    // Populate the user table on initial page load
    OnLoad();

    /*$("#transactionList input[type='checkbox']").each(function(){
        $(this).click(function(){
            var id = $(this).attr("id");

            if ($(this).is(":checked"))
                alert('#top' + id);
            else
                alert('... Dang cap nhat');
                //$('#top' + id).removeClass('hide');
        });        
    })*/
});

// Data

var profileHost = '/profile/';
var channelCode = [];
var transactionCode;
var productCode;
var data = [];
var time1 = 60000;
var time2 = 3600000;
var time3 = 86400000;
var jsonObj;
var jsonObj_time1;
var jsonObj_time2;
var jsonObj_time3;
var totalCount = 0;
var totalSum = 0;
var chartCD1;
var dataRanking;
var dataRanking_time1;
var dataRanking_time2;
var dataRanking_time3;
var SlidingTimeCbo;
var timer;

function OpenSocket(){
    /*socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "stackedColumn", showInLegend: true, name: channelCode[i].ChannelName, dataPoints:[]};
            data.push(dataSeries);
        }
    });*/
    socket.on('listProductTypeJson',function(data1){
	//alert("vao day");
        /*transactionCode = $.map(data1, function (value, index) {
            return [value];
        });*/
	//alert(data1[0].TransactionName);
        /*for (var p = 0; p < transactionCode.length; p++) {
            socket.on('emitRanking-Ranking-' + transactionCode[p] + '-' + time1.toString(), function (data1) {
                dataRanking_time1 = data1;
                //TopBot();
            });
            socket.on('emitRanking-Ranking-' + transactionCode[p] + '-' + time2.toString(), function (data1) {
                dataRanking_time2 = data1;
                //TopBot();
            });
            socket.on('emitRanking-Ranking-' + transactionCode[p] + '-' + time3.toString(), function (data1) {
                dataRanking_time3 = data1;
                //TopBot();
            });

        }*/
	productCode = JSON.parse('[' + data1 + ']');
	//alert(productCode);	
        for (var i = 0; i < productCode.length; i++) {
	//alert("vao day:"+productCode[i].ProductCode+"-namr:"+productCode[i].ProductName);
	var adddata=true;
	    for(var j=0;j<data.length;j++)
	    {
		if(productCode[i].ProductName==data[j].name)
                {
			adddata=false;
		}
	    }
	    if(adddata)
	    {
	    var dataSeries = { axisYType: "secondary",xValueType: "dateTime",click: onClick, type: "line", showInLegend: false, name: productCode[i].ProductName, dataPoints:[]};
            data.push(dataSeries);
	    }
        }
	if(productCode.length>0)
	{
	    var dataSeries = { xValueType: "dateTime",click: onClick, type: "column", showInLegend: false, name: "Total Product", dataPoints:[]};
            data.push(dataSeries);
	}
    });
    socket.on('listChart-real-time-count-product-' + time1,function(data1){
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
	//alert(data1);
        //alert(time1);
    });
    socket.on('listChart-real-time-count-product-' + time2,function(data1){
        jsonObj_time2 = $.parseJSON('[' + data1 + ']');
	//alert(time2);
    });
    socket.on('listChart-real-time-count-product-' + time3,function(data1){
        jsonObj_time3 = $.parseJSON('[' + data1 + ']');
    });
}

function AddCombobox(){
    var x = document.getElementById("SlidingTimeCbo");
    var Cbotext = ["1 Minute", "1 Hour", "1 Day"];
    var CboValue = ["60000", "3600000", "86400000"];
    for(var i = 0; i<Cbotext.length; i++) {
        var option = document.createElement("option");
        option.text = Cbotext[i];
        option.value = CboValue[i];
        x.add(option);
    }
}

function OnLoad() {
    OpenSocket();
    AddCombobox();
    CreateChart();
	//jsonObj = jsonObj_time1;
    //set event selected cho combobox time
    $('#SlidingTimeCbo').change(function() {
        var SlidingTimeSmoothieCbo = document.getElementById("SlidingTimeCbo");
        var CboValue = SlidingTimeSmoothieCbo.options[SlidingTimeSmoothieCbo.selectedIndex].value;

        if(CboValue.toLowerCase().localeCompare("60000".toLowerCase()) == 0)
        {
            jsonObj = jsonObj_time1;
        }
        else if(CboValue.toLowerCase().localeCompare("3600000".toLowerCase()) == 0)
        {
            jsonObj = jsonObj_time2;
        }
        else if(CboValue.toLowerCase().localeCompare("86400000".toLowerCase()) == 0)
        {
            jsonObj = jsonObj_time3;
        }

    }).change();

}

function CreateChart(){
    chartCD1 = new CanvasJS.Chart("chartCD",
        {
            zoomEnabled: true,
            title: {
                text: ""
            },
            toolTip: {
                shared: true

            },
            axisY:{
                includeZero: false
		,maximum: 2000
            },
            legend:{
                cursor:"pointer",
                itemclick : function(e) {
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                        /*for(var m=0;m<channelCode.length;m++){
                            if(transactionCode[m].TransactionName.toString() === e.dataSeries.name.toString()) {
                                //channelCode[m].Display = "0";
                            }
                        }*/
                    }
                    else {
                        e.dataSeries.visible = true;
                        /*for(var i = 0;i<channelCode.length;i++){
                            if(channelCode[i].ChannelName.toString() === e.dataSeries.name.toString()) {
                                channelCode[i].Display = "1";
                            }
                        }*/
                    }
                    chartCD1.render();
                }
            },
            data:data
        });
    chartCD1.render();
}

setInterval(function() {
    SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;
    //alert(timer);
    if(timer == time1)
        jsonObj = jsonObj_time1;
    else if(timer == time2)
        jsonObj = jsonObj_time2;
    else if(timer == time3)
        jsonObj = jsonObj_time3;
    if(jsonObj)
    {
    buidData();
    }
    chartCD1.render();
    TotalCountAmount();
}, 1000);

/*
// set ranking
function TopBot(){
    SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;
    if(timer == time1)
        dataRanking = dataRanking_time1;
    else if(timer == time2)
        dataRanking = dataRanking_time2;
    else if(timer == time3)
        dataRanking = dataRanking_time3;
    if(dataRanking != null) {
        var jsonRanking = $.parseJSON('[' + dataRanking + ']');
        if (jsonRanking[0] != null) {
                var dataTop = '';
                var dataBot = '';
            for (var j = 1; j <= 5; j++) {

                var arrayBot = [jsonRanking[0]["TopTen-Bot" + j.toString() + "-" + timer.toString() + "-Acc"],
                    jsonRanking[0]["TopTen-Bot" + j.toString() + "-" + timer.toString() + "-Amount"]];
                var arrayTop = [jsonRanking[0]["TopTen-Top" + j.toString() + "-" + timer.toString() + "-Acc"],
                    jsonRanking[0]["TopTen-Top" + j.toString() + "-" + timer.toString() + "-Amount"]];

                var valueAmount = (arrayTop[1] != null || arrayTop[1] != undefined) ? arrayTop[1] : '';
                var valueAccount = (arrayTop[0] != null || arrayTop[0] != undefined) ? arrayTop[0] : '';
                //Lay du lieu tung hang
                dataTop = dataTop + "<li><span class='topstatistic' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Top"+j.toString() + "-Amount'>" + valueAmount +
                "</span><span class='icons'><i class='fa fa-envelope'></i></span><a href='"+  profileHost + valueAccount + "' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Top"+j.toString() +"-Account'>"+ valueAccount + "</a></li>";

                valueAmount =  (arrayBot[1] != null || arrayBot[1] != undefined) ? arrayBot[1] : '';
                valueAccount = (arrayBot[0] != null || arrayBot[0] != undefined) ? arrayBot[0] : '';

                dataBot = "<li><span class='topstatistic' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Bot"+j.toString() + "-Amount'>" + valueAmount +
                "</span><span class='icons'><i class='fa fa-envelope'></i></span><a href='" + profileHost+ valueAccount + "' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Bot"+j.toString() +"-Account'>"+ valueAccount + "</a></li>" + dataBot;
            }
            //Show du lieu
            if($('#topFive' + jsonRanking[0]["TransactionType"]).is(":visible"))
                $('#topFive' + jsonRanking[0]["TransactionType"] +'  ul').html(dataTop);

            if($('#topFiveDown' + jsonRanking[0]["TransactionType"]).is(":visible"))
                $('#topFiveDown'+ jsonRanking[0]["TransactionType"] +' ul').html(dataBot);
        }
    }
}*/

function SetRanking(Top, array, TranType){
    var TopTenDepositTop1Acc = document.getElementById(Top + "-Account");
    if (TopTenDepositTop1Acc == null || TopTenDepositTop1Acc == undefined)
        return;

    TopTenDepositTop1Acc.innerHTML = (array[0] != null || array[0] != undefined) ? array[0] : '';
    TopTenDepositTop1Acc.href = profileHost + TopTenDepositTop1Acc.innerHTML;

    var TopTenDepositTop1Amount = document.getElementById(Top + "-Amount");
    if (TopTenDepositTop1Amount == null || TopTenDepositTop1Amount == undefined)
        return;
    TopTenDepositTop1Amount.innerHTML = (array[1] != null || array[1] != undefined) ? array[1] : '';
}
// build data for chart
function buidData()
{
	//alert(timer);	
    if(timer == time1)
    {
	//alert(timer);	
        jsonObj = jsonObj_time1;
    }
	//alert(jsonObj);
    else if(timer == time2)
        jsonObj = jsonObj_time2;
    else if(timer == time3)
        jsonObj = jsonObj_time3;
	//alert(data.length);
 
    for(var k=0;k<data.length;k++) {
	data[k].showInLegend = false;
        for (var i = 0; i < productCode.length; i++) {
            if(data[k].name == productCode[i].ProductName) {
		data[k].showInLegend = true;
                var dataPoints = [];
                var visible = false;
                for (var j = 0; j < jsonObj.length; j ++) {
                    if (visible == false && jsonObj[j][productCode[i].ProductCode + "-count"] != null && jsonObj[j][productCode[i].ProductCode + "-count"] != "") visible = true;
                    if(jsonObj[j][productCode[i].ProductCode + "-count"] != null && jsonObj[j][productCode[i].ProductCode + "-count"] != "") {
                        dataPoints.push({
                            x: new Date(jsonObj[j]["time"]),
                            y: parseInt(jsonObj[j][productCode[i].ProductCode + "-count"])
                        });
		    }
		      
                }
		
		
		//alert(data.length);
                if (visible == true) {
                    data[k].dataPoints = dataPoints;
                    data[k].showInLegend = true;
                }else{
                    data[k].showInLegend = false;
                    data[k].dataPoints = [];
                }
            }
        }
	
	
    }
	for (var j = 0; j < jsonObj.length; j ++) {
		totalCount = 0;
		var date;
		//alert("--"+j);
		for (var i = 0; i < data.length; i++) {
			if(jsonObj[j][data[i].name + "-count"] != null && jsonObj[j][data[i].name + "-count"] != "") {
			//alert(productCode[i].ProductCode);
                        totalCount = parseInt(totalCount) + parseInt(jsonObj[j][data[i].name + "-count"]);
			date= new Date(jsonObj[j]["time"]);
			//alert("totalCount"+i+": "+parseInt(jsonObj[j][productCode[i].ProductCode + "-count"]));
                        }
		        
		}
		for (var k = 0; k < data.length; k++) {
		//data[k].showInLegend = true;
		if(data[k].name == "Total Product")
		{
		data[k].showInLegend = true;
		//alert("date: "+date+" - totalCount: "+totalCount);
		if(date&&totalCount)
		{
		dataPoints.push({
		                    x: date,
		                    y: parseInt(totalCount)
		                });
		
		data[k].dataPoints = dataPoints;
		}
		}
		}
    	}
}

//set total count & amount
function TotalCountAmount()
{
    /*totalSum = 0;
    totalCount = 0;
    for(var i=0;i<productCode.length;i++){
        if(channelCode[i].Display == "1") {
            if(jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-count"] != null
                && jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-count"] != undefined) {
                totalCount = parseInt(totalCount) + parseInt(jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-count"]);
            }
            if(jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-sum"] != null
                && jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-sum"] != undefined) {
                totalSum = parseInt(totalSum) + parseInt(jsonObj[jsonObj.length - 1][productCode[i].ProductCode + "-sum"]);
            }
        }
    }

    if(totalCount == null || totalCount == undefined)
        totalCount = 0;
    if(totalSum == null || totalSum == undefined)
        totalSum = 0;

    var TotalNoTran = document.getElementById("TotalNoTran");
    TotalNoTran.innerHTML = totalCount.toString();

    var TotalAmount = document.getElementById("TotalAmount");
    TotalAmount.innerHTML = totalSum.toString();*/
}

//pie chart
var datapie=[];
function onClick(e) {
		//alert(  e.dataSeries.type + ", dataPoint { x:" + e.dataPoint.x + ", y: "+ e.dataPoint.y + " }" );
		datapie=[];
		var index=0;
		for(var i=0;i<data.length;i++)
		{
			if(data[i].dataPoints.length>0)
			{
				for(var j=0;j<data[i].dataPoints.length;j++)
				{
					//alert(data[i].dataPoints[j].x);
					if(data[i].dataPoints[j].x==e.dataPoint.x)
					{
						//alert(data[i].dataPoints[j].x);
						index = j;
					}
					
				}
			}
			
		}
		for(var i=0;i<data.length;i++)
		{
			if(data[i].dataPoints.length>0&&data[i].name!="Total Product")
			{
				for(var j=0;j<data[i].dataPoints.length;j++)
				{
					if(index>0)
					{
						//alert(data[i].name+"--"+data[i].dataPoints[index].x+"---"+data[i].dataPoints[index].y);
						if(datapie.length==0)
						{
						//alert();
						if(data[i].dataPoints[index].y)
						{
						datapie.push({  y: data[i].dataPoints[index].y, legendText: data[i].name, label: data[i].name});						
						}
						}
						else
						{
							var ispush=true;
							for(var k=0;k<datapie.length;k++)
							{
								if(data[i].name==datapie[k].label)
								{
									ispush=false;
								}
							}
							if(ispush)
							{
								if(typeof(data[i].dataPoints[index]) !== "undefined" && data[i].dataPoints[index] !== null)
								{
								datapie.push({  y: data[i].dataPoints[index].y, legendText: data[i].name,exploded: true, label: data[i].name});
								}
							}
						}
					}
				}
			}
			
		}
		var chart1 = new CanvasJS.Chart("chartCD1",
		{
			title:{
				text: "product pie chart"
			},
			exportFileName: "Pie Chart",
			exportEnabled: true,
		        animationEnabled: true,
			legend:{
				verticalAlign: "bottom",
				horizontalAlign: "center"
			},
			data: [
			{       
				type: "pie",
				showInLegend: true,
				toolTipContent: "{legendText}: <strong>{y}</strong>",
				indexLabel: "{label} #percent %",
				dataPoints: datapie
			}
		]
		});
		chart1.render();
                
                $( "#dialog" ).dialog();
    		//$('#dialog').css('width', '700px');
	}

//-----popup------
/*$(function() {
    //$('#dialog').css('width', '700px');
    //$( "#dialog" ).dialog();
  });*/
