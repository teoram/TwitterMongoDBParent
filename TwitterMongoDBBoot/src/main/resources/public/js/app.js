
var tweetApp = angular.module("tweetApp", [ 'ngRoute', 'ngResource','ngSanitize' ]);

tweetApp.config(function($routeProvider) {
	$routeProvider.when('/getTweets', {
		controller : 'tweetsController',
		templateUrl : '/views/feedback.html'	
	}).when('/getMessages', {
		controller : 'messageController',
		templateUrl : '/views/feedback.html'	
	}).when('/rankFestivalOnWordAmazing', {
		controller : 'wordController',
		templateUrl : '/views/feedback.html'	
	}).when('/rankFestivalOnMultipleWords', {
		controller : 'multipleWordController',
		templateUrl : '/views/feedback.html'	
	}).otherwise({
		controller : 'homeController',
		templateUrl: '/views/home.html'
    });
});



tweetApp.factory( 'tweetService', [ '$resource', function( $resource ){
	return new Tweet( $resource );
}] );
 
function Tweet( resource ) {
 
	this.resource = resource; 
 
	this.getTweets = function( scope ) {
		//
		// Query Action Method
		//
		scope.message = "started to retrieve tweets";
		
//		var tweets = resource('/retrieveTweets');
		var tweets = resource('/camel/retrieveTweets');
		tweets.get(function(response){
			scope.message = response.message;
		});
	}
	
	this.getMessages = function( scope, http ) {
		http.get("/camel/getMessages").
			success(function(data, status, headers, config){
				scope.message = data;
			}).
			error(function(data,status,headers,config){
				scope.message = "error occured with status " + status;
			})
		
	}	
	
	this.getWordRankAmazing = function(scope) {
		scope.message = "Going for the word rank of word amazing";
		
		var wordRankTomorrow = resource('/rankFestivalOnWord', {word:'amazing'})
		wordRankTomorrow.get(function(response){
			scope.message = response.message;
		});
	}
	
	this.getMultipleWordRank = function(scope) {
		scope.message = "Going for the word rank of word amazing";
		
		var wordRankTomorrow = resource('/rankFestivalOnWord', {word:'de,gorilla,zat,op,wc'})
		wordRankTomorrow.get(function(response){
			scope.message = response.message;
		});
	}
	
	this.queryWords = function(scope) {
		if(scope.query == null || scope.query==""){
			scope.message = "Insufficient Data! Please provide words";
		}else{
			scope.message = "retrieving data";
			var wordRank = resource('/rankFestivalOnWord', {word:scope.query})
			wordRank.get(function(response){
				scope.message = "response returned";
				scope.queryResponse = response;
				
				var dataAsText = JSON.stringify(response);
				$('#queryResponsePrint').text(dataAsText);
				
				loadReport(response);
				
			});
		}
	 };
}



//Controller when the main page/view loads
tweetApp.controller("homeController", [ '$scope', function($scope) {			
} ]);
// Controller to get all tweets
tweetApp.controller("tweetsController", [ '$scope','tweetService', function($scope, tweetService) {	
	tweetService.getTweets( $scope );		
} ]);
// Controller to get all messages
tweetApp.controller("messageController", [ '$scope', '$http','tweetService', function($scope, $http, tweetService) {	
	tweetService.getMessages( $scope, $http );		
} ]);
//Controller to get handle words
tweetApp.controller("wordController", [ '$scope','tweetService', function($scope, tweetService) {	
	tweetService.getWordRankAmazing( $scope );		
} ]);
//Controller to get multiple words
tweetApp.controller("multipleWordController", [ '$scope','tweetService', function($scope, tweetService) {	
	tweetService.getMultipleWordRank( $scope );		
} ]);

//Controller to handle queries
tweetApp.controller("queryController", [ '$scope','tweetService', function($scope, tweetService) {	
	$scope.callQueryWords = function(msg) {
		 tweetService.queryWords( $scope );
	};
}]);


function loadReport(data) {
	var ranks = [];
	var festivalsarray = [];
	var completeData = [];
	for (var rankFestival in data.rank) {
		ranks.push(data.rank[rankFestival]);
		festivalsarray.push(rankFestival);
		completeData.push({name: rankFestival, y:data.rank[rankFestival]});
	}
	
	$('#chartsContainer').highcharts({
	    chart: {
	        type: "column"
	    },
	    title: {
	        text: ' festivals rating for words you entered '
	    },
	    xAxis: {
	    	categories: festivalsarray
	    },
	    yAxis: {
	        title: {
	            text: 'Rating'
	        }
	    },
	    tooltip: {
            formatter: function() {
                return '<b>'+ this.series.name +'</b><br/>: '+ this.y;
        	}
        },
	    series: [{name: 'ranks for festivals',
	    	 data:ranks,
	    	 stack:'test'}]
	});
	
	$('#chartsContainer2').highcharts({
		chart: {
			type: "column"
		},
		title: {
			text: ' festivals rating for words you entered '
		},
		xAxis: {
			categories: festivalsarray
		},
		yAxis: {
			title: {
				text: 'Rating'
			}
		},
		tooltip: {
			formatter: function() {
				return '<b>'+ this.series.name +'</b><br/>: '+ this.y;
			}
		},
		series: [{type: 'pie', 
			name:'pie overview',
			data:completeData}]
	});
}







