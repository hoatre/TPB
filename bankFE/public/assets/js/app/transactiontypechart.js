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
    /*socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "stackedColumn", showInLegend: true, name: channelCode[i].ChannelName, dataPoints:[]};
            data.push(dataSeries);
        }
    });*/
    socket.on('listTransactionCodeJson',function(data1){
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
	transactionCode = JSON.parse('[' + data1 + ']');	
        for (var i = 0; i < transactionCode.length; i++) {
	//alert("vao day:"+transactionCode[i].TransactionCode+"-namr:"+transactionCode[i].TransactionName);
            var dataSeries = { xValueType: "dateTime", type: "stackedColumn", showInLegend: true, name: transactionCode[i].TransactionName, dataPoints:[]};
            data.push(dataSeries);
        }
    });
    socket.on('listChart-real-time-count-tran-' + time1,function(data1){
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
	//alert(data1);
    });
    socket.on('listChart-real-time-count-tran-' + time2,function(data1){
        jsonObj_time2 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-count-tran-' + time3,function(data1){
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
	jsonObj = jsonObj_time1;
    //set event selected cho combobox time
    /*$('#SlidingTimeCbo').change(function() {
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

    }).change();*/

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
                includeZero: true

            },
            data:data
        });
    chartCD1.render();
}

setInterval(function() {
    //SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    //timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;
    //if(timer == time1)
        jsonObj = jsonObj_time1;
    //else if(timer == time2)
    //    jsonObj = jsonObj_time2;
    //else if(timer == time3)
    //    jsonObj = jsonObj_time3;
    buidData();
    chartCD1.render();
    //TotalCountAmount();
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
	
    //if(timer == time1)
        jsonObj = jsonObj_time1;
	//alert(jsonObj);
    /*else if(timer == time2)
        jsonObj = jsonObj_time2;
    else if(timer == time3)
        jsonObj = jsonObj_time3;*/
	//alert(data.length);
    for(var k=0;k<data.length;k++) {
        for (var i = 0; i < transactionCode.length; i++) {
            if(data[k].name == transactionCode[i].TransactionName) {
                var dataPoints = [];
                var visible = false;
                for (var j = 0; j < jsonObj.length; j += 1) {

                    //if (visible == false && jsonObj[j][transactionCode[i].TransactionCode + "-count"] != null && jsonObj[j][transactionCode[i].TransactionCode + "-count"] != "") visible = true;
                    //if(jsonObj[j][transactionCode[i].TransactionCode + "-count"] != null && jsonObj[j][transactionCode[i].TransactionCode + "-count"] != "") {
			//if(dataPoints.length>40)
			//{
				//dataPoints.splice(0, dataPoints.length-40);
			//}
			//alert(jsonObj[j][transactionCode[i].TransactionCode + "-count"]);
                        dataPoints.push({
                            x: new Date(jsonObj[j]["time"]),
                            y: parseInt(jsonObj[j][transactionCode[i].TransactionCode + "-count"])
                        });
                    }
                }
		data[k].dataPoints = dataPoints;
                /*if (visible == true) {
                    
                    data[k].showInLegend = true;
                    if(typeof(data[k].visible) === "undefined" || data[k].visible)
                        transactionCode[i].Display = "1";
                }else{
                    data[k].showInLegend = false;
                    data[k].dataPoints = [];
                    transactionCode[i].Display = "0";
                }*/
        }
    }

}

//set total count & amount
function TotalCountAmount()
{
    totalSum = 0;
    totalCount = 0;
    for(var i=0;i<transactionCode.length;i++){
        if(channelCode[i].Display == "1") {
            if(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-count"] != null
                && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-count"] != undefined) {
                totalCount = parseInt(totalCount) + parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-count"]);
            }
            if(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-sum"] != null
                && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-sum"] != undefined) {
                totalSum = parseInt(totalSum) + parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-sum"]);
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
