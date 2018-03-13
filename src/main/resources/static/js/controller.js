var app = angular.module('demo', []);

app.controller('Hello', function($scope, $http, $window) {

	$scope.filterParam = 'defaultFilterParam';

//	$http.get('https://localhost:8080/greeting').then(function(response) {
//	$http.get('https://aqueous-thicket-65474.herokuapp.com/greeting').then(function(response) {
	$http.get('/greeting').then(function(response) {
		$scope.greeting = response.data;
	});

	$scope.filter = function(param) {
		$scope.filterParam = param;
	};

	$scope.graph = function() {
		$scope.filterParam = 'graph';
//		var url = "https://localhost:8080/authenticate";
//		var url = "https://aqueous-thicket-65474.herokuapp.com/authenticate";
		var url = "/authenticate";
		$window.location.href = url;
	};

});

app.controller('Graph', function($scope, $http, $location) {

	// DOM element where the Timeline title is displayed
	var title = document.getElementById('timelineTitle');
	title.innerHTML = "Loading timeline data"

	// get the query parameters to pass on to the request to DropBox
	$scope.filterParam = 'defaultFilterParam';
	var queryParams = $location.absUrl().split('?')[1];

	// Timeline object.
	var timeline;

//	$http.get('https://localhost:8080/filelist?' + queryParams).then(
//	$http.get('https://aqueous-thicket-65474.herokuapp.com/filelist?' + queryParams).then(
	$http.get('/filelist?' + queryParams).then(
			function(response) {

				// DOM element where the Timeline will be attached
				var container = document.getElementById('timelinecontainer');

				// Get the actual data from the back-end.
				$scope.filelist = response.data;

				// Create a DataSet (allows two way data-binding)
				var items = new vis.DataSet();
				for (i = 0; i < $scope.filelist.data.length; i++) {
					items.add({
						id : i,
						content : $scope.filelist.data[i].title,
						start : $scope.filelist.data[i].dateDash
					});
				}

				// Configuration for the Timeline
				var options = {};

				// Create a Timeline
				timeline = new vis.Timeline(container, items, options);

				// Change the title to indicate loading is done
				title.innerHTML = "Timeline"
					
				var selectedContent = "NULL";
			  	timeline.on('select', function (properties) {
			  		var filename = $scope.filelist.data[properties.items].filename;
//					$http.get('https://localhost:8080/filecontent?filename=' + filename).then(
//					$http.get('https://aqueous-thicket-65474.herokuapp.com/filecontent?filename=' + filename).then(
					$http.get('/filecontent?filename=' + filename).then(
						function(response) {
							alert(response.data.filename);
							selectedContent = response.data.filename;
							document.getElementById('contentTitle').innerHTML = selectedContent;
						},
						function() {
							selectedContent = "failure";
							document.getElementById('contentTitle').innerHTML = selectedContent;
						});
			  	});
			});

	$scope.fitTimeline = function() {
		timeline.fit(true);
	};

	$scope.refresh = function() {
		alert("Not implemented yet");
	};

	$scope.filter = function() {
		var newitems = new vis.DataSet();
		for (i = 0; i < $scope.filelist.data.length; i++) {
			if ($scope.filelist.data[i].title.toLowerCase().includes($scope.filterParam.toLowerCase())) {
				newitems.add({
					id : i,
					content : $scope.filelist.data[i].title,
					start : $scope.filelist.data[i].dateDash
				});
			}
		}
		timeline.setItems(newitems);
		timeline.redraw();
		timeline.fit(true);
	};
});