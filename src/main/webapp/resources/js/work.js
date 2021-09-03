
var datasets;
var config;
window.chartColors = {
		red: 'rgb(222, 91, 73)',
		orange: 'rgb(227, 123, 64)',
		yellow: 'rgb(240, 202, 77)',
		green: 'rgb(70, 178, 57)',
		blue: 'rgb(75, 135, 168)',
		purple: 'rgb(50, 77, 92)',
		grey: 'rgb(201, 203, 207)'
};

/******************** CYCLE CHARTS ********************/
var cycleDates;
var cycleUserInfo;
var cycleTimeValues;

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
		var context = chart.getContext('2d');
		chart.height = "350px";
		window.myLine = new Chart(chart, config);
	});
}

function setupCycleChart(startDate, endDate, timeValues, userInfo) {
	cycleDates = getDates(convertDate(startDate), convertDate(endDate));
	datasets = [];
	var index = 0;
	var colors = ["red", "blue", "orange", "purple", "yellow", "green", "grey"];
	
	for (var i in timeValues) {
		
		var newDataset = {
				label: userInfo[index].username,
				backgroundColor: window.chartColors[colors[index % colors.length]],
				borderColor: window.chartColors[colors[index % colors.length]],
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
var generatedRoleCharts = false;
var generatedTagCharts = false;
var generatedCollaboratorCharts = false;

function initializeWorkStats(type, scope, id) {
	var dataUrl;
	if(scope == 'project') {
		dataUrl = "/coop/work/stats/project/"+id + "/" + type;
	} else if(scope == 'cycle') {
		dataUrl = "/coop/work/stats/cycle/"+id + "/" + type;
	} else {
		dataUrl = "/coop/work/stats/" + type;
	}
	$.post(dataUrl, {}, function(data, status) {
		var assignedColors = [];
		if(type == 'role') {
			if(!generatedRoleCharts) {
				//generate a unique color for each user
				let colorList = generateColorListForMap(data["allRoles"]);
				createChart(data, 'allRoles', 'All Roles', scope, id, colorList, assignedColors);
				createChart(data, 'owner', 'Owner', scope, id, colorList, assignedColors);
				createChart(data, 'helper', 'Helper', scope, id, colorList, assignedColors);
				createChart(data, 'reviewer', 'Reviewer', scope, id, colorList, assignedColors);
				generatedRoleCharts = true;
			}
			displayRoleCharts();
		} else if(type == 'tag') {
			if(!generatedTagCharts) {
				//generate a unique color for each user
				let colorList = generateColorListForMap(data["allTags"]);
				createChart(data, 'allTags', 'All Tags', scope, id, colorList, assignedColors);
				createChart(data, 'research', 'Research', scope, id, colorList, assignedColors);
				createChart(data, 'feature', 'Feature', scope, id, colorList, assignedColors);
				createChart(data, 'test', 'Test', scope, id, colorList, assignedColors);
				createChart(data, 'bugFix', 'Bug Fix', scope, id, colorList, assignedColors);
				createChart(data, 'refactor', 'Refactor', scope, id, colorList, assignedColors);
				createChart(data, 'other', 'Other', scope, id, colorList, assignedColors);
				generatedTagCharts = true;
			}
			displayTagCharts();
		} else if(type == 'collaborators') {
			if(!generatedCollaboratorCharts) {
				let colorList = generateColorListForMap(data["allRolesCollaborator"]);
				createChart(data, 'allRolesCollaborator', 'All Roles', scope, id, colorList, assignedColors);
				createChart(data, 'ownerCollaborator', 'Owner', scope, id, colorList, assignedColors);
				createChart(data, 'helperCollaborator', 'Helper', scope, id, colorList, assignedColors);
				createChart(data, 'reviewerCollaborator', 'Reviewer', scope, id, colorList, assignedColors);
				generatedCollaboratorCharts = true;
			}
			displayCollaboratorCharts();
		}
	});
}

function createChart(dataMap, key, chartTitle, scope, id, colorList, assignedColors) {
	var ctx;
	if(id != undefined) {
		ctx = document.getElementById(key+'Canvas-'+scope+id);
	} else {
		ctx = document.getElementById(key+'Canvas');
	}

	let myLabels = generateWorkStatLabels(dataMap[key]);
	let myData = generateWorkStatDatasets(dataMap[key]);
	let myChart = new Chart(ctx, {
		type: 'pie',
		data: {
			labels: myLabels,
			datasets: [{
		        data: myData,
		        backgroundColor: getUserColors(myLabels, colorList, assignedColors)
		    }]
		},
		options: {
			plugins: {
				title: {
					display: true,
					text: chartTitle,
					font: {
						size: 16,
						style: 'bold'
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
	for (var user in userMinutesMap) {
		workLabels.push(user);
	}
	return workLabels;
}

function displayRoleCharts() {
	$(".roleCharts").each(function() {
		$(this).show();
	});
	$(".tagCharts").each(function() {
		$(this).hide();
	});
	$(".collaboratorCharts").each(function() {
		$(this).hide();
	});
	$("#workPercentileItem").show();
	$("#collaboratorPercentileItem").hide();
}

function displayTagCharts() {
	$(".tagCharts").each(function() {
		$(this).show();
	});
	$(".roleCharts").each(function() {
		$(this).hide();
	});
	$(".collaboratorCharts").each(function() {
		$(this).hide();
	});
	$("#workPercentileItem").show();
	$("#collaboratorPercentileItem").hide();
}

function displayCollaboratorCharts() {
	$(".collaboratorCharts").each(function() {
		$(this).show();
	});
	$(".roleCharts").each(function() {
		$(this).hide();
	});
	$(".tagCharts").each(function() {
		$(this).hide();
	});
	$("#collaboratorPercentileItem").show();
	$("#workPercentileItem").hide();
}

function generateWorkStatDatasets(userMinutesMap) {
	let workStatDataset = [];
	for (var user in userMinutesMap) {
		workStatDataset.push(userMinutesMap[user]);
	}
	return workStatDataset;
}

function getUserColors(users, availableColors, assignedColors) {
	var userColors = [];
	for(var i in users) {
		//if the user has already been assigned a color
		if(assignedColors[users[i]] != undefined) {
			userColors.push(assignedColors[users[i]]);
		} 
		//if the user hasn't yet been assigned a color 
		else {
			let color = availableColors.shift();
			assignedColors[users[i]] = color;
			userColors.push(color);
		}
	}
	return userColors;
}

function generateColorListForMap(map) {
	var numColors = 0;
	var availableColors = [];
	for(var i in map) {
		//numColors++;
		availableColors.push('#'+Math.floor(Math.random()*16777215).toString(16));
	}
	return availableColors;
}