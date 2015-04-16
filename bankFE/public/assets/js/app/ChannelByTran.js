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
var transactionSelected;
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
var chartCD1;
var SlidingTimeCbo;
var timer;

function OpenSocket(){
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < channelCode.length; i++) {
            var dataSeries = { xValueType: "dateTime", type: "line", showInLegend: true, name: channelCode[i].ChannelName, dataPoints:[]};
            data.push(dataSeries);
        }
    });
    socket.on('listTransactionCodeJson',function(data1){
        transactionCode = JSON.parse('[' + data1 + ']');

        var x = document.getElementById("TransactionCbo");
        for(var i = 0; i<transactionCode.length; i++) {
            var option = document.createElement("option");
            option.text = transactionCode[i].TransactionName;
            option.value = transactionCode[i].TransactionCode;
            x.add(option);
        }
        EventTransactionCbo();

    });
    socket.on('listChart-real-time-count-chart-tran-' + time1,function(data1){
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-count-chart-tran-' + time2,function(data1){
        jsonObj_time2 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-count-chart-tran-' + time3,function(data1){
        jsonObj_time3 = $.parseJSON('[' + data1 + ']');
    });
}

function EventTransactionCbo(){
    $('#TransactionCbo').change(function() {
        var TransactionCbo = document.getElementById("TransactionCbo");
        transactionSelected = TransactionCbo.options[TransactionCbo.selectedIndex].value;
    }).change();
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
    chartCD1 = new CanvasJS.Chart("ChannelByTranChart",
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

setInterval(function() {
    SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;
    if(timer == time1)
        jsonObj = jsonObj_time1;
    else if(timer == time2)
        jsonObj = jsonObj_time2;
    else if(timer == time3)
        jsonObj = jsonObj_time3;
    buidData();
    chartCD1.render();
}, 1000);

// build data for chart
function buidData()
{
    if(timer == time1)
        jsonObj = jsonObj_time1;
    else if(timer == time2)
        jsonObj = jsonObj_time2;
    else if(timer == time3)
        jsonObj = jsonObj_time3;
    for(var k=0;k<data.length;k++) {
        for (var i = 0; i < channelCode.length; i++) {
            if(data[k].name == channelCode[i].ChannelName) {
                var dataPoints = [];
                var visible = false;
                for (var j = 0; j < jsonObj.length; j += 1) {

                    if (visible == false && jsonObj[j][transactionSelected + "-" + channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][transactionSelected + "-" + channelCode[i].ChannelCode + "-count"] != "") visible = true;
                    if(jsonObj[j][transactionSelected + "-" + channelCode[i].ChannelCode + "-count"] != null && jsonObj[j][transactionSelected + "-" + channelCode[i].ChannelCode + "-count"] != "") {
                        dataPoints.push({
                            x: new Date(jsonObj[j]["time"]),
                            y: parseInt(jsonObj[j][transactionSelected + "-" + channelCode[i].ChannelCode + "-count"])
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
}
