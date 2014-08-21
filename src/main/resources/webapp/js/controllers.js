'use strict';

/* Controllers */

var unitransControllers = angular.module('unitransControllers', []);

unitransControllers.controller('FileListingController', ['$scope', '$http',
    function ($scope, $http) {
        $scope.metaDataBlocks = [];
        $http.get('api/metadata/root').success(function (data) {
            $scope.metaDataBlocks.push(data);
        });
    }]);
unitransControllers.controller('StrangerListingController', ['$scope', '$http',
    function ($scope, $http) {
        $scope.strangers = [];
        $http.get('api/strangers.json').success(function (data) {
            $scope.strangers = data;
        });
    }]);
