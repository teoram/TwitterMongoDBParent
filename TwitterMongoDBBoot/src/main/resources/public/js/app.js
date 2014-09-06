
var tweetApp = angular.module("tweetApp", [ 'ngRoute', 'ngResource' ]);

tweetApp.config(function($routeProvider) {
	$routeProvider.when('/getTweets', {
		controller : 'tweetsController',
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
		
		var tweets = resource('/retrieveTweets');
		tweets.get(function(response){
			scope.message = response.message;
		});
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









