var app = angular.module("snmtClient", ["ngRoute"]);
app.config(function ($routeProvider) {
    $routeProvider
        .when("/", {
            templateUrl: "home.html",
        })
        .when("/gui", {
            templateUrl: "gui.html",
            controller: "guiCtrl"
        })
        .when("/contact", {
            templateUrl: "contact.html",
            controller: "contactCtrl"
        });
});
app.controller("guiCtrl", function ($scope, $http, $interval) {
    $scope.actions = ["Get", "GetNext", "GetTable", "Monitor", "GetTraps"];
    $scope.showGetsLock = true;
    $scope.monitorOn = false;
    $scope.showTrapsLock = false;
    $scope.getResponses = [];
    $scope.monitoredObjectValues = [];
    $scope.traps=[];
    var startTrapListeneter = function () {
        $http.get("http://localhost:8080/snmp/startTraps");
    };
    startTrapListeneter();

    $scope.performAction = function () {

        if (angular.equals($scope.selectedAction, $scope.actions[0])) {
            $http.get("http://localhost:8080/snmp/get/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.getResponses.push(response.data);

                });
            $scope.showGets();
        }

        if ($scope.selectedAction == "GetNext") {
            $http.get("http://localhost:8080/snmp/getNext/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.getResponses.push(response.data);

                });
            $scope.showGets();
        }

        if ($scope.selectedAction == "GetTable") {
            $http.get("http://localhost:8080/snmp/getTable/" + $scope.ipAddress + "/" + $scope.oid + "/")
                .then(function (response) {
                    $scope.getTableResponse = response.data;
                    $scope.columnNames = getTableResponse.columnsNames;
                    $scope.records = getTableResponse.values;
                });
            $scope.showTable();
        }

        if ($scope.selectedAction == "Monitor") {
            $scope.monitoredOID = $scope.oid;
            $scope.monitoredAddress = $scope.ipAddress;
            $scope.monitorOn = true;
        }
        if ($scope.selectedAction == "GetTraps") {
            $scope.showTraps();
        }

    };


    var updateMonitoredValue = function () {
        if ($scope.monitorOn) {

            $http.get("http://localhost:8080/snmp/getWithTime/" + $scope.monitoredAddress + "/" + $scope.monitoredOID + "/")
                .then(function (response) {
                    $scope.monitoredObjectValues.push(response.data);
                });
        }
    };
    var updateTraps = function () {
        $http.get("http://localhost:8080/snmp/getTraps")
            .then(function (response) {
                $scope.traps=response.data;
            });
    };

    $interval(function () {
        updateMonitoredValue();
        updateTraps();
    }, 1000);

    $scope.showGets = function () {
        $scope.showGetsLock = true;
        $scope.showTableLock = false;
        $scope.showMonitorLock = false;
        $scope.showTrapsLock = false;

    }

    $scope.showTable = function () {
        $scope.showGetsLock = false;
        $scope.showTableLock = true;
        $scope.showMonitorLock = false;
        $scope.showTrapsLock = false;
    }
    $scope.showMonitor = function () {
        $scope.showGetsLock = false;
        $scope.showTableLock = false;
        $scope.showMonitorLock = true;
        $scope.showTrapsLock = false;
    }
    $scope.showTraps = function () {
        $scope.showGetsLock = false;
        $scope.showTableLock = false;
        $scope.showMonitorLock = false;
        $scope.showTrapsLock = true;
    }

});
app.controller("contactCtrl", function ($scope, $http) {

});





