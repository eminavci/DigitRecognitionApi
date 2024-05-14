'use strict';

angular.module('hwdrApp', [
    'hwdrApp.services',
    'hwdrApp.controllers',
    'hwdrApp.directives',
    'hwdrApp.filters',
    'ngRoute',
    'ui.bootstrap',
    'angular-loading-bar',
    'ng-fusioncharts'
  ])
.config(function ($routeProvider, $httpProvider,$logProvider, cfpLoadingBarProvider) {
  $routeProvider.when('/digitrecognition', {templateUrl: 'app/view/dr.html'});
  $routeProvider.when('/trainingset', {templateUrl: 'app/view/trainingset.html'});
    $routeProvider.when('/freqpatterns', {templateUrl: 'app/view/freqpatterns.html'});
  $routeProvider.otherwise({redirectTo: '/digitrecognition'});

  /* CORS... */
  /* http://stackoverflow.com/questions/17289195/angularjs-post-data-to-external-rest-api */
/*  $httpProvider.defaults.useXDomain = true;
  delete $httpProvider.defaults.headers.common['X-Requested-With'];*/


$logProvider.debugEnabled(true);// **TODO** set false in production mode
cfpLoadingBarProvider.includeSpinner = true;
cfpLoadingBarProvider.includeBar = true;


$httpProvider.defaults.useXDomain = true;
delete $httpProvider.defaults.headers.common["X-Requested-With"];
$httpProvider.defaults.headers.common["Accept"] = "application/json;charset=UTF-8";
$httpProvider.defaults.headers.common["Content-Type"] = "application/json;charset=UTF-8";
}).run(['$rootScope', '$location', '$http',
        function ($rootScope, $location, $http) {
            $http.defaults.headers.put['Content-Type'] = 'application/json;charset=UTF-8';
            $http.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8';
}]);

