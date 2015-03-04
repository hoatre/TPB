$(document).ready(function() {

    // Populate the user table on initial page load
    OnLoad();
});

var socket = io.connect();

function OnLoad() {
    var ColumnChart = new CanvasJS.Chart("ColumnChart",
        {
            theme: "theme3",
            animationEnabled: true,
            title:{
                text: "Crude Oil Reserves Vs Production, 2011",
                fontSize: 30
            },
            toolTip: {
                shared: true
            },
            axisX:{
                title: "Source: U.S. Energy Information Administration"
            },

            axisY: {
                title: "billion of barrels"
            },
            axisY2: {
                title: "million barrels/day"
            },

            legend:{
                verticalAlign: "top",
                horizontalAlign: "center"
            },
            data: [
                {
                    type: "column",
                    name: "Proven Oil Reserves (bn)",
                    legendText: "Proven Oil Reserves",
                    showInLegend: true,
                    dataPoints:[
                        {label: "Saudi", y: 262},
                        {label: "Venezuela", y: 211},
                        {label: "Canada", y: 175},
                        {label: "Iran", y: 137},
                        {label: "Iraq", y: 115},
                        {label: "Kuwait", y: 104},
                        {label: "UAE", y: 97.8},
                        {label: "Russia", y: 60},
                        {label: "US", y: 23.3},
                        {label: "China", y: 20.4}


                    ]
                },
                {
                    type: "column",
                    name: "Oil Production (million/day)",
                    legendText: "Oil Production",
                    axisYType: "secondary",
                    showInLegend: true,
                    dataPoints:[
                        {label: "Saudi", y: 11.15},
                        {label: "Venezuela", y: 2.5},
                        {label: "Canada", y: 3.6},
                        {label: "Iran", y: 4.2},
                        {label: "Iraq", y: 2.6},
                        {label: "Kuwait", y: 2.7},
                        {label: "UAE", y: 3.1},
                        {label: "Russia", y: 10.23},
                        {label: "US", y: 10.3},
                        {label: "China", y: 4.3}


                    ]
                }

            ],
            legend:{
                cursor:"pointer",
                itemclick: function(e){
                    if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
                        e.dataSeries.visible = false;
                    }
                    else {
                        e.dataSeries.visible = true;
                    }
                    chart.render();
                }
            }
        });

    ColumnChart.render();

    var PieChart = new CanvasJS.Chart("PieChart",
        {
            title:{
                text: "Gaming Consoles Sold in 2012"
            },
            animationEnabled: true,
            legend:{
                verticalAlign: "bottom",
                horizontalAlign: "center"
            },
            data: [
                {
                    indexLabelFontSize: 20,
                    indexLabelFontFamily: "Monospace",
                    indexLabelFontColor: "darkgrey",
                    indexLabelLineColor: "darkgrey",
                    indexLabelPlacement: "outside",
                    type: "pie",
                    showInLegend: true,
                    toolTipContent: "{y} - <strong>#percent%</strong>",
                    dataPoints: [
                        {  y: 4181563, legendText:"PS 3", indexLabel: "PlayStation 3" },
                        {  y: 2175498, legendText:"Wii", indexLabel: "Wii" },
                        {  y: 3125844, legendText:"360",exploded: true, indexLabel: "Xbox 360" },
                        {  y: 1176121, legendText:"DS" , indexLabel: "Nintendo DS"},
                        {  y: 1727161, legendText:"PSP", indexLabel: "PSP" },
                        {  y: 4303364, legendText:"3DS" , indexLabel: "Nintendo 3DS"},
                        {  y: 1717786, legendText:"Vita" , indexLabel: "PS Vita"}
                    ]
                }
            ]
        });
    PieChart.render();
}