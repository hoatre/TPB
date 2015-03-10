$(document).ready(function() {

    // Populate the user table on initial page load
    OnLoad();
});

var socket = io.connect();

var channelCode = [];
var transactionCode;
var productType;


function OpenSocket(){
    socket.on('listChannelCode',function(data1){

        channelCode = JSON.parse('[' + data1 + ']');

        var x = document.getElementById("ChannelCbo");
        for(var i = 0; i<channelCode.length; i++) {
            var option = document.createElement("option");
            option.text = channelCode[i].ChannelName;
            option.value = channelCode[i].ChannelCode;
            x.add(option);
        }
        EventComboboxColumn();
    });

    socket.on('listTransactionCode',function(data1){
        transactionCode = $.map(data1, function (value, index) {
            return [value];
        });
    });

    socket.on('listProductType',function(data1){
        productType = $.map(data1, function (value, index) {
            return [value];
        });
    });

    socket.on('GetDataColumnChart',function(data1){
        var MonthInYear = ["Jan", "Feb",  "Mar", "Apr", "May", "Jun", "Jul", "Agu", "Sep", "Oct", "Nov", "Dec"];
        var dataPointsColumnChartCount = [];
        var dataPointsColumnChartAmount = [];
        var dataColumnChart = [];
        dataColumnChart = JSON.parse('[' + data1 + ']');
        for(var i = 0;i<dataColumnChart.length;i++){
            if(dataColumnChart[i] != null && dataColumnChart[i] != undefined) {
                dataPointsColumnChartCount.push({
                    label: MonthInYear[i],
                    y: parseInt(dataColumnChart[i].Count)
                });
                dataPointsColumnChartAmount.push({
                    label: MonthInYear[i],
                    y: parseInt(dataColumnChart[i].Amount)
                });
            }
        }
        CreateColumnChart(dataPointsColumnChartCount, dataPointsColumnChartAmount);
    });

    socket.on('GetDataPieChart',function(data1){
        var dataPieChart = [];
        dataPieChart = JSON.parse('[' + data1 + ']');
        var dataPointsPieChartCount = [];
        for(var i = 0;i<dataPieChart.length;i++)

            if(dataPieChart[i] != null && dataPieChart[i] != undefined) {
                dataPointsPieChartCount.push({
                    y: parseInt(dataPieChart[i].Count),
                    legendText:dataPieChart[i].ProductType.toString(),
                    indexLabel:dataPieChart[i].ProductType.toString() + "(#percent%)"
                });
            }

        CreatePieChart(dataPointsPieChartCount);
    });
}

function CreateColumnChart(dataPointsColumnChartCount, dataPointsColumnChartAmount){

    var ColumnChart = new CanvasJS.Chart("ColumnChart",
        {
            theme: "theme3",
            animationEnabled: true,
            toolTip: {
                shared: true
            },

            axisY: {
                title: "Count"
            },
            axisY2: {
                title: "Amount"
            },
            data: [
                {
                    type: "column",
                    name: "Count",
                    legendText: "Count",
                    showInLegend: true,
                    dataPoints:dataPointsColumnChartCount
                },
                {
                    type: "line",
                    name: "Amount",
                    legendText: "Amount",
                    axisYType: "secondary",
                    showInLegend: true,
                    dataPoints:dataPointsColumnChartAmount
                }

            ],
            legend:{
                verticalAlign: "bottom",
                horizontalAlign: "center",
                fontSize:12,
                cursor:"pointer",
                itemclick: function(e){
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    }
                    else {
                        e.dataSeries.visible = true;
                    }
                    ColumnChart.render();
                }
            }
        });

    ColumnChart.render();

}

function CreatePieChart(dataPointsPieChartCount){
    var PieChart = new CanvasJS.Chart("PieChart",
        {
            title:{
                text: ""
            },
            animationEnabled: true,
            legend:{
                verticalAlign: "bottom",
                horizontalAlign: "center",
                fontSize: 12
            },
            data: [
                {
                    indexLabelFontSize: 12,
                    indexLabelFontFamily: "Monospace",
                    indexLabelFontColor: "darkgrey",
                    indexLabelLineColor: "darkgrey",
                    indexLabelPlacement: "outside",
                    type: "pie",
                    showInLegend: true,
                    toolTipContent: "<strong>#percent%</strong>",
                    dataPoints: dataPointsPieChartCount
                }
            ]
        });
    PieChart.render();
}

function getSelectionText() {
    var text = "";
    if (window.getSelection) {
        text = window.getSelection().toString();
    } else if (document.selection && document.selection.type != "Control") {
        text = document.selection.createRange().text;
    }
    return text;
}

function EventComboboxPie(){
    $('#YearPieCbo').change(function() {
        var YearPieCbo = document.getElementById("YearPieCbo");
        var yearPie = YearPieCbo.options[YearPieCbo.selectedIndex].text;
        var data = [yearPie];
        socket.emit('EventComboboxPie',data);
    }).change();
}

function EventComboboxColumn(){

    $('#ChannelCbo').change(function() {
        var ChannelCbo = document.getElementById("ChannelCbo");
        var Channel = ChannelCbo.options[ChannelCbo.selectedIndex].value;
        var YearColumnCbo = document.getElementById("YearColumnCbo");
        var year = YearColumnCbo.options[YearColumnCbo.selectedIndex].text;
        var data = [Channel, year];
        socket.emit('EventComboboxColumn',data);
    }).change();

    $('#YearColumnCbo').change(function() {
        var ChannelCbo = document.getElementById("ChannelCbo");
        var Channel = ChannelCbo.options[ChannelCbo.selectedIndex].value;
        var YearColumnCbo = document.getElementById("YearColumnCbo");
        var year = YearColumnCbo.options[YearColumnCbo.selectedIndex].text;
        var data = [Channel, year];
        socket.emit('EventComboboxColumn',data);
    }).change();

}

function AddDataCombobox(){
    var x = document.getElementById("YearColumnCbo");
    var y = document.getElementById("YearPieCbo");
    for(var i = 0; i<=10; i++) {
        var option = document.createElement("option");
        option.text = new Date().getFullYear() - i;
        var optionY = document.createElement("option");
        optionY.text = new Date().getFullYear() - i;
        x.add(option);
        y.add(optionY);
    }
}

function OnLoad() {
    OpenSocket();
    AddDataCombobox();
    EventComboboxPie();
}