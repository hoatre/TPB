var socket = io.connect();

// DOM Ready =============================================================
$(document).ready(function() {
    
    // Populate the user table on initial page load
    createTimeline();

    jQuery("abbr.timeago").timeago();
    // Username link click
    //$('#userList table tbody').on('click', 'td a.linkshowuser', showUserInfo);

    // Add User button click
    //$('#btnAddUser').on('click', addUser);

    // Delete User link click
    //$('#userList table tbody').on('click', 'td a.linkdeleteuser', deleteUser);


});

// Data
var line1 = new TimeSeries();
var line2 = new TimeSeries();
var line3 = new TimeSeries();
var line4 = new TimeSeries();

var line5 = new TimeSeries();
var line6 = new TimeSeries();
var line7 = new TimeSeries();
var line8 = new TimeSeries();

var Deposit = "DE";
var Withdrawal = "WI";
var Transfer_From = "TF";
var Branch1 = "B1";
var Branch2 = "B2";
var Branch3 = "B3";
var Contact_Center = "Contact";
var profileHost = '/profile/';

var countB1 = 0;
var sumB1 = 0;
var countB2 = 0;
var sumB2 = 0;
var countB3 = 0;
var sumB3 = 0;
var countCenter = 0;
var sumCenter = 0;

var time1 = 60000;
var time2 = 3600000;
var time3 = 86400000;

var totalCount = 0;
var totalSum = 0;
var Line11Checked = true;
var Line21Checked = true;
var Line31Checked = true;
var Line41Checked = true;
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
        //console.log(TopTenDepositTop1Acc.innerHTML + ': TopTenDepositTop1Acc');


        var TopTenDepositTop1Amount = document.getElementById(Top + "-Amount");
        if(TopTenDepositTop1Amount == null || TopTenDepositTop1Amount == undefined)
            return;
        TopTenDepositTop1Amount.innerHTML = (array[1] != null || array[1] != undefined)? array[1] : '';
    });
}

// var chart = new SmoothieChart({
//             millisPerPixel: time1/(window.innerWidth - 300),
//             //millisPerPixel: time1/($('#chart').width()),
//             grid: {
//                 fillStyle: 'transparent',
//                 strokeStyle: 'rgba(166,197,103,0.20)',
//                 borderVisible: true
//             },
//             labels: { fillStyle: '#000000' }
//         }),canvas = document.getElementById('chart'),
//         series = new TimeSeries();

