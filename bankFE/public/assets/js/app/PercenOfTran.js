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
var listColor = ["#0000FF", "#8A2BE2", "#A52A2A", "#228B22", "#7FFF00", "#DC143C", "#FF00FF", "#FF8C00", "#FFE4C4"];
var channelSelected;
var channelCode = [];
var transactionCode = [];
var data = [];
var dataS = [];
var time1 = 60000;
var time2 = 3600000;
var time3 = 86400000;
var jsonObj;
var jsonObj_time1;
var jsonObj_time2;
var jsonObj_time3;
var jsonObjS;
var jsonObjS_time1;
var jsonObjS_time2;
var jsonObjS_time3;
var totalCount = 0;
var chartCD1;
var chartS;
var SlidingTimeCbo;
var timer;
var TongCountTran = 0;
var TongCountTranS = 0;

function OpenSocket(){
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');


        var x = document.getElementById("ChannelCbo");
        var option1 = document.createElement("option");
        option1.text = "All Channel";
        option1.value = "All";
        x.add(option1);
        for(var i = 0; i<channelCode.length; i++) {
            var option = document.createElement("option");
            option.text = channelCode[i].ChannelName;
            option.value = channelCode[i].ChannelCode;
            x.add(option);
        }
        EventChannelCbo();
    });
    socket.on('listTransactionCodeJson',function(data1){
        transactionCode = JSON.parse('[' + data1 + ']');
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

    socket.on('listChart-real-time-sum-chart-tran-' + time1,function(data1){
        jsonObjS_time1 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-sum-chart-tran-' + time2,function(data1){
        jsonObjS_time2 = $.parseJSON('[' + data1 + ']');
    });
    socket.on('listChart-real-time-sum-chart-tran-' + time3,function(data1){
        jsonObjS_time3 = $.parseJSON('[' + data1 + ']');
    });
}

function EventChannelCbo(){
    $('#ChannelCbo').change(function() {
        var ChannelCbo = document.getElementById("ChannelCbo");
        channelSelected = ChannelCbo.options[ChannelCbo.selectedIndex].value;
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
            jsonObjS = jsonObjS_time1;
        }
        else if(CboValue.toLowerCase().localeCompare("3600000".toLowerCase()) == 0)
        {
            jsonObj = jsonObj_time2;
            jsonObjS = jsonObjS_time2;
        }
        else if(CboValue.toLowerCase().localeCompare("86400000".toLowerCase()) == 0)
        {
            jsonObj = jsonObj_time3;
            jsonObjS = jsonObjS_time3;
        }

    }).change();
}

function CreateChart(){
    chartCD1 = new CanvasJS.Chart("PercenOfTran",
        {
            zoomEnabled: true,
            title: {
                text: ""
            },
            toolTip: {
                shared: true

            },
            axisY2:{
                minimum: 0,
                maximum: 100,
                interval: 100,
                suffix: " %",
                stripLines:[
                    {
                        value:80
                    }
                ]
            },
            legend: {
                cursor: "pointer",
                itemclick: function (e) {
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    } else {
                        e.dataSeries.visible = true;
                    }
                    chartCD1.render();
                }
            },
            data:data
        });
    chartCD1.render();

    chartS = new CanvasJS.Chart("PercenOfTranS",
        {
            zoomEnabled: true,
            title: {
                text: ""
            },
            toolTip: {
                shared: true

            },
            axisY2:{
                minimum: 0,
                maximum: 100,
                interval: 100,
                suffix: " %",
                stripLines:[
                    {
                        value:80
                    }
                ]
            },
            legend: {
                cursor: "pointer",
                itemclick: function (e) {
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    } else {
                        e.dataSeries.visible = true;
                    }
                    chartS.render();
                }
            },
            data:dataS
        });
    chartS.render();
}

setInterval(function() {
    SlidingTimeCbo = document.getElementById("SlidingTimeCbo");
    timer = SlidingTimeCbo.options[SlidingTimeCbo.selectedIndex].value;

    if(timer == time1) {
        jsonObj = jsonObj_time1;
        jsonObjS = jsonObjS_time1;
    }
    else if(timer == time2){
        jsonObj = jsonObj_time2;
        jsonObjS = jsonObjS_time2;
    }
    else if(timer == time3){
        jsonObj = jsonObj_time3;
        jsonObjS = jsonObjS_time3;
    }

    buidData();
    chartCD1.render();
    chartS.render();
}, 1000);

