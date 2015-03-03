var socket = io.connect();

$(document).ready(function() {
	
	$('#btnConfigAll').click( function(){		
		var obj=GetValue();
		if($('#btnConfigAll').text() =='Start') {

			for(i = 0; i <= 3; i++)
				obj[i].Status = 'Start';

			$('#btnConfigAll').text('Stop');
			$('#btnConfig1').text('Stop');
			$('#btnConfig1').text('Stop');
			$('#btnConfig2').text('Stop');
			$('#btnConfig3').text('Stop');
			$('#btnConfig4').text('Stop');
		}
		else {
			for(i = 0; i <= 3; i++)
				obj[i].Status = 'Stop';

			$('#btnConfigAll').text('Start');
			$('#btnConfig1').text('Start');
			$('#btnConfig1').text('Start');
			$('#btnConfig2').text('Start');
			$('#btnConfig3').text('Start');
			$('#btnConfig4').text('Start');
		}

		socket.emit('btnConfigAll',obj);
	});

	$('#btnConfig1').click( function() {		
		buttonClick(1);
	});

	$('#btnConfig2').click( function() {
		buttonClick(2);
	});

	$('#btnConfig3').click( function() {
		buttonClick(3);
	});

	$('#btnConfig4').click( function() {
		buttonClick(4);
	});
	
});

//Eo biet cai nay dung de lam gi :D
function isNumberKey(evt){
	var charCode = (evt.which) ? evt.which : evt.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57))
		return false;
	return true;
}

//Begin - Su ly su kien tu server 
socket.on('btnConfig1', function (data) {
		$('#btnConfig1').text('Start');
	CheckButton();
});

socket.on('btnConfig2', function (data) {    	
		$('#btnConfig2').text('Start');
	CheckButton();
});

socket.on('btnConfig3', function (data) {
		$('#btnConfig3').text('Start');
	CheckButton();
});

socket.on('btnConfig4', function (data) {
		$('#btnConfig4').text('Start');
	CheckButton();
});

//End - Su ly su kien tu server 

//Dat lai trang thai button all
function CheckButton(){
	var countStop = 0;
	var countStart = 0;

	//Duyet qua cac cum cau hinh
	for(i = 1; i<= 4; i++){
		if($('#btnConfig'+ i).text()=='Stop')
			countStop++;
		else
			countStart++;
	}
	
	//Ca 4 nut chua hoat dong hoac da chay xong
	if(countStart === 4)
		$('#btnConfigAll').text('Start');

	//Ca 4 nut dang chay
	if(countStop === 4)
		$('#btnConfigAll').text('Stop');

	console.log('countStart: ' + countStart + '; countStop: ' + countStop);
}


//Ham lay du lieu tu cac cum cau hinh
function GetValue()
{
	var objs = [];
	for(i = 1; i<= 4; i++){

		var obj = {
			Account: $('#txtAccount'+i).val(),
			Time: $('#txtTime'+i).val(),
			CountMessage: $('#txtCountMessage'+i).val(),			
			AmountFrom: $('#txtAmountFrom'+i).val(),
			AmountTo: $('#txtAmountTo'+i).val(),
			Channel:$('#cboChannel'+i).val(),
			Product:$('#cboProduct'+i).val(),
			TransactionType:$('#cboTransactionType'+i).val(),
			Status:$('#btnConfig'+i).text(),
			a:5,
			b:5
		}
		objs.push(obj);
	}
	console.info(objs);
	return objs;
}

//Ham xu ly su kien button
function buttonClick(i) {
	var obj=GetValue();
	if($('#btnConfig'+i).text()=='Start')	{
		$('#btnConfig'+i).text('Stop');
	}
	else{
		$('#btnConfig'+i).text('Start');
	}

	CheckButton();
	socket.emit('btnConfig'+i,obj);
}

<!----------------TESTCHANNEL-------------------------!>
socket.on('SIMULATOR-DATA-SEND-ALL-CHANNELS', function(data_channel){
	var totalTransaction=0;
	var totalAmount=0;
	var datahtml="";
	for(var i=0;i<data_channel.length;i++)
	{
		datahtml+='<tr><td>'+getChannelName(data_channel[i].channel)+"</td><td>"+data_channel[i].transaction+"</td><td>"+data_channel[i].amount+"</td></tr>";
		totalAmount = totalAmount+data_channel[i].amount;
		totalTransaction = totalTransaction+data_channel[i].transaction;
	}
	//alert(datahtml);
	datahtml+="<tr><td><span style='font-weight:bold'>Total</span></td><td><span style='font-weight:bold'>"+totalTransaction+"</span></td><td><span style='font-weight:bold'>"+totalAmount+"</span></td></tr>";
	$('#dataCompare tbody').html(datahtml);
});

//Lay ten channal name
function getChannelName(code){	
	var name = 'no data';
	$("#cboChannel1 option").each(function(){
		if($(this).val() == code){
			name = $(this).text();
		}
	});
	return name;
}