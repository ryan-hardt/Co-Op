
let datasets;
let config;
let colorMap = new Map([
	["Owner", '#BF7B7F'],
	["Helper", 'darkgrey'],
	["Reviewer", '#DE5B49'],
	["Research", '#F0CA4D'],
	["Feature Implementation", '#46B29D'],
	["Bug Fix", '#324D5C'],
	["Unit Test", '#4B87A8'],
	["Refactor", '#E37B40'],
	["Other", '#BF7B7F']
]);

/******************** CYCLE CHARTS ********************/
let cycleDates;
let cycleUserInfo;
let cycleTimeValues;

function initializeCycleChart(cycleId, startDate, endDate) {
	$.post("/coop/work/cycleUsers/"+cycleId, {}, function (data, status){
		cycleUserInfo = data;
		getCycleTimeValues(cycleId, startDate, endDate);
	});
}

function getCycleTimeValues(cycleId, startDate, endDate) {
	$.post("/coop/work/cycleWork/"+cycleId, {}, function(data, status) {
		cycleTimeValues = data;
		setupCycleChart(startDate, endDate, cycleTimeValues, cycleUserInfo);
		
		var chart = document.getElementById('workCanvas');
		//var context = chart.getContext('2d');
		chart.height = "350px";
		window.myLine = new Chart(chart, config);
	});
}

function setupCycleChart(startDate, endDate, timeValues, userInfo) {
	cycleDates = getDates(convertDate(startDate), convertDate(endDate));
	datasets = [];
	let index = 0;
	let colors = ["#DE5B49", "#F0CA4D", "#46B29D", "#324D5C", "#4B87A8", "#E37B40", "#BF7B7F"];
	
	for(let i in timeValues) {
		let newDataset = {
				label: userInfo[index].username,
				backgroundColor: colors[index % colors.length],
				borderColor: colors[index % colors.length],
				data: timeValues[i],
				fill: false
		};
		datasets.push(newDataset);
		index++;
	}
	configCycleChart();
}

function configCycleChart() {
	config = {
			type : 'line',
			data : {
				labels : cycleDates,
				datasets : datasets
			},
			options : {
				responsive : true,
				elements: {
			        line: {
			            tension: 0
			        },
			        point:{
		                radius: 0
		            }
			    },
				title : {
					display : false
				},
				tooltips : {
					mode : 'index',
					intersect : false,
					callbacks: {
		                label: function (tooltipItems, data) {
		                	return ' ' + data.datasets[tooltipItems.datasetIndex].label + ' - ' + data.datasets[tooltipItems.datasetIndex].data[tooltipItems.index] + ' min';
		                }
		           }
				},
				hover : {
					mode : 'nearest',
					intersect : true
				},
				scales : {
					xAxes : [ {
						display : true,
						scaleLabel : {
							display : true,
							labelString : 'Date'
						},
						ticks: {
							autoSkip: false,
					        maxRotation: 0,
					        interval: 3
					    },
					    gridLines : {
		                    display : true
		                },
		                afterTickToLabelConversion: function(data){
		                    var xLabels = data.ticks;

		                    var interval = 2;
		                    
		                    xLabels.forEach(function (labels, i) {
		                        if (i % interval != 0) {
		                            xLabels[i] = '';
		                        }
		                    });
		                }
					} ],
					yAxes : [ {
						display : true,
						scaleLabel : {
							display : true,
							labelString : 'Minutes'
						},
						ticks: {
							beginAtZero: true,
							stepSize: 15
						}
					} ]
				}
			}
		};
}

//converts from string date to JS date
function convertDate(date) {
	return new Date(date);
}

// generates list of days between start and end dates for x-axis
function getDates(startDate, endDate) {
    var dateArray = [];
    var date = startDate;
    while (date <= endDate) {
    		var format = (date.getMonth()+1) + "/" + date.getDate();
    		dateArray.push(format);
    		date.setDate(date.getDate() + 1);
    }
    return dateArray;
}

/******************** WORK CHARTS ********************/
function initializeWorkStats(scope, id) {
	let roleDataUrl = "/coop/work/stats/"+scope+"/"+id + "/role";
	let tagDataUrl = "/coop/work/stats/"+scope+"/"+id + "/tag";

	$.post(roleDataUrl, {}, function(data, status) {
		for(let userId in data) {
			createChart(data[userId], userId,'Role');
		}
	});
	$.post(tagDataUrl, {}, function(data, status) {
		for(let userId in data) {
			createChart(data[userId], userId,'Tag');
		}
	});
}

function createChart(dataMap, userId, chartTitle) {
	let ctx = document.getElementById('user'+userId+'Canvas-'+chartTitle);
	let myLabels = generateWorkStatLabels(dataMap);
	let myData = generateWorkStatDatasets(dataMap);

	let myChart = new Chart(ctx, {
		type: 'pie',
		data: {
			labels: myLabels,
			datasets: [{
		        data: myData,
		        backgroundColor: getBackgroundColor(myLabels)
		    }]
		},
		options: {
			plugins: {
				title: {
					display: true,
					text: chartTitle,
					font: {
						size: 16
					}
				},
				legend: {
					position: 'top'
				}
			}
		}
	});
	return myChart;
}

function generateWorkStatLabels(userMinutesMap) {
	let workLabels = [];
	for (let role in userMinutesMap) {
		workLabels.push(role);
	}
	return workLabels;
}

function generateWorkStatDatasets(userMinutesMap) {
	let workStatDataset = [];
	for (let role in userMinutesMap) {
		workStatDataset.push(userMinutesMap[role]);
	}
	return workStatDataset;
}

function getBackgroundColor(roleLabels) {
	let backgroundColors = [];
	for(let i in roleLabels) {
		backgroundColors.push(colorMap.get(roleLabels[i]));
	}
	return backgroundColors;
}