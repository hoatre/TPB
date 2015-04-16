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
var transactionCode = [];
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
    socket.on('listTransactionCodeJson',function(data1){
        //transactionCode = $.map(data1, function (value, index) {
        //    return [value];
        //});
        transactionCode = JSON.parse('[' + data1 + ']');

        for (var i = 0; i < transactionCode.length; i++) {
            var dataSeries = {
                xValueType: "dateTime",
                type: "scatter",
                showInLegend: true,
                name: transactionCode[i].TransactionName,
                toolTipContent: "<span style='\"'color: {color};'\"'><strong>{name}</strong></span><br/><strong> time </strong> {x} <br/><strong> amount </strong></span> {y}",
                dataPoints:[]};
            data.push(dataSeries);
        }
    });
    socket.on('listChart-Sliding-data-' + time1,function(data1){
        jsonObj_time1 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-Sliding-data-' + time2,function(data1){
        jsonObj_time2 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-Sliding-data-' + time3,function(data1){
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
    chartCD1 = new CanvasJS.Chart("pointChart",
        {
            zoomEnabled: true,
            title: {
                text: ""
            },
            toolTip: {
                //shared: true

            },
            axisY:{
                //includeZero: false

            },
            legend:{
                cursor:"pointer",
                itemclick : function(e) {
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    }
                    else {
                        e.dataSeries.visible = true;
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
        for (var i = 0; i < transactionCode.length; i++) {
            if(data[k].name==(transactionCode[i].TransactionName)) {
                var dataPoints = [];

                for (var j = 0; j < jsonObj.length; j += 1) {
                    if(jsonObj[j]["trx_code"] == transactionCode[i].TransactionCode) {

                        dataPoints.push({
                            x: new Date(jsonObj[j]["timestamp"]),
                            y: jsonObj[j]["amount"]
                        });
                    }
                }
                if(dataPoints.length > 0) {
                    data[k].dataPoints = dataPoints;
                    data[k].showInLegend = true;
                }else
                {
                    data[k].dataPoints = [];
                    data[k].showInLegend = false;
                }
            }

        }
    }
}