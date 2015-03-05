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
var totalCount = 0;
var totalSum = 0;
var ListRanking = [];

function OpenSocket(){
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "line", showInLegend: true, name: channelCode[i].ChannelName, dataPoints:[]};
            data.push(dataSeries);
        }
    });
    socket.on('listTransactionCode',function(data1){
        transactionCode = $.map(data1, function (value, index) {
            return [value];
        });
        TopBot(time1);
        TopBot(time2);
        TopBot(time3);
    });
}


function OnLoad() {
    OpenSocket();

    //set event selected cho combobox time
    $('#SlidingTimeCbo').change(function() {
        var str = "";
        $( "select option:selected" ).each(function() {
            str += $( this ).text();
        });
        if(str.toLowerCase().localeCompare("1 Minute".toLowerCase()) == 0)
        {
            socket.emit('open 1 Minute',"1 Minute");
            createLineChart(time1);
            //alert('1 minute');
        }
        else if(str.toLowerCase().localeCompare("1 hour".toLowerCase()) == 0)
        {
            socket.emit('open 1 hour',"1 hour");
            createLineChart(time2);
            //alert('1 hour');
        }
        else if(str.toLowerCase().localeCompare("1 day".toLowerCase()) == 0)
        {
            socket.emit('open 1 day',"1 day");
            createLineChart(time3);
            //alert('1 day');
        }
    }).change();
}

////set ranking
//function TopTen(Top, slidingTime) {
//    socket.on('Top-' + Top + '-' + slidingTime, function (data1) {
//        var array = $.map(data1, function (value, index) {
//            return [value];
//            console.log(value);
//        });
//
//        var TopTenDepositTop1Acc = document.getElementById(Top + "-Account");
//        if(TopTenDepositTop1Acc == null || TopTenDepositTop1Acc == undefined)
//            return;
//
//        TopTenDepositTop1Acc.innerHTML = (array[0] != null || array[0] != undefined)? array[0] : '';
//        TopTenDepositTop1Acc.href = profileHost + TopTenDepositTop1Acc.innerHTML;
//
//        var TopTenDepositTop1Amount = document.getElementById(Top + "-Amount");
//        if(TopTenDepositTop1Amount == null || TopTenDepositTop1Amount == undefined)
//            return;
//        TopTenDepositTop1Amount.innerHTML = (array[1] != null || array[1] != undefined)? array[1] : '';
//    });
//}


setInterval(function() {    
    //TopBot(time1);
    //TopBot(time2);
    //TopBot(time3);
    TotalCountAmount();
}, 1000);

// set ranking
function TopBot(slidingTime){
    if (transactionCode == null || transactionCode == undefined)
        return;
    for (var p = 0; p < transactionCode.length; p++) {
        //ListRanking.clean();
        socket.on('emitRanking-Ranking-' + transactionCode[p] + '-' + slidingTime.toString(), function (data1) {
            if(data1 != null) {
                var jsonRanking = $.parseJSON('[' + data1 + ']');
                if (jsonRanking[0] != null) {
                        var dataTop = '';
                        var dataBot = '';
                    for (var j = 1; j <= 5; j++) {
                    
                        var arrayBot = [jsonRanking[0]["TopTen-Bot" + j.toString() + "-" + slidingTime.toString() + "-Acc"],
                            jsonRanking[0]["TopTen-Bot" + j.toString() + "-" + slidingTime.toString() + "-Amount"]];
                        var arrayTop = [jsonRanking[0]["TopTen-Top" + j.toString() + "-" + slidingTime.toString() + "-Acc"],
                            jsonRanking[0]["TopTen-Top" + j.toString() + "-" + slidingTime.toString() + "-Amount"]];
                        //SetRanking('TopTen' + jsonRanking[0]["TransactionType"] + '-Top' + j.toString(), arrayTop, jsonRanking[0]["TransactionType"]);
                        //SetRanking('TopTen' + jsonRanking[0]["TransactionType"] + '-Bot' + j.toString(), arrayBot, jsonRanking[0]["TransactionType"]);

                        var valueAmount = (arrayTop[1] != null || arrayTop[1] != undefined) ? arrayTop[1] : '';
                        var valueAccount = (arrayTop[0] != null || arrayTop[0] != undefined) ? arrayTop[0] : '';
                        //Lay du lieu tung hang
                        dataTop = dataTop + "<li><span class='topstatistic' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Top"+j.toString() + "-Amount'>" + valueAmount +
                        "</span><span class='icons'><i class='fa fa-envelope'></i></span><a href='"+  valueAccount + "' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Top"+j.toString() +"-Account'>"+ valueAccount + "</a></li>";

                        valueAmount = (arrayBot[1] != null || arrayBot[1] != undefined) ? arrayBot[1] : '';
                        valueAccount = (arrayBot[0] != null || arrayBot[0] != undefined) ? arrayBot[0] : '';

                        dataBot = "<li><span class='topstatistic' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Bot"+j.toString() + "-Amount'>" + valueAmount +
                        "</span><span class='icons'><i class='fa fa-envelope'></i></span><a href='"+ valueAccount + "' id='TopTen" + jsonRanking[0]["TransactionType"] + "-Bot"+j.toString() +"-Account'>"+ valueAccount + "</a></li>" + dataBot;
                    }
                    //Show du lieu
                    if($('#topFive' + jsonRanking[0]["TransactionType"]).is(":visible"))
                        $('#topFive' + jsonRanking[0]["TransactionType"] +'  ul').html(dataTop);

                    if($('#topFiveDown' + jsonRanking[0]["TransactionType"]).is(":visible"))
                        $('#topFiveDown'+ jsonRanking[0]["TransactionType"] +' ul').html(dataBot);
                    //console.info(dataTop); //Debug
                }
            }

        });
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
}
// build data for chart
function buidData(data)
{
    for(var k=0;k<data.length;k++) {
        for (var i = 0; i < channelCode.length; i++) {


            if(data[k].name == channelCode[i].ChannelName) {
                var dataPoints = [];
                var visible = false;
                for (var j = 0; j < jsonObj.length; j += 1) {

                    if (visible == false && jsonObj[j][channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][channelCode[i].ChannelCode + "-count"] != "") visible = true;
                    if(jsonObj[j][channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][channelCode[i].ChannelCode + "-count"] != "") {
                        dataPoints.push({
                            x: new Date(jsonObj[j]["time"]),
                            y: parseInt(jsonObj[j][channelCode[i].ChannelCode + "-count"])
                        });
                    }
                }
                if (visible == true) {
                    data[k].dataPoints = dataPoints;
                    data[k].showInLegend = true;
                    if(typeof(data[k].visible) === "undefined" || data[k].visible)
                        channelCode[i].Display = "1";
                }else{
                    data[k].showInLegend = false;
                    data[k].dataPoints = [];
                    channelCode[i].Display = "0";
                }
            }
        }
    }

    return data;
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

// khoi tao chart
function createLineChart(time) {

    var chartCD1 = new CanvasJS.Chart("chartCD",
        {
            zoomEnabled: true,
            title: {
                text: "Line Chart TPB"
            },
            toolTip: {
                shared: true

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
  socket.on('listChart-real-time-count-chart-' + time,function(data1){
      jsonObj = $.parseJSON('[' + data1 + ']');
      buidData(data);
      chartCD1.render();
  });

}