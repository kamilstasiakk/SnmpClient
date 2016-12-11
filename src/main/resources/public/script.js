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
app.controller("guiCtrl", function ($scope, $http) {
    $scope.actions = ["Get", "GetNext", "GetTable", "Monitor"];

    $scope.getResponses = [];
    $scope.performAction = function() {

        if(angular.equals( $scope.selectedAction, $scope.actions[0])) {
            $http.get("http://localhost:8080/snmp/get/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.getResponses.push(response.data);

                });
        }

        if( $scope.selectedAction == "GetNext") {
            $http.get("http://localhost:8080/snmp/getNext/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.getResponses.push(response.data);

                });
        }

        if( $scope.selectedAction == "GetTable") {
            $http.get("http://localhost:8080/snmp/getTable/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.table = response.data;

                });
        }


    };

});
app.controller("contactCtrl", function ($scope, $http) {

});





