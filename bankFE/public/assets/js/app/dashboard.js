var socket = io.connect();

// DOM Ready =============================================================
$(document).ready(function() {
    
    // Populate the user table on initial page load
    OnLoad();
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

function OpenSocket(){
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "line", showInLegend: true, name: channelCode[i].ChannelName, dataPoints:[]};
            data.push(dataSeries);
        }
    });
    socket.on('listTransactionCode',function(data1){
        transactionCode = data1;
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

//set ranking
function TopTen(Top, slidingTime) {
    socket.on('Top-' + Top + '-' + slidingTime, function (data1) {
        var array = $.map(data1, function (value, index) {
            return [value];
            console.log(value);
        });        

        var TopTenDepositTop1Acc = document.getElementById(Top + "-Account");
        if(TopTenDepositTop1Acc == null || TopTenDepositTop1Acc == undefined)
            return;

        TopTenDepositTop1Acc.innerHTML = (array[0] != null || array[0] != undefined)? array[0] : '';
        TopTenDepositTop1Acc.href = profileHost + TopTenDepositTop1Acc.innerHTML;

        var TopTenDepositTop1Amount = document.getElementById(Top + "-Amount");
        if(TopTenDepositTop1Amount == null || TopTenDepositTop1Amount == undefined)
            return;
        TopTenDepositTop1Amount.innerHTML = (array[1] != null || array[1] != undefined)? array[1] : '';
    });
}


setInterval(function() {    
    TopBot(time1);
    TopBot(time2);
    TopBot(time3);
    TotalCountAmount();
}, 1000);

// set ranking
function TopBot(slidingTime){
    for(var z = 1;z<=5;z++) {
        if(transactionCode == null || transactionCode == undefined)
            return;
        var tranCode = $.map(transactionCode, function (value, index) {
            return [value];
        });
        for (var i = 0; i < tranCode.length; i++) {
            TopTen('TopTen' + tranCode[i] + '-Top' + z.toString(), slidingTime.toString());
            TopTen('TopTen' + tranCode[i] + '-Bot' + z.toString(), slidingTime.toString());
        }

    }
}

// build data for chart
function buidData(data)
{
    for(var k=0;k<data.length;k++) {
        for (var i = 0; i < channelCode.length; i++) {
            var dataPoints = [];

            if(data[k].name == channelCode[i].ChannelName) {
                var visible = false;
                for (var j = 0; j < jsonObj.length; j += 1) {

                    if (visible == false && jsonObj[j][channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][channelCode[i].ChannelCode + "-count"] != "") visible = true;
                    dataPoints.push({
                        x: new Date(jsonObj[j]["time"]),
                        y: parseInt(jsonObj[j][channelCode[i].ChannelCode + "-count"])
                    });
                }
                if (visible == true) {
                    data[k].dataPoints = dataPoints;
                    data[k].showInLegend = true;
                    if(typeof(data[k].visible) === "undefined" || data[k].visible)
                        channelCode[i].Display = "1";
                }else{
                    data[k].showInLegend = false;
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
            totalCount = parseInt(totalCount) + parseInt(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-count"]);
            totalSum = parseInt(totalSum) + parseInt(jsonObj[jsonObj.length - 1][channelCode[i].ChannelCode + "-sum"]);
        }
    }

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