// build data for bar chart
function buidData()
{
    if(timer == time1) {
        jsonObj = jsonObj_time1;
        jsonObjS = jsonObjS_time1;
    }
    else if(timer == time2){
        jsonObj = jsonObj_time2;
        jsonObjS = jsonObjS_time2;
    }
    else if(timer == time3){
        jsonObj = jsonObj_time3;
        jsonObjS = jsonObjS_time3;
    }

    var dataPoints = [];
    var dataPointsS = [];
    data.length = 0;
    dataS.length = 0;

    if(transactionCode.length > 0) {
        for (var i = 0; i < transactionCode.length; i++) {
            var visible = false;

            if (channelSelected == "All"){
                for(var f=0; f<channelCode.length; f++){
                    if (visible == false && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != "") visible = true;
                    if (jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != "") {

                        dataPoints.push({
                            y: parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"]),
                            label: transactionCode[i].TransactionName
                            //color: listColor[i]
                        });
                    }
                }
            }else {
                if (visible == false && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != "") visible = true;
                if (jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != "") {

                    dataPoints.push({
                        y: parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"]),
                        label: transactionCode[i].TransactionName
                        //color: listColor[i]
                    });


                }
            }

            var visibleS = false;

            if (channelSelected == "All"){
                for(var f=0; f<channelCode.length; f++){
                    if (visibleS == false && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != "") visibleS = true;
                    if (jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != "") {

                        dataPointsS.push({
                            y: parseInt(jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"]),
                            label: transactionCode[i].TransactionName
                            //color: listColor[i]
                        });
                    }
                }
            }else {
                if (visibleS == false && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != "") visibleS = true;
                if (jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != "") {

                    dataPointsS.push({
                        //x: new Date(jsonObj[jsonObj.length - 1]["time"]),
                        y: parseInt(jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"]),
                        label: transactionCode[i].TransactionName
                    });


                }
            }
        }
    }

    var result = Enumerable.From(dataPoints)
        .GroupBy(function (x) { return x.label },     // Key selector
        function (x) {                          // Element selector
            return {
                label: x.label,
                y: x.y
            }
        },
        function (x, grouping) {                   // Result selector
            return {
                label: x,
                y: grouping.Sum(function (item) {
                    return item.y
                })
            }
        })
        .ToArray();

    var resultS = Enumerable.From(dataPointsS)
        .GroupBy(function (x) { return x.label },     // Key selector
        function (x) {                          // Element selector
            return {
                label: x.label,
                y: x.y
            }
        },
        function (x, grouping) {                   // Result selector
            return {
                label: x,
                y: grouping.Sum(function (item) {
                    return item.y
                })
            }
        })
        .ToArray();

    result.sort(function (a, b) {

        if(a.y != "undefined" && b.y != "undefined") {
            if (parseInt(a.y) < parseInt(b.y)) {
                return 1;
            }
            if (parseInt(a.y) > parseInt(b.y)) {
                return -1;
            }
        }else{
            return -1;
        }
        // a must be equal to b
        return 0;
    });
    resultS.sort(function (a, b) {

        if(a.y != "undefined" && b.y != "undefined") {
            if (parseInt(a.y) < parseInt(b.y)) {
                return 1;
            }
            if (parseInt(a.y) > parseInt(b.y)) {
                return -1;
            }
        }else{
            return -1;
        }
        // a must be equal to b
        return 0;
    });

    //var linq = Enumerable.From(dataPoints);
    //var result =
    //    linq.GroupBy(function(x){return x.label;})
    //        .Select(function(x){return {color: x.color, label:x.Key(), y: x.Sum(function(y){return y.y|0;}) };})
    //        .ToArray();



    TongCountTran = 0;
    TongCountTranS = 0;

    for(var i = 0; i < result.length; i++){
        TongCountTran = parseInt(TongCountTran) + parseInt(result[i].y);
    }
    for(var i = 0; i < resultS.length; i++){
        TongCountTranS = parseInt(TongCountTranS) + parseInt(resultS[i].y);
    }
    var dataSeries = {
        type: "column",
        //showInLegend: true,
        //name: transactionCode[i].TransactionName,
        toolTipContent : "<span style='\"'color: {color};'\"'><strong>{label}</strong></span> count : </span> {y}",
        dataPoints:result,
        indexLabelPlacement: "inside",
        indexLabelFontColor: "white",
        indexLabelFontWeight: 600,
        indexLabelFontFamily: "Verdana"
    };
    var dataSeriesS = {
        type: "column",
        //showInLegend: true,
        //name: transactionCode[i].TransactionName,
        toolTipContent : "<span style='\"'color: {color};'\"'><strong>{label}</strong></span> sum : </span> {y}",
        dataPoints:resultS,
        indexLabelPlacement: "inside",
        indexLabelFontColor: "white",
        indexLabelFontWeight: 600,
        indexLabelFontFamily: "Verdana"
    };
    data.push(dataSeries);
    dataS.push(dataSeriesS);
    //}
    buidDataLine();
}

// build data for line chart
function buidDataLine()
{
    var dataPoints = [];
    var dataPointsS = [];
    if(transactionCode.length > 0) {
        for (var i = 0; i < transactionCode.length; i++) {
            var visible = false;
            if (channelSelected == "All"){
                for(var f=0; f<channelCode.length; f++){
                    if (visible == false && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != "") visible = true;
                    if (jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"] != "") {
                        //if(dataPoints.length > 0) {
                        //    var cong = parseInt(dataPoints[dataPoints.length - 1].y);
                        dataPoints.push({
                            y: (parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-count"]) / parseInt(TongCountTran)) * 100,
                            label: transactionCode[i].TransactionName
                        });
                        //}
                    }
                }
            }else {
                if (visible == false && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != "") visible = true;
                if (jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != null && jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"] != "") {
                    //if(dataPoints.length > 0) {
                    //    var cong = parseInt(dataPoints[dataPoints.length - 1].y);
                    dataPoints.push({
                        y: (parseInt(jsonObj[jsonObj.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-count"]) / parseInt(TongCountTran)) * 100,
                        label: transactionCode[i].TransactionName
                    });
                    //}
                }
            }

            var visibleS = false;
            if (channelSelected == "All"){
                for(var f=0; f<channelCode.length; f++){
                    if (visibleS == false && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != "") visibleS = true;
                    if (jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"] != "") {
                        //if(dataPoints.length > 0) {
                        //    var cong = parseInt(dataPoints[dataPoints.length - 1].y);
                        dataPointsS.push({
                            y: (parseInt(jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelCode[f].ChannelCode + "-sum"]) / parseInt(TongCountTranS)) * 100,
                            label: transactionCode[i].TransactionName
                        });
                        //}
                    }
                }
            }else {
                if (visibleS == false && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != "") visibleS = true;
                if (jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != null && jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"] != "") {
                    dataPointsS.push({
                        y: (parseInt(jsonObjS[jsonObjS.length - 1][transactionCode[i].TransactionCode + "-" + channelSelected + "-sum"]) / parseInt(TongCountTranS)) * 100,
                        label: transactionCode[i].TransactionName
                    });
                    //}
                }
            }
        }
    }

    var result = Enumerable.From(dataPoints)
        .GroupBy(function (x) { return x.label },     // Key selector
        function (x) {                          // Element selector
            return {
                label: x.label,
                y: x.y
            }
        },
        function (x, grouping) {                   // Result selector
            return {
                label: x,
                y: grouping.Sum(function (item) {
                    return item.y
                })
            }
        })
        .ToArray();

    var resultS = Enumerable.From(dataPointsS)
        .GroupBy(function (x) { return x.label },     // Key selector
        function (x) {                          // Element selector
            return {
                label: x.label,
                y: x.y
            }
        },
        function (x, grouping) {                   // Result selector
            return {
                label: x,
                y: grouping.Sum(function (item) {
                    return item.y
                })
            }
        })
        .ToArray();

    result.sort(function (a, b) {

        if(a.y != "undefined" && b.y != "undefined") {
            if (parseInt(a.y) < parseInt(b.y)) {
                return 1;
            }
            if (parseInt(a.y) > parseInt(b.y)) {
                return -1;
            }
        }else{
            return -1;
        }
        // a must be equal to b
        return 0;
    });
    resultS.sort(function (a, b) {

        if(a.y != "undefined" && b.y != "undefined") {
            if (parseInt(a.y) < parseInt(b.y)) {
                return 1;
            }
            if (parseInt(a.y) > parseInt(b.y)) {
                return -1;
            }
        }else{
            return -1;
        }
        // a must be equal to b
        return 0;
    });



    for(var i = 0; i<result.length; i++){
        if(i > 0) {
            result[i].y += result[i-1].y;
        }
    }
    for(var i = 0; i<resultS.length; i++){
        if(i > 0) {
            resultS[i].y += resultS[i-1].y;
        }
    }
    var dataSeries = {
        yValueFormatString: "####.00",
        type: "line",
        axisYType: "secondary",
        //showInLegend: true,
        name: "Transaction percent",
        toolTipContent : "<span style='\"'color: {color};'\"'> {y} %",
        dataPoints:result
    };
    var dataSeriesS = {
        yValueFormatString: "####.00",
        type: "line",
        axisYType: "secondary",
        //showInLegend: true,
        name: "Transaction percent",
        toolTipContent : "<span style='\"'color: {color};'\"'> {y} %",
        dataPoints:resultS
    };
    data.push(dataSeries);
    dataS.push(dataSeriesS);
    //}

}
