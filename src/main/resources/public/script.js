var app = angular.module("snmtClient", ["ngRoute"]);
app.config(function($routeProvider) {
    $routeProvider
        .when("/", {
            templateUrl : "home.html",
        })
        .when("/gui", {
            templateUrl : "gui.html",
            controller : "guiCtrl"
        })
        .when("/contact", {
            templateUrl : "contact.html",
            controller : "contactCtrl"
        });
});
app.controller("guiCtrl", function ($scope, $http, $filter, $interval) {
    $scope.actions = ["Get", "GetNext", "GetTable", "Monitor"];

    $scope.performAction = function() {

        if(selectedAction == "Get") {
            $http.get("http://localhost:8080/snmp/get/" + $scope.ipAddress + "/" + $.scope.oid + "/")
                .then(function (response) {
                    $scope.getResponses.push(response.data);

                });
        }
    };

});
app.controller("contactCtrl", function ($scope, $http) {

});





