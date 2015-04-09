var socket = io.connect();

// DOM Ready =============================================================
$(document).ready(function() {
    
    // Populate the user table on initial page load
    OnLoad();

    $("#transactionList input[type='checkbox']").each(function(){
        $(this).click(function(){
            var id = $(this).attr("id");

            if ($(this).is(":checked"))
                alert('#top' + id);
            else
                alert('... Dang cap nhat');
                //$('#top' + id).removeClass('hide');
        });        
    })
});

// Data

var profileHost = '/profile/';
var channelCode = [];
var transactionCode;
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
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        //for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "line", showInLegend: false, name: "totaltrans", dataPoints:[]};
            data.push(dataSeries);
        //}
    });
    socket.on('listTransactionCode',function(data1){
        transactionCode = $.map(data1, function (value, index) {
            return [value];
        });
        for (var p = 0; p < transactionCode.length; p++) {
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
        }
    });

    socket.on('loadtransactionchart-real-time-count-chart-60000',function(data1){
	//alert('loadtransactionchart-real-time-count-chart-');
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
	buidDataOff();
    });

    socket.on('listChart-real-time-count-chart-' + time1,function(data1){
	//alert('listChart-real-time-count-chart');
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-count-chart-' + time2,function(data1){
        jsonObj_time2 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-count-chart-' + time3,function(data1){
        jsonObj_time3 = $.parseJSON('[' + data1 + ']');
    });
}

/*function AddCombobox(){
    var x = document.getElementById("SlidingTimeCbo");
    var Cbotext = ["1 Minute", "1 Hour", "1 Day"];
    var CboValue = ["60000", "3600000", "86400000"];
    for(var i = 0; i<Cbotext.length; i++) {
        var option = document.createElement("option");
        option.text = Cbotext[i];
        option.value = CboValue[i];
        x.add(option);
    }
}*/

function OnLoad() {
    OpenSocket();
    //AddCombobox();
    CreateChart();
    

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

            },
            legend:{
                cursor:"pointer",
                itemclick : function(e) {
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                        for(var m=0;m<channelCode.length;m++){
                            if(channelCode[m].ChannelName.toString() === e.dataSeries.name.toString()) {
                                channelCode[m].Display = "0";
                            }
                        }
                    }
                    else {
                        e.dataSeries.visible = true;
                        for(var i = 0;i<channelCode.length;i++){
                            if(channelCode[i].ChannelName.toString() === e.dataSeries.name.toString()) {
                                channelCode[i].Display = "1";
                            }
                        }
                    }
                    chartCD1.render();
                }
            },
            data:data
        });
    chartCD1.render();
}
//int cnt=0;
setInterval(function() {
    SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    //timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;
    jsonObj = jsonObj_time1;
    if(dataPoints.length>0)
    {
    buidData();
    }
    if(dataPoints.length==0)
    {
    	//buidDataOff();
    }
    chartCD1.render();
    TotalCountAmount();
}, 1000);

// set ranking
/*function TopBot(){
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
}

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
}*/
// build data for chart
var dataPoints = [];
function buidData()
{
    jsonObj = jsonObj_time1;
    

    totalCount = 0;
    
    for(var k=0;k<data.length;k++) {
        for (var i = 0; i < channelCode.length; i++) {
            //if(data[k].name == channelCode[i].ChannelName) {
                
                var visible = true;
		if(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"] != null
                && jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"] != undefined) {
                totalCount = parseInt(totalCount) + parseInt(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"]);
            }
                
	}
	
	var timestamp = dataPoints[0].x.getTime();
	var timestamp1mn=timestamp-60000;
	//alert(timestamp+"_"+timestamp1mn);
	
	var date= new Date(jsonObj[jsonObj.length - 1]["time"]);
	//alert(date.getTime());
	var timestamp = date.getTime();
	var timestamp1mn=timestamp-60000;
	for(var clr=0;clr<dataPoints.length-1;clr++)
	{
		if(timestamp1mn>dataPoints[clr].x.getTime())
		{
			dataPoints.splice(clr, 1);
		}
	}

			dataPoints.push({
		                    x: date,
		                    y: parseInt(totalCount)
		                });
			//dataPoints.splice(0, 2);
			data[k].dataPoints = dataPoints;

    }
}
//buidDataOff();
socket.emit('BuidDataOff',"Start");
function buidDataOff()
{
    jsonObj = jsonObj_time1;
    
    
    dataPoints=[];
    for(var k=0;k<data.length;k++) {
	
	
	for (var j = 0; j < jsonObj.length; j += 1) {
		totalCount = 0;
		var date;
		//var total = 0;
		//var time;
		
		for (var i = 0; i < channelCode.length; i++) {
			if(jsonObj[j][channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][channelCode[i].ChannelCode + "-count"] != "") {
			
                        totalCount = parseInt(totalCount) + parseInt(jsonObj[j][channelCode[i].ChannelCode + "-count"]);
			date= new Date(jsonObj[j]["time"]);
			
                        }
		        
		}
		dataPoints.push({
		                    x: date,
		                    y: parseInt(totalCount)
		                });
		
			data[k].dataPoints = dataPoints;
		}
    }
}

//set total count & amount
function TotalCountAmount()
{
    totalSum = 0;
    totalCount = 0;
    for(var i=0;i<channelCode.length;i++){
        if(channelCode[i].Display == "1") {
            if(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"] != null
                && jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"] != undefined) {
                totalCount = parseInt(totalCount) + parseInt(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"]);
            }
            if(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-sum"] != null
                && jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-sum"] != undefined) {
                totalSum = parseInt(totalSum) + parseInt(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-sum"]);
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
    TotalAmount.innerHTML = totalSum.toString();
}
