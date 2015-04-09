window.onload = function () {

        

        //initial value of dataPoints 

        var dps = [

        {label: "Cash Deposits", y: randomIntFromInterval(10,100)},

        {label: "Cash Withdrawal", y: randomIntFromInterval(10,100)},

        {label: "Current Account Opening", y: randomIntFromInterval(10,100)},

        {label: "Fixed Deposit", y: randomIntFromInterval(10,100)},

        {label: "Issue Debit Card", y: randomIntFromInterval(10,100)},

        {label: "Issue Credit Card", y: randomIntFromInterval(10,100)},
		
	{label: "Gold Purchase", y: randomIntFromInterval(10,100)},

        {label: "eBank", y: randomIntFromInterval(10,100)},

        {label: "Car Loan", y: randomIntFromInterval(10,100)},
		
	{label: "Mortage Loan", y: randomIntFromInterval(10,100)}
        ];  
		
		var dps1 = [

        {label: "Cash Deposits", y: randomIntFromInterval(10,100)},

        {label: "Cash Withdrawal", y: randomIntFromInterval(10,100)},

        {label: "Current Account Opening", y: randomIntFromInterval(10,100)},

        {label: "Fixed Deposit", y: randomIntFromInterval(10,100)},

        {label: "Issue Debit Card", y: randomIntFromInterval(10,100)},

        {label: "Issue Credit Card", y: randomIntFromInterval(10,100)},
		
	{label: "Gold Purchase", y: randomIntFromInterval(10,100)},

        {label: "eBank", y: randomIntFromInterval(10,100)},

        {label: "Car Loan", y: randomIntFromInterval(10,100)},
		
	{label: "Mortage Loan", y: randomIntFromInterval(10,100)}

        ];   
 

        var chart = new CanvasJS.Chart("chartCD",{           

            title: {

                text: "Branch Transactions Distribution"      

            },
	    axisX: {                

                labelAngle: 50,
            }, 
            axisY: {                

                //suffix: " C"
            }, 
	    axisY2: {                
		
                suffix: " %"

            },     

            legend :{

                verticalAlign: 'bottom',

                horizontalAlign: "center"

            },

            data: [

            {

                type: "column",

		showInLegend: true, 
		
		name: "Count", 

                bevelEnabled: true,             

                //indexLabel: "{y} C",

                dataPoints: dps                 

            },
			{
				axisYType: "secondary",
                type: "line", 

		showInLegend: true, 
		
		name: "Cumulative",

                bevelEnabled: true,             

                //indexLabel: "{y} C",

                dataPoints: dps1                 

            }

            ]

        });

 	chart.render();

        

        var updateInterval = 1000;  

        

 
		var k=10;
        var updateChart = function () {
			k=k+1;
			dps.splice(0, 1);
			dps1.splice(0, 1);
			dps.push({label: "boiler11", y: randomIntFromInterval(10,100)});
			dps1.push({label: "boiler11", y: randomIntFromInterval(10,100)});
            for (var i = 0; i < dps.length; i++) {
                // updating the dataPoint
				var boilerColor;
                dps[i] = {label: "boiler"+(i+k) , y: dps[i].y, color: boilerColor};
            };
			for (var i = 0; i < dps1.length; i++) {
                // updating the dataPoint
				var boilerColor;

                dps1[i] = {label: "boiler"+(i+k) , y: dps1[i].y, color: boilerColor};
            };
 

            chart.render();

        };

        //updateChart();      
	//updateChart();   
        // update chart after specified interval 

        //setInterval(function(){updateChart()}, updateInterval);

 

 

    }

	function randomIntFromInterval(min,max)
	{
		return Math.floor(Math.random()*(max-min+1)+min);
	}
