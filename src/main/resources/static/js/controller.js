var app = angular.module('demo', []);


app.controller('Hello', function($scope, $http, $window) {

	$scope.filterParam = 'defaultFilterParam';

	$http.get('https://localhost:8080/greeting').then(function(response) {
		$scope.greeting = response.data;
	});

	$scope.filter = function(param) {
		$scope.filterParam = param;
	};

	$scope.graph = function() {
		$scope.filterParam = 'graph';
		var url = "https://localhost:8080/authenticate";
        $window.location.href = url;
	};

});

app.controller('Graph', function($scope, $http, $location) {
	
	$scope.filterParam = 'defaultFilterParam';
	var queryParams = $location.absUrl().split('?')[1];
	
	var timeline;
	
	$http.get('https://localhost:8080/filelist?' + queryParams).then(function(response) {
		$scope.filelist = response.data;
		
	  	// DOM element where the Timeline will be attached
	  	var container = document.getElementById('timelinecontainer');

	  	// Create a DataSet (allows two way data-binding)
	   	var items = new vis.DataSet();
	    for (i = 0; i < $scope.filelist.data.length; i++) {
	   		items.add({id: i, content: $scope.filelist.data[i].title, start: $scope.filelist.data[i].dateDash});
	    }
	  
	  	// Configuration for the Timeline
	  	var options = {};

	  	// Create a Timeline
	  	timeline = new vis.Timeline(container, items, options);
	});
	
  	$scope.fitTimeline = function() {
  	    timeline.fit(true);
  	};

	$scope.refresh = function() {
		alert("Not implemented yet");
	};

	$scope.filter = function(param) {
		$scope.filterParam = param;
	};
});