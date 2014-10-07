'use strict';

/* Controllers */

var unitransModule = angular.module('unitransModule', ['ui.tree']);

unitransModule.controller('FileListingController', ['$scope', '$http', '$log',
    function ($scope, $http, $log) {
        $scope.metaDataBlocks = [];
        $scope.metaDataTree = [
            {
                "id": 1,
                "title": "node1",
                "nodes": []
            },
            {
                "id": 4,
                "title": "node4",
                "nodes": [
                    {
                        "id": 41,
                        "title": "node4.1",
                        "nodes": []
                    }
                ]
            }
        ];



        $http.get('api/metadata/root').success(function (data) {
            $scope.metaDataBlocks.push(data);
        }).error(function () {
                $log.error("Failed to download root data");
        });
    }]);
unitransModule.controller('StrangerListingController', ['$scope', '$http', '$log',
    function ($scope, $http, $log) {
        $scope.strangers = [];
        $http.get('api/strangers.json').success(function (data) {
            $scope.strangers = data;
        }).error(function () {
                $log.error("Failed to get strangers listing");
            });
    }]);