function Total(slidingTime){
    socket.on('Total-TotalNoTran-' + slidingTime, function (data1) {
        var TotalNoTran = document.getElementById("TotalNoTran");
        TotalNoTran.innerHTML = data1.toString();
    });
    socket.on('Total-TotalAmount-' + slidingTime, function (data1) {
        var TotalAmount = document.getElementById("TotalAmount");
        TotalAmount.innerHTML = data1.toString();
    });
}
setInterval(function() {    
    totalCount = parseInt(countB1) + parseInt(countB2) + parseInt(countB3) + parseInt(countCenter);
    totalSum = parseInt(sumB1) + parseInt(sumB2) + parseInt(sumB3) + parseInt(sumCenter);

    if(!Line11Checked)
    {
        totalCount = parseInt(totalCount) - parseInt(countB1);
        totalSum = parseInt(totalSum) - parseInt(sumB1);
    }
    if(!Line21Checked)
    {
        totalCount = parseInt(totalCount) - parseInt(countB2);
        totalSum = parseInt(totalSum) - parseInt(sumB2);
    }
    if(!Line31Checked)
    {
        totalCount = parseInt(totalCount) - parseInt(countB3);
        totalSum = parseInt(totalSum) - parseInt(sumB3);
    }
    if(!Line41Checked) {
        totalCount = parseInt(totalCount) - parseInt(countCenter);
        totalSum = parseInt(totalSum) - parseInt(sumCenter);
    }

    var TotalNoTran = document.getElementById("TotalNoTran");
    TotalNoTran.innerHTML = totalCount.toString();

    var TotalAmount = document.getElementById("TotalAmount");
    TotalAmount.innerHTML = totalSum.toString();

}, 1000);
function TopBot(slidingTime){
    for(z = 1;z<=5;z++) {
        TopTen('TopTen' + Deposit + '-Top' + z.toString(), slidingTime.toString());
        TopTen('TopTen' + Deposit + '-Bot' + z.toString(), slidingTime.toString());
        TopTen('TopTen' + Withdrawal + '-Top' + z.toString(), slidingTime.toString());
        TopTen('TopTen' + Withdrawal + '-Bot' + z.toString(), slidingTime.toString());
        TopTen('TopTen' + Transfer_From + '-Top' + z.toString(), slidingTime.toString());
        TopTen('TopTen' + Transfer_From + '-Bot' + z.toString(), slidingTime.toString());
    }
}
function LineChart(slidingTime){

    socket.on('CountChart-real-time-count-' + Branch1 + '-' + slidingTime, function (data1) {
        //line1.append(new Date().getTime(), data1);
        countB1 = parseInt(data1);
        console.log(countB1);

    });
    socket.on('CountChart-real-time-count-' + Branch2 + '-' + slidingTime, function (data1) {
        //line2.append(new Date().getTime(), data1);
        countB2 = parseInt(data1);
        console.log(countB2);
    });
    socket.on('CountChart-real-time-count-' + Branch3 + '-' + slidingTime, function (data1) {
        //line3.append(new Date().getTime(), data1);
        countB3 = parseInt(data1);
        console.log(countB3);
    });
    socket.on('CountChart-real-time-count-' + Contact_Center + '-' + slidingTime, function (data1) {
        //line4.append(new Date().getTime(), data1);
        countCenter = parseInt(data1);
        console.log(countCenter);
    });
}
function SumPer(slidingTime){
    socket.on('CountChart-real-time-sum-' + Branch1 + '-' + slidingTime, function (data1) {
        sumB1 = parseInt(data1);
    });
    socket.on('CountChart-real-time-sum-' + Branch2 + '-' + slidingTime, function (data1) {
        sumB2 = parseInt(data1);
    });
    socket.on('CountChart-real-time-sum-' + Branch3 + '-' + slidingTime, function (data1) {
        sumB3 = parseInt(data1);
    });
    socket.on('CountChart-real-time-sum-' + Contact_Center + '-' + slidingTime, function (data1) {
        sumCenter = parseInt(data1);
    });
}
function createTimeline() {

    // $( "input" ).change(function() {
    //     Line11Checked = $('input[name="Line11"]:checked').length > 0;
    //     Line21Checked = $('input[name="Line21"]:checked').length > 0;
    //     Line31Checked = $('input[name="Line31"]:checked').length > 0;
    //     Line41Checked = $('input[name="Line41"]:checked').length > 0;
    // }).change();

    // $("#chart").attr({'width': window.innerWidth - 300, 'height': 220});

    // $('#line11').click(line11);
    // $('#line21').click(line21);
    // $('#line31').click(line31);
    // $('#line41').click(line41);

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

TopBot(time1);
SumPer(time1);
/*
    
    TopBot(time2);
    TopBot(time3);    
    
    SumPer(time2);
    SumPer(time3);
    */
    LineChart(time1);
    //LineChart(time2);
    //LineChart(time3);
//          Total(30000);
//          Total(60000);
//          Total(600000);
    // chart.addTimeSeries(line1,
    //         { strokeStyle: '#A6C567', lineWidth:3 });
    // chart.addTimeSeries(line2,
    //         { strokeStyle:'#0000FF', lineWidth:3 });
    // chart.addTimeSeries(line3,
    //         { strokeStyle:'#DC143C', lineWidth:3 });
    // chart.addTimeSeries(line4,
    //         { strokeStyle:'#FFD700', lineWidth:3 });

    // chart.streamTo(document.getElementById("chart"), 1000);
    
}

// function line11() {
//     console.log('line11');
//     if($('#line11').is(':checked')) {
//         chart.addTimeSeries(line1,
//                 {strokeStyle: '#A6C567', lineWidth: 3});
//     }
//     else{
//         chart.removeTimeSeries(line1);
//     }
// }


// function line21() {
//     console.log('line21');
//     if($('#line21').is(':checked'))
//         chart.addTimeSeries(line2,
//                 { strokeStyle:'#0000FF', lineWidth:3 });

//     else{
//         chart.removeTimeSeries(line2);
//     }
// }

// function line31() {
//     console.log('line31');
//     if($('#line31').is(':checked'))
//         chart.addTimeSeries(line3,
//                 { strokeStyle:'#DC143C', lineWidth:3 });
//     else{
//         chart.removeTimeSeries(line3);
//     }
// }

// function line41() {
//     console.log('line41');
//     if($('#line41').is(':checked'))
//         chart.addTimeSeries(line4,
//                 { strokeStyle:'#FFD700', lineWidth:3 });
//     else{
//         chart.removeTimeSeries(line4);
//     }
// }

function createLineChart(time) {
  //var socket1 = io.connect('http://localhost:4000/');
  socket.on('listChart-real-time-count-chart-' + time,function(data1){
      var jsonObj = $.parseJSON('[' + data1 + ']');
      var y = 0;
      var data = [];
      var dataSeriesB1 = { type: "line", showInLegend: true, name: "TPBank Ben Thanh" };
      var dataPointsB1 = [];
      var dataSeriesB2 = { type: "line", showInLegend: true, name: "TPBank Da Nang" };
      var dataPointsB2 = [];
      var dataSeriesB3 = { type: "line", showInLegend: true, name: "TPBank Can Tho" };
      var dataPointsB3 = [];
      var dataSeriesCen = { type: "line", showInLegend: true, name: "Contact Center" };
      var dataPointsCen = [];
      for (var i = 0; i < jsonObj.length; i += 1) {
          if(Line11Checked) {
              dataPointsB1.push({
                  x: new Date(jsonObj[i]["time"]),
                  y: parseInt(jsonObj[i]["B1"])
              });
          }
          if(Line21Checked) {
              dataPointsB2.push({
                  x: new Date(jsonObj[i]["time"]),
                  y: parseInt(jsonObj[i]["B2"])
              });
          }
          if(Line31Checked) {
              dataPointsB3.push({
                  x: new Date(jsonObj[i]["time"]),
                  y: parseInt(jsonObj[i]["B3"])
              });
          }
          if(Line41Checked){
              dataPointsCen.push({
                  x: new Date(jsonObj[i]["time"]),
                  y: parseInt(jsonObj[i]["Contact"])
              });
          }
      }
      if(!Line11Checked) {
          dataSeriesB1.visible = false;
      }
      else
      {
          dataSeriesB1.visible = true;
      }
      if(!Line21Checked) {
          dataSeriesB2.visible = false;
      }
      else
      {
          dataSeriesB2.visible = true;
      }
      if(!Line31Checked) {
          dataSeriesB3.visible = false;
      }
      else
      {
          dataSeriesB3.visible = true;
      }
      if(!Line41Checked){
          dataSeriesCen.visible = false;
      }
      else
      {
          dataSeriesCen.visible = true;
      }
      dataSeriesB1.dataPoints = dataPointsB1;
      dataSeriesB2.dataPoints = dataPointsB2;
      dataSeriesB3.dataPoints = dataPointsB3;
      dataSeriesCen.dataPoints = dataPointsCen;

        data.push(dataSeriesB1);
        data.push(dataSeriesB2);
        data.push(dataSeriesB3);
        data.push(dataSeriesCen);
      var chartCD1 = new CanvasJS.Chart("chartCD",
              {
                  title: {
                      text: "Line Chart TPB"
                  },
                  legend: {
                      cursor: "pointer",
                      itemclick: function (e) {
                          if((e.dataSeries.name === "TPBank Ben Thanh" && e.dataSeries.visible)){
                              Line11Checked = false;
                              e.dataSeries.visible = false;
                          } else if(e.dataSeries.name === "TPBank Ben Thanh") {
                              Line11Checked = true;
                              e.dataSeries.visible = true;
                          }
                          if((e.dataSeries.name === "TPBank Da Nang" && e.dataSeries.visible)){
                              Line21Checked = false;
                              e.dataSeries.visible = false;
                          } else if(e.dataSeries.name === "TPBank Da Nang")  {
                              Line21Checked = true;
                              e.dataSeries.visible = true;
                          }
                          if((e.dataSeries.name === "TPBank Can Tho" && e.dataSeries.visible)){
                              Line31Checked = false;
                              e.dataSeries.visible = false;
                          } else if(e.dataSeries.name === "TPBank Can Tho")  {
                              Line31Checked = true;
                              e.dataSeries.visible = true;
                          }
                          if((e.dataSeries.name === "Contact Center" && e.dataSeries.visible)){
                              Line41Checked = false;
                              e.dataSeries.visible = false;
                          } else if(e.dataSeries.name === "Contact Center")  {
                              Line41Checked = true;
                              e.dataSeries.visible = true;
                          }
                          e.chart.render();
                      }
                  },
                  data: data

              });

      chartCD1.render();
  });


